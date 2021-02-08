/**
* \file    ECAN1_PinRemap.h
* \brief   ��������� ����� �� ������ � ECAN1
*
* \version 1.0.1
* \date    17-03-2017
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef ECAN1_PinRemap_h
#define ECAN1_PinRemap_h

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <xc.h>

//*****************************************************************************
// ������� ���� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ��������� ����� �� ������ � ECAN1.
///
#define ECAN1_PIN_REMAP                                                                 \
do                                                                                      \
{                                                                                       \
    INIC_PORT_OUT(B, 6, OPEN_DRAIN_ON);     /* CAN1 Tx pin directions ������� 70 */     \
     _TRISB5 = 1;                           /* CAN1 Rx pin directions ������� 68 */     \
     RPOR2bits.RP38R = _RPOUT_C1TX;         /* CAN1 Tx pin remap.                */     \
     RPINR26bits.C1RXR = 0b1001000;         /* CAN1 Rx pin remap.                */     \
} while( 0 )                                                                          

#endif

//*****************************************************************************
/**
* ������� ���������:
*
* ������ 1.0.1
* ����   17-03-2017
* �����  ��������� �.�.
*
* ���������:
*    ������� ������.
*/
