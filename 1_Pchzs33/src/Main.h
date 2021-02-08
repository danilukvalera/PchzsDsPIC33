//*****************************************************************************
// Команды препроцессора для предотвращения повторного включения содержимого файла
//*****************************************************************************
#ifndef MAIN_H
#define MAIN_H

#include "wait.h"

#define TURN_ON  1
#define TURN_OFF 0

#define STATE_ON  1
#define STATE_OFF 0


#define MAX_TIME_RECEIVE	100					//мс, max время от признака начала команды до признака конца команды
#define ADC_LSB             806                 //одно деление АЦП, мкВ.  
#define ADC_REF             3300000UL           //опорное напряжение АЦП, мкВ.  
#define U5V_MIN             3000000UL           //минимальное   напряжение на шине 5В со стороны ОК, мкВ (пока предполагаем что шина 5В и 3,3В общая)
#define U3V3_MIN            3000000UL           //минимальное   напряжение на шине 3.3В со стороны ОК, мкВ
#define URES_MAX            1000000UL           //максимальное   напряжение на шине RES_M (RES_S)) в состоянии сброса, мкВ

//*****************************************************************************
/// \brief Структура Предыдущее состояние различных данных
///
typedef  struct
    {
        uint8_t previousStateButtonPower        :1;        ///< предыдущее состояние кнопки включения питания
        uint8_t res1                            :1;        ///< резерв
        uint8_t res2                            :1;        ///< резерв
        uint8_t res3                            :1;        ///< резерв
        uint8_t res4                            :1;        ///< резерв
        uint8_t res5                            :1;        ///< резерв
        uint8_t res6                            :1;        ///< резерв
        uint8_t res7                            :1;        ///< резерв
} Previous;

//*****************************************************************************
/// \brief Структура дискретных входов
///
typedef union InputsDiscreteUnion
{
    uint8_t data;             ///< Представление данных в виде \a uint8_t.
    
    /// \brief Представление данных в виде структуры.
    /// 
    struct
    {
        uint8_t stateButtonPower        :1;        ///< кнопка включения питания
        uint8_t stateLinebtState        :1;        ///< bluetooth STATE 1-есть сопряжение, 0- нет сопряжения
        uint8_t res1                    :1;        ///< резерв
        uint8_t stateLineConnectGp3s    :1;        ///< Сигнал подключения ГП3С
        uint8_t stateLineResM           :1;        ///< состояние линии RESET_M
        uint8_t stateLineResS           :1;        ///< состояние линии RESET_S
        uint8_t stateLine5V             :1;        ///< состояние линии питания 5V
        uint8_t stateLine3V3            :1;        ///< состояние линии питания 3V3
    } str;
} InputsDiscrete;

//*****************************************************************************
/// \brief Состояние входа, определяющего состояние кнопки включения питания.
#define BUTTON_POWER                ! PORTBbits.RB3
/// \brief Состояние входа, определяющего вывод Bluetooth STATE
#define BT_STATE                    PORTFbits.RF10
/// \brief Состояние входа, определяющего вывод Bluetooth MODE
#define BT_MODE                     PORTCbits.RC0
/// \brief Состояние входа, определяющего сигнал подключения ГП3С
#define CONTLOL_CONNECT_GP3S        PORTCbits.RC4
//*****************************************************************************



//вывод контроллера ПЧЗС RES_MASTER установить в "0"
#define RES_M_0 do             \
{                              \
    LATEbits.LATE14 = 0;       \
} while( 0 ) 

//вывод контроллера ПЧЗС RES_MASTER установить в "1"
#define RES_M_1 do             \
{                              \
    LATEbits.LATE14 = 1;       \
} while( 0 ) 

//вывод контроллера ПЧЗС RES_SLAVE установить в "0"
#define RES_S_0 do             \
{                              \
    LATEbits.LATE0 = 0;        \
} while( 0 ) 

//вывод контроллера ПЧЗС RES_SLAVE установить в "1"
#define RES_S_1 do             \
{                              \
    LATEbits.LATE0 = 1;        \
} while( 0 ) 

//линии RESET для Master и Slave установить в "1"
#define RES_M_AND_S_1 do       \
{                              \
    RES_M_0;                   \
    RES_S_0;                   \
} while( 0 ) 

//линии RESET для Master и Slave установить в "0"
#define RES_M_AND_S_0 do       \
{                              \
    RES_M_1;                   \
    RES_S_1;                   \
} while( 0 ) 

//Сигнал RESET для  Master
#define RESET_M do             \
{                              \
    RES_M_1;                   \
    wait1ms();                 \
    RES_M_0;                   \
} while( 0 ) 

//Сигнал RESET для Slave
#define RESET_S do             \
{                              \
    RES_S_1;                   \
    wait1ms();                 \
    RES_S_0;                   \
} while( 0 ) 
        
