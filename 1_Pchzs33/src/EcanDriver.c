/**
* \file    EcanDriver.c
* \brief   \copybrief EcanDriver.h
*
* \version 1.0.4
* \date    26-11-2019
* \author  Третьяков В.Ж.
*/

//*****************************************************************************
// Подключаемые файлы
//*****************************************************************************
#include <xc.h>
#include "Ecan_regsPIC33.h"
#include "MainRegisters.h"
#include "MainInitial.h"
#include "Ecan_DMA.h"
#include "ECAN1_PinRemap.h"
#include "ECAN2_PinRemap.h"
#include "EcanDriver.h"

//*****************************************************************************
// Локальные константы, определенные через макросы
//*****************************************************************************

//*****************************************************************************
/// \brief Преобразование указателя на данные в указатель на ECAN.
/// \param self_ – указатель на данные.
///
#define DRV_DATA( self_ )  ( (Ecan *) self_ )

//*****************************************************************************
/// \brief Указатель на регистры модуля CAN. 
/// \details Макрос предназначен для упрощения записи указателей на регистры
/// модуля CAN.
/// \param self_ – указатель на структуру ECAN.
/// 
#define DRV_REGS( self_ ) ( (volatile Ecan *) self_ )->pEcanRegs

//*****************************************************************************
/// \brief Номер буфера для хранения принятых сообщений.
///
#define NUM_BUFF_IN   8

//*****************************************************************************
/// \brief Номер буфера для хранения передаваемых сообщений.
///
#define NUM_BUFF_OUT  0

//*****************************************************************************
/// \brief Имя бита с принятой информацией.
///
#define NAME_BIT_RXFULL RXFUL8

//*****************************************************************************
/// \brief Количество буферов сообщений для модуля ECAN.
///
#define NUMBER_OF_ECAN_MESSAGE 32

//*****************************************************************************
/// \brief Размер буфера сообщения для модуля ECAN.
///
#define SIZE_OF_ECAN_MESSAGE 8

//*****************************************************************************
// Объявление типов данных
//*****************************************************************************

//*****************************************************************************
/// \brief Состояние передатчика.
///
typedef enum {
    eEcanTrStateIdle = 0,          ///< передатчик свободен и готов к работе
    eEcanTrStateInProgress,        ///< передатчик в процессе передачи данных
//  eEcanTrStateRequest,           ///< передатчик поступил запрос на передачу данных
    eEcanTrStateError,             ///< передача закончилась ошибкой
} EcanTrState;

//*****************************************************************************
/// \brief Содержание регистра CxINTF.
///
typedef union {
    unsigned short  data;
    C1INTFBITS      bits;
} TempINTF; 

//*****************************************************************************
/// \brief Данные драйвера.
///
typedef struct {
    ArrayIoDriver pFunctions;            ///< Адреса интерфейсных функций.
    bool          receiverIsReady;       ///< Данные приняты и готовы к чтению.
    EcanTrState   transmissionState;     ///< Состояние передатчика.
    TempINTF      tempINTF;              ///< Буфер для хранения значения регистра INTF.
    EcanMode       workMode;              ///< Режим работы модуля.
    uint16_t      lengthPacket;          ///< Длина пакета данных.
    volatile  dsPIC33E_CanRegs 
                *pEcanRegs;               ///< Указатель на регистры модуля ECAN.
    uint16_t      error;                 ///< Результат завершения передачи (если #ePS_NoError, то норма).
} Ecan;

//*****************************************************************************
// Объявление локальных констант
//*****************************************************************************

//*****************************************************************************
const uint16_t MAX_DATA_LENGHT = 8;        ///< Максимальная длина пакета данных.

#define RAM_END_ADDRESS 0x7000  ///< Адрес конца области ОЗУ


//*****************************************************************************
// Объявление локальных переменных
//*****************************************************************************

//*****************************************************************************
static Ecan aEcan[ eEcanCount ];           ///< Данные драйверов ECAN.

//*****************************************************************************
/// \brief Буфера сообщений модуля ECAN1.
///
__eds__ uint16_t aEcan1MsgBuf[NUMBER_OF_ECAN_MESSAGE][SIZE_OF_ECAN_MESSAGE]  __attribute__((address(RAM_END_ADDRESS)));

