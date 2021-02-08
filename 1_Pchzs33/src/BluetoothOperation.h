#ifndef BLUETOOTH_OPERATION_H
#define	BLUETOOTH_OPERATION_H

#include <stdbool.h>

#define MAX_RECEIVE			50                      // размер буфера прима данных от BT

void clearReceiveArr(void);                         // очистка приемного буфера
void prepareReceiveBuffer(void);                    // подготовка приемного буфера к приему новой команды
bool compare(const char *B);                        // Сравнение слов с привязкой к концу принятого массива
void sendString(char *pstr, bool clear, bool x0d);  // Передача строки или АТ команды в Bluetooth
void send_byte(char b);                             // Выдача байта в Bluetooth
bool checkConnectionBluetooth(void);                // проверка сопряжения с тедлефоном



#endif	/* XC_HEADER_TEMPLATE_H */

