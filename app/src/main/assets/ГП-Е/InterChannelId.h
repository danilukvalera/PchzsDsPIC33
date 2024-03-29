/**
* \file    InterChannelId.h
* \brief   ������ ��������������� ���������� ������������� ������ ������� ����-�
*
* \version 1.0.1
* \date    20-08-2018
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef INTERCHANNELID_H
#define INTERCHANNELID_H

//*****************************************************************************
// ����������� ����� ������
//*****************************************************************************

//*****************************************************************************
/// \brief �������������� ����������, ���������������� ������������ �������.
/// \details ������ �������������� �������� ���������� �����������, ���������� ���
/// ���� �������. � ������ ������������� �������� ������ ���������, ��� 
/// ������������� ���������� �������� � ��� ������������. ��������� ���� 
/// �������� ���������� �� �������� #eInterChannelIdCount. 
/// \details ������������� ��������� �� ��������� ������ ��� �������� 
/// #eInterChannelIdCount ��� #eInterChannelIdBegin ����� ��������� ��������� � �������� � ����� 
/// � ��.
/// \details ��������� ����� ��������� ��� ������������� � ������� �������
/// ������������� ������. ������������ ��������� ����� �������� �� 
/// ��������� #eInterChannelIdCount-1, ����������� - �� 
/// ��������� #eInterChannelIdBegin+1.
///
typedef enum
{
    eInterChannelIdBegin        = 0,              ///< ������������� ������� ���������
                                                  
    eICId_BlackBox              = 1,              ///< ������ �� (EEPROM)
                                                  
    eICId_ProtStateCode         = 2,              ///< ���������� �������� "��� ������"
                                                  
    eICId_MonPar1               = 3,              ///< ���������� �������� 1
    eICId_MonPar2               = 4,              ///< ���������� �������� 2
    eICId_MonPar3               = 5,              ///< ���������� �������� 3
    eICId_MonPar4               = 6,              ///< ���������� �������� 4
                                                  
    eICId_Sheduler              = 7,              ///< ����� ������ �����
    eICId_Eeprom                = 8,              ///< ������ EEPROM
                                                  
    eICId_ControlMC             = 9,              ///< ���������� ���������� �������� ��
    eICId_ControlMC_ROM         = 10,             ///< ������� ����������� ����� ���
    eICId_ControlMC_CF          = 11,             ///< ������� ������� �������� ������ �������
                                                  
    eICId_BinPin2Canal          = 12,             ///< �������� ���������� ������ �� ���� �������
    eICId_BinPin1Canal          = 13,             ///< �������� ���������� ������ ������ ������ (�������� � ������))
    eICId_BinAddress            = 14,             ///< ����� ��� ������ �� RS-422
    eICId_BinCrc                = 21,             ///< �������� ��������� CRC 
                                                  

    eICId_Meas_U_Ref            = 15,             ///< �������� �������� ���������� �� 
    eICId_Meas_U_MC             = 16,             ///< ���������� ������� ����������������
    eICId_Meas_TestBufReg       = 17,             ///< ���������� ������������ ��������� �������� ���
    eICId_Meas_U_POWER          = 18,             ///< ���������� �������� �������
    eICId_Meas_U_CO             = 19,             ///< ���������� ������� �������� 
    eICId_Meas_I_CO             = 20,             ///< ��� ������� �������� 
    
    eICId_EepromLevel           = 22,             ///< �������� ������ �� EEPROM � �������� ��������        
    eICId_BlockExchLm_1         = 23,             ///< ������������� �������� ������ �� �������� �������, �������� �� ����� �������, 1-�� ����� 
    eICId_BlockExchLs_1         = 24,             ///< ������������� �������� ������ �� �������� �������, �������� �� ����� ������,  1-�� �����
    eICId_BlockExchLm_2         = 25,             ///< ������������� �������� ������ �� �������� �������, �������� �� ����� �������, 2-�� ����� 
    eICId_BlockExchLs_2         = 26,             ///< ������������� �������� ������ �� �������� �������, �������� �� ����� ������,  2-�� �����

    eICId_Rs422Sync_Rs1         = 27,             ///< ������������� 1-�� ������ RS-422
    eICId_Rs422Sync_Rs2         = 28,             ///< ������������� 2-�� ������ RS-422

    eInterChannelIdCount                          ///< ���������� ���������������� ����������
} InterChannelId;

#endif

//*****************************************************************************
/**
* ������� ���������:
*
* ������ 1.0.1
* ����   20-08-2018
* �����  ��������� �.�.
*
* ���������:
*    ������� ������.
*/