//*****************************************************************************
/// \brief Буфера сообщений модуля ECAN2.
///
__eds__ uint16_t aEcan2MsgBuf[NUMBER_OF_ECAN_MESSAGE][SIZE_OF_ECAN_MESSAGE]  __attribute__((address(RAM_END_ADDRESS + 
    NUMBER_OF_ECAN_MESSAGE * 16)));

//*****************************************************************************
// Прототипы локальных функций
//*****************************************************************************

//*****************************************************************************
/// \brief Перевод модуля CAN в заданный режим.
/// \param self - указатель на данные объекта, который вернул конструктор;
/// \param mode - заданный режим.
///
void Ecan_setMode( const void *self, uint16_t mode );

//*****************************************************************************
// Реализация интерфейсных функций
//*****************************************************************************

//*****************************************************************************
// Конструктор 
const void *Ecan_ctor( EcanNumber numEcan,
                       uint16_t txSid, 
                       uint16_t rxSid,
                       uint16_t mode,
                       uint16_t lengthPacket, 
                       uint16_t tipeCAN )
{
    Ecan *pEcan;
    unsigned long localAddress;

    pEcan = &aEcan[ numEcan ];
    pEcan->receiverIsReady   = false;
    pEcan->transmissionState = eEcanTrStateIdle;
    pEcan->lengthPacket = lengthPacket;
    
    pEcan->pFunctions.run = Ecan_run;
    pEcan->pFunctions.isInReady = Ecan_isInReady;
    pEcan->pFunctions.isOutReady = Ecan_isOutReady;
    pEcan->pFunctions.set = Ecan_set;
    pEcan->pFunctions.get = Ecan_get;
    pEcan->pFunctions.getError = Ecan_getError;
    pEcan->pFunctions.reset = Ecan_reset;
    pEcan->pFunctions.start = Ecan_start;
    pEcan->pFunctions.getSID = Ecan_getSID;
    
    switch ( numEcan )
    {
        case eEcan1:
            pEcan->pEcanRegs = &dsPIC33E_Can1Regs;
            ECAN1_PIN_REMAP; 
            ECAN_CONFIG_DMA(DMA0, eDMA_readRAM,  eDMA_ECAN1_TX, C1TXD, aEcan1MsgBuf, localAddress);
            ECAN_CONFIG_DMA(DMA1, eDMA_writeRAM, eDMA_ECAN1_RX, C1RXD, aEcan1MsgBuf, localAddress);
            aEcan1MsgBuf[NUM_BUFF_OUT][0] = (txSid << 2);
            aEcan1MsgBuf[NUM_BUFF_OUT][1] = 0x0000;
            aEcan1MsgBuf[NUM_BUFF_OUT][2] = lengthPacket;
            break;
        case eEcan2:
            pEcan->pEcanRegs = &dsPIC33E_Can2Regs;
            ECAN2_PIN_REMAP;
            ECAN_CONFIG_DMA(DMA2, eDMA_readRAM,  eDMA_ECAN2_TX, C2TXD, aEcan2MsgBuf, localAddress);
            ECAN_CONFIG_DMA(DMA3, eDMA_writeRAM, eDMA_ECAN2_RX, C2RXD, aEcan2MsgBuf, localAddress);
            aEcan2MsgBuf[NUM_BUFF_OUT][0] = (txSid << 2);
            aEcan2MsgBuf[NUM_BUFF_OUT][1] = 0x0000;
            aEcan2MsgBuf[NUM_BUFF_OUT][2] = lengthPacket;
            break;
        default:
            break;
    }
    pEcan->workMode = mode; // режим работы модуля

    //*****************************************************************************
    // Настройка модуля CAN
    Ecan_setMode( (const void *) pEcan, eEcanModeConfig );
    
    if(tipeCAN == GARS_S){
        pEcan->pEcanRegs->CTRL1bits.WIN    = 0;     // Окно регистров 0
        pEcan->pEcanRegs->CTRL1bits.CSIDL  = 1;     // Запрет работы CAN в режиме "Idle" 
        pEcan->pEcanRegs->CTRL1bits.CANCKS = 1;     // Выбор частоты Fcan = 2 * 58,9824 MHz = 117,9648 MHz
        pEcan->pEcanRegs->CTRL1bits.CANCAP = 1;     // CAN capture включен

        pEcan->pEcanRegs->CFG1bits.SJW     = 0;     // Ширина перехода синхронизации равна 1*Tq 
        pEcan->pEcanRegs->CFG1bits.BRP     = 7;     // Определяем Tq (квант времени) равным 117,9648 MHz / 2 / 8 = 135/2 = 7,3728 MHz (Tq)
                                                    // Номинальное время передачи бита (Nominal Bit Time - NBT) 
                                                    // 7372800/8 = (921600 бит/с)
        //настройки регистра CFG2 выполнены как переделал Агулов для dsPIC30 CFG2 = 02D0
        //закоментированы значения которые настроены в ГАРС-С
                                                    // Nominal Bit Time - NBT = 8*Tq (1+1+3+3), т.к. 
                                                    // Сегмент синхронизации (SyncSeg)  - Длительность его фиксирована и всегда равна 1*Tq
        pEcan->pEcanRegs->CFG2bits.PRSEG    = 0;//1;// Сегмент распространения (PropSeg)- PRSEG=0,  т.е. 1*Tq
        pEcan->pEcanRegs->CFG2bits.SEG1PH   = 2;//1;// Сегмент фазы 1(PS1)              - SEG1PH=2, т.е. 3*Tq
        pEcan->pEcanRegs->CFG2bits.SEG2PH   = 2;    // Сегмент фазы 2(PS2)              - SEG2PH=2, т.е. 3*Tq
        pEcan->pEcanRegs->CFG2bits.SAM      = 1;
        pEcan->pEcanRegs->CFG2bits.SEG2PHTS = 1;//0;
        pEcan->pEcanRegs->CFG2bits.WAKFIL   = 0;
        
        pEcan->pEcanRegs->FCTRLbits.DMABS = 0b110;

        // Конфигурация приемника CAN 
        pEcan->pEcanRegs->FMSKSEL1bits.F0MSK = 0;      // Select Acceptance Filter Mask 0 for Acceptance Filter 0
        pEcan->pEcanRegs->CTRL1bits.WIN    = 1;        // Окно регистров 1
    
        pEcan->pEcanRegs->RXF0SIDbits.SID = rxSid;     // Configure Acceptance Filter 0 to match standard identifier
        pEcan->pEcanRegs->RXF0SIDbits.EXIDE= 0x0;      // Стандартный идентификатор 

        //pEcan->pEcanRegs->RXM0SID = (0x7FF << 5);    // записали маску 1111 1111 1110 0000 (значения SID не могут отличаться
        pEcan->pEcanRegs->RXM0SID = (0x7FF << 6);      // записали маску 1111 1111 1100 0000 (значения SID могут отличаться в младшем бите)
        pEcan->pEcanRegs->RXM0SIDbits.MIDE = 0x1;      // Acceptance Filter 0 to check for Standard Identifier
    
        pEcan->pEcanRegs->BUFPNT1bits.F0BP = NUM_BUFF_IN; // Acceptance Filter 0 to use Message Buffer xx to store message
   
        pEcan->pEcanRegs->CTRL1bits.WIN    = 0;       // Окно регистров 0
    
        pEcan->pEcanRegs->FEN1bits.FLTEN0=0x1;        // Filter 0 enabled for Identifier match with incoming message 
        pEcan->pEcanRegs->CTRL1bits.WIN=0x0;          // Clear Window Bit to Access ECAN

        // Конфигурация передатчика CAN 
        /* Configure Message Buffer 0 for Transmission and assign priority */
        pEcan->pEcanRegs->TR01CONbits.TXEN0  = 1;     // Configure Message Buffer xx for Transmission
        pEcan->pEcanRegs->TR01CONbits.TX0PRI = 0x3;   // Configure Message  priority 

        pEcan->pEcanRegs->CTRL1bits.WIN = 0;
    }
    
    if(tipeCAN == GKLS_E){
        pEcan->pEcanRegs->CTRL1bits.WIN    = 0;     // Окно регистров 0
        pEcan->pEcanRegs->CTRL1bits.CSIDL  = 1;     // Запрет работы CAN в режиме "Idle" 
        pEcan->pEcanRegs->CTRL1bits.CANCKS = 1;     // Выбор частоты Fcan = 2 * 58,9824 MHz = 117,9648 MHz
        pEcan->pEcanRegs->CTRL1bits.CANCAP = 1;     // CAN capture включен

        pEcan->pEcanRegs->CFG1bits.SJW     = 0;     // Ширина перехода синхронизации равна 1*Tq 
        pEcan->pEcanRegs->CFG1bits.BRP     = 7;     // Определяем Tq (квант времени) равным 117,9648 MHz / 2 / 8 = 135/2 = 7,3728 MHz (Tq)
                                                    // Номинальное время передачи бита (Nominal Bit Time - NBT) 
                                                    // 7372800/7 = (1053257 бит/с)
        
        //для старых устройств типа ГКЛС-Е должно быть CFG2 = 0189
                                                    // Nominal Bit Time - NBT = 7*Tq (1+2+2+2), т.к.
                                                    // Сегмент синхронизации (SyncSeg)  - Длительность его фиксирована и всегда равна 1*Tq
        pEcan->pEcanRegs->CFG2bits.PRSEG    = 1;    // Сегмент распространения (PropSeg)- PRSEG=1,  т.е. 2*Tq
        pEcan->pEcanRegs->CFG2bits.SEG1PH   = 1;    // Сегмент фазы 1(PS1)              - SEG1PH=1, т.е. 2*Tq
        pEcan->pEcanRegs->CFG2bits.SEG2PH   = 1;    // Сегмент фазы 2(PS2)              - SEG2PH=1, т.е. 2*Tq
        pEcan->pEcanRegs->CFG2bits.SAM      = 0;
        pEcan->pEcanRegs->CFG2bits.SEG2PHTS = 1;
        pEcan->pEcanRegs->CFG2bits.WAKFIL   = 0;
        
        pEcan->pEcanRegs->FCTRLbits.DMABS = 0b110;

        // Конфигурация приемника CAN 
        pEcan->pEcanRegs->FMSKSEL1bits.F0MSK = 0;      // Select Acceptance Filter Mask 0 for Acceptance Filter 0
        pEcan->pEcanRegs->CTRL1bits.WIN    = 1;        // Окно регистров 1
    
        pEcan->pEcanRegs->RXF0SIDbits.SID = rxSid;     // Configure Acceptance Filter 0 to match standard identifier
        pEcan->pEcanRegs->RXF0SIDbits.EXIDE= 0x0;      // Стандартный идентификатор 

        //pEcan->pEcanRegs->RXM0SID = (0x7FF << 5);    // записали маску 1111 1111 1110 0000 (значения SID не могут отличаться
        pEcan->pEcanRegs->RXM0SID = (0x7FF << 6);      // записали маску 1111 1111 1100 0000 (значения SID могут отличаться в младшем бите)
        pEcan->pEcanRegs->RXM0SIDbits.MIDE = 0x1;      // Acceptance Filter 0 to check for Standard Identifier
    
        pEcan->pEcanRegs->BUFPNT1bits.F0BP = NUM_BUFF_IN; // Acceptance Filter 0 to use Message Buffer xx to store message
   
        pEcan->pEcanRegs->CTRL1bits.WIN    = 0;       // Окно регистров 0
    
        pEcan->pEcanRegs->FEN1bits.FLTEN0=0x1;        // Filter 0 enabled for Identifier match with incoming message 
        pEcan->pEcanRegs->CTRL1bits.WIN=0x0;          // Clear Window Bit to Access ECAN

        // Конфигурация передатчика CAN 
        /* Configure Message Buffer 0 for Transmission and assign priority */
        pEcan->pEcanRegs->TR01CONbits.TXEN0  = 1;     // Configure Message Buffer xx for Transmission
        pEcan->pEcanRegs->TR01CONbits.TX0PRI = 0x3;   // Configure Message  priority 

        pEcan->pEcanRegs->CTRL1bits.WIN = 0;
    }
    
    Ecan_setMode( (const void *) pEcan, pEcan->workMode );
 
    pEcan->pEcanRegs->INTF = 0;
 
    return (const void *) pEcan;
}

