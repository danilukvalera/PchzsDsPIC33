/**
* \file    Combination4from6.c
* \brief   \copybrief Combination4from6.h
*
* \version 1.0.1
* \date    27-06-2018
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <stdint.h>
#include "Combination4from6.h"

//*****************************************************************************
// ��������� ���������, ������������ ����� �������
//*****************************************************************************

//*****************************************************************************
/// \brief ����������� ���������� ����������� �����.
///
#define N_COMBINATION 4

//*****************************************************************************
/// \brief ���������� ������.
///
#define N_DATA        6

//*****************************************************************************
/// \brief ������ ������� ��������.
///
#define N_ARR         60

//*****************************************************************************
// ���������� ��������� �������������� ��������
//*****************************************************************************

//*****************************************************************************
/// \brief ������� � ������� ��� ��������� 4 �� 6.
/// \note 1 - ������� ����� �������.
///
const int8_t aIndex4from6[N_ARR] = 
{
    0, 1, 2, 3,
    0, 1, 2, 4,
    0, 1, 2, 5,
    0, 1, 3, 4,
    0, 1, 3, 5,
    0, 1, 4, 5,
    0, 2, 3, 4,
    0, 2, 3, 5,
    0, 2, 4, 5,
    0, 3, 4, 5,
    1, 2, 3, 4,
    1, 2, 3, 5,
    1, 2, 4, 5,
    1, 3, 4, 5,
    2, 3, 4, 5
};

//*****************************************************************************
// ����������������, ����������� �����������
//*****************************************************************************

#ifdef TYPE_DATA  
    #undef TYPE_DATA  
#endif

//*****************************************************************************
/// \brief ������� ��������� ������� ������ �� ��������� 4 �� 6. 
///
#define TYPE_DATA uint16_t    // 16-������ ������
#include "Combination4from6_template.h"
#ifdef TYPE_DATA  
    #undef TYPE_DATA  
#endif

//*****************************************************************************
/// \brief ������� ��������� ������� ������ �� ��������� 4 �� 6.
///
#define TYPE_DATA uint8_t    // 8-������ ������
#include "Combination4from6_template.h"
#ifdef TYPE_DATA  
    #undef TYPE_DATA  
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