//Сигнал RESET для Master и Slave
#define RESET_GLOBAL do        \
{                              \
    RES_M_1;                   \
    RES_S_1;                   \
    wait1ms();                 \
    RES_M_0;                   \
    RES_S_0;                   \
} while( 0 ) 

//управление красным светодиодом
//включить красный светодиод
#define IND_RED_ON do          \
{                              \
    LATBbits.LATB4 = 1;        \
} while( 0 ) 
//выключить красный светодиод
#define IND_RED_OFF do         \
{                              \
    LATBbits.LATB4 = 0;        \
} while( 0 ) 
//Инверсия красного светодиода
#define IND_RED_INVERS do                               \
{                                                       \
    if ( LATBbits.LATB4 == 0 )  LATBbits.LATB4 = 1;     \
    else                        LATBbits.LATB4 = 0;     \
} while( 0 ) 

//управление зеленым светодиодом
//включить зеленый светодиод
#define IND_GREEN_ON do        \
{                              \
    LATAbits.LATA8 = 1;        \
} while( 0 ) 
//выключить зеленый светодиод
#define IND_GREEN_OFF do       \
{                              \
    LATAbits.LATA8 = 0;        \
} while( 0 ) 
//Инверсия зеленого светодиода
#define IND_GREEN_INVERS do                             \
{                                                       \
    if ( LATAbits.LATA8 == 0 )  LATAbits.LATA8 = 1;     \
    else                        LATAbits.LATA8 = 0;     \
} while( 0 ) 

//Перемычка снятия защитного состояния (включаются сразу обе)
//Установить перемычку снятия защитного состояния
#define JUMPER_PROTECT_STATE_ON do                      \
{                                                       \
    LATAbits.LATA9 = 1;                                 \
    LATAbits.LATA4 = 1;                                 \
} while( 0 ) 
//Снять перемычку снятия защитного состояния
#define JUMPER_PROTECT_STATE_OFF do                     \
{                                                       \
    LATAbits.LATA9 = 0;                                 \
    LATAbits.LATA4 = 0;                                 \
} while( 0 ) 

//вывод контроллера BT_RESET установить в "0"
#define BT_RESET_0 do                               \
{                                                       \
    LATFbits.LATF9 = 0;                                 \
} while( 0 ) 

//вывод контроллера BT_RESET установить в "1"
#define BT_RESET_1 do                               \
{                                                       \
    LATFbits.LATF9 = 1;                                 \
} while( 0 ) 

//аппаратный сброс Bluetooth
#define RESET_BLU_HARD do                               \
{                                                       \
    BT_RESET_0;                                 \
    wait10ms();                                         \
    BT_RESET_1;                                 \
} while( 0 ) 

//включить питание
#define POWER_ON do                                     \
{                                                       \
    LATBbits.LATB2 = 1;                                 \
} while( 0 ) 

//выключить питание
#define POWER_OFF do                                    \
{                                                       \
    LATBbits.LATB2 = 0;                                 \
} while( 0 ) 

//Инверсия выключателя питания
#define POWER_INVERS do                             \
{                                                       \
    if ( LATBbits.LATB2 == 0 )  LATBbits.LATB2 = 1;     \
    else                        LATBbits.LATB2 = 0;     \
} while( 0 ) 

//включить 3,3V
#define VOLTAGE_3V3_ON do                               \
{                                                       \
    LATEbits.LATE8 = 1;                                 \
} while( 0 ) 

//выключить 3,3V
#define VOLTAGE_3V3_OFF do                              \
{                                                       \
    LATEbits.LATE8 = 0;                                 \
} while( 0 ) 

//включить 5V
#define VOLTAGE_3V3_OR_5V_ON do                                \
{                                                       \
    LATAbits.LATA12 = 1;                                \
} while( 0 ) 

//выключить 5V
#define VOLTAGE_3V3_OR_5V_OFF do                               \
{                                                       \
    LATAbits.LATA12 = 0;                                \
} while( 0 ) 

//переключать 3,3В/5В установить 3,3В
#define SWITCH_VOLTAGE_TO_3V3 do                        \
{                                                       \
    LATEbits.LATE9 = 0;                                 \
} while( 0 ) 

//переключать 3,3В/5В установить 5В
#define SWITCH_VOLTAGE_TO_5V do                         \
{                                                       \
    LATEbits.LATE9 = 1;                                 \
} while( 0 ) 

//Bluetooth режим передачи
#define BT_MODE_TRANSMIT do                         \
{                                                       \
    LATCbits.LATC0 = 0;                                 \
} while( 0 ) 

//Bluetooth режим AT-команд
#define BT_MODE_AT_COMMAND do                         \
{                                                       \
    LATCbits.LATC0 = 1;                                 \
} while( 0 ) 

#endif

//*****************************************************************************
