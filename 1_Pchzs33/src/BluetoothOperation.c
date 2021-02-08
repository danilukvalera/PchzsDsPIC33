/*
 * File:   BluetoothOperation.c
 * Author: daniluk.v
 *
 * Created on 10 ���� 2020 �., 11:02
 */


#include <xc.h> // include processor files - each processor file is guarded.  
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
//#include <stdio.h>
#include "MainRegisters.h"
#include "UartDrv.h"
#include "BluetoothOperation.h"

//*****************************************************************************
// ��������� ����������
volatile char  btData[MAX_RECEIVE];                 //�������� ������ �� BT 
volatile uint16_t indexR;                           //���-�� �������� ��������
volatile uint16_t fComand = 0;                      //�������� ������� �� BT: 0-��� ������� ���������
													//      1-���� ������� ������ �����-�������
													//      2-���� ������� ����� �����-�������

//*****************************************************************************
//������� ����������
extern const void *drvUart1;
extern uint16_t regDSWPAG;                        // ��������� �������� \a DSWPAG.
extern uint16_t regDSRPAG;                        // ��������� �������� \a DSRPAG.

//-----------------------------------------------------------------
//   ������� ��������� ������
//-----------------------------------------------------------------
void clearReceiveArr(void) {
    memset( (char *)btData, 0, sizeof(btData));
}
//-----------------------------------------------------------------
//   ���������� ��������� ������ � ������ ����� �������
//-----------------------------------------------------------------
void prepareReceiveBuffer(void) {
    fComand = 0;	
    indexR  = 0;	
    clearReceiveArr() ;
}
//-----------------------------------------------------------------
//  ��������� ���� � ��������� � ����� ��������� �������
//-----------------------------------------------------------------
bool compare(const char *B)
{
  uint8_t i;
  uint16_t size = strlen(B); //���������� ������ ������� ������������� �����, ������ ��� ����� ��������� ���� ������� "0"
  for(i=0 ; i< size; i++) {
    if(btData[i] != B[i])
      return false;     //��������� �� �������
  }
  return true;			//��������� �������
}
//-----------------------------------------------------------------
//  �������� ������ 
//  ��� �� ������� � Bluetooth
//-----------------------------------------------------------------
//char new_line[] = {'\n', '\r'};     // �������� "������� ������", "������ �������"
//�������----------------------------------------------------------
//*pstr ��������� �� ������ ������� ��������
//clear  true - ������� ������  receiveMobile, false - �� �������
//x0d    true - �������� � �����  0x0D,        false - �� ��������
void sendString(char *pstr, bool clear, bool x0d)  {
  	unsigned char index;

  	index =0;
  	while (pstr[index] != 0x00)   {
        while(! UartDrv_isOutReady(drvUart1));
    	UartDrv_set(drvUart1, pstr[index]);
	    index++;
  	}
  	if(x0d)  {
        while(! UartDrv_isOutReady(drvUart1));
    	char Ox0D = 0x0D;
        UartDrv_set(drvUart1, Ox0D);

        while(! UartDrv_isOutReady(drvUart1));
    	char Ox0A = 0x0A;
        UartDrv_set(drvUart1, Ox0A);
  	}
    
   	//������� ��������� �������
    if(clear){
        clearReceiveArr();
    }                     


}

//-----------------------------------------------------------------
// ������ ����� � Bluetooth
//-----------------------------------------------------------------
void send_byte(char b)  {
    while(! UartDrv_isOutReady(drvUart1));
    UartDrv_set(drvUart1, b);
}

//*****************************************************************************
// ������� ��������� ���������� U1RX
void __attribute__((__interrupt__, auto_psv, save(MODCON, DSRPAG, DSWPAG))) _U1RXInterrupt(void)
{
    MODCON = 0;
    DSWPAG = regDSWPAG;
    DSRPAG = regDSRPAG;
    
    //user code
    unsigned char t;
    //��������� ������ �����-������� � ������ ������� '%'
    if(indexR==0) {
	   	t = UartDrv_get(drvUart1);
	   	if(t=='%') {
		   btData[indexR]= t;
		   indexR++;
		   fComand =1; 
		} else {
			U1RX_INTERRUPT_CLEAR_FLAG;     //����� ����� ���������� U1RX
    		return;
		}
	}
	//���� � �������� �������� UART ���� ��� ������, ����� �� �  �����
   	while(UartDrv_isInReady(drvUart1))  { 
	   	//���� ����� ������� ��������� �����
    	if(indexR+1 == MAX_RECEIVE) {
            prepareReceiveBuffer(); 	       //���������� ��������� ������ � ������ ����� �������
			U1RX_INTERRUPT_CLEAR_FLAG;     //����� ����� ���������� U1RX
    		return;
    	}
    	t = UartDrv_get(drvUart1);
	   	btData[indexR] = t;
    	indexR++;
	   	//���� �������-������ '%' ��������� ������� 
	   	if(t=='%') {
	   		fComand = 2;
	   		indexR--;				//indexR - ����� ��������������� ������� ����� ������� '%'
		}

  	}
    //user code end
    U1RX_INTERRUPT_CLEAR_FLAG;  //����� ����� ���������� U1RX
    
}

//*****************************************************************************
// ������� �������� ���������� � ����������
bool checkConnectionBluetooth(void){
    return false;
}