//*****************************************************************************
// Чтение данных из периферийного устройства  
uint16_t Ecan_get( const void *self, uint8_t *array, uint16_t size )
{
//    DASSERT_ID( eGrPS_EcanDrv, ePS_ErrorParameters,
//                ( ( self == &aEcan[ eDsPIC30Ecan1 ] ) || ( self == &aEcan[ eDsPIC30Ecan2 ] ) ) );
//
//    ASSERT_EX2_ID( eGrPS_EcanDrv, ePS_ErrorParameters,
//                   DRV_DATA( self )->lengthPacket == size,
//                   DRV_DATA( self )->lengthPacket, size );
    
    __eds__ uint8_t *pMsgBuf;        ///< Указатель массива данных ECAN.
    uint8_t i;

    if (self == &aEcan[eEcan1]) {
        pMsgBuf  = (__eds__ uint8_t *)&aEcan1MsgBuf[NUM_BUFF_IN][3];
    } else {
        pMsgBuf  = (__eds__ uint8_t *)&aEcan2MsgBuf[NUM_BUFF_IN][3];
    }
    for ( i = 0; i < size; i++) { 
        *array++ = *pMsgBuf++;
    }
 
    DRV_DATA( self )->receiverIsReady = false;

    return DRV_DATA( self )->lengthPacket;
}

