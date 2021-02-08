#ifndef MAININITIAL_H
#define MAININITIAL_H

/// \brief ����� �������� CAN ������������� ������ ����-�
#define ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GARS_S  0x2A0 
#define ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GARS_S   0x2A1

/// \brief ����� �������� CAN ������������� ������ ����-�
#define ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GKLS_E  0x00B 
#define ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GKLS_E   0x00A

#define GKLS_E 0
#define GARS_S 1


/// \brief ������������� �� ������ ��.
void MainInitial( void );


#endif
