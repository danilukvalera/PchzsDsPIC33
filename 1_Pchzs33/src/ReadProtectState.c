/*
 * File:   ReadProtectState.c
 * Author: daniluk.v
 *
 * Created on 10 ���� 2020 �., 16:50
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
//������� ����������
extern const void *drvCAN1;
extern const void *drvCAN2;
extern uint16_t caunter_waite;
extern InputsDiscrete inputData;

//���������
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
    
    //�������� ���������� ������� �� � ��������� ����� ������ Master � Slave
    if (! checkPowerDevice()) {
        //sendString(READ_ZS_END, 0, 0);
        //����� �������� ��������� ���������� ������� ��
        sendString(ERROR_POWER_DEVICE, 0, 0);
        return;
    }
    //���������� ����� RESET � "0"
    RES_M_AND_S_0;
    //����� ������ ���������� (����� ��������� ����� ��������� ������� � ������� ������ �������� ������)
    C1INTFbits.RBIF = 0;
    C2INTFbits.RBIF = 0;
    Ecan_run(drvCAN1);                      //������ ������ �� CAN1
    wait1ms();
    Ecan_run(drvCAN2);                      //������ ������ �� CAN2
    caunter_waite = 0;
    //���������� ����� RESET � "1"
    RES_M_AND_S_1;    
    
    while(true){
        //����������� �� ������ ���������� ����� ����� ������
        if(typeInterface == 0){
            while(true){
                if (C1INTFbits.RBIF == 1) {          //�������� ����� ������ ������ � CAN1
                    typeInterface = 1;
                    maxTimeWaite = 100;
                    caunter_waite = 0;
                    
                    break;
                }
                
                if (C2INTFbits.RBIF == 1) {          //�������� ����� ������  ������ � CAN2
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

        //����� ������� ������ �� CAN1
        if(typeInterface == 1){
            Ecan_run(drvCAN1);  //������ ������ �� CAN1
            //���� ������� ������ �� CAN1
            if (Ecan_isInReady(drvCAN1)) {
                //�������� SID (adressMK)
                sid = Ecan_getSID(drvCAN1);
                //�������� ������
                Ecan_get(drvCAN1, byteCanArr, 8);
                //�������� SID
                hex16ToStr(sid, charArr);
                sendString(charArr, 0, 0);
                //�������� 8 ���� ������
                for (i = 0; i < 8; i++) {
                    hex8ToStr(byteCanArr[i], charArr);
                    sendString(charArr, 0, 0);
                }
                //�������� ����� ���������� ������ (adressMK + 0x1000), �.�. ��������� � ������ ������� 1
                hex16ToStr(sid + 0x1000, charArr);
                sendString(charArr, 0, 0);
                caunter_waite = 0;
            }
        }

        //����� ������� ������ �� CAN2
        if(typeInterface == 2){
            Ecan_run(drvCAN2);  //������ ������ �� CAN2
           //���� ������� ������ �� CAN2
            if(Ecan_isInReady(drvCAN2)) {
                 //�������� ������
                Ecan_get(drvCAN2, byteCanArr, 8);
                for (i = 0; i < 8; i++) {
                    hex8ToStr(byteCanArr[i], charArr);
                    sendString(charArr, 0, 0);
                }
                caunter_waite = 0;
            }
        }
        
        //���� ������ ��� ������ maxTimeWaite �� ������ �������� ��������, �����
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
        //����� �������� ��������� ���������� ������� ��
        sendString(ERROR_POWER_DEVICE, 0, 0);
    }
}

bool checkPowerDevice(void) {
    //�������� ���������� �� ���� 5/3,3� (���� ������������ ��� ���� 5� � 3,3� �����!!!!)
    if (inputData.str.stateLine5V == STATE_OFF) {
       //�������� 3,3�
        SWITCH_VOLTAGE_TO_3V3;
        VOLTAGE_3V3_OR_5V_ON;
        wait500ms();
        //�������� ��������� ����� ������ Master � Slave
        if (inputData.str.stateLineResM == STATE_OFF && inputData.str.stateLineResS == STATE_OFF) {
            //�������� 3,3�
            SWITCH_VOLTAGE_TO_5V;
            VOLTAGE_3V3_OR_5V_ON;
            wait500ms();
        } else {
            return true;
        }
    }
    //�������� ��������� ����� ������ Master � Slave
    if (inputData.str.stateLineResM == STATE_OFF && inputData.str.stateLineResS == STATE_OFF) {
        SWITCH_VOLTAGE_TO_3V3;
        VOLTAGE_3V3_OR_5V_OFF;
        return false;
    }
    return true;
}