//*****************************************************************************
/// Чтение SID из пакета данных  
uint16_t Ecan_getSID( const void *self)
{
    uint16_t sid;

    if (self == &aEcan[eEcan1]) {
        sid = (uint8_t)(aEcan1MsgBuf[NUM_BUFF_IN][0] >> 10);
        sid = sid<<8 | (uint8_t)(aEcan1MsgBuf[NUM_BUFF_IN][0] >> 2);
    } else {
        sid = (uint8_t)(aEcan2MsgBuf[NUM_BUFF_IN][0] >> 10);
        sid = sid<<8 | (uint8_t)(aEcan2MsgBuf[NUM_BUFF_IN][0] >> 2);
    }

    return sid;
}

//*****************************************************************************
// Готовность устройства к чтению принятых данных    
bool Ecan_isInReady( const void *self )
{
    return DRV_DATA( self )->receiverIsReady;
}

//*****************************************************************************
// Передача данных драйверу для вывода
void Ecan_set( const void *self, uint8_t *array, uint16_t size )
{
    __eds__ uint8_t *pMsgBuf;        ///< Указатель массива данных ECAN.
    uint8_t i;

    if (self == &aEcan[eEcan1]) {
        pMsgBuf  = (__eds__ uint8_t *)&aEcan1MsgBuf[NUM_BUFF_OUT][3];
    } else {
        pMsgBuf  = (__eds__ uint8_t *)&aEcan2MsgBuf[NUM_BUFF_OUT][3];
    }

    for (i = 0; i < size; i++) { 
        *pMsgBuf++ = *array++;
    }
    Ecan_startTransmite( self );
}

