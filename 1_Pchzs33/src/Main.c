//*****************************************************************************
// Подключаемые файлы
//*****************************************************************************
#include <xc.h>
#include <stdint.h>
#include <stdbool.h>
//#include <stdio.h>
//#include <string.h>
#include "Main.h"
#include "MainInitial.h"
#include "wait.h"
#include "MainRegisters.h"
#include "BluetoothInitial.h"
#include "BluetoothOperation.h"
#include "ReadProtectState.h"
#include "EcanDriver.h"
#include "Combination4from6/src/Combination4from6.h"

//*****************************************************************************
// Локальные переменные
volatile uint16_t timeReceiveComand = 0; //счетчик максимального времени према команды (считает в Т5)
volatile uint16_t caunter_waite = 0; //счетчик максимального времени према данных по CAN (считает в Т5)
volatile uint32_t caunterTimeOff = 0; //счетчик времени отключения питания 

uint16_t regDSWPAG; // Состояние регистра \a DSWPAG.
uint16_t regDSRPAG; // Состояние регистра \a DSRPAG.

const char *READ_PROTECT_STATE = "%READ_PROTECT_STATE%";
const char *REMOV_PROTECT_STATE = "%REMOV_PROTECT_STATE%";
const uint16_t timeOff = 10*60;                 // время отключения (min*60) 

Previous previousData; //регистр значений предыдущего состояния различных данных
InputsDiscrete inputData; //регистр значений дискретных входов (с подавленным дребезгом и шумами)
InputsDiscrete arrInputData[6]; //накопительный массив состояния дискр. входов, для подавления дребезга и помех, изменяется в прерывании

uint16_t indexArrInputData = 0; //индекс накопительного массива, изменяется в прерывании

uint16_t voltageLineResM; //напряжении на линии RES_M
uint16_t voltageLineResS; //напряжении на линии RES_S
uint16_t voltageLine5V; //напряжении на линии 5V
uint16_t voltageLine3V3; //напряжении на линии 3.3V
//*****************************************************************************
//Внешние переменные
extern uint16_t fComand;
extern char btData[];

//extern const void *drvCAN1;
//*****************************************************************************

//*****************************************************************************
//чтение входных данных

InputsDiscrete readDiscreteInputs(void) {
    InputsDiscrete currentInputData;

    if (BUTTON_POWER == 1) currentInputData.str.stateButtonPower = STATE_OFF;
    else currentInputData.str.stateButtonPower = STATE_ON;

    if (BT_STATE == 1) currentInputData.str.stateLinebtState = STATE_ON;
    else currentInputData.str.stateLinebtState = STATE_OFF;

    if (CONTLOL_CONNECT_GP3S == 1) currentInputData.str.stateLineConnectGp3s = STATE_ON;
    else currentInputData.str.stateLineConnectGp3s = STATE_OFF;

    if (voltageLine5V > U5V_MIN / 2 / ADC_LSB) currentInputData.str.stateLine5V = STATE_ON;
    else currentInputData.str.stateLine5V = STATE_OFF;
    
    if (voltageLine3V3 > U3V3_MIN / 2 / ADC_LSB) currentInputData.str.stateLine3V3 = STATE_ON;
    else currentInputData.str.stateLine3V3 = STATE_OFF;

    if (voltageLineResM > URES_MAX / ADC_LSB) currentInputData.str.stateLineResM = STATE_ON;
    else currentInputData.str.stateLineResM = STATE_OFF;

    if (voltageLineResS > URES_MAX / ADC_LSB) currentInputData.str.stateLineResS = STATE_ON;
    else currentInputData.str.stateLineResS = STATE_OFF;

    return currentInputData;
}

//*****************************************************************************
// Функция обработки прерывания главного таймера (период 1ms)

