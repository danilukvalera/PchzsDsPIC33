/**
* \file    MainRegisters.h
* \brief   �������� ����� � �������� ��� ��������� �� dsPIC33
*
* \version 1.0.1
* \date    14-03-2018
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef MAINREGISTERS_H
#define MAINREGISTERS_H

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <xc.h>

//*****************************************************************************
// ���������� ���������, ������������ ����� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ���������� ������ ����������� �������.
///
#define ENABLE_WATCHDOG  RCONbits.SWDTEN = 1;


//*****************************************************************************
/// \brief ��� ������������ �������� ���������� � ����� � PLL.
///
#define FNOSC_PRIPLL_            3

//*****************************************************************************
/// \brief �������� �������.
///
#define F_OSC                    117964800UL

//*****************************************************************************
/// \brief ��������� ������� ����������.
///
#define F_CY                      58982400UL

//*****************************************************************************
/// \brief ����� ���������� 1 ������� �� � �������� ����*10.
///
#define T_CY_NANO_10              170U

//*****************************************************************************
/// \brief ������ ���������� �� ������� � �������� ����.
///
#define TIME_PERIOD_INTERRUPT_NS  31250U 

//*****************************************************************************
///// \brief ������ ���������� ������� � Tcy.
/////
//#define TIME_PERIOD_INTERRUPT    ((TIME_PERIOD_INTERRUPT_NS)/(T_CY_NANO_10))*10

//*****************************************************************************
// ������� �������� �������
/// \brief ������� ������ ����������.
///
#define MAIN_TIMER_LOAD_PR5     58983  //1.0000102 ms ��� Fcy  = 58.9824 ��� (Tcy = 16.954 ��)

//*****************************************************************************
/// \brief ������� ������ ����������.
///
#define MAIN_TIMER  TMR5

//*****************************************************************************
/// \brief ������� ������� �������� ������� ����������.
///
#define MAIN_TIMER_PRx PR5

//*****************************************************************************
/// \brief ����������� ������� �������� ������� ����������.
///
#define MAIN_TIMER_TxCON T5CON 

//*****************************************************************************
/// \brief ��������� �������� ������� ����������.
///
#define MAIN_TIMER_STOP  T5CONbits.TON = 0

//*****************************************************************************
/// \brief ����� ����� ���������� �������� �������.
///
#define MAIN_TIMER_INTERRUPT_CLEAR_FLAG  IFS1bits.T5IF = 0

//*****************************************************************************
/// \brief ������ ����������  �������� �������.
///
#define MAIN_TIMER_DISABLE_INTERRUPT  (IEC1bits.T5IE = 0)

//*****************************************************************************
/// \brief ���������� ����������  �������� �������.
///
#define MAIN_TIMER_ENABLE_INTERRUPT   (IEC1bits.T5IE = 1)

//*****************************************************************************

//*****************************************************************************
/// \brief ����� ����� ���������� U1RX (UART1)
///
#define U1RX_INTERRUPT_CLEAR_FLAG  (IFS0bits.U1RXIF = 0)

//*****************************************************************************
/// \brief ������ ����������  U1RX (UART1)
///
#define U1RX_DISABLE_INTERRUPT  (IEC0bits. U1RXIE = 0)

//*****************************************************************************
/// \brief ���������� ����������  U1RX (UART1)
///
#define U1RX_ENABLE_INTERRUPT   (IEC0bits. U1RXIE = 1)

//*****************************************************************************

/// \brief ��������� ���������� (�����������).
/// \note 
/// 1. ������������ ��������� �������:
///     - Fin  = 14.7456  - ������� ������.
///     - Fref = 3.6864 ���.
///     - Fsys = 235.9296 ���.
///     - Fosc = 117.9648 ���.
///     - Fcy  = 58.9824 ��� (Tcy = 16.954 ��).
/// \note 2. ������������� ������������ PLLPRE   (N1 = 4), ������������ PLLPOST  (N2 = 2) � �������� ����� PLLDIV (M = 64).
/// \note 3. ������������ ������������ �������� ���������� � ����� � PLL.
/// \note 4. �������� ������������ �������� ���������� � ����� XT � PLL.  
///
#define SET_OSCILLATOR do { \
    CLKDIVbits.PLLPRE  = 2; \
    CLKDIVbits.PLLPOST = 0; \
    PLLFBDbits.PLLDIV  = 62; \
     __builtin_write_OSCCONH(FNOSC_PRIPLL_); \
     __builtin_write_OSCCONL(0x01); \
    while (OSCCONbits.COSC != FNOSC_PRIPLL_) {\
    }\
    while (OSCCONbits.LOCK != 1) {\
    }\
} while (0)

//*****************************************************************************
/// \brief ��������� �������� ������� ����������.
///
#define MAIN_TIMER_INIT do {                                         \
    T5CONbits.TON   = 0;          /* Stop any 16-bit Timer operation         */\
    T5CONbits.TCS   = 0;          /* Select internal instruction cycle clock */\
    T5CONbits.TGATE = 0;          /* Disable Gated Timer mode                */\
    T5CONbits.TCKPS = 0b00;       /* Select 1:1 Prescaler                    */\
    TMR5            = 0x00;       /* Clear 16-bit Timer                      */\
    PR5 = MAIN_TIMER_LOAD_PR5;    /* Load 16-bit period value                */\
} while(0)
//*****************************************************************************
/// \brief ������ �������� ������� ����������.
///
#define MAIN_TIMER_START do {                                                  \
    T5CONbits.TON   = 1;          /* Start 16-bit Timer  */                    \
} while(0)


//*****************************************************************************
/// \brief ��������� ����������.
///  
#define INTERRUPT_INIT do {                                                                           \
    INTCON1bits.NSTDIS = 0;          /* ���������� ����������� ���������� */                          \
    IPC7bits.T5IP      = 4;          /* ��������� ���������� ���������� �� �������� ������� */        \
                                     /* ��������� ���������� �������� ������� */                      \
    MAIN_TIMER_INTERRUPT_CLEAR_FLAG; /* ����� ����� ������� ���������� �������� ������� */            \
    MAIN_TIMER_ENABLE_INTERRUPT;     /* ���������� ���������� �� �������� ������� */                  \
    U1RX_INTERRUPT_CLEAR_FLAG;       /* ����� ����� ���������� U1RX */                                \
    U1RX_ENABLE_INTERRUPT;     /* ���������� ���������� �� U1RX */                              \
} while(0)

//*****************************************************************************
/// \brief ������ ���� ����������.
///
#define INTERRUPT_ALL_DISABLE do {                                                 \
    MAIN_TIMER_DISABLE_INTERRUPT; /* ���������� ���������� �� �������� �������  */ \
    RCONbits.SWDTEN = 0;                                                           \
} while(0)    

#endif

//*****************************************************************************
/**
* ������� ���������:
* 
* ������ 1.0.1
* ����   14-03-2018
* �����  ��������� �.�.
*
* ���������:
*    ������� ������.
*/