//*****************************************************************************
// Сброс операции передачи данных
void Ecan_AbortTransmite( const void *self )
{
    DRV_REGS( self )->CTRL1bits.ABAT = 1; // Сброс всех передач
    DRV_DATA( self )->transmissionState = eEcanTrStateIdle;
}

//*****************************************************************************
// Запустить операцию передачи данных
void Ecan_startTransmite( const void *self )
{
    DRV_REGS( self )->TR01CONbits.TXREQ0 = 1;
    DRV_DATA( self )->transmissionState = eEcanTrStateInProgress;
}

//*****************************************************************************
// Запуск операции передачи.
void Ecan_start( const void *self, uint8_t *array, uint16_t size )
{
    Ecan_set( self, array, size );
    Ecan_startTransmite( self );
}

//*****************************************************************************
// Готовность устройства к выводу новых данных
bool Ecan_isOutReady( const void *self )
{
    return ( DRV_DATA( self )->transmissionState == eEcanTrStateIdle) ? true : false;
}

//*****************************************************************************
// Рабочий цикл
void Ecan_run( const void *self )
{
    DRV_DATA( self )->tempINTF.data = DRV_REGS( self )->INTF;

    // Сброс Bus-off state модуля CAN
   if ((DRV_DATA( self )->tempINTF.bits.TXBP  == 1) ||
       (DRV_DATA( self )->tempINTF.bits.EWARN == 1)) {
       Ecan_reset( self );
   }

    // Прием данных
    if ( (DRV_DATA( self )->tempINTF.bits.IVRIF == 1) || 
         (DRV_DATA( self )->tempINTF.bits.RBOVIF == 1) )  { 
        DRV_REGS( self )->INTFbits.RBOVIF = 0; 
        DRV_REGS( self )->INTFbits.RBIF = 0;
        DRV_REGS( self )->RXFUL1bits.NAME_BIT_RXFULL = 0;
        DRV_REGS( self )->INTFbits.IVRIF = 0;
        DRV_REGS( self )->INTFbits.ERRIF = 0;
    }
    if (DRV_DATA( self )->tempINTF.bits.RBIF == 1) {
        DRV_DATA( self )->receiverIsReady = true;
        DRV_REGS( self )->INTFbits.RBIF = 0;
        DRV_REGS( self )->RXFUL1bits.NAME_BIT_RXFULL = 0;
    }
/*    
    // Передача данных
    switch ( DRV_DATA( self )->transmissionState ) {
        case eEcanTrStateIdle:

            break;
        case eEcanTrStateInProgress:
           if( DRV_REGS( self )->TR01CONbits.TXREQ0 == 0 ) { // ждем окончания передачи
                DRV_REGS( self )->CTRL1bits.ABAT = 1;
                if (DRV_REGS( self )->INTFbits.ERRIF == 0 )
                {
                    DRV_DATA( self )->error = 0; //??? ePS_NoError;
                }
                else
                {
                    DRV_DATA( self )->error = 1; //??? ePS_ErrorDevice;
                }
                DRV_DATA( self )->transmissionState = eEcanTrStateIdle;               
            }
            break;
        case eEcanTrStateError:
            break;
    }
*/ 
}

