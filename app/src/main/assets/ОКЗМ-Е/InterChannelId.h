/**
* \file    InterChannelId.h
* \brief   ������ ��������������� ���������� ������������� ������
*
* \version 1.0.3
* \date    05-04-2021
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef INTERCHANNELID_H
#define INTERCHANNELID_H

//*****************************************************************************
// ���������� ����� ������
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
                                                  
    eICId_BlackBox              = 1,              ///< ������ �� ( \a EEPROM)
                                                  
    eICId_ProtStateCode         = 2,              ///< ���������� �������� "��� ������"
                                                  
    eICId_MonPar1               = 3,              ///< ���������� �������� 1
    eICId_MonPar2               = 4,              ///< ���������� �������� 2
    eICId_MonPar3               = 5,              ///< ���������� �������� 3
    eICId_MonPar4               = 6,              ///< ���������� �������� 4
                                                  
    eICId_Sheduler              = 7,              ///< ����� ������ �����
    eICId_Eeprom                = 8,              ///< ������ \a EEPROM
                                                  
    eICId_ControlMC             = 9,              ///< ���������� ���������� �������� ��
    eICId_ControlMC_ROM         = 10,             ///< ������� ����������� ����� ���
    eICId_ControlMC_CF          = 11,             ///< ������� ������� �������� ������ �������
                                                  
    eICId_BinAddress            = 12,             ///< ����� ��� ������ �� RS-422, ����� 1-2
    eICId_BinConfCrc            = 13,             ///< �������� ��������� ������������ � CRC ���������, ����� 1-2
    eICId_BinOther              = 14,             ///< �������� ������ ���������� ������
                                                  
    eICId_Meas_URefM            = 15,             ///< �������� �������� ���������� �� Master
    eICId_Meas_URefS            = 16,             ///< �������� �������� ���������� �� Slave
    eICId_Meas_TestBufReg       = 17,             ///< ���������� ������������ ��������� �������� ���
    eICId_Meas_U220V            = 18,             ///< �������� ���������� 220 �
    eICId_Meas_dFdT1            = 19,             ///< ���������� �� ������� dFdT ������� �������� 1
    eICId_Meas_dFdT2            = 20,             ///< ���������� �� ������� dFdT ������� �������� 2
    eICId_Meas_dFdT3            = 21,             ///< ���������� �� ������� dFdT ������� �������� 3
    eICId_Meas_dFdT4            = 22,             ///< ���������� �� ������� dFdT ������� �������� 4
    eICId_Meas_dFdT5            = 23,             ///< ���������� �� ������� dFdT ������� �������� 5
    eICId_Meas_Ico1             = 24,             ///< ��� ������� �������� 1
    eICId_Meas_IcomCo1          = 25,             ///< ��� ����� ������� �������� 1
    eICId_Meas_Uco1             = 26,             ///< ���������� ������� �������� 1
    eICId_Meas_Ico2             = 27,             ///< ��� ������� �������� 2
    eICId_Meas_IcomCo2          = 28,             ///< ��� ����� ������� �������� 2
    eICId_Meas_Uco2             = 29,             ///< ���������� ������� �������� 2
    eICId_Meas_Ico3             = 30,             ///< ��� ������� �������� 3
    eICId_Meas_IcomCo3          = 31,             ///< ��� ����� ������� �������� 3
    eICId_Meas_Uco3             = 32,             ///< ���������� ������� �������� 3
    eICId_Meas_Ico4             = 33,             ///< ��� ������� �������� 4
    eICId_Meas_IcomCo4          = 34,             ///< ��� ����� ������� �������� 4
    eICId_Meas_Uco4             = 35,             ///< ���������� ������� �������� 4
    eICId_Meas_Ico5             = 36,             ///< ��� ������� �������� 5
    eICId_Meas_IcomCo5          = 37,             ///< ��� ����� ������� �������� 5
    eICId_Meas_Uco5             = 38,             ///< ���������� ������� �������� 5
    eICId_Meas_U5V              = 39,             ///< ���������� ������� 5 �
    eICId_Meas_U0V              = 40,             ///< ���������� ������� 0 �
 
    eICId_CalibrM               = 41,             ///< �������� �������������� �������� �� Master � Slave
    eICId_CalibrS               = 42,             ///< �������� �������������� �������� �� Slave � Master

    eICId_BlockExch             = 43,             ///< ����� "�������� - ���������" ������������� �������� ������ 
    eICId_Rs422Sync_Rs1         = 44,             ///< ������������� 1-�� ������ RS-422
    eICId_Rs422Sync_Rs2         = 45,             ///< ������������� 2-�� ������ RS-422
            
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
*
* ������ 1.0.2
* ����   22-02-2021
* �����  ��������� �.�.
*
* ���������:
*    ������� �������������� eICId_Ebilock950Line2, eICId_Ebilock950Line1.
*
* ������ 1.0.3
* ����   05-04-2021
* �����  ��������� �.�.
*
* ���������:
*    ��������� �������������� eICId_CalibrM, eICId_CalibrM.
*/
