//*****************************************************************************
// ������������ �����
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
// ��������� ����������
volatile uint16_t timeReceiveComand = 0; //������� ������������� ������� ����� ������� (������� � �5)
volatile uint16_t caunter_waite = 0; //������� ������������� ������� ����� ������ �� CAN (������� � �5)
volatile uint32_t caunterTimeOff = 0; //������� ������� ���������� ������� 

uint16_t regDSWPAG; // ��������� �������� \a DSWPAG.
uint16_t regDSRPAG; // ��������� �������� \a DSRPAG.

const char *READ_PROTECT_STATE = "%READ_PROTECT_STATE%";
const char *REMOV_PROTECT_STATE = "%REMOV_PROTECT_STATE%";
const uint16_t timeOff = 10*60;                 // ����� ���������� (min*60) 

Previous previousData; //������� �������� ����������� ��������� ��������� ������
InputsDiscrete inputData; //������� �������� ���������� ������ (� ����������� ��������� � ������)
InputsDiscrete arrInputData[6]; //������������� ������ ��������� �����. ������, ��� ���������� �������� � �����, ���������� � ����������

uint16_t indexArrInputData = 0; //������ �������������� �������, ���������� � ����������

uint16_t voltageLineResM; //���������� �� ����� RES_M
uint16_t voltageLineResS; //���������� �� ����� RES_S
uint16_t voltageLine5V; //���������� �� ����� 5V
uint16_t voltageLine3V3; //���������� �� ����� 3.3V
//*****************************************************************************
//������� ����������
extern uint16_t fComand;
extern char btData[];

//extern const void *drvCAN1;
//*****************************************************************************

//*****************************************************************************
//������ ������� ������

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
// ������� ��������� ���������� �������� ������� (������ 1ms)

void __attribute__((__interrupt__, auto_psv, save(MODCON, DSRPAG, DSWPAG))) _T5Interrupt(void) {
    TMR5 = 0;

    MODCON = 0;
    DSWPAG = regDSWPAG;
    DSRPAG = regDSRPAG;

    //user code
    //������� ������� �������� � �. �.
    caunter_waite++;
    caunterTimeOff++;
    //������� ������������ ������� ���������� ������� 
    if (fComand == 1) {
        timeReceiveComand++;
    }
    if (timeReceiveComand == MAX_TIME_RECEIVE) {
        timeReceiveComand = 0;
        prepareReceiveBuffer(); //���������� ��������� ������ � ������ ����� �������
    }
    //�������� �������� ���������� ������ (���������� � �������������� ������ � ������� � �������� ADC1BUFx)
    while (!AD1CON1bits.DONE); // �������� ���������� �����������
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
    //������� �������� ���������� ������
    arrInputData[indexArrInputData] = readDiscreteInputs();
    //���������� ��������
    inputData.data = Combination4from6_uint8_t(&arrInputData[0].data, inputData.data);
    if (++indexArrInputData == 6) {
        indexArrInputData = 0;
    }
    //����������� �������
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
// ���������� ������� �������.

int main(void) {
    regDSWPAG = DSWPAG;
    regDSRPAG = DSRPAG;

    MainInitial();
    
    //��������� ������������� ���������� � ������� �����������
    previousData.previousStateButtonPower = TURN_OFF; // ���������� ��������� ������ POWER �� ������!!!
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
    
    INTERRUPT_INIT; //���������� ����������
    //����� ���������� ���������� � ������� �������� ���/���� �������
    
    //ClrWdt();   //����� WDT
    //RESET_GLOBAL;
    //��������� ���������� ������
    uint16_t i;
    for (i = 0; i < 5; i++) {
        IND_RED_ON;
        IND_GREEN_OFF;
        wait100ms();
        IND_RED_OFF;
        IND_GREEN_ON;
        wait100ms();
    }

    //�������� (� ��� ������������� ���������) ���������� Bluetooth, 
     U1RX_DISABLE_INTERRUPT;
    //��������� �������� � ��������� ������
    IND_RED_ON;
    IND_GREEN_ON;
    while (!setParamsBluetooth()) {
        IND_GREEN_OFF;
        IND_RED_ON;
        wait500ms();
        //��������� �������� � ��������� ������
        IND_RED_ON;
        IND_GREEN_ON;
    }
    IND_RED_OFF;
    IND_GREEN_OFF;
    U1RX_ENABLE_INTERRUPT;
    
    //IND_GREEN_ON;
    //while(1);

    //������� ����    
    while (true) {
        while (fComand != 2) {
            if(((uint16_t)(caunterTimeOff/1000)) >= timeOff){
                POWER_OFF;
            }
            IND_GREEN_INVERS;
            wait100ms();
        }

        caunterTimeOff = 0;                 //��������� ������� ������� ���������� �������
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
        prepareReceiveBuffer(); //���������� ��������� ������ � ������ ����� �������
    }
    return 0;
}

//*****************************************************************************