//*****************************************************************************
// Сброс операций приема и передачи данных
void Ecan_reset( const void *self )
{
    Ecan_setMode( self, eEcanModeConfig );
    Ecan_setMode( self, DRV_DATA(self)->workMode );
    DRV_REGS( self )->CTRL1bits.ABAT = 1; // Сброс всех передач
    DRV_DATA( self )->transmissionState = eEcanTrStateIdle;
}

//*****************************************************************************
// Запрос результата операции передачи.
uint16_t Ecan_getError( const void *self )
{
    return DRV_DATA( self )->error;
}

//*****************************************************************************
// Реализация локальных функций
//*****************************************************************************

//*****************************************************************************
// Перевод модуля CAN в заданный режим
void Ecan_setMode( const void *self, uint16_t mode )
{
     DRV_REGS(self)->CTRL1bits.REQOP = mode;
     while ( DRV_REGS(self)->CTRL1bits.OPMODE != mode ) {
        ;
    }
}

//*****************************************************************************
/**
* История изменений:
* 
* Версия 1.0.1
* Дата   06-10-2017
* Автор  Третьяков В.Ж.
* 
* Изменения:
*    Базовая версия.
*
* Версия 1.0.2
* Дата   25-06-2019
* Автор  Третьяков В.Ж.
* 
* Изменения:
*    Изменена процедура работы с eds памятью, была в словах, стала в байтах.
*    Позволяет работать с массивами с нечетным числом байт.
*    Добавлена реализация стандартного интерфейса драйвера ArrayIoDriver.
*    Удалены из структуры счетчик и указатель, реализованы как локальные переменные.     
*
* Версия 1.0.3
* Дата   30-10-2019
* Автор  Третьяков В.Ж.
* 
* Изменения:
*    Добавлена обработка ошибки переполнения буфера.
*/
