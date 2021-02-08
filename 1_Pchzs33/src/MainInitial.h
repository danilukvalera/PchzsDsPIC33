#ifndef MAININITIAL_H
#define MAININITIAL_H

/// \brief Адрес абонента CAN межканального обмена ГАРС-С
#define ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GARS_S  0x2A0 
#define ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GARS_S   0x2A1

/// \brief Адрес абонента CAN межканального обмена ГКЛС-Е
#define ADDRESS_CAN_EXTERNAL_DEVICE_MASTER_GKLS_E  0x00B 
#define ADDRESS_CAN_EXTERNAL_DEVICE_SLAVE_GKLS_E   0x00A

#define GKLS_E 0
#define GARS_S 1


/// \brief Инициализация по сбросу МК.
void MainInitial( void );


#endif
