//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <xc.h>
#include <stdint.h>
#include <stdbool.h>
#include "MainRegisters.h"  
#include "IOports.h"
#include "EcanDriver.h"
#include "MainInitial.h"
#include "UartDrv.h"
#include "ConfigurationMC.h" // ��������� ����� ������������ ��

const void *drvUart1;
const void *drvCAN1;
const void *drvCAN2;

// ������������� �� ������ ��.
void MainInitial( void )
{
    SET_OSCILLATOR;
    
    //��������� ������
            //��� ������ �� ��������� ��������� ��� ���������� ����� �����/������,
            //����� ����������� �� ��������
        ANSELA = ANSELB = ANSELC = ANSELD = ANSELE = ANSELG = 0x0000;
    
    INIC_PORT_OUT(B, 4, OPEN_DRAIN_OFF);  //"POWER" - �������,  OUT (pin 50)
    INIC_PORT_OUT(A, 8, OPEN_DRAIN_OFF);  //"POWER" - �������,  OUT (pin 49)
    INIC_PORT_OUT(E, 9, OPEN_DRAIN_OFF);  //5V/3.3V,            OUT (pin 19)
    INIC_PORT_OUT(A, 12, OPEN_DRAIN_OFF); //5V_ON,              OUT (pin 20)
    INIC_PORT_OUT(E, 8, OPEN_DRAIN_OFF);  //3.3V_ON,            OUT (pin 18)
    INIC_PORT_IN(B, 3, 0);                //SIGN_POWER_ON,      IN  (pin 27)
    INIC_PORT_OUT(B, 2, OPEN_DRAIN_OFF);  //POW_ON,             OUT (pin 26)
    INIC_PORT_OUT(E, 14, OPEN_DRAIN_OFF); //RESET_M,            OUT (pin 43)
    INIC_PORT_OUT(E, 0, OPEN_DRAIN_OFF);  //RESET_S,            OUT (pin 52)
    INIC_PORT_OUT(A, 9, OPEN_DRAIN_OFF);  //RES_ZS1,            OUT (pin 54)
    INIC_PORT_OUT(A, 4, OPEN_DRAIN_OFF);  //RES_ZS2,            OUT (pin 51)
    INIC_PORT_IN(C, 4, 0);                //C_GP3S_+5V,         IN  (pin 56)
    INIC_PORT_OUT(C, 0, OPEN_DRAIN_OFF);  //BT_MODE,            OUT (pin 32)
    INIC_PORT_IN(F, 10, 0);               //BT_STATE,           IN  (pin 29)
    INIC_PORT_OUT(F, 9, OPEN_DRAIN_OFF);  //BT_RESET,           OUT (pin 28)

    
    //��������� UART1 (����� � Bluetooth �������))
    drvUart1 = UartDrv_ctor(
                    eUART_module1,
                    eUART_8bit_parity_not,
                    eUART_bitStop_1,
                    eUART_speed_low,
                    BRG_UART_LOW_SPEED(115200)
                );
    
    

    drvCAN1 = Ecan_ctor(eEcan1, ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GARS_S,
                                    ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GARS_S,
                                    eEcanModeNormal,
                                    8, 
                                    GARS_S);
    
    drvCAN2 = Ecan_ctor( eEcan2, ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GKLS_E,
                                    ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GKLS_E,
                                    eEcanModeNormal,
                                    8, 
                                    GKLS_E );
    
        //***********��������� ���***********
        ANSELGbits.ANSG6 = 1;       //��� ����� ���������� (�.10, RG6, AN19, �_+3.3V_OUT)
        ANSELGbits.ANSG7 = 1;       //��� ����� ���������� (�.11, RG7, AN18, �_+5V_OUT)
        ANSELEbits.ANSE15 = 1;      //��� ����� ���������� (�.44, E15, AN15, �_RES_M)
        ANSELEbits.ANSE13 = 1;      //��� ����� ���������� (�.42, E13, AN13, �_RES_S)

        AD1CON2bits.ALTS = 0;       //������ ���������� ����� ����� �� �������������� A
        AD1CON2bits.CHPS = 0;       //����� CH0
        AD1CHS0bits.CH0NA = 1;      //���� ������ CH0- ���������� � Avcc (�.�. GND)
        AD1CON2bits.CSCNA = 1;      //���� ������ CHO+ ������ ����������� �����
        AD1CSSHbits.CSS19 = 1;      //����������� ���� AN19 (�.10, RG6, AN19, �_+3.3V_OUT), ��������� ����� � �������� ADC1BUF3 (ADC1BUFB)
        AD1CSSHbits.CSS18 = 1;      //����������� ���� AN18 (�.11, RG7, AN18, �_+5V_OUT),   ��������� ����� � �������� ADC1BUF2 (ADC1BUFA)
        AD1CSSLbits.CSS15 = 1;      //����������� ���� AN15 (�.44, E15, AN15, �_RES_M),     ��������� ����� � �������� ADC1BUF1 (ADC1BUF9)
        AD1CSSLbits.CSS13 = 1;      //����������� ���� AN13 (�.42, E13, AN13, �_RES_S),     ��������� ����� � �������� ADC1BUF0 (ADC1BUF8)
        AD1CON2bits.SMPI = 3;       //��� DMA ��� ���������� ������������ ������ �� ���� ������������ 1+3=4 (�� ��������� 1)

        AD1CON4bits.ADDMAEN = 0;    //��������� DMA
        AD1CON2bits.BUFM = 1;       //���������� ��������� ��������� ������� � 1-� �������� ������� �������, ����� �� ������
                                    //������ ������ ���������:
                                    //���� BUFS = 0 ADC ��������� �������� ADC1BUF0-ADC1BUF7,������ �� ��������� ADC1BUF8-ADC1BUFF
                                    //���� BUFS = 1 ADC ��������� �������� ADC1BUF8-ADC1BUFF,������ �� ��������� ADC1BUF0-ADC1BUF7

        AD1CON1bits.AD12B = 1;      //12-�� ������ �����
        AD1CON2bits.VCFG = 0;       //������� ���������� �� AVcc � AVss
        //AD1CON2bits.VCFG = 1;       //������� ���������� �� Vref � AVss
        AD1CON3bits.ADRC = 0;       //������������ ADC �� ��������� ������� ����� ��������
        AD1CON3bits.ADCS = 255;     //�������� ��������� ������� ��� ADC
                                    //ADC Clock Period (TAD) = TCY � (ADCS + 1)
                                    //F_CY = 58982400   TAD = 1/58982400 * (255+1) = 4,34 ���
        AD1CON1bits.FORM = 0;       //������ ������ �� 0 �� 4096

        AD1CON1bits.ASAM = 1;       //�������������� �������������
        AD1CON1bits.SSRCG = 0;      //� ��������������
        AD1CON1bits.SSRC = 7;       //����������

        AD1CON3bits.SAMC = 23;      //����� ������������� TSMP = SAMC<4:0> � TAD
                                    //TSMP = 23 * 4,34 ��� = 100 ���
                                    //����� ����������� (��� 12 ��� ������) TCONV = 14 � TAD = 61 ���
                                    //����� ����� �������������� TSMP + TCONV = 161 ���

        AD1CON1bits.ADON = 1;       //��������� ������ ADC

    //***********************************

    
    //ENABLE_WATCHDOG;   
    
    MAIN_TIMER_INIT;                     //��������� ������� 5 �� 1 ��
    MAIN_TIMER_START;

    return;    
}

//*****************************************************************************
