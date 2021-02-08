#ifndef BLUETOOTH_OPERATION_H
#define	BLUETOOTH_OPERATION_H

#include <stdbool.h>

#define MAX_RECEIVE			50                      // ������ ������ ����� ������ �� BT

void clearReceiveArr(void);                         // ������� ��������� ������
void prepareReceiveBuffer(void);                    // ���������� ��������� ������ � ������ ����� �������
bool compare(const char *B);                        // ��������� ���� � ��������� � ����� ��������� �������
void sendString(char *pstr, bool clear, bool x0d);  // �������� ������ ��� �� ������� � Bluetooth
void send_byte(char b);                             // ������ ����� � Bluetooth
bool checkConnectionBluetooth(void);                // �������� ���������� � ����������



#endif	/* XC_HEADER_TEMPLATE_H */

