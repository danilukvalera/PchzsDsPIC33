//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef MAIN_H
#define MAIN_H

#include "wait.h"

#define TURN_ON  1
#define TURN_OFF 0

#define STATE_ON  1
#define STATE_OFF 0


#define MAX_TIME_RECEIVE	100					//��, max ����� �� �������� ������ ������� �� �������� ����� �������
#define ADC_LSB             806                 //���� ������� ���, ���.  
#define ADC_REF             3300000UL           //������� ���������� ���, ���.  
#define U5V_MIN             3000000UL           //�����������   ���������� �� ���� 5� �� ������� ��, ��� (���� ������������ ��� ���� 5� � 3,3� �����)
#define U3V3_MIN            3000000UL           //�����������   ���������� �� ���� 3.3� �� ������� ��, ���
#define URES_MAX            1000000UL           //������������   ���������� �� ���� RES_M (RES_S)) � ��������� ������, ���

//*****************************************************************************
/// \brief ��������� ���������� ��������� ��������� ������
///
typedef  struct
    {
        uint8_t previousStateButtonPower        :1;        ///< ���������� ��������� ������ ��������� �������
        uint8_t res1                            :1;        ///< ������
        uint8_t res2                            :1;        ///< ������
        uint8_t res3                            :1;        ///< ������
        uint8_t res4                            :1;        ///< ������
        uint8_t res5                            :1;        ///< ������
        uint8_t res6                            :1;        ///< ������
        uint8_t res7                            :1;        ///< ������
} Previous;

//*****************************************************************************
/// \brief ��������� ���������� ������
///
typedef union InputsDiscreteUnion
{
    uint8_t data;             ///< ������������� ������ � ���� \a uint8_t.
    
    /// \brief ������������� ������ � ���� ���������.
    /// 
    struct
    {
        uint8_t stateButtonPower        :1;        ///< ������ ��������� �������
        uint8_t stateLinebtState        :1;        ///< bluetooth STATE 1-���� ����������, 0- ��� ����������
        uint8_t res1                    :1;        ///< ������
        uint8_t stateLineConnectGp3s    :1;        ///< ������ ����������� ��3�
        uint8_t stateLineResM           :1;        ///< ��������� ����� RESET_M
        uint8_t stateLineResS           :1;        ///< ��������� ����� RESET_S
        uint8_t stateLine5V             :1;        ///< ��������� ����� ������� 5V
        uint8_t stateLine3V3            :1;        ///< ��������� ����� ������� 3V3
    } str;
} InputsDiscrete;

//*****************************************************************************
/// \brief ��������� �����, ������������� ��������� ������ ��������� �������.
#define BUTTON_POWER                ! PORTBbits.RB3
/// \brief ��������� �����, ������������� ����� Bluetooth STATE
#define BT_STATE                    PORTFbits.RF10
/// \brief ��������� �����, ������������� ����� Bluetooth MODE
#define BT_MODE                     PORTCbits.RC0
/// \brief ��������� �����, ������������� ������ ����������� ��3�
#define CONTLOL_CONNECT_GP3S        PORTCbits.RC4
//*****************************************************************************



//����� ����������� ���� RES_MASTER ���������� � "0"
#define RES_M_0 do             \
{                              \
    LATEbits.LATE14 = 0;       \
} while( 0 ) 

//����� ����������� ���� RES_MASTER ���������� � "1"
#define RES_M_1 do             \
{                              \
    LATEbits.LATE14 = 1;       \
} while( 0 ) 

//����� ����������� ���� RES_SLAVE ���������� � "0"
#define RES_S_0 do             \
{                              \
    LATEbits.LATE0 = 0;        \
} while( 0 ) 

//����� ����������� ���� RES_SLAVE ���������� � "1"
#define RES_S_1 do             \
{                              \
    LATEbits.LATE0 = 1;        \
} while( 0 ) 

//����� RESET ��� Master � Slave ���������� � "1"
#define RES_M_AND_S_1 do       \
{                              \
    RES_M_0;                   \
    RES_S_0;                   \
} while( 0 ) 

//����� RESET ��� Master � Slave ���������� � "0"
#define RES_M_AND_S_0 do       \
{                              \
    RES_M_1;                   \
    RES_S_1;                   \
} while( 0 ) 

//������ RESET ���  Master
#define RESET_M do             \
{                              \
    RES_M_1;                   \
    wait1ms();                 \
    RES_M_0;                   \
} while( 0 ) 

//������ RESET ��� Slave
#define RESET_S do             \
{                              \
    RES_S_1;                   \
    wait1ms();                 \
    RES_S_0;                   \
} while( 0 ) 
        
