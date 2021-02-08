/*
 * File:   ReadProtectState.c
 * Author: daniluk.v
 *
 * Created on 10 июня 2020 г., 16:50
 */


#include <xc.h>
#include <stdint.h>
#include <stdbool.h>
#include "Main.h"
#include "wait.h"
#include "EcanDriver.h"
#include "BluetoothOperation.h"
#include "StringFunctions.h"
#include "ReadProtectState.h"
#include "MainRegisters.h"
#include "UartDrv.h"

//*****************************************************************************
//Внешние переменные
extern const void *drvCAN1;
extern const void *drvCAN2;
extern uint16_t caunter_waite;
extern InputsDiscrete inputData;

//Константы
char *READ_ZS_END = "READ_ZS_END";
char *CAN_OLD = "_CAN_OLD";
char *CAN_NEW = "_CAN_NEW";
char *ERROR_POWER_DEVICE = "_ERROR_POWER_DEVICE_";
char *RESET_PROTECT_STATE_END = "RESET_PROTECT_STATE_END";
//char *RESET_PROTECT_STATE_OK = "_RESET_PROTECT_STATE_OK";
//*****************************************************************************

void readProtectState(void) {
    char charArr[5];
    uint8_t byteCanArr[8];
    uint16_t sid;
    uint8_t i;
    int maxTimeWaite = 2000;
    uint8_t typeInterface = 0;
    
    //проверка напряжения питания ОК и состояния линий сброса Master и Slave
    if (! checkPowerDevice()) {
        //sendString(READ_ZS_END, 0, 0);
        //ЗДЕСЬ ВСТАВИТЬ ИНДИКАЦИЮ ОТСУТСТВИЯ ПИТАНИЯ ОК
        sendString(ERROR_POWER_DEVICE, 0, 0);
        return;
    }
    //установить линии RESET в "0"
    RES_M_AND_S_0;
    //сброс флагов прерывания (могли взвестись после включения питания и ложного начала передачи данных)
    C1INTFbits.RBIF = 0;
    C2INTFbits.RBIF = 0;
    Ecan_run(drvCAN1);                      //запуск чтения по CAN1
    wait1ms();
    Ecan_run(drvCAN2);                      //запуск чтения по CAN2
    caunter_waite = 0;
    //установить линии RESET в "1"
    RES_M_AND_S_1;    
    
    while(true){
        //определение по какому интерфейсу будет прием данных
        if(typeInterface == 0){
            while(true){
                if (C1INTFbits.RBIF == 1) {          //проверка флага приема данных в CAN1
                    typeInterface = 1;
                    maxTimeWaite = 100;
                    caunter_waite = 0;
                    
                    break;
                }
                
                if (C2INTFbits.RBIF == 1) {          //проверка флага приема  данных в CAN2
                    typeInterface = 2;
                    maxTimeWaite = 100;
                    caunter_waite = 0;

                    break;
                }
                
                if(caunter_waite > maxTimeWaite){
                    break;
                }
            }
        }

        //Прием пакетов данных по CAN1
        if(typeInterface == 1){
            Ecan_run(drvCAN1);  //запуск чтения по CAN1
            //Если приняты данные по CAN1
            if (Ecan_isInReady(drvCAN1)) {
                //получить SID (adressMK)
                sid = Ecan_getSID(drvCAN1);
                //получить данные
                Ecan_get(drvCAN1, byteCanArr, 8);
                //передаем SID
                hex16ToStr(sid, charArr);
                sendString(charArr, 0, 0);
                //передаем 8 байт данных
                for (i = 0; i < 8; i++) {
                    hex8ToStr(byteCanArr[i], charArr);
                    sendString(charArr, 0, 0);
                }
                //передаем адрес завершения пакета (adressMK + 0x1000), т.е. добавляем к первой тетраде 1
                hex16ToStr(sid + 0x1000, charArr);
                sendString(charArr, 0, 0);
                caunter_waite = 0;
            }
        }

        //Прием пакетов данных по CAN2
        if(typeInterface == 2){
            Ecan_run(drvCAN2);  //запуск чтения по CAN2
           //Если приняты данные по CAN2
            if(Ecan_isInReady(drvCAN2)) {
                 //получить данные
                Ecan_get(drvCAN2, byteCanArr, 8);
                for (i = 0; i < 8; i++) {
                    hex8ToStr(byteCanArr[i], charArr);
                    sendString(charArr, 0, 0);
                }
                caunter_waite = 0;
            }
        }
        
        //если данных нет больше maxTimeWaite мс значит передача окончена, выход
        if (caunter_waite > maxTimeWaite) {
            sendString(READ_ZS_END, 0, 0);
            switch (typeInterface) {
                case 1:{
                    sendString(CAN_NEW, 0, 0);
                    break;
                }
                case 2:{
                    sendString(CAN_OLD, 0, 0);
                    break;
                }
                default: {
                    sendString("_NO_DATA", 0, 0);
                    break;
                }
            }
            SWITCH_VOLTAGE_TO_3V3;
            VOLTAGE_3V3_OR_5V_OFF;
            prepareReceiveBuffer();     //ddd
            return;
        }
    }
}

void resetProtectState(void) {
    if (checkPowerDevice()) {
        RES_M_AND_S_0;
        JUMPER_PROTECT_STATE_ON;
        wait10ms();
        RES_M_AND_S_1;
        int i;
        for(i=0; i<5; i++){
            wait1s();
            sendString("WAITING... ", 0, 0);
        }    
        RES_M_AND_S_0;
        JUMPER_PROTECT_STATE_OFF;
        SWITCH_VOLTAGE_TO_3V3;
        VOLTAGE_3V3_OR_5V_OFF;
        wait500ms();
        RES_M_AND_S_1;
        sendString(RESET_PROTECT_STATE_END, 0, 0);
        //sendString(RESET_PROTECT_STATE_OK, 0, 0);
    } else {
        //sendString(RESET_PROTECT_STATE_END, 0, 0);
        //ЗДЕСЬ ВСТАВИТЬ ИНДИКАЦИЮ ОТСУТСТВИЯ ПИТАНИЯ ОК
        sendString(ERROR_POWER_DEVICE, 0, 0);
    }
}

bool checkPowerDevice(void) {
    //проверка напряжения на шине 5/3,3В (пока предполагаем что шина 5В и 3,3В общая!!!!)
    if (inputData.str.stateLine5V == STATE_OFF) {
       //включить 3,3В
        SWITCH_VOLTAGE_TO_3V3;
        VOLTAGE_3V3_OR_5V_ON;
        wait500ms();
        //проверка состояния линий сброса Master и Slave
        if (inputData.str.stateLineResM == STATE_OFF && inputData.str.stateLineResS == STATE_OFF) {
            //включить 3,3В
            SWITCH_VOLTAGE_TO_5V;
            VOLTAGE_3V3_OR_5V_ON;
            wait500ms();
        } else {
            return true;
        }
    }
    //проверка состояния линий сброса Master и Slave
    if (inputData.str.stateLineResM == STATE_OFF && inputData.str.stateLineResS == STATE_OFF) {
        SWITCH_VOLTAGE_TO_3V3;
        VOLTAGE_3V3_OR_5V_OFF;
        return false;
    }
    return true;
}

