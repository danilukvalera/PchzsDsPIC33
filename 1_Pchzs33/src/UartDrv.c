/**
* \file    UartDrv.c
* \brief   \copybrief UartDrv.h
*
* \version 1.0.1
* \date    10-07-2019
* \author  Третьяков В.Ж.
*/

//*****************************************************************************
// Подключаемые файлы
//*****************************************************************************
#include <xc.h>
#include <string.h>
//#include "ProtectionState_codes.h"
#include "IOdrv.h"
#include "UartPinRemap.h"
#include "UartDrv.h"
//#include "MainRegisters.h"

//*****************************************************************************
// Объявление типов данных
//*****************************************************************************

//*****************************************************************************
/// \brief Определение указателей на регистры периферийного модуля МК UART.
///
typedef struct
{
    /// \brief MODE: регистр контроля.
    ///
    union
	{
        uint16_t    MODE;
        UxMODEBITS  MODEbits;
    };
    /// \brief STA: регистр статуса.
    ///
    union
	{
        uint16_t    STA;
        UxSTABITS   STAbits;
    };
    uint16_t TXREG;      ///< Регистр передатчика.
    uint16_t RXREG;      ///< Регистр приемника.
    uint16_t BRG;        ///< Регистр делителя тактовой частоты.
}Ds33EP_UART_Regs;

//*****************************************************************************
/// \brief Структура данных драйвера I2C.
///
typedef struct
{
    Uint8IoDriver       pFunctions;   ///< Адреса интерфейсных функций.
    volatile 
    Ds33EP_UART_Regs    *pRegs;       ///< Указатель на регистры модуля UART.
    uint16_t            dataSTA;      ///< Содержимое регистра статуса.
    uint16_t            dataRX;       ///< Содержимое регистра принятых данных.
} UartDrv;

//*****************************************************************************
// Объявление локальных переменных
//*****************************************************************************

//*****************************************************************************
static UartDrv aDrv[ eUART_moduleAmount ];     ///< Данные драйверов UART.

//*****************************************************************************
/// \brief Определение блока адресов регистров UART модуля 1.
///
extern volatile Ds33EP_UART_Regs module_1_UART_Regs __attribute__ ((sfr(0x0220)));

//*****************************************************************************
/// \brief Определение блока адресов регистров UART модуля 2.
///
extern volatile Ds33EP_UART_Regs module_2_UART_Regs __attribute__ ((sfr(0x0230)));

//*****************************************************************************
/// \brief Определение блока адресов регистров UART модуля 3.
///
extern volatile Ds33EP_UART_Regs module_3_UART_Regs __attribute__ ((sfr(0x0250)));

//*****************************************************************************
/// \brief Определение блока адресов регистров UART модуля 4.
///
extern volatile Ds33EP_UART_Regs module_4_UART_Regs __attribute__ ((sfr(0x02B0)));

//*****************************************************************************
// Реализация интерфейсных функций
//*****************************************************************************

//*****************************************************************************
// Конструктор
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
    //*** Конфигурация регистров управления UART ***
    me->pRegs->BRG = brg;                     // скорость обмена
    me->pRegs->MODE = 0;
    me->pRegs->MODEbits.PDSEL = bitAndParity; // количество бит данных и контроль четности
    me->pRegs->MODEbits.STSEL = bitStop;      // количество стоп битов
    me->pRegs->MODEbits.BRGH  = speed;        // тип скорости  
    me->pRegs->MODEbits.UARTEN = 1;           // разрешение работы UART
    me->pRegs->STAbits.UTXEN = 1;             // разрешение работы передатчика UART

    return me; 
}

//*****************************************************************************
// Рабочий цикл
void UartDrv_run( const void *self )
{
}

//*****************************************************************************
// Чтение принятых данных по UART.
uint8_t UartDrv_get( const void *self )
{
    UartDrv *me = ( UartDrv * ) self;
    if (  me->pRegs->STAbits.URXDA )  me->dataRX = me->pRegs->RXREG;
    me->dataSTA  = me->pRegs->STA;
    if ( me->pRegs->STAbits.OERR ) me->pRegs->STAbits.OERR = 0;
    return me->dataRX;   
}
 
//*****************************************************************************
// Запуск передачи байта данных в UART.
void UartDrv_set( const void *self, uint8_t data )
{
    ( ( UartDrv * )self )->pRegs->TXREG = data;
}

//*****************************************************************************
// Наличие принятых данных.
bool UartDrv_isInReady( const void *self )
{
    return ( ( ( UartDrv * )self )->pRegs->STAbits.URXDA == 1 ) ? true : false;
}

//*****************************************************************************
// Готовность к передаче данных.
bool UartDrv_isOutReady( const void *self )
{
    return ( ( ( UartDrv * )self )->pRegs->STAbits.TRMT == 1 ) ? true : false;
}

//*****************************************************************************
// Запрос результата операции по UART.
uint16_t UartDrv_getError( const void *self )
{
    return ( ( UartDrv * )self )->dataSTA & 0x000E; // биты ошибок
}

//*****************************************************************************
// Сброс модуля периферии UART.
void UartDrv_reset( const void *self )
{
    UartDrv *me = ( UartDrv * ) self;
    me->dataSTA  = me->pRegs->STA;
    me->pRegs->STAbits.OERR = 0;
    me->pRegs->STAbits.PERR = 0;
    me->pRegs->STAbits.FERR = 0;
}

//*****************************************************************************
// Установка скорости обмена (битрейт) UART.
void UartDrv_setBtr( const void *self, uint32_t btr)
{
   ( ( UartDrv * )self )->pRegs->BRG = BRG_UART_LOW_SPEED(btr); 
}


//*****************************************************************************
/**
* История изменений:
* 
* Версия 1.0.1
* Дата   10-07-2019
* Автор  Третьяков В.Ж.
* 
* Изменения:
*    Базовая версия.
*/
