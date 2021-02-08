/**
* \file    UartDrv.c
* \brief   \copybrief UartDrv.h
*
* \version 1.0.1
* \date    10-07-2019
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <xc.h>
#include <string.h>
//#include "ProtectionState_codes.h"
#include "IOdrv.h"
#include "UartPinRemap.h"
#include "UartDrv.h"
//#include "MainRegisters.h"

//*****************************************************************************
// ���������� ����� ������
//*****************************************************************************

//*****************************************************************************
/// \brief ����������� ���������� �� �������� ������������� ������ �� UART.
///
typedef struct
{
    /// \brief MODE: ������� ��������.
    ///
    union
	{
        uint16_t    MODE;
        UxMODEBITS  MODEbits;
    };
    /// \brief STA: ������� �������.
    ///
    union
	{
        uint16_t    STA;
        UxSTABITS   STAbits;
    };
    uint16_t TXREG;      ///< ������� �����������.
    uint16_t RXREG;      ///< ������� ���������.
    uint16_t BRG;        ///< ������� �������� �������� �������.
}Ds33EP_UART_Regs;

//*****************************************************************************
/// \brief ��������� ������ �������� I2C.
///
typedef struct
{
    Uint8IoDriver       pFunctions;   ///< ������ ������������ �������.
    volatile 
    Ds33EP_UART_Regs    *pRegs;       ///< ��������� �� �������� ������ UART.
    uint16_t            dataSTA;      ///< ���������� �������� �������.
    uint16_t            dataRX;       ///< ���������� �������� �������� ������.
} UartDrv;

//*****************************************************************************
// ���������� ��������� ����������
//*****************************************************************************

//*****************************************************************************
static UartDrv aDrv[ eUART_moduleAmount ];     ///< ������ ��������� UART.

//*****************************************************************************
/// \brief ����������� ����� ������� ��������� UART ������ 1.
///
extern volatile Ds33EP_UART_Regs module_1_UART_Regs __attribute__ ((sfr(0x0220)));

//*****************************************************************************
/// \brief ����������� ����� ������� ��������� UART ������ 2.
///
extern volatile Ds33EP_UART_Regs module_2_UART_Regs __attribute__ ((sfr(0x0230)));

//*****************************************************************************
/// \brief ����������� ����� ������� ��������� UART ������ 3.
///
extern volatile Ds33EP_UART_Regs module_3_UART_Regs __attribute__ ((sfr(0x0250)));

//*****************************************************************************
/// \brief ����������� ����� ������� ��������� UART ������ 4.
///
extern volatile Ds33EP_UART_Regs module_4_UART_Regs __attribute__ ((sfr(0x02B0)));

//*****************************************************************************
// ���������� ������������ �������
//*****************************************************************************

//*****************************************************************************
// �����������
const void *UartDrv_ctor( eUART_modules      numberModule,  
                          eUART_bitAndParity bitAndParity,
                          eUART_bitStop      bitStop,  
                          eUART_speed        speed,
                          uint16_t           brg )
{
    if ( numberModule >= eUART_moduleAmount )  return 0;
 
    UartDrv *me = &aDrv[numberModule]; 
 
    me->pFunctions.run        = UartDrv_run;
    me->pFunctions.isInReady  = UartDrv_isInReady;
    me->pFunctions.isOutReady = UartDrv_isOutReady;
    me->pFunctions.set        = UartDrv_set;
    me->pFunctions.get        = UartDrv_get;
    me->pFunctions.getError   = UartDrv_getError;
    me->pFunctions.reset      = UartDrv_reset;
   
    me->dataSTA = 0;
    
    switch ( numberModule ) 
    {
        case eUART_module1 :
            me->pRegs  = &module_1_UART_Regs;
            UART1_PIN_REMAP;
            break;
        case eUART_module2 :
            me->pRegs  = &module_2_UART_Regs;
            UART2_PIN_REMAP;
            break;
        case eUART_module3 :
            me->pRegs  = &module_3_UART_Regs;
            UART3_PIN_REMAP;
            break;
        case eUART_module4 :
            me->pRegs  = &module_4_UART_Regs;
            UART4_PIN_REMAP;
            break;
    default :
            return 0;
            break;
    } 
    //*** ������������ ��������� ���������� UART ***
    me->pRegs->BRG = brg;                     // �������� ������
    me->pRegs->MODE = 0;
    me->pRegs->MODEbits.PDSEL = bitAndParity; // ���������� ��� ������ � �������� ��������
    me->pRegs->MODEbits.STSEL = bitStop;      // ���������� ���� �����
    me->pRegs->MODEbits.BRGH  = speed;        // ��� ��������  
    me->pRegs->MODEbits.UARTEN = 1;           // ���������� ������ UART
    me->pRegs->STAbits.UTXEN = 1;             // ���������� ������ ����������� UART

    return me; 
}

//*****************************************************************************
// ������� ����
void UartDrv_run( const void *self )
{
}

//*****************************************************************************
// ������ �������� ������ �� UART.
uint8_t UartDrv_get( const void *self )
{
    UartDrv *me = ( UartDrv * ) self;
    if (  me->pRegs->STAbits.URXDA )  me->dataRX = me->pRegs->RXREG;
    me->dataSTA  = me->pRegs->STA;
    if ( me->pRegs->STAbits.OERR ) me->pRegs->STAbits.OERR = 0;
    return me->dataRX;   
}
 
//*****************************************************************************
// ������ �������� ����� ������ � UART.
void UartDrv_set( const void *self, uint8_t data )
{
    ( ( UartDrv * )self )->pRegs->TXREG = data;
}

//*****************************************************************************
// ������� �������� ������.
bool UartDrv_isInReady( const void *self )
{
    return ( ( ( UartDrv * )self )->pRegs->STAbits.URXDA == 1 ) ? true : false;
}

//*****************************************************************************
// ���������� � �������� ������.
bool UartDrv_isOutReady( const void *self )
{
    return ( ( ( UartDrv * )self )->pRegs->STAbits.TRMT == 1 ) ? true : false;
}

//*****************************************************************************
// ������ ���������� �������� �� UART.
uint16_t UartDrv_getError( const void *self )
{
    return ( ( UartDrv * )self )->dataSTA & 0x000E; // ���� ������
}

//*****************************************************************************
// ����� ������ ��������� UART.
void UartDrv_reset( const void *self )
{
    UartDrv *me = ( UartDrv * ) self;
    me->dataSTA  = me->pRegs->STA;
    me->pRegs->STAbits.OERR = 0;
    me->pRegs->STAbits.PERR = 0;
    me->pRegs->STAbits.FERR = 0;
}

//*****************************************************************************
// ��������� �������� ������ (�������) UART.
void UartDrv_setBtr( const void *self, uint32_t btr)
{
   ( ( UartDrv * )self )->pRegs->BRG = BRG_UART_LOW_SPEED(btr); 
}


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
