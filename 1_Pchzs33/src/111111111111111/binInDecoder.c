/**
* \file    binInDecoder.c
* \brief   \copybrief binInDecoder.h
*
* \version 1.0.1
* \date    10-06-2019
* \author  Третьяков В.Ж.
*/

//*****************************************************************************
// Подключаемые файлы
//*****************************************************************************
#include <stdbool.h>
#include "asserts_ex.h"
#include "ProtectionState_codes.h"
#include "Utility\Combination4from6.h"
#include "Testing\ControlMC\CheckCallFunctions.h"
#include "BinInDecoder.h"
#include "BinInDecoderDrv.h"

//*****************************************************************************
// Локальные константы, определенные через макросы
//*****************************************************************************

//*****************************************************************************
/// \brief Количество чтений тестовых значений с декодера.
///
#define N_READ_TEST       3 

//*****************************************************************************
/// \brief Количество чтений значений входов с декодера.
///
#define N_READ_DATA       6

//*****************************************************************************
// Объявление типов данных
//*****************************************************************************

//*****************************************************************************
/// \brief Идентификаторы состояния функции #BinInDecoder_run.
///
typedef enum
{
    eBIDR_setReadData = 0,        ///< выдача управления на коммутацию для чтения 0-ого набора входных сигналов
    eBIDR_readData,               ///< чтение 0-го набора входных сигналов
    eBIDR_setReadTest,            ///< выдача управления на коммутацию для чтения тестового набора входных сигналов
    eBIDR_readTest,               ///< чтение тестового набора входных сигналов 
    eBIDR_waite                   ///< задержка на установление входных сигналов 
} eBinInDecoder_run;

//*****************************************************************************
/// \brief Структура объекта дискретных входов, считываемых через дешифратор.
///
typedef struct BinInDecoderTag
{
    eBinInDecoder_run stateFunction;                          ///< Состояние автомата функции #BinInDecoder_run.
    
    /// \brief Переход из состояния #eBIDR_waite автомата функции #BinInDecoder_run.
    ///
    eBinInDecoder_run stateFunctionTemp;

    /// \brief Состояние входов дешифратора до проверки тестовых входов.
    ///
    uint8_t stateReadTemp;
    
    /// \brief Состояние входов дешифратора для антидребезговой обработки.
    ///
    uint8_t aStateRead[N_READ_DATA];
    
    uint8_t state;                                            ///< Состояние входов дешифратора.
                                                               
    /// \brief Тестовое состояние входов дешифратора для противопомеховой обработки.
    ///
    uint8_t aStateTest[N_READ_TEST];                           
    uint8_t stateTest;                                         ///< Тестовое состояние входов дешифратора.
    uint8_t ndxTest;                                           ///< Индекс массива при чтении тестовых значений.
    uint8_t ndxData;                                           ///< Индекс массива при чтении входов дешифратора.
    
    /// \brief Индекс массива при чтении входов дешифратора реле и кнопок.
    ///
    uint8_t ndx4;
    
    uint16_t cWaitePS;                                         ///< Счетчик задержки на переход в состояние ЗОт.
    void (*pfSet)( void );                                     ///< Указатель функции.
} BinInDecoder;

//*****************************************************************************
// Объявление локальных типизированных констант
//*****************************************************************************

//*****************************************************************************
/// \brief Эталонное значение входов дешифратора в тестовом режиме.
///
static const uint8_t  MODEL_TEST_DATA = 0x7F;

//*****************************************************************************
/// \brief Время задержки на переход в ЗО по тесту, мс.
///
static const uint16_t TIME_WAITE_PS_TEST = ( 200U * CASSERT_INC );

//*****************************************************************************
// Объявление локальных переменных
//*****************************************************************************

//*****************************************************************************
static BinInDecoder binInDecoder;        ///< Объект дискретных входов, заведенных на дешифратор.

//*****************************************************************************
// Прототипы локальных функций
//*****************************************************************************

//*****************************************************************************
/// \brief Определение состояния тестовых входов (противопомеховая обработка).
/// \note Критерий определения: если 2 измерения из 3 имеют высокий уровень,
/// то состояние входа 1, иначе - 0.
/// \note Работа с данными локального объекта binInDecoder.c.
/// \note Входные данные:
///     - \a binInDecoder.aStateTest
/// \note Результат работы:
///     - \a binInDecoder.stateTest - состояние тестовых входов.
///
void BinInDecoder_definitionInTest( void );

