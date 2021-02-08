/*
 * File:   BluetoothOperation.c
 * Author: daniluk.v
 *
 * Created on 10 июня 2020 г., 11:02
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
// Локальные переменные
volatile char  btData[MAX_RECEIVE];                 //приемный массив от BT 
volatile uint16_t indexR;                           //кол-во принятых символов
volatile uint16_t fComand = 0;                      //принятая команда по BT: 0-все команды выполнены
													//      1-есть признак начала слова-команды
													//      2-есть признак конца слова-команды

//*****************************************************************************
//Внешние переменные
extern const void *drvUart1;
extern uint16_t regDSWPAG;                        // Состояние регистра \a DSWPAG.
extern uint16_t regDSRPAG;                        // Состояние регистра \a DSRPAG.

//-----------------------------------------------------------------
//   очистка приемного буфера
//-----------------------------------------------------------------
void clearReceiveArr(void) {
    memset( (char *)btData, 0, sizeof(btData));
}
//-----------------------------------------------------------------
//   подготовка приемного буфера к приему новой команды
//-----------------------------------------------------------------
void prepareReceiveBuffer(void) {
    fComand = 0;	
    indexR  = 0;	
    clearReceiveArr() ;
}
//-----------------------------------------------------------------
//  Сравнение слов с привязкой к концу принятого массива
//-----------------------------------------------------------------
bool compare(const char *B)
{
  uint8_t i;
  uint16_t size = strlen(B); //определяем размер массива сравниваемого слова, выдает без учета последний байт массива "0"
  for(i=0 ; i< size; i++) {
    if(btData[i] != B[i])
      return false;     //сравнение не успешно
  }
  return true;			//сравнение успешно
}
//-----------------------------------------------------------------
//  Передача строки 
//  или АТ команды в Bluetooth
//-----------------------------------------------------------------
//char new_line[] = {'\n', '\r'};     // спецкоды "перевод строки", "возрат каретки"
//Функция----------------------------------------------------------
//*pstr указатель на массив который посылать
//clear  true - очищать массив  receiveMobile, false - не очищать
//x0d    true - посылать в конце  0x0D,        false - не посылать
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
    
   	//очистка приемного массива
    if(clear){
        clearReceiveArr();
    }                     


}

//-----------------------------------------------------------------
// Выдача байта в Bluetooth
//-----------------------------------------------------------------
void send_byte(char b)  {
    while(! UartDrv_isOutReady(drvUart1));
    UartDrv_set(drvUart1, b);
}

//*****************************************************************************
// Функция обработки прерывания U1RX
void __attribute__((__interrupt__, auto_psv, save(MODCON, DSRPAG, DSWPAG))) _U1RXInterrupt(void)
{
    MODCON = 0;
    DSWPAG = regDSWPAG;
    DSRPAG = regDSRPAG;
    
    //user code
    unsigned char t;
    //принимаем только слово-команду с первым симолом '%'
    if(indexR==0) {
	   	t = UartDrv_get(drvUart1);
	   	if(t=='%') {
		   btData[indexR]= t;
		   indexR++;
		   fComand =1; 
		} else {
			U1RX_INTERRUPT_CLEAR_FLAG;     //Сброс флага прерывания U1RX
    		return;
		}
	}
	//если в приемном регистре UART есть еще данные, пишем их в  буфер
   	while(UartDrv_isInReady(drvUart1))  { 
	   	//если длина команды превысила буфер
    	if(indexR+1 == MAX_RECEIVE) {
            prepareReceiveBuffer(); 	       //подготовкА приемного буфера к приему новой команды
			U1RX_INTERRUPT_CLEAR_FLAG;     //Сброс флага прерывания U1RX
    		return;
    	}
    	t = UartDrv_get(drvUart1);
	   	btData[indexR] = t;
    	indexR++;
	   	//ищем признак-символ '%' скончания команды 
	   	if(t=='%') {
	   		fComand = 2;
	   		indexR--;				//indexR - будет соответствовать индексу конца команды '%'
		}

  	}
    //user code end
    U1RX_INTERRUPT_CLEAR_FLAG;  //Сброс флага прерывания U1RX
    
}

//*****************************************************************************
// Функция проверки сопряжения с тедлефоном
bool checkConnectionBluetooth(void){
    return false;
}