//������ RESET ��� Master � Slave
#define RESET_GLOBAL do        \
{                              \
    RES_M_1;                   \
    RES_S_1;                   \
    wait1ms();                 \
    RES_M_0;                   \
    RES_S_0;                   \
} while( 0 ) 

//���������� ������� �����������
//�������� ������� ���������
#define IND_RED_ON do          \
{                              \
    LATBbits.LATB4 = 1;        \
} while( 0 ) 
//��������� ������� ���������
#define IND_RED_OFF do         \
{                              \
    LATBbits.LATB4 = 0;        \
} while( 0 ) 
//�������� �������� ����������
#define IND_RED_INVERS do                               \
{                                                       \
    if ( LATBbits.LATB4 == 0 )  LATBbits.LATB4 = 1;     \
    else                        LATBbits.LATB4 = 0;     \
} while( 0 ) 

//���������� ������� �����������
//�������� ������� ���������
#define IND_GREEN_ON do        \
{                              \
    LATAbits.LATA8 = 1;        \
} while( 0 ) 
//��������� ������� ���������
#define IND_GREEN_OFF do       \
{                              \
    LATAbits.LATA8 = 0;        \
} while( 0 ) 
//�������� �������� ����������
#define IND_GREEN_INVERS do                             \
{                                                       \
    if ( LATAbits.LATA8 == 0 )  LATAbits.LATA8 = 1;     \
    else                        LATAbits.LATA8 = 0;     \
} while( 0 ) 

//��������� ������ ��������� ��������� (���������� ����� ���)
//���������� ��������� ������ ��������� ���������
#define JUMPER_PROTECT_STATE_ON do                      \
{                                                       \
    LATAbits.LATA9 = 1;                                 \
    LATAbits.LATA4 = 1;                                 \
} while( 0 ) 
//����� ��������� ������ ��������� ���������
#define JUMPER_PROTECT_STATE_OFF do                     \
{                                                       \
    LATAbits.LATA9 = 0;                                 \
    LATAbits.LATA4 = 0;                                 \
} while( 0 ) 

//����� ����������� BT_RESET ���������� � "0"
#define BT_RESET_0 do                               \
{                                                       \
    LATFbits.LATF9 = 0;                                 \
} while( 0 ) 

//����� ����������� BT_RESET ���������� � "1"
#define BT_RESET_1 do                               \
{                                                       \
    LATFbits.LATF9 = 1;                                 \
} while( 0 ) 

//���������� ����� Bluetooth
#define RESET_BLU_HARD do                               \
{                                                       \
    BT_RESET_0;                                 \
    wait10ms();                                         \
    BT_RESET_1;                                 \
} while( 0 ) 

//�������� �������
#define POWER_ON do                                     \
{                                                       \
    LATBbits.LATB2 = 1;                                 \
} while( 0 ) 

//��������� �������
#define POWER_OFF do                                    \
{                                                       \
    LATBbits.LATB2 = 0;                                 \
} while( 0 ) 

//�������� ����������� �������
#define POWER_INVERS do                             \
{                                                       \
    if ( LATBbits.LATB2 == 0 )  LATBbits.LATB2 = 1;     \
    else                        LATBbits.LATB2 = 0;     \
} while( 0 ) 

//�������� 3,3V
#define VOLTAGE_3V3_ON do                               \
{                                                       \
    LATEbits.LATE8 = 1;                                 \
} while( 0 ) 

//��������� 3,3V
#define VOLTAGE_3V3_OFF do                              \
{                                                       \
    LATEbits.LATE8 = 0;                                 \
} while( 0 ) 

//�������� 5V
#define VOLTAGE_3V3_OR_5V_ON do                                \
{                                                       \
    LATAbits.LATA12 = 1;                                \
} while( 0 ) 

//��������� 5V
#define VOLTAGE_3V3_OR_5V_OFF do                               \
{                                                       \
    LATAbits.LATA12 = 0;                                \
} while( 0 ) 

//����������� 3,3�/5� ���������� 3,3�
#define SWITCH_VOLTAGE_TO_3V3 do                        \
{                                                       \
    LATEbits.LATE9 = 0;                                 \
} while( 0 ) 

//����������� 3,3�/5� ���������� 5�
#define SWITCH_VOLTAGE_TO_5V do                         \
{                                                       \
    LATEbits.LATE9 = 1;                                 \
} while( 0 ) 

//Bluetooth ����� ��������
#define BT_MODE_TRANSMIT do                         \
{                                                       \
    LATCbits.LATC0 = 0;                                 \
} while( 0 ) 

//Bluetooth ����� AT-������
#define BT_MODE_AT_COMMAND do                         \
{                                                       \
    LATCbits.LATC0 = 1;                                 \
} while( 0 ) 

#endif

//*****************************************************************************