void __attribute__((__interrupt__, auto_psv, save(MODCON, DSRPAG, DSWPAG))) _T5Interrupt(void) {
    TMR5 = 0;

    MODCON = 0;
    DSWPAG = regDSWPAG;
    DSRPAG = regDSRPAG;

    //user code
    //счетчик времени задеожек и т. д.
    caunter_waite++;
    caunterTimeOff++;
    //счетчик макимального времени дешифрации команды 
    if (fComand == 1) {
        timeReceiveComand++;
    }
    if (timeReceiveComand == MAX_TIME_RECEIVE) {
        timeReceiveComand = 0;
        prepareReceiveBuffer(); //подготовкА приемного буфера к приему новой команды
    }
    //записать значение аналоговых входов (измеряется в автоматическом режиме и пишется в регистры ADC1BUFx)
    while (!AD1CON1bits.DONE); // ожидание завершения конвертации
    if (AD1CON2bits.BUFS == 1) {
        voltageLineResS = ADC1BUF0;
        voltageLineResM = ADC1BUF1;
        voltageLine5V = ADC1BUF2;
        voltageLine3V3 = ADC1BUF3;
    } else {
        voltageLineResS = ADC1BUF8;
        voltageLineResM = ADC1BUF9;
        voltageLine5V = ADC1BUFA;
        voltageLine3V3 = ADC1BUFB;
    }
    //считать значение дискретных входов
    arrInputData[indexArrInputData] = readDiscreteInputs();
    //подавление дребезга
    inputData.data = Combination4from6_uint8_t(&arrInputData[0].data, inputData.data);
    if (++indexArrInputData == 6) {
        indexArrInputData = 0;
    }
    //выключатель питания
    if (inputData.str.stateButtonPower == STATE_ON) {
        if (previousData.previousStateButtonPower == STATE_OFF) {
            POWER_INVERS;
        }
        previousData.previousStateButtonPower = TURN_ON;
    } else {
        previousData.previousStateButtonPower = TURN_OFF;
    }

    //user code end

    MAIN_TIMER_INTERRUPT_CLEAR_FLAG; //MAIN_TIMER_INTERRUPT_CLEAR_FLAG;
}
//*****************************************************************************
//*****************************************************************************
// Управление главным потоком.

int main(void) {
    regDSWPAG = DSWPAG;
    regDSRPAG = DSRPAG;

    MainInitial();
    
    //начальная инициализация переменных и выходов контроллера
    previousData.previousStateButtonPower = TURN_OFF; // предыдущее состояние кнопки POWER не нажато!!!
    RES_M_0;
    RES_S_0;
    IND_RED_OFF;
    IND_GREEN_OFF;
    JUMPER_PROTECT_STATE_OFF;
    BT_RESET_1;
    BT_MODE_TRANSMIT;
    POWER_OFF;
    VOLTAGE_3V3_OFF;
    VOLTAGE_3V3_OR_5V_OFF;
    SWITCH_VOLTAGE_TO_3V3;
    
    INTERRUPT_INIT; //разрешение прерываний
    //после разрешения прерываний в таймере работает вкл/выкл питания
    
    //ClrWdt();   //сброс WDT
    //RESET_GLOBAL;
    //Индикатор начального старта
    uint16_t i;
    for (i = 0; i < 5; i++) {
        IND_RED_ON;
        IND_GREEN_OFF;
        wait100ms();
        IND_RED_OFF;
        IND_GREEN_ON;
        wait100ms();
    }

    //Проверка (и при необходимости настройка) рараметров Bluetooth, 
     U1RX_DISABLE_INTERRUPT;
    //индикатор проверки и настройки блютуз
    IND_RED_ON;
    IND_GREEN_ON;
    while (!setParamsBluetooth()) {
        IND_GREEN_OFF;
        IND_RED_ON;
        wait500ms();
        //индикатор проверки и настройки блютуз
        IND_RED_ON;
        IND_GREEN_ON;
    }
    IND_RED_OFF;
    IND_GREEN_OFF;
    U1RX_ENABLE_INTERRUPT;
    
    //IND_GREEN_ON;
    //while(1);

    //Главный цикл    
    while (true) {
        while (fComand != 2) {
            if(((uint16_t)(caunterTimeOff/1000)) >= timeOff){
                POWER_OFF;
            }
            IND_GREEN_INVERS;
            wait100ms();
        }

        caunterTimeOff = 0;                 //сбросисть счетчик времени отключения питания
        if (compare(READ_PROTECT_STATE)) {
            IND_RED_ON;
            IND_GREEN_ON;
            readProtectState();
            IND_RED_OFF;
            IND_GREEN_OFF;
        } else if (compare(REMOV_PROTECT_STATE)) {
            IND_RED_ON;
            IND_GREEN_ON;
            resetProtectState();
            IND_RED_OFF;
            IND_GREEN_OFF;
        } else {
            sendString(btData, false, true);
        }
        prepareReceiveBuffer(); //подготовкА приемного буфера к приему новой команды
    }
    return 0;
}

//*****************************************************************************
