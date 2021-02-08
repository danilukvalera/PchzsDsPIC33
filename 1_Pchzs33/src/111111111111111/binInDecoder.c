/**
* \file    binInDecoder.c
* \brief   \copybrief binInDecoder.h
*
* \version 1.0.1
* \date    10-06-2019
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <stdbool.h>
#include "asserts_ex.h"
#include "ProtectionState_codes.h"
#include "Utility\Combination4from6.h"
#include "Testing\ControlMC\CheckCallFunctions.h"
#include "BinInDecoder.h"
#include "BinInDecoderDrv.h"

//*****************************************************************************
// ��������� ���������, ������������ ����� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ���������� ������ �������� �������� � ��������.
///
#define N_READ_TEST       3 

//*****************************************************************************
/// \brief ���������� ������ �������� ������ � ��������.
///
#define N_READ_DATA       6

//*****************************************************************************
// ���������� ����� ������
//*****************************************************************************

//*****************************************************************************
/// \brief �������������� ��������� ������� #BinInDecoder_run.
///
typedef enum
{
    eBIDR_setReadData = 0,        ///< ������ ���������� �� ���������� ��� ������ 0-��� ������ ������� ��������
    eBIDR_readData,               ///< ������ 0-�� ������ ������� ��������
    eBIDR_setReadTest,            ///< ������ ���������� �� ���������� ��� ������ ��������� ������ ������� ��������
    eBIDR_readTest,               ///< ������ ��������� ������ ������� �������� 
    eBIDR_waite                   ///< �������� �� ������������ ������� �������� 
} eBinInDecoder_run;

//*****************************************************************************
/// \brief ��������� ������� ���������� ������, ����������� ����� ����������.
///
typedef struct BinInDecoderTag
{
    eBinInDecoder_run stateFunction;                          ///< ��������� �������� ������� #BinInDecoder_run.
    
    /// \brief ������� �� ��������� #eBIDR_waite �������� ������� #BinInDecoder_run.
    ///
    eBinInDecoder_run stateFunctionTemp;

    /// \brief ��������� ������ ����������� �� �������� �������� ������.
    ///
    uint8_t stateReadTemp;
    
    /// \brief ��������� ������ ����������� ��� ��������������� ���������.
    ///
    uint8_t aStateRead[N_READ_DATA];
    
    uint8_t state;                                            ///< ��������� ������ �����������.
                                                               
    /// \brief �������� ��������� ������ ����������� ��� ���������������� ���������.
    ///
    uint8_t aStateTest[N_READ_TEST];                           
    uint8_t stateTest;                                         ///< �������� ��������� ������ �����������.
    uint8_t ndxTest;                                           ///< ������ ������� ��� ������ �������� ��������.
    uint8_t ndxData;                                           ///< ������ ������� ��� ������ ������ �����������.
    
    /// \brief ������ ������� ��� ������ ������ ����������� ���� � ������.
    ///
    uint8_t ndx4;
    
    uint16_t cWaitePS;                                         ///< ������� �������� �� ������� � ��������� ���.
    void (*pfSet)( void );                                     ///< ��������� �������.
} BinInDecoder;

//*****************************************************************************
// ���������� ��������� �������������� ��������
//*****************************************************************************

//*****************************************************************************
/// \brief ��������� �������� ������ ����������� � �������� ������.
///
static const uint8_t  MODEL_TEST_DATA = 0x7F;

//*****************************************************************************
/// \brief ����� �������� �� ������� � �� �� �����, ��.
///
static const uint16_t TIME_WAITE_PS_TEST = ( 200U * CASSERT_INC );

//*****************************************************************************
// ���������� ��������� ����������
//*****************************************************************************

//*****************************************************************************
static BinInDecoder binInDecoder;        ///< ������ ���������� ������, ���������� �� ����������.

//*****************************************************************************
// ��������� ��������� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ����������� ��������� �������� ������ (���������������� ���������).
/// \note �������� �����������: ���� 2 ��������� �� 3 ����� ������� �������,
/// �� ��������� ����� 1, ����� - 0.
/// \note ������ � ������� ���������� ������� binInDecoder.c.
/// \note ������� ������:
///     - \a binInDecoder.aStateTest
/// \note ��������� ������:
///     - \a binInDecoder.stateTest - ��������� �������� ������.
///
void BinInDecoder_definitionInTest( void );

//*****************************************************************************
// ���������� ������������ �������
//*****************************************************************************

//*****************************************************************************
// ������������� ���������� ������
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
// ������������ ��������� ���������� ������, ���������� ����� ����������
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
// �������� ��������� ���������� ������, ����������� ����� ����������
uint8_t BinInDecoder_getState( void )
{
    return ~binInDecoder.state;
}

//*****************************************************************************
// ���������� ��������� �������
//*****************************************************************************

//*****************************************************************************
// ����������� ��������� �������� ������ (���������������� ���������)
void BinInDecoder_definitionInTest( void )
{
    binInDecoder.stateTest = ( binInDecoder.aStateTest[0] & binInDecoder.aStateTest[1] ) |
            ( binInDecoder.aStateTest[0] & binInDecoder.aStateTest[2] ) |
            ( binInDecoder.aStateTest[1] & binInDecoder.aStateTest[2] );
}

//*****************************************************************************
/**
* ������� ���������: 
* 
* ������ 1.0.1
* ����   29-06-2018
* �����  ��������� �.�.
* 
* ���������:
*    ������� ������.
*/
