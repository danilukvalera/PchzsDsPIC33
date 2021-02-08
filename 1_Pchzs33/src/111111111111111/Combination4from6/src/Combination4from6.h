/**
* \file    Combination4from6.h
* \brief   ������� ��������� ������� �� ��������� 4 �� 6
* \details ���������
*
* \version 1.0.1
* \date    27-06-2018
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef COMBINATION4FROM6_H
#define COMBINATION4FROM6_H

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************

//*****************************************************************************
/// \brief ������� ��������� ������� 16-������ ������ �� ��������� 4 �� 6.
/// \details � ���������� ��� ��������������� � 1, ���� � �������� ������� ��������������� ��� 
/// ���������� ��� ������� � 4 ���������.
/// \param arr       - ��������� �� ������ �� 6 ��������� �������� ������;
/// \param oldResult - ���������� ��������� ���������.
/// \return ��������� ���������.
/// \note � �������� ���������� ������� ���������� ������ ������� ������.
/// 
uint16_t Combination4from6_uint16_t( uint16_t *arr, uint16_t oldResult );

//*****************************************************************************
/// \brief ������� ��������� ������� 8-������ ������ �� ��������� 4 �� 6.
/// \details � ���������� ��� ��������������� � 1, ���� � �������� ������� ��������������� ��� 
/// ���������� ��� ������� � 4 ���������.
/// \param arr       - ��������� �� ������ �� 6 ��������� �������� ������;
/// \param oldResult - ���������� ��������� ���������.
/// \return ��������� ���������.
/// \note � �������� ���������� ������� ���������� ������ ������� ������.
/// 
uint8_t Combination4from6_uint8_t( uint8_t *arr, uint8_t oldResult );

#endif

//*****************************************************************************
/**
* ������� ���������:
* 
* ������ 1.0.1
* ����   27-06-2018
* �����  ��������� �.�.
* 
* ���������:
*    ������� ������.
*/
