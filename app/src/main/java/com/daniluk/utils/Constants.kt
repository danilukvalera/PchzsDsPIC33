package com.daniluk.utils

import com.daniluk.R
object Constants {
    const val colorRED: Int = R.color.colorLightRed
    const val colorGREEN: Int = R.color.colorLightGreen
    const val colorBLUE: Int = R.color.colorLightBlue
    const val colorGray: Int = R.color.colorGray

    //количество попыток автоматического переподключения
    const val NUMBER_ATTEMPTS_AUTO_CONNECT = 5

    //Состояние ПЧЗС
    const val STATE_ERROR_BLUETOOTH_INIT                = 0
    const val STATE_NO_CONNECT                          = 1
    const val STATE_READY_READ_CODE                     = 2
    const val STATE_CONNECTION_IN_PROGRESS              = 3
    const val STATE_DISCONNECTION_IN_PROGRESS           = 4
    const val STATE_READ_CODE_IN_PROGRESS               = 5
    const val STATE_REMOVE_PROTECT_CODE_IN_PROGRESS     = 6

    //Команды управления Bluetooth
    const val COMMAND_BT_DO_NOTHING             = 0
    const val COMMAND_BT_ON                     = 1
    const val COMMAND_SEARCH_PERMISSION_REQUEST = 2
    const val COMMAND_WRITE_PERMISSION_REQUEST  = 3

    //запросы к модулю сопряжения
    const val BLUETOOTH_DEVICE_NAME = "PCHZS-DSPIC33"
    const val REMOV_PROTECT_STATE = "%REMOV_PROTECT_STATE%"

    const val READ_PROTECT_STATE = "%READ_PROTECT_STATE%";
    //const val READ_PROTECT_STATE = "%READ_PROTECT_STATE_GARS%"
    //const val READ_PROTECT_STATE = "%READ_PROTECT_STATE_GKLS%";
    //const val READ_PROTECT_STATE = "%READ_PROTECT_STATE_ERROR%";

    const val MASTER = 1
    const val SLAVE = 0
    const val MASTER_STR = "Master"
    const val SLAVE_STR = "Slave"

    const val FLAG_DECODE_CODE = 1
    const val FLAG_DECODE_NON_ERASABLE_CODE = 2

    const val TAG_CAN_NEW = 1
    const val TAG_CAN_OLD = 2
    const val TAG_PP3S = 3
    const val TAG_GP3S = 4

    const val NAME_FILE_MASTER_HEX = "_master"
    const val NAME_FILE_SLAVE_HEX = "_slave"
    const val NAME_DIRECTOTY = "PCHZS"


    //ответы модуля сопряжения
    const val READ_ZS_END = "READ_ZS_END" //признак окончания процедуры чтения ЗС

    const val CAN_NEW = "_CAN_NEW" //признак данных принятых по новому CAN

    const val CAN_OLD = "_CAN_OLD" //признак данных принятых по старому CAN
    //const val CAN_OLD_MASTER = "_CAN_OLD_MASTER" //признак данных принятых по старому CAN от MASTER (этот признак записывается в список данных)
    //const val CAN_OLD_SLAVE = "_CAN_OLD_SLAVE" //признак данных принятых по старому CAN от SLAVE (этот признак записывается в список данных)

    const val PP3S = "_PP3S" //признак данных принятых от приемника ТРЦ

    const val GP3S = "_GP3S" //признак данных принятых от генератора ТРЦ

    const val ERROR_POWER_DEVICE = "_ERROR_POWER_DEVICE_";           //признак отсутствия питания ОК
    const val RESET_PROTECT_STATE_END = "RESET_PROTECT_STATE_END" //признак окончания процедуры снятия ЗС

    const val RESET_PROTECT_STATE_OK = "_RESET_PROTECT_STATE_OK";    //признак успешного выполнения снятия ЗС

 //*************************Для CAN_NEW**********************************************************
    const val ID_MASTER_NEW_CAN = "02A0" //Адрес master для CAN_NEW

    const val ID_SLAVE_NEW_CAN = "02A1" //Адрес slave для CAN_NEW


    const val deviceNameStartAdress = 0x02
    const val deviceNameEndAdress = 0x0E
    const val pocessorIdStartAdress = 0x0E
    const val pocessorIdEndAdress = 0x10
    const val programVersionStartAdress = 0x10
    const val programVersionEndAdress = 0x20

    //public static int programDateReleaseStartAdress = 0x20;
    const val programDateReleaseStartAdress = 0x25
    const val programDateReleaseEndAdress = 0x30
    const val defectFileNameStartAdress = 0x50
    const val defectFileNameEndAdress = 0x6E
    const val defectStringNumberAdress = 0x6E

    const val adressCodProtectState1 = 0x0030
    const val adressCodProtectState2 = 0x0030 //0x0FF0;   таких адресов в памяти нет!!!

    const val adressCodProtectState3 = 0x0030 //0x0FF2;   таких адресов в памяти нет!!!

    const val adressCodProtectState4 = 0x0032

    const val NO_PROTECT_STATE = "A55A"
    const val TYPE_DEFECT = "тип отказа"
    const val END_BLOCK = "конец "

    //*************************Для CAN_OLD**********************************************************
    const val FILE_NAME_MAP_DEVICE_CAN_OLD = "mapDevice.txt" //Адрес master для CAN_OLD


    const val ID_MASTER_OLD_CAN = "001C" //Адрес master для CAN_OLD

    const val ID_SLAVE_OLD_CAN = "041C" //Адрес slave для CAN_OLD


    const val NO_PROTECT_STATE_OLD_CAN = "A55A" //код отсутствия отказа

    const val LABEL_TYPE_DEVICE = "тип прибора"
    const val LABEL_END_TYPE_DEVICE = "конец тип прибора"

