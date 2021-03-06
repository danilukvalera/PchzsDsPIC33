/**
* \file    UartDrv.h
* \brief   ������� ������ UART (Master) ��� �� dsPIC33
* \details ���������
*
* \version 1.0.1
* \date    10-07-2019
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef UARTDRV_h
#define UARTDRV_h

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <stdint.h>
#include <stdbool.h>
#include "IOports.h"
#include "MainRegisters.h"
//*****************************************************************************
// ���������� ����� ������
//*****************************************************************************


//*****************************************************************************
/// \brief ����� ������ UART.
///
typedef enum
{
    eUART_module1 = 0,    ///< ������ �� UART1
    eUART_module2,        ///< ������ �� UART2
    eUART_module3,        ///< ������ �� UART3
    eUART_module4,        ///< ������ �� UART4
    eUART_moduleAmount    ///< ���������� ��������� ������� UART
} eUART_modules;

//*****************************************************************************
/// \brief ���������� ��� ������ � �� ��������.
///
typedef enum
{
    eUART_8bit_parity_not  = 0,    ///< 8 ���, ��� �������� �� ��������
    eUART_8bit_parity_even = 1,    ///< 8 ���, �������� �� ��������
    eUART_8bit_parity_odd  = 2,    ///< 8 ���, �������� �� ����������
    eUART_9bit_parity_not  = 3     ///< 9 ���, ��� ��������
} eUART_bitAndParity;

//*****************************************************************************
/// \brief ���������� ���� ���.
///
typedef enum
{
    eUART_bitStop_1 = 0,    ///< 1 ���� ���
    eUART_bitStop_2 = 1     ///< 2 ���� ����
} eUART_bitStop;

//*****************************************************************************
/// \brief ��� ��������.
///
typedef enum
{
    eUART_speed_low  = 0,    ///< ������ ��������,  ������������� ���  Fcy / (16*( BRG + 1 ))
    eUART_speed_high = 1     ///< ������� ��������, ������������� ���  Fcy / ( 4*( BRG + 1 )) 
} eUART_speed;

//*****************************************************************************
// ���������� ���������, ������������ ����� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ����������� �������� ��� �������� BRG ��� ������ �������� (���������� �������� ������).
/// \param speed - �������� ������, ���.
///
//#define BRG_UART_LOW_SPEED( speed )  ( F_CY / ( 4 * speed ) - 1 )
#define BRG_UART_LOW_SPEED( speed )  ( F_CY / ( 16UL * speed ) - 1 )

//*****************************************************************************
/// \brief ����������� �������� ��� �������� BRG ��� ������� �������� (���������� �������� ������).
/// \param speed - �������� ������, ���.
///
//#define BRG_UART_HIGH_SPEED( speed )  ( F_CY / ( 16 * speed ) - 1 )
#define BRG_UART_HIGH_SPEED( speed )  ( F_CY / ( 4UL * speed ) - 1 )

//*****************************************************************************
// ��������� ������������ �������
//*****************************************************************************

//*****************************************************************************
/// \brief �����������.
/// \param numberModule � ����� ������ UART;
/// \param bitAndParity - ���������� ��� ������ � �� ��������;
/// \param bitStop - ���������� ���� ���;
/// \param speed - ��� �������� (������� ��� ������);
/// \param brg - �������� ������� Fcy, ������ ������� ����.
/// \return ����� ��������, ���� 0 - �� ������ �������������.
///
const void *UartDrv_ctor( eUART_modules      numberModule,  
                          eUART_bitAndParity bitAndParity,
                          eUART_bitStop      bitStop,  
                          eUART_speed        speed,
                          uint16_t           brg );

//*****************************************************************************
/// \brief ������� ���� ������ �� UART.
/// \param self - ��������� �� ������ �������� (�����, ������� ������ �����������).
/// \note ������ �������.
///
void UartDrv_run(const void *self);

//*****************************************************************************
/// \brief ������ �������� ������ �� UART.
/// \param self  - ��������� �� ������ �������, ������� ������ �����������.
/// \return �������� ���� ������.
///
uint8_t UartDrv_get( const void *self );
        
//*****************************************************************************
/// \brief ������ �������� ����� ������ � UART.
/// \param self - ��������� �� ������ �������� (�����, ������� ������ �����������);
/// \param data � ���� ��� ��������.
///
void UartDrv_set( const void *self, uint8_t data );

//*****************************************************************************
/// \brief ������� �������� ������.
/// \param self - ��������� �� ������ �������� (�����, ������� ������ �����������).
/// \retval true - ���� ������;
/// \retval false - ��� ������.
///
bool UartDrv_isInReady(const void *self);

//*****************************************************************************
/// \brief ���������� � �������� ������.
/// \param self - ��������� �� ������ �������� (�����, ������� ������ �����������).
/// \retval true - ���� ����������;
/// \retval false - ��� ����������.
///
bool UartDrv_isOutReady(const void *self);

//*****************************************************************************
/// \brief ������ ���������� �������� �� UART.
/// \param self - ��������� �� ������ �������� (�����, ������� ������ �����������).
/// \return ���� ������ �������� �������.
///
uint16_t UartDrv_getError(const void *self);

//*****************************************************************************
/// \brief ����� ������ ��������� UART.
/// \param self - ��������� �� ������ �������, ������� ������ �����������.
///
void UartDrv_reset( const void *self );

//*****************************************************************************
/// \brief ��������� �������� ������ UART.
/// \param self - ��������� �� ������ �������, ������� ������ �����������.
/// \param btr - �������� ������
///
void UartDrv_setBtr( const void *self, uint32_t btr);

#endif

//*****************************************************************************
/**
* ������� ���������: 
*
* ������ 1.0.1
* ����   10-07-2019
* �����  ��������� �.�.
*
* ���������:
*    ������� ������.
*/
