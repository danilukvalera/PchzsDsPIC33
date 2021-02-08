/*
 * File:   BluetoothInitial.c
 * Author: daniluk.v
 *
 * Created on 15 ���� 2020 �., 14:00
 */


#include <xc.h> // include processor files - each processor file is guarded. 
#include "BluetoothOperation.h"
#include "UartDrv.h"
#include "MainRegisters.h"
#include "Main.h"
#include "wait.h"
#include <stdbool.h>
#include "BluetoothInitial.h"
#include "StringFunctions.h"


//������� ���������� ***********************************************************
extern volatile uint16_t indexR;                           //���-�� �������� ��������
extern const void *drvUart1;
extern uint16_t caunter_waite;
extern char  btData[];


//*****************************************************************************
bool setParamsBluetooth(void){
    const char* REQUEST_BAUD =  "AT+UART?";
    const char* ANSWER_BAUD_460800 =  "+UART=460800,0,0";
    const char* SET_BAUD_460800 =  "AT+UART=460800,0,0";

    const char* REQUEST_NAME =  "AT+NAME?";
    const char* ANSWER_NAME  =  "+NAME:PCHZS-DSPIC33";
    const char* SET_NAME  =  "AT+NAME=PCHZS-DSPIC33";       // PCHZS-DSPIC33 

    const char* REQUEST_PASSWORD =  "AT+ PSWD?";
    const char* ANSWER_PASSWORD  =  "+PSWD:1234";
    const char* SET_PASSWORD =  "AT+PSWD=1234";
    
    const char* REQUEST_RESET  =  "AT+RESET";
    //const char* REQUEST_DISC  =  "AT+DISC";
    //const char* ANSWER_OK  =  "+OK";

    //��������� ������ � ����� �� ������
    //wait500ms();
    BT_MODE_AT_COMMAND;
    wait100ms();
    //��������� UART ���������� �� ���������� 460800
    UartDrv_setBtr(drvUart1, 460800);
    UartDrv_get(drvUart1);          //������� ������ ����� �������� ��������
    //�������� ����� �� 460800
    sendString((char*)REQUEST_BAUD, true, true);
    //���� ����� ��� �� ���������� ��������� �������
    //���� ������ �� ���� �������� ����������� �� ������ ���������
    if(! receivingDataBluetooth()){
        //�������� ����� �� 9600
        UartDrv_setBtr(drvUart1, 9600);
        sendString((char*)REQUEST_BAUD, true, true);
        if(! receivingDataBluetooth()){
            //�������� ����� �� 19200
            UartDrv_setBtr(drvUart1, 38400);
            sendString((char*)REQUEST_BAUD, true, true);
            if(! receivingDataBluetooth()){
                //�������� ����� �� 38400
                UartDrv_setBtr(drvUart1, 115200);
                sendString((char*)REQUEST_BAUD, true, true);
                //���� ����� ������ �� ����
                if(! receivingDataBluetooth()){
                    BT_MODE_TRANSMIT;
                    prepareReceiveBuffer();       //ddd
                    return false;
                }
            }
        }
        //���� ����� ���, �� ����� UART ���������� �������� �� ������ �������
        //��������� ������� 460800
        sendString((char*)SET_BAUD_460800, true, true);
        wait100ms();
        sendString((char*)REQUEST_RESET, true, true);
        wait100ms();
        BT_MODE_TRANSMIT;
        wait100ms();
        BT_MODE_AT_COMMAND;
        wait100ms();
        //�������� ��������� ������� 460800
        UartDrv_setBtr(drvUart1, 460800);
        UartDrv_get(drvUart1);          //������� ������ ����� �������� ��������
        //�������� ��������� ������� 115200
        sendString((char*)REQUEST_BAUD, true, true);
        if(! receivingDataBluetooth()){
            BT_MODE_TRANSMIT;
            prepareReceiveBuffer();       //ddd
            return false;
        }
        if(! compare(ANSWER_BAUD_460800)){
            BT_MODE_TRANSMIT;    
            prepareReceiveBuffer();       //ddd
            return false;
        }
    } 
    

    //�������� � ��������� ��� bluetooth 
    //wait500ms();
    sendString((char*)REQUEST_NAME, true, true);
    if(! receivingDataBluetooth()){
        prepareReceiveBuffer();       //ddd
        return false;
    }
    if(! compare(ANSWER_NAME)){
        //wait500ms();
        sendString((char*)SET_NAME, true, true);
        wait100ms();
        UartDrv_get(drvUart1);          //������� ������ ����� �������� ��������
        sendString((char*)REQUEST_NAME, true, true);
        if(! receivingDataBluetooth()){
            BT_MODE_TRANSMIT;    
            prepareReceiveBuffer();       //ddd
            return false;
        }
        if(! compare(ANSWER_NAME)){
            BT_MODE_TRANSMIT;    
            prepareReceiveBuffer();       //ddd
            return false;
        }
    } 

    //�������� � ��������� ������ 
    sendString((char*)REQUEST_PASSWORD, true, true);
    if(! receivingDataBluetooth()){
        BT_MODE_TRANSMIT;    
        prepareReceiveBuffer();       //ddd
        return false;
    }
    if(! compare(ANSWER_PASSWORD)){
        sendString((char*)SET_PASSWORD, true, true);
        wait100ms();
        UartDrv_get(drvUart1);          //������� ������ ����� �������� ��������
        sendString((char*)REQUEST_PASSWORD, true, true);
        if(! receivingDataBluetooth()){
            BT_MODE_TRANSMIT;    
            prepareReceiveBuffer();       //ddd
            return false;
        }
        if(! compare(ANSWER_PASSWORD)){
            BT_MODE_TRANSMIT;    
            prepareReceiveBuffer();       //ddd
            return false;
        }
    } 
    BT_MODE_TRANSMIT;
    prepareReceiveBuffer();       //ddd
    return true;
}

bool receivingDataBluetooth(void){
    caunter_waite = 0;
    indexR =0;
    char t;
    while(1){
    	//���� � �������� �������� UART ���� ������, ����� �� �  �����
        if(UartDrv_isInReady(drvUart1))  { 
                //���� ����� ������� ��������� �����
            if(indexR+1 == MAX_RECEIVE) {
                prepareReceiveBuffer(); 	   //���������� ��������� ������ � ������ ����� �������
                return false;
            }
            t = UartDrv_get(drvUart1);
            btData[indexR] = t;
            indexR++;
            caunter_waite = 0;
        } else {
            if(caunter_waite > 500){
                //bluetooth �� ��������, ������ �� ����� �� �������
                if(indexR == 0){
                    //sendString("bluetooth no connect", true, false);
                    //wait500ms();
                    return false;
                }
                //������ �������
                else{
                    //sendString(btData, false, false);
                    //clearReceiveArr();
                    return true;
                }
            }
        }
    }
}