    //идентификаторы сообщений
    const val ID_COD_PROTECT_STATE = "01" //код защитного состояния

    const val ID_COD_PROTECT_STATE_NOT_ERASABLE = "04" //не стираемый код защитного состояния

    const val ID_COD_DEVICE = "05" //тип прибора

    const val ID_COD_VERSION_PO = "06" //версия ПО

    const val ID_COD_VERSION_BUILD = "60" //версия сборки ПО

    const val ID_COD_KS_PZU_VALUE = "07" //контрольная сумма ПЗУ

    const val ID_COD_DATE_COMPIL = "18" //дата компиляции

}
//Данные от Master и Slave  ГАРС-С
//outMessage = "02A00100000900C3C00B12A002A10100000900C3C00B12A102A0010004D0D12DD10C12A002A1010004D0D12DD10C12A102A001000800FFFFFF0C12A002A101000800FFFFFF0C12A102A001000CFFFF00010C12A002A101000CFFFF00000C12A102A0010010766572200C12A002A1010010766572200C12A102A0010014312E302E0C12A002A1010014312E302E0C12A102A00100183600FFFF0C12A002A10100183600FFFF0C12A102A001001CFFFFFFFF0C12A002A101001CFFFFFFFF0C12A102A0010020646174650C12A002A1010020646174650C12A102A00100242032312D0C12A002A10100242032312D0C12A102A001002831302D320C12A002A101002831302D320C12A102A001002C303139000C12A002A101002C303139000C12A102A0010030022B022B0C12A002A1010030022B022B0C12A102A0010034008300020C12A002A1010034008300020C12A102A0010038017300030C12A002A1010038017300030C12A102A001003C043A952F0C12A002A101003C0492952F0C12A102A0010040001103FF0C12A002A1010040001103FF0C12A102A001004403FF00000C12A002A101004403FF00000C12A102A0010048000000000C12A002A1010048000000000C12A102A001004C000000000C12A002A101004C000000000C12A102A0010050496E74650C12A002A1010050496E74650C12A102A0010054724368610C12A002A1010054724368610C12A102A00100586E6E656C0C12A002A10100586E6E656C0C12A102A001005C2E6300000C12A002A101005C2E6300000C12A102A0010060000000000C12A002A1010060000000000C12A102A0010064000000000C12A002A1010064000000000C12A102A0010068000000000C12A002A1010068000000000C12A102A001006C000002280C12A002A101006C000002280C12A102A0010070014000020C12A002A1010070014000020C12A102A0010074020000040C12A002A1010074020000040C12A102A00100780010FFFF0C12A002A10100780010FFFF0C12A102A001007CFFFFFFFF0C12A002A101007CFFFFFFFF0C12A102A00100800001496E0C12A002A10100800001496E0C12A102A0010084746572430C12A002A1010084746572430C12A102A001008868616E6E0C12A002A101008868616E6E0C12A102A001008C656C5F720C12A002A101008C656C5F720C12A102A0010090756E00000C12A002A1010090756E00000C12A102A00100940000FFFF0C12A002A10100940000FFFF0C12A102A0010098FFFFFFFF0C12A002A1010098FFFFFFFF0C12A102A001009CFFFFFFFF0C12A002A101009CFFFFFFFF0C12A102A00100A0008000FF0C12A002A10100A0008100FF0C12A102A00100A4001000100C12A002A10100A4001000100C12A102A00100A8000000040C12A002A10100A8000000000C12A102A00100AC0000FFFF0C12A002A10100AC0000FFFF0C12A102A00100B0000600000C12A002A10100B0000600000C12A102A00100B4000006740C12A002A10100B4000006720C12A102A00100B8000005DB0C12A002A10100B8000005DA0C12A102A00100BC000001F70C12A002A10100BC000001F70C12A102A00100C000E101760C12A002A10100C000E101760C12A102A00100C400000A5C0C12A002A10100C400000A5C0C12A102A00100C80000FFFF0C12A002A10100C80000FFFF0C12A102A00100CCFFFFFFFF0C12A002A10100CCFFFFFFFF0C12A102A00100D0FFFFFFFF0C12A002A10100D0FFFFFFFF0C12A102A00100D4FFFFFFFF0C12A002A10100D4FFFFFFFF0C12A102A00100D8FFFFFFFF0C12A002A10100D8FFFFFFFF0C12A102A00100DCFFFFFFFF0C12A002A10100DCFFFFFFFF0C12A102A00100E0FFFFFFFF0C12A002A10100E0FFFFFFFF0C12A102A00100E4FFFFFFFF0C12A002A10100E4FFFFFFFF0C12A102A00100E8FFFFFFFF0C12A002A10100E8FFFFFFFF0C12A102A00100ECFFFFFFFF0C12A002A10100ECFFFFFFFF0C12A102A00100F0FFFFFFFF0C12A002A10100F0FFFFFFFF0C12A102A00100F4FFFFFFFF0C12A002A10100F4FFFFFFFF0C12A102A00100F8FFFFFFFF0C12A002A10100F8FFFFFFFF0C12A102A00100FCFFFFFFFF0C12A002A10100FCFFFFFFFF0C12A102A0010100FFFFFFFF0C12A002A1010100FFFFFFFF0C12A102A0010104FFFFFFFF0C12A002A1010104FFFFFFFF0C12A102A0010108FFFFFFFF0C12A002A1010108FFFFFFFF0C12A102A001010CFFFFFFFF0C12A002A101010CFFFFFFFF0C12A102A0010110FFFFFFFF0C12A002A1010110FFFFFFFF0C12A102A0010114FFFFFFFF0C12A002A1010114FFFFFFFF0C12A102A0010118FFFFFFFF0C12A002A1010118FFFFFFFF0C12A102A001011CFFFFFFFF0C12A002A101011CFFFFFFFF0C12A102A0010120000000000C12A002A1010120000000000C12A102A0010124000000000C12A002A1010124000000000C12A102A0010128000000000C12A002A1010128000000000C12A102A001012CFFFFFFFF0C12A002A101012CFFFFFFFF0C12A102A0010130FFFFFFFF0C12A002A1010130FFFFFFFF0C12A102A0010134FFFFFFFF0C12A002A1010134FFFFFFFF0C12A102A0010138FFFFFFFF0C12A002A1010138FFFFFFFF0C12A102A001013CFFFFFFFF0C12A002A101013CFFFFFFFF0C12A102A0010140000000000C12A002A1010140000000000C12A102A0010144000000000C12A002A1010144000000000C12A102A0010148000000000C12A002A1010148000000000C12A102A001014C000000000C12A002A101014C000000000C12A102A0010150000000000C12A002A1010150000000000C12A102A0010154000000000C12A002A1010154000000000C12A102A0010158000000000C12A002A1010158000000000C12A102A001015C000000000C12A002A101015C000000000C12A102A0010160000000000C12A002A1010160000000000C12A102A0010164000000000C12A002A1010164000000000C12A102A0010168000000000C12A002A1010168000000000C12A102A001016C000000000C12A002A101016C000000000C12A102A0010170000000000C12A002A1010170000000000C12A102A0010174000000000C12A002A1010174000000000C12A102A0010178000000000C12A002A1010178000000000C12A102A001017C000000000C12A002A101017C000000000C12A102A0010180000000000C12A002A1010180000000000C12A102A0010184000000000C12A002A1010184000000000C12A102A0010188000000000C12A002A1010188000000000C12A102A001018C000000000C12A002A101018C000000000C12A102A0010190000000000C12A002A1010190000000000C12A102A0010194000000000C12A002A1010194000000000C12A102A0010198000000000C12A002A1010198000000000C12A102A001019C000000000C12A002A101019C000000000C12A102A00101A0000000000C12A002A10101A0000000000C12A102A00101A4000000000C12A002A10101A4000000000C12A102A00101A8000000000C12A002A10101A8000000000C12A102A00101AC000000000C12A002A10101AC000000000C12A102A00101B0000000000C12A002A10101B0000000000C12A102A00101B4000000000C12A002A10101B4000000000C12A102A00101B8000000000C12A002A10101B8000000000C12A102A00101BC000000000C12A002A10101BC000000000C12A102A00101C0000000000C12A002A10101C0000000000C12A102A00101C4000000000C12A002A10101C4000000000C12A102A00101C8000000000C12A002A10101C8000000000C12A102A00101CC000000000C12A002A10101CC000000000C12A102A00101D0000000000C12A002A10101D0000000000C12A102A00101D4000000000C12A002A10101D4000000000C12A102A00101D8000000000C12A002A10101D8000000000C12A102A00101DC000000000C12A002A10101DC000000000C12A102A00101E0000000000C12A002A10101E0000000000C12A102A00101E4000000000C12A002A10101E4000000000C12A102A00101E8000000000C12A002A10101E8000000000C12A102A00101EC000000000C12A002A10101EC000000000C12A102A00101F0000000000C12A002A10101F0000000000C12A102A00101F4000000000C12A002A10101F4000000000C12A102A00101F8000000000C12A002A10101F8000000000C12A102A00101FC000000000C12A002A10101FC000000000C12A102A0010200000000000C12A002A1010200000000000C12A102A0010204000000000C12A002A1010204000000000C12A102A0010208000000000C12A002A1010208000000000C12A102A001020C000000000C12A002A101020C000000000C12A102A0010210000000000C12A002A1010210000000000C12A102A0010214000000000C12A002A1010214000000000C12A102A0010218000000000C12A002A1010218000000000C12A102A001021C000000000C12A002A101021C000000000C12A102A0010220000000000C12A002A1010220000000000C12A102A0010224000000000C12A002A1010224000000000C12A102A0010228000000000C12A002A1010228000000000C12A102A001022C000000000C12A002A101022C000000000C12A102A0010230000000000C12A002A1010230000000000C12A102A0010234000000000C12A002A1010234000000000C12A102A0010238000000000C12A002A1010238000000000C12A102A001023C000000000C12A002A101023C000000000C12A102A0010240000000000C12A002A1010240000000000C12A102A0010244000000000C12A002A1010244000000000C12A102A0010248000000000C12A002A1010248000000000C12A102A001024C000000000C12A002A101024C000000000C12A102A0010250000000000C12A002A1010250000000000C12A102A0010254000000000C12A002A1010254000000000C12A102A0010258000000000C12A002A1010258000000000C12A102A001025C000000000C12A002A101025C000000000C12A102A0010260000000000C12A002A1010260000000000C12A102A0010264000000000C12A002A1010264000000000C12A102A0010268000000000C12A002A1010268000000000C12A102A001026C000000000C12A002A101026C000000000C12A102A0010270000000000C12A002A1010270000000000C12A102A0010274000000000C12A002A1010274000000000C12A102A0010278000000000C12A002A1010278000000000C12A102A001027C000000000C12A002A101027C000000000C12A102A0010280000000000C12A002A1010280000000000C12A102A0010284000000000C12A002A1010284000000000C12A102A0010288000000000C12A002A1010288000000000C12A102A001028C000000000C12A002A101028C000000000C12A102A0010290000000000C12A002A1010290000000000C12A102A0010294000000000C12A002A1010294000000000C12A102A0010298000000000C12A002A1010298000000000C12A102A001029C000000000C12A002A101029C000000000C12A102A00102A0000000000C12A002A10102A0000000000C12A102A00102A4000000000C12A002A10102A4000000000C12A102A00102A8000000000C12A002A10102A8000000000C12A102A00102AC000000000C12A002A10102AC000000000C12A102A00102B0000000000C12A002A10102B0000000000C12A102A00102B4000000000C12A002A10102B4000000000C12A102A00102B8000000000C12A002A10102B8000000000C12A102A00102BC000000000C12A002A10102BC000000000C12A102A00102C0000000000C12A002A10102C0000000000C12A102A00102C4000000000C12A002A10102C4000000000C12A102A00102C8000000000C12A002A10102C8000000000C12A102A00102CC000000000C12A002A10102CC000000000C12A102A00102D0000000000C12A002A10102D0000000000C12A102A00102D4000000000C12A002A10102D4000000000C12A102A00102D8000000000C12A002A10102D8000000000C12A102A00102DC000000000C12A002A10102DC000000000C12A102A00102E0000000000C12A002A10102E0000000000C12A102A00102E4000000000C12A002A10102E4000000000C12A102A00102E8000000000C12A002A10102E8000000000C12A102A00102EC000000000C12A002A10102EC000000000C12A102A00102F0000000000C12A002A10102F0000000000C12A102A00102F4000000000C12A002A10102F4000000000C12A102A00102F8000000000C12A002A10102F8000000000C12A102A00102FC000000000C12A002A10102FC000000000C12A102A0010300000000000C12A002A1010300000000000C12A102A0010304000000000C12A002A1010304000000000C12A102A0010308000000000C12A002A1010308000000000C12A102A001030C000000000C12A002A101030C000000000C12A102A0010310000000000C12A002A1010310000000000C12A102A0010314000000000C12A002A1010314000000000C12A102A0010318000000000C12A002A1010318000000000C12A102A001031C000000000C12A002A101031C000000000C12A102A0010320000000000C12A002A1010320000000000C12A102A0010324000000000C12A002A1010324000000000C12A102A0010328000000000C12A002A1010328000000000C12A102A001032C000000000C12A002A101032C000000000C12A102A0010330000000000C12A002A1010330000000000C12A102A0010334000000000C12A002A1010334000000000C12A102A0010338000000000C12A002A1010338000000000C12A102A001033C000000000C12A002A101033C000000000C12A102A0010340000000000C12A002A1010340000000000C12A102A0010344000000000C12A002A1010344000000000C12A102A0010348000000000C12A002A1010348000000000C12A102A001034C000000000C12A002A101034C000000000C12A102A0010350000000000C12A002A1010350000000000C12A102A0010354000000000C12A002A1010354000000000C12A102A0010358000000000C12A002A1010358000000000C12A102A001035C000000000C12A002A101035C000000000C12A102A0010360000000000C12A002A1010360000000000C12A102A0010364000000000C12A002A1010364000000000C12A102A0010368000000000C12A002A1010368000000000C12A102A001036C000000000C12A002A101036C000000000C12A102A0010370000000000C12A002A1010370000000000C12A102A0010374000000000C12A002A1010374000000000C12A102A0010378000000000C12A002A1010378000000000C12A102A001037C000000000C12A002A101037C000000000C12A102A0010380000000000C12A002A1010380000000000C12A102A0010384000000000C12A002A1010384000000000C12A102A0010388000000000C12A002A1010388000000000C12A102A001038C000000000C12A002A101038C000000000C12A102A0010390000000000C12A002A1010390000000000C12A102A0010394000000000C12A002A1010394000000000C12A102A0010398000000000C12A002A1010398000000000C12A102A001039C000000000C12A002A101039C000000000C12A102A00103A0000000000C12A002A10103A0000000000C12A102A00103A4000000000C12A002A10103A4000000000C12A102A00103A8000000000C12A002A10103A8000000000C12A102A00103AC000000000C12A002A10103AC000000000C12A102A00103B0000000000C12A002A10103B0000000000C12A102A00103B4000000000C12A002A10103B4000000000C12A102A00103B8000000000C12A002A10103B8000000000C12A102A00103BC000000000C12A002A10103BC000000000C12A102A00103C0000000000C12A002A10103C0000000000C12A102A00103C4000000000C12A002A10103C4000000000C12A102A00103C8000000000C12A002A10103C8000000000C12A102A00103CC000000000C12A002A10103CC000000000C12A102A00103D0000000000C12A002A10103D0000000000C12A102A00103D4000000000C12A002A10103D4000000000C12A102A00103D8000000000C12A002A10103D8000000000C12A102A00103DC000000000C12A002A10103DC000000000C12A102A00103E0000000000C12A002A10103E0000000000C12A102A00103E4000000000C12A002A10103E4000000000C12A102A00103E8000000000C12A002A10103E8000000000C12A102A00103EC000000000C12A002A10103EC000000000C12A102A00103F0000000000C12A002A10103F0000000000C12A102A00103F4000000000C12A002A10103F4000000000C12A102A00103F8000000000C12A002A10103F8000000000C12A102A00103FC000000000C12A002A10103FC000000000C12A102A0010400000000000C12A002A1010400000000000C12A102A0010404000000000C12A002A1010404000000000C12A102A0010408000000000C12A002A1010408000000000C12A102A001040C000000000C12A002A101040C000000000C12A102A0010410000000000C12A002A1010410000000000C12A102A0010414000000000C12A002A1010414000000000C12A102A0010418000000000C12A002A1010418000000000C12A102A001041C000000000C12A002A101041C000000000C12A102A0010420000000000C12A002A1010420000000000C12A102A0010424000000000C12A002A1010424000000000C12A102A0010428000000000C12A002A1010428000000000C12A102A001042C000000000C12A002A101042C000000000C12A102A0010430000000000C12A002A1010430000000000C12A102A0010434000000000C12A002A1010434000000000C12A102A0010438000000000C12A002A1010438000000000C12A102A001043C000000000C12A002A101043C000000000C12A102A0010440000000000C12A002A1010440000000000C12A102A0010444000000000C12A002A1010444000000000C12A102A0010448000000000C12A002A1010448000000000C12A102A001044C000000000C12A002A101044C000000000C12A102A0010450000000000C12A002A1010450000000000C12A102A0010454000000000C12A002A1010454000000000C12A102A0010458000000000C12A002A1010458000000000C12A102A001045C000000000C12A002A101045C000000000C12A102A0010460000000000C12A002A1010460000000000C12A102A0010464000000000C12A002A1010464000000000C12A102A0010468000000000C12A002A1010468000000000C12A102A001046C000000000C12A002A101046C000000000C12A102A0010470000000000C12A002A1010470000000000C12A102A0010474000000000C12A002A1010474000000000C12A102A0010478000000000C12A002A1010478000000000C12A102A001047C000000000C12A002A101047C000000000C12A102A0010480000000000C12A002A1010480000000000C12A102A0010484000000000C12A002A1010484000000000C12A102A0010488000000000C12A002A1010488000000000C12A102A001048C000000000C12A002A101048C000000000C12A102A0010490000000000C12A002A1010490000000000C12A102A0010494000000000C12A002A1010494000000000C12A102A0010498000000000C12A002A1010498000000000C12A102A001049C000000000C12A002A101049C000000000C12A102A00104A0000000000C12A002A10104A0000000000C12A102A00104A4000000000C12A002A10104A4000000000C12A102A00104A8000000000C12A002A10104A8000000000C12A102A00104AC000000000C12A002A10104AC000000000C12A102A00104B0000000000C12A002A10104B0000000000C12A102A00104B4000000000C12A002A10104B4000000000C12A102A00104B8000000000C12A002A10104B8000000000C12A102A00104BC000000000C12A002A10104BC000000000C12A102A00104C0000000000C12A002A10104C0000000000C12A102A00104C4000000000C12A002A10104C4000000000C12A102A00104C8000000000C12A002A10104C8000000000C12A102A00104CC000000000C12A002A10104CC000000000C12A102A00104D0000000000C12A002A10104D0000000000C12A102A00104D4000000000C12A002A10104D4000000000C12A102A00104D8000000000C12A002A10104D8000000000C12A102A00104DC000000000C12A002A10104DC000000000C12A102A00104E0000000000C12A002A10104E0000000000C12A102A00104E4000000000C12A002A10104E4000000000C12A102A00104E8000000000C12A002A10104E8000000000C12A102A00104EC000000000C12A002A10104EC000000000C12A102A00104F0000000000C12A002A10104F0000000000C12A102A00104F4000000000C12A002A10104F4000000000C12A102A00104F8000000000C12A002A10104F8000000000C12A102A00104FC000000000C12A002A10104FC000000000C12A102A0010500000000000C12A002A1010500000000000C12A102A0010504000000000C12A002A1010504000000000C12A102A0010508000000000C12A002A1010508000000000C12A102A001050C000000000C12A002A101050C000000000C12A102A0010510000000000C12A002A1010510000000000C12A102A0010514000000000C12A002A1010514000000000C12A102A0010518000000000C12A002A1010518000000000C12A102A001051C000000000C12A002A101051C000000000C12A102A0010520000000000C12A002A1010520000000000C12A102A0010524000000000C12A002A1010524000000000C12A102A0010528000000000C12A002A1010528000000000C12A102A001052C000000000C12A002A101052C000000000C12A102A0010530000000000C12A002A1010530000000000C12A102A0010534000000000C12A002A1010534000000000C12A102A0010538000000000C12A002A1010538000000000C12A102A001053C000000000C12A002A101053C000000000C12A102A0010540000000000C12A002A1010540000000000C12A102A0010544000000000C12A002A1010544000000000C12A102A0010548000000000C12A002A1010548000000000C12A102A001054C000000000C12A002A101054C000000000C12A102A0010550000000000C12A002A1010550000000000C12A102A0010554000000000C12A002A1010554000000000C12A102A0010558000000000C12A002A1010558000000000C12A102A001055C000000000C12A002A101055C000000000C12A102A0010560000000000C12A002A1010560000000000C12A102A0010564000000000C12A002A1010564000000000C12A102A0010568000000000C12A002A1010568000000000C12A102A001056C000000000C12A002A101056C000000000C12A102A0010570000000000C12A002A1010570000000000C12A102A0010574000000000C12A002A1010574000000000C12A102A0010578000000000C12A002A1010578000000000C12A102A001057C000000000C12A002A101057C000000000C12A102A0010580000000000C12A002A1010580000000000C12A102A0010584000000000C12A002A1010584000000000C12A102A0010588000000000C12A002A1010588000000000C12A102A001058C000000000C12A002A101058C000000000C12A102A0010590000000000C12A002A1010590000000000C12A102A0010594000000000C12A002A1010594000000000C12A102A0010598000000000C12A002A1010598000000000C12A102A001059C000000000C12A002A101059C000000000C12A102A00105A0000000000C12A002A10105A0000000000C12A102A00105A4000000000C12A002A10105A4000000000C12A102A00105A8000000000C12A002A10105A8000000000C12A102A00105AC000000000C12A002A10105AC000000000C12A102A00105B0000000000C12A002A10105B0000000000C12A102A00105B4000000000C12A002A10105B4000000000C12A102A00105B8000000000C12A002A10105B8000000000C12A102A00105BC000000000C12A002A10105BC000000000C12A102A00105C0000000000C12A002A10105C0000000000C12A102A00105C4000000000C12A002A10105C4000000000C12A102A00105C8000000000C12A002A10105C8000000000C12A102A00105CC000000000C12A002A10105CC000000000C12A102A00105D0000000000C12A002A10105D0000000000C12A102A00105D4000000000C12A002A10105D4000000000C12A102A00105D8000000000C12A002A10105D8000000000C12A102A00105DC000000000C12A002A10105DC000000000C12A102A00105E0000000000C12A002A10105E0000000000C12A102A00105E4000000000C12A002A10105E4000000000C12A102A00105E8000000000C12A002A10105E8000000000C12A102A00105EC000000000C12A002A10105EC000000000C12A102A00105F0000000000C12A002A10105F0000000000C12A102A00105F4000000000C12A002A10105F4000000000C12A102A00105F8000000000C12A002A10105F8000000000C12A102A00105FC000000000C12A002A10105FC000000000C12A102A0010600000000000C12A002A1010600000000000C12A102A0010604000000000C12A002A1010604000000000C12A102A0010608000000000C12A002A1010608000000000C12A102A001060C000000000C12A002A101060C000000000C12A102A0010610000000000C12A002A1010610000000000C12A102A0010614000000000C12A002A1010614000000000C12A102A0010618000000000C12A002A1010618000000000C12A102A001061C000000000C12A002A101061C000000000C12A102A0010620000000000C12A002A1010620000000000C12A102A0010624000000000C12A002A1010624000000000C12A102A0010628000000000C12A002A1010628000000000C12A102A001062C000000000C12A002A101062C000000000C12A102A0010630000000000C12A002A1010630000000000C12A102A0010634000000000C12A002A1010634000000000C12A102A0010638000000000C12A002A1010638000000000C12A102A001063C000000000C12A002A101063C000000000C12A102A0010640000000000C12A002A1010640000000000C12A102A0010644000000000C12A002A1010644000000000C12A102A0010648000000000C12A002A1010648000000000C12A102A001064C000000000C12A002A101064C000000000C12A102A0010650000000000C12A002A1010650000000000C12A102A0010654000000000C12A002A1010654000000000C12A102A0010658000000000C12A002A1010658000000000C12A102A001065C000000000C12A002A101065C000000000C12A102A0010660000000000C12A002A1010660000000000C12A102A0010664000000000C12A002A1010664000000000C12A102A0010668000000000C12A002A1010668000000000C12A102A001066C000000000C12A002A101066C000000000C12A102A0010670000000000C12A002A1010670000000000C12A102A0010674000000000C12A002A1010674000000000C12A102A0010678000000000C12A002A1010678000000000C12A102A001067C000000000C12A002A101067C000000000C12A102A0010680000000000C12A002A1010680000000000C12A102A0010684000000000C12A002A1010684000000000C12A102A0010688000000000C12A002A1010688000000000C12A102A001068C000000000C12A002A101068C000000000C12A102A0010690000000000C12A002A1010690000000000C12A102A0010694000000000C12A002A1010694000000000C12A102A0010698000000000C12A002A1010698000000000C12A102A001069C000000000C12A002A101069C000000000C12A102A00106A0000000000C12A002A10106A0000000000C12A102A00106A4000000000C12A002A10106A4000000000C12A102A00106A8000000000C12A002A10106A8000000000C12A102A00106AC000000000C12A002A10106AC000000000C12A102A00106B0000000000C12A002A10106B0000000000C12A102A00106B4000000000C12A002A10106B4000000000C12A102A00106B8000000000C12A002A10106B8000000000C12A102A00106BC000000000C12A002A10106BC000000000C12A102A00106C0000000000C12A002A10106C0000000000C12A102A00106C4000000000C12A002A10106C4000000000C12A102A00106C8000000000C12A002A10106C8000000000C12A102A00106CC000000000C12A002A10106CC000000000C12A102A00106D0000000000C12A002A10106D0000000000C12A102A00106D4000000000C12A002A10106D4000000000C12A102A00106D8000000000C12A002A10106D8000000000C12A102A00106DC000000000C12A002A10106DC000000000C12A102A00106E0000000000C12A002A10106E0000000000C12A102A00106E4000000000C12A002A10106E4000000000C12A102A00106E8000000000C12A002A10106E8000000000C12A102A00106EC000000000C12A002A10106EC000000000C12A102A00106F0000000000C12A002A10106F0000000000C12A102A00106F4000000000C12A002A10106F4000000000C12A102A00106F8000000000C12A002A10106F8000000000C12A102A00106FC000000000C12A002A10106FC000000000C12A102A0010700000000000C12A002A1010700000000000C12A102A0010704000000000C12A002A1010704000000000C12A102A0010708000000000C12A002A1010708000000000C12A102A001070C000000000C12A002A101070C000000000C12A102A0010710000000000C12A002A1010710000000000C12A102A0010714000000000C12A002A1010714000000000C12A102A0010718000000000C12A002A1010718000000000C12A102A001071C000000000C12A002A101071C000000000C12A102A0010720000000000C12A002A1010720000000000C12A102A0010724000000000C12A002A1010724000000000C12A102A0010728000000000C12A002A1010728000000000C12A102A001072C000000000C12A002A101072C000000000C12A102A0010730000000000C12A002A1010730000000000C12A102A0010734000000000C12A002A1010734000000000C12A102A0010738000000000C12A002A1010738000000000C12A102A001073C000000000C12A002A101073C000000000C12A102A0010740000000000C12A002A1010740000000000C12A102A0010744000000000C12A002A1010744000000000C12A102A0010748000000000C12A002A1010748000000000C12A102A001074C000000000C12A002A101074C000000000C12A102A0010750000000000C12A002A1010750000000000C12A102A0010754000000000C12A002A1010754000000000C12A102A0010758000000000C12A002A1010758000000000C12A102A001075C000000000C12A002A101075C000000000C12A102A0010760000000000C12A002A1010760000000000C12A102A0010764000000000C12A002A1010764000000000C12A102A0010768000000000C12A002A1010768000000000C12A102A001076C000000000C12A002A101076C000000000C12A102A0010770000000000C12A002A1010770000000000C12A102A0010774000000000C12A002A1010774000000000C12A102A0010778000000000C12A002A1010778000000000C12A102A001077C000000000C12A002A101077C000000000C12A102A0010780000000000C12A002A1010780000000000C12A102A0010784000000000C12A002A1010784000000000C12A102A0010788000000000C12A002A1010788000000000C12A102A001078C000000000C12A002A101078C000000000C12A102A0010790000000000C12A002A1010790000000000C12A102A0010794000000000C12A002A1010794000000000C12A102A0010798000000000C12A002A1010798000000000C12A102A001079C000000000C12A002A101079C000000000C12A102A00107A0000000000C12A002A10107A0000000000C12A102A00107A4000000000C12A002A10107A4000000000C12A102A00107A8000000000C12A002A10107A8000000000C12A102A00107AC000000000C12A002A10107AC000000000C12A102A00107B0000000000C12A002A10107B0000000000C12A102A00107B4000000000C12A002A10107B4000000000C12A102A00107B8000000000C12A002A10107B8000000000C12A102A00107BC000000000C12A002A10107BC000000000C12A102A00107C0000000000C12A002A10107C0000000000C12A102A00107C4000000000C12A002A10107C4000000000C12A102A00107C8000000000C12A002A10107C8000000000C12A102A00107CC000000000C12A002A10107CC000000000C12A102A00107D0000000000C12A002A10107D0000000000C12A102A00107D4000000000C12A002A10107D4000000000C12A102A00107D8000000000C12A002A10107D8000000000C12A102A00107DC000000000C12A002A10107DC000000000C12A102A00107E0000000000C12A002A10107E0000000000C12A102A00107E4000000000C12A002A10107E4000000000C12A102A00107E8000000000C12A002A10107E8000000000C12A102A00107EC000000000C12A002A10107EC000000000C12A102A00107F0000000000C12A002A10107F0000000000C12A102A00107F4000000000C12A002A10107F4000000000C12A102A00107F8000000000C12A002A10107F8000000000C12A102A00107FC000000000C12A002A10107FC000000000C12A102A0010800000000000C12A002A1010800000000000C12A102A0010804000000000C12A002A1010804000000000C12A102A0010808000000000C12A002A1010808000000000C12A102A001080C000000000C12A002A101080C000000000C12A102A0010810000000000C12A002A1010810000000000C12A102A0010814000000000C12A002A1010814000000000C12A102A0010818000000000C12A002A1010818000000000C12A102A001081C000000000C12A002A101081C000000000C12A102A0010820000000000C12A002A1010820000000000C12A102A0010824000000000C12A002A1010824000000000C12A102A0010828000000000C12A002A1010828000000000C12A102A001082C000000000C12A002A101082C000000000C12A102A0010830000000000C12A002A1010830000000000C12A102A0010834000000000C12A002A1010834000000000C12A102A0010838000000000C12A002A1010838000000000C12A102A001083C000000000C12A002A101083C000000000C12A102A0010840000000000C12A002A1010840000000000C12A102A0010844000000000C12A002A1010844000000000C12A102A0010848000000000C12A002A1010848000000000C12A102A001084C000000000C12A002A101084C000000000C12A102A0010850000000000C12A002A1010850000000000C12A102A0010854000000000C12A002A1010854000000000C12A102A0010858000000000C12A002A1010858000000000C12A102A001085C000000000C12A002A101085C000000000C12A102A0010860000000000C12A002A1010860000000000C12A102A0010864000000000C12A002A1010864000000000C12A102A0010868000000000C12A002A1010868000000000C12A102A001086C000000000C12A002A101086C000000000C12A102A0010870000000000C12A002A1010870000000000C12A102A0010874000000000C12A002A1010874000000000C12A102A0010878000000000C12A002A1010878000000000C12A102A001087C000000000C12A002A101087C000000000C12A102A0010880000000000C12A002A1010880000000000C12A102A0010884000000000C12A002A1010884000000000C12A102A0010888000000000C12A002A1010888000000000C12A102A001088C000000000C12A002A101088C000000000C12A102A0010890000000000C12A002A1010890000000000C12A102A0010894000000000C12A002A1010894000000000C12A102A0010898000000000C12A002A1010898000000000C12A102A001089C000000000C12A002A101089C000000000C12A102A00108A0000000000C12A002A10108A0000000000C12A102A00108A4000000000C12A002A10108A4000000000C12A102A00108A8000000000C12A002A10108A8000000000C12A102A00108AC000000000C12A002A10108AC000000000C12A102A00108B0000000000C12A002A10108B0000000000C12A102A00108B4000000000C12A002A10108B4000000000C12A102A00108B8000000000C12A002A10108B8000000000C12A102A00108BC000000000C12A002A10108BC000000000C12A102A00108C0000000000C12A002A10108C0000000000C12A102A00108C4000000000C12A002A10108C4000000000C12A102A00108C8000000000C12A002A10108C8000000000C12A102A00108CC000000000C12A002A10108CC000000000C12A102A00108D0000000000C12A002A10108D0000000000C12A102A00108D4000000000C12A002A10108D4000000000C12A102A00108D8000000000C12A002A10108D8000000000C12A102A00108DC000000000C12A002A10108DC000000000C12A102A00108E0000000000C12A002A10108E0000000000C12A102A00108E4000000000C12A002A10108E4000000000C12A102A00108E8000000000C12A002A10108E8000000000C12A102A00108EC000000000C12A002A10108EC000000000C12A102A00108F0000000000C12A002A10108F0000000000C12A102A00108F4000000000C12A002A10108F4000000000C12A102A00108F8000000000C12A002A10108F8000000000C12A102A00108FC000000000C12A002A10108FC000000000C12A102A0010900000000000C12A002A1010900000000000C12A102A0010904000000000C12A002A1010904000000000C12A102A0010908000000000C12A002A1010908000000000C12A102A001090C000000000C12A002A101090C000000000C12A102A0010910000000000C12A002A1010910000000000C12A102A0010914000000000C12A002A1010914000000000C12A102A0010918000000000C12A002A1010918000000000C12A102A001091C000000000C12A002A101091C000000000C12A102A0010920000000000C12A002A1010920000000000C12A102A0010924000000000C12A002A1010924000000000C12A102A0010928000000000C12A002A1010928000000000C12A102A001092C000000000C12A002A101092C000000000C12A102A0010930000000000C12A002A1010930000000000C12A102A0010934000000000C12A002A1010934000000000C12A102A0010938000000000C12A002A1010938000000000C12A102A001093C000000000C12A002A101093C000000000C12A102A0010940FFFFFFFF0C12A002A1010940FFFFFFFF0C12A102A0010944FFFFFFFF0C12A002A1010944FFFFFFFF0C12A102A0010948FFFFFFFF0C12A002A1010948FFFFFFFF0C12A102A001094CFFFFFFFF0C12A002A101094CFFFFFFFF0C12A102A0010950FFFFFFFF0C12A002A1010950FFFFFFFF0C12A102A0010954FFFFFFFF0C12A002A1010954FFFFFFFF0C12A102A0010958FFFFFFFF0C12A002A1010958FFFFFFFF0C12A102A001095CFFFFFFFF0C12A002A101095CFFFFFFFF0C12A102A0010960FFFFFFFF0C12A002A1010960FFFFFFFF0C12A102A0010964FFFFFFFF0C12A002A1010964FFFFFFFF0C12A102A0010968FFFFFFFF0C12A002A1010968FFFFFFFF0C12A102A001096CFFFFFFFF0C12A002A101096CFFFFFFFF0C12A102A0010970FFFFFFFF0C12A002A1010970FFFFFFFF0C12A102A0010974FFFFFFFF0C12A002A1010974FFFFFFFF0C12A102A0010978FFFFFFFF0C12A002A1010978FFFFFFFF0C12A102A001097CFFFFFFFF0C12A002A101097CFFFFFFFF0C12A102A0010980FFFFFFFF0C12A002A1010980FFFFFFFF0C12A102A0010984FFFFFFFF0C12A002A1010984FFFFFFFF0C12A102A0010988FFFFFFFF0C12A002A1010988FFFFFFFF0C12A102A001098CFFFFFFFF0C12A002A101098CFFFFFFFF0C12A102A0010990FFFFFFFF0C12A002A1010990FFFFFFFF0C12A102A0010994FFFFFFFF0C12A002A1010994FFFFFFFF0C12A102A0010998FFFFFFFF0C12A002A1010998FFFFFFFF0C12A102A001099CFFFFFFFF0C12A002A101099CFFFFFFFF0C12A102A00109A0FFFFFFFF0C12A002A10109A0FFFFFFFF0C12A102A00109A4FFFFFFFF0C12A002A10109A4FFFFFFFF0C12A102A00109A8FFFFFFFF0C12A002A10109A8FFFFFFFF0C12A102A00109ACFFFFFFFF0C12A002A10109ACFFFFFFFF0C12A102A00109B0FFFFFFFF0C12A002A10109B0FFFFFFFF0C12A102A00109B4FFFFFFFF0C12A002A10109B4FFFFFFFF0C12A102A00109B8FFFFFFFF0C12A002A10109B8FFFFFFFF0C12A102A00109BCFFFFFFFF0C12A002A10109BCFFFFFFFF0C12A102A00109C0FFFFFFFF0C12A002A10109C0FFFFFFFF0C12A102A00109C4FFFFFFFF0C12A002A10109C4FFFFFFFF0C12A102A00109C8FFFFFFFF0C12A002A10109C8FFFFFFFF0C12A102A00109CCFFFFFFFF0C12A002A10109CCFFFFFFFF0C12A102A00109D0008200140C12A002A10109D0008200140C12A102A00109D4001400140C12A002A10109D4001400140C12A102A00109D8001400AA0C12A002A10109D8001400AA0C12A102A00109DC0018C0710C12A002A10109DC0018C0710C12A102A00109E0000100010C12A002A10109E0000100010C12A102A00109E4FFFFFFFF0C12A002A10109E4FFFFFFFF0C12A102A00109E8FFFFFFFF0C12A002A10109E8FFFFFFFF0C12A102A00109ECFFFFFFFF0C12A002A10109ECFFFFFFFF0C12A102A00109F0FFFFFFFF0C12A002A10109F0FFFFFFFF0C12A102A00109F4FFFFFFFF0C12A002A10109F4FFFFFFFF0C12A102A00109F8FFFFFFFF0C12A002A10109F8FFFFFFFF0C12A102A00109FCFFFFFFFF0E12A002A10109FCFFFFFFFF0E12A1READ_ZS_END_CAN_NEW";
//Данные от Master ГКЛС-Е
//outMessage = "001C010200020200001C030200040200001C081005092E65001C050005060903001C078C24600000001C1000001197FE001C120000130000001C140100150100001C1677A617CBFD001C180000192100001C1A00001B4100001C1C17001B1700001C1E170018531FREAD_ZS_END_CAN_OLD";
//Данные от Master и Slave ГКЛС-Е
//outMessage = "001C011001021001041C011001021001041C031001041001041C050005060903041C100000111B58041C164FBD17E668041C1A00001B0000001C011001021001001C031001041001001C050005060903001C100000111B58001C169A5E178C4B001C1A00001B0000READ_ZS_END_CAN_OLD";
