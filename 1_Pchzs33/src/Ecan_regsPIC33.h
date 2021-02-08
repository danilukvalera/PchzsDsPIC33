/**
* \file    Ecan_regsPIC33.h
* \brief   ����� ������ ECAN
*
* \version 1.0.1
* \date    16-03-2017
* \author  ��������� �.�.
*/

//*****************************************************************************
// ������� ������������� ��� �������������� ���������� ��������� ����������� �����
//*****************************************************************************
#ifndef Ecan_regsPIC33_h
#define Ecan_regsPIC33_h

//*****************************************************************************
// ������������ �����
//*****************************************************************************
#include <xc.h>

//*****************************************************************************
// ���������� ����� ������
//*****************************************************************************

//*****************************************************************************
/// \brief ��������� ������ ������ ECAN.
///
typedef struct {
    /// \brief ������� CxCTRL1.
    ///
    union {
        unsigned short  CTRL1;
        C1CTRL1BITS     CTRL1bits;
    };

    /// \brief ������� CxCTRL2.
    ///
    union {
        unsigned short  CTRL2;
        C1CTRL2BITS     CTRL2bits;
    };

    /// \brief ������� CxVEC.
    ///
    union {
        unsigned short  VEC;
        C1VECBITS       VECbits;
    };

    /// \brief ������� CxFCTRL.
    ///
    union {
        unsigned short  FCTRL;
        C1FCTRLBITS     FCTRLbits;
    };

    /// \brief ������� CxFIFO.
    ///
    union {
        unsigned short  FIFO;
        C1FIFOBITS      FIFObits;
    };

    /// \brief ������� CxINTF.
    ///
    union {
        unsigned short  INTF;
        C1INTFBITS      INTFbits;
    }; 

    /// \brief ������� CxINTE.
    ///
    union {
        unsigned short  INTE;
        C1INTEBITS      INTEbits;
    }; 

    /// \brief ������� CxEC.
    ///
    union {
        unsigned short  EC;
        C1ECBITS        ECbits;
    }; 

     /// \brief ������� CxCFG1.
     ///
    union {
        unsigned short  CFG1;
        C1CFG1BITS      CFG1bits;
    };
    
    /// \brief ������� CxCFG2.
    ///
    union {
        unsigned short  CFG2;
        C1CFG2BITS      CFG2bits;
    };

    /// \brief ������� CxFEN1.
    ///
    union {
        unsigned short  FEN1;
        C1FEN1BITS      FEN1bits;     // 14
    };

    unsigned short aNoUseRegs1;        // 16

    /// \brief ������� CxFMSKSEL1.
    ///
    union {
        unsigned short  FMSKSEL1;
        C1FMSKSEL1BITS  FMSKSEL1bits; // 18
    };
    
    /// \brief ������� CxFMSKSEL2.
    ///
    union {
        unsigned short  FMSKSEL2;
        C1FMSKSEL2BITS  FMSKSEL2bits;  // 1A
    };
  
    unsigned short aNoUseRegs2[2];     // 1C - x1E

    /// \brief ������� CxRXFUL1  CxBUFPNT1.
    ///    
    union {
        unsigned short  RXFUL1;
        C1RXFUL1BITS    RXFUL1bits;   // 20
        unsigned short  BUFPNT1;
        C1BUFPNT1BITS   BUFPNT1bits;  // 20  WIN = 1
    }; 

    /// \brief ������� CxRXFUL2.
    ///
    union {
        unsigned short  RXFUL2;
        C1RXFUL2BITS    RXFUL2bits;  // 22
    }; 

    unsigned short aNoUseRegs3[2];     // 24 - 26

    /// \brief ������� CxRXOVF1.
    ///
    union {
        unsigned short  RXOVF1;
        C1RXOVF1BITS    RXOVF1bits;  // 28
    }; 
    
    /// \brief ������� CxRXOVF2.
    ///
    union {
        unsigned short  RXOVF2;
        C1RXOVF2BITS    RXOVF2bits;  // 2A
    }; 

    unsigned short aNoUseRegs4[2];     // 2C - 2E

    /// \brief ������� CxTR01CON CxRXM0SID.
    ///
    union {
        unsigned short  TR01CON;
        C1TR01CONBITS   TR01CONbits;  // 30 WIN = 0
        unsigned short  RXM0SID;
        C1RXM0SIDBITS   RXM0SIDbits;  // 30 WIN = 1
    }; 

    unsigned short aNoUseRegs5[7];    // 32 - 3E

    /// \brief ������� CxRXF0SID.
    ///
    union {
        unsigned short  RXF0SID;
        C1RXF0SIDBITS   RXF0SIDbits;  // 40 WIN= 1
    }; 

}dsPIC33E_CanRegs;

//*****************************************************************************
// ���������� ���������� ����������
//*****************************************************************************

//*****************************************************************************
/// \brief SFR blocks for each CAN1 module.
///
extern volatile dsPIC33E_CanRegs dsPIC33E_Can1Regs __attribute__ ((sfr(0x0400)));

//*****************************************************************************
/// \brief SFR blocks for each CAN2 module.
///
extern volatile dsPIC33E_CanRegs dsPIC33E_Can2Regs __attribute__ ((sfr(0x0500)));

#endif

//*****************************************************************************
/**
* ������� ���������: 
*
* ������ 1.0.1
* ����   16-03-2017
* �����  ��������� �.�.
*
* ���������:
*    ������� ������.
*/