//*****************************************************************************
// Реализация интерфейсных функций
//*****************************************************************************

//*****************************************************************************
// Инициализация переменных модуля
void BinInDecoder_ctor( void )
{
    uint8_t i;

    binInDecoder.stateFunction = eBIDR_setReadData;

    binInDecoder.ndxData = 0U;
    binInDecoder.ndx4    = 0U;

    binInDecoder.stateReadTemp = 0U;
    binInDecoder.state         = 0U;
    for( i = 0; i < N_READ_DATA; i++ )
    {
        binInDecoder.aStateRead[i] = 0U;
    }
    BinInDecoderDrv_ctor();
}

//*****************************************************************************
// Формирование состояния дискретных входов, заведенных через дешифратор
void BinInDecoder_run( void )
{
    switch( binInDecoder.stateFunction )
    {
        case eBIDR_setReadData:
            binInDecoder.pfSet = &BinInDecoderDrv_setReadData;
            binInDecoder.stateFunctionTemp = eBIDR_readData;
            binInDecoder.stateFunction = eBIDR_waite;
            break;

        case eBIDR_readData:
            binInDecoder.stateReadTemp = BinInDecoderDrv_readData();
            binInDecoder.pfSet = &BinInDecoderDrv_setReadDataTest; 
            binInDecoder.stateFunctionTemp = eBIDR_readTest;
            binInDecoder.ndxTest = 0;
            binInDecoder.stateFunction = eBIDR_waite;
            break;
        case eBIDR_readTest:
            binInDecoder.aStateTest[binInDecoder.ndxTest] = BinInDecoderDrv_readData();
            binInDecoder.ndxTest++;
            if( binInDecoder.ndxTest == N_READ_TEST )
            {
                binInDecoder.stateFunction = eBIDR_setReadData;
                BinInDecoder_definitionInTest();
                CASSERT_EX2_ID( eGrPS_BinIn, ePS_BinInErrorTestDecoder,
                                binInDecoder.cWaitePS, TIME_WAITE_PS_TEST,
                                binInDecoder.stateTest == MODEL_TEST_DATA,
                                binInDecoder.stateTest, MODEL_TEST_DATA );
                if( binInDecoder.stateTest == MODEL_TEST_DATA )
                {
//===============================================================================================
                    binInDecoder.aStateRead[binInDecoder.ndxData] = binInDecoder.stateReadTemp;
                    binInDecoder.state = Combination4from6_uint8_t( &binInDecoder.aStateRead[0], binInDecoder.state );
                    if( ++binInDecoder.ndxData == N_READ_DATA )
                    {
                        binInDecoder.ndxData = 0;
                    }
//=================================================================================================
                }
            }
            break;
        case eBIDR_waite:
            binInDecoder.pfSet();
            binInDecoder.stateFunction = binInDecoder.stateFunctionTemp; 
            break;        
        default :
            binInDecoder.stateFunction = eBIDR_setReadData;
            break;
    }

    MARKED_CALL_FUNCTION;
}

//*****************************************************************************
// Получить состояние дискретных входов, считываемых через дешифратор
uint8_t BinInDecoder_getState( void )
{
    return ~binInDecoder.state;
}

//*****************************************************************************
// Реализация локальных функций
//*****************************************************************************

//*****************************************************************************
// Определение состояния тестовых входов (противопомеховая обработка)
void BinInDecoder_definitionInTest( void )
{
    binInDecoder.stateTest = ( binInDecoder.aStateTest[0] & binInDecoder.aStateTest[1] ) |
            ( binInDecoder.aStateTest[0] & binInDecoder.aStateTest[2] ) |
            ( binInDecoder.aStateTest[1] & binInDecoder.aStateTest[2] );
}

//*****************************************************************************
/**
* История изменений: 
* 
* Версия 1.0.1
* Дата   29-06-2018
* Автор  Третьяков В.Ж.
* 
* Изменения:
*    Базовая версия.
*/
