/*
 * File:   StringFunctions.c
 * Author: daniluk.v
 *
 * Created on 10 июня 2020 г., 9:47
 */


#include <xc.h>
#include <stdbool.h>
#include <string.h>
#include "StringFunctions.h"

//-----------------------------------------------------------------
//   Внешние переменные
//-----------------------------------------------------------------
//extern volatile char  btData[];                            //приемный массив от BT 

//-----------------------------------------------------------------
//   перевод hex от 0 до А в символ
//-----------------------------------------------------------------
char hexToSymbol(char hex8) {
	if(hex8 < 0xA) 
		hex8 = hex8 + '0';
	else hex8 = hex8 + 0x41 - 10;		
	return hex8;
}
//-----------------------------------------------------------------
//   перевод Hex16 числа в строку
//-----------------------------------------------------------------
void hex16ToStr(uint16_t hex16, char *str) {
	str[4] = 0;
	str[3] = hexToSymbol(hex16 & 0x0F);
	str[2] = hexToSymbol(hex16>>4  & 0x0F);
	str[1] = hexToSymbol(hex16>>8  & 0x0F);
	str[0] = hexToSymbol(hex16>>12 & 0x0F);

}
//-----------------------------------------------------------------
//   перевод Hex8 числа в строку
//-----------------------------------------------------------------
void hex8ToStr(uint8_t hex8, char *str) {
	str[2] = 0;
	str[1] = hexToSymbol(hex8 & 0x0F);
	str[0] = hexToSymbol(hex8>>4  & 0x0F);

}

