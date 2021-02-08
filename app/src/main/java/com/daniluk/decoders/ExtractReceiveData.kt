package com.daniluk.decoders

import com.daniluk.MainViewModel
import com.daniluk.utils.Constants.CAN_NEW
import com.daniluk.utils.Constants.CAN_OLD
import com.daniluk.utils.Constants.GP3S
import com.daniluk.utils.Constants.ID_MASTER_NEW_CAN
import com.daniluk.utils.Constants.ID_MASTER_OLD_CAN
import com.daniluk.utils.Constants.ID_SLAVE_NEW_CAN
import com.daniluk.utils.Constants.ID_SLAVE_OLD_CAN
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.PP3S
import com.daniluk.utils.Constants.READ_ZS_END
import com.daniluk.utils.Constants.SLAVE
import com.daniluk.utils.byteToStrHex
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

//Извлечение данных из принятого сообщения от ПЧЗС (возвращает список байт в виде строк или пустой список)
fun extractData(device: Int): List<String> {
    val viewModel: MainViewModel = MainViewModel.instansViewModel
    val lengthReceiveData = viewModel.responsePchzs.indexOf(READ_ZS_END)
    when {
        //если данные приняты от устройств с новым CAN
        viewModel.responsePchzs.indexOf(CAN_NEW, lengthReceiveData) > 0 -> return extractDataCanNew(
            device
        )
        //если данные приняты от устройств с старым CAN
        viewModel.responsePchzs.indexOf(CAN_OLD, lengthReceiveData) > 0 -> return extractDataCanOld(
            device
        )
        //если данные приняты от ПП3С
        viewModel.responsePchzs.indexOf(PP3S, lengthReceiveData) > 0 -> return extractDataPp3s(
            device
        )
        //если данные приняты от ГП3С
        viewModel.responsePchzs.indexOf(GP3S, lengthReceiveData) > 0 -> return extractDataGp3s(
            device
        )

        else -> return mutableListOf()
    }
}

//извлечение данных из принятого сообщения CAN_NEW (возвращает список байт в виде строк или пустой список)
fun extractDataCanNew(device: Int): List<String> {
    var index: Int
    var indexEndPacket: Int
    var adressEEPROM: Int
    val viewModel: MainViewModel = MainViewModel.instansViewModel

    val idDevice = when (device) {
        MASTER -> {
            ID_MASTER_NEW_CAN
        }
        SLAVE -> {
            ID_SLAVE_NEW_CAN
        }
        else -> return mutableListOf()
    }
    //признак конца пакета от одного процессора: адрес + 0х1000
    val idDeviceAdded = (idDevice.toInt(16) + 0x1000).toString(16)
        .toUpperCase(Locale.getDefault())
    //длина принятых данных
    val lengthReceiveData = viewModel.responsePchzs.indexOf(READ_ZS_END)

    //определение размера необходимого массива
    //находим последний полный пакет с нужным адресом, в нем номер последней переданной ячейки EEPROM
    var m: Int = lengthReceiveData - 4
    do {
        do {
            indexEndPacket = viewModel.responsePchzs.indexOf(idDeviceAdded, m)
            m--
        } while (indexEndPacket < 0 && m > 0)
        index = indexEndPacket - 20 //указывает на первый символ адреса пакета
        if (index < 0) {
            return mutableListOf()
        }
        m--
    } while (viewModel.responsePchzs.substring(index, index + 4) != idDevice && m > 0)

    val sizeArray: Int = viewModel.responsePchzs.substring(index + 6, index + 10).toInt(16) + 4
    if (sizeArray < 4) {
        return mutableListOf()
    }
    //создать массив eeProm и заполнить значениями FF
    val eeProm = MutableList(sizeArray) { "FF" }

    //заполнение массива принятыми данными
    index = 0
    while (index < lengthReceiveData) {
        //ищем признак конца пакета от конкретного микроконтроллера (master или slave)
        indexEndPacket = viewModel.responsePchzs.indexOf(idDeviceAdded, index)
        if (indexEndPacket == -1) {
            break
        }
        index = indexEndPacket - 20 //указывает на первый символ адреса пакета

        //проверка длины пакета: наличие впереди пакета адреса микроконтроллера.
        //между адресом и признаком конца пакета находятся четыре 16-ти разрядных слов данных
        if (viewModel.responsePchzs.substring(index, index + 4) != idDevice) {
            index = indexEndPacket + 1
            continue  //если нет адреса в нужном месте, пропускаем и в начало цикла
        }
        //запись в массив eeProm
        adressEEPROM = viewModel.responsePchzs.substring(index + 6, index + 10).toInt(16)
        eeProm[adressEEPROM] = viewModel.responsePchzs.substring(index + 10, index + 12)
        eeProm[adressEEPROM + 1] = viewModel.responsePchzs.substring(index + 12, index + 14)
        eeProm[adressEEPROM + 2] = viewModel.responsePchzs.substring(index + 14, index + 16)
        eeProm[adressEEPROM + 3] = viewModel.responsePchzs.substring(index + 16, index + 18)
        index = indexEndPacket + 1
    }
    // добавить строку признака от какого устройства приняты данные
    eeProm.add(CAN_NEW)
    return eeProm
}

//извлечение данных из принятого сообщения CAN_OLD (возвращает список байт в виде строк или пустой список)
fun extractDataCanOld(device: Int): List<String> {
    val viewModel: MainViewModel = MainViewModel.instansViewModel

    val idDevice = when (device) {
        MASTER -> {
            ID_MASTER_OLD_CAN
        }
        SLAVE -> {
            ID_SLAVE_OLD_CAN
        }
        else -> return mutableListOf()
    }

    val listDataDevice = mutableListOf<String>()
    var index = 0
    while (index < viewModel.responsePchzs.length - 15) {
        index = viewModel.responsePchzs.indexOf(idDevice, index)
        if (index == -1) {
            break
        }
        val tempString = viewModel.responsePchzs.subSequence(index, index + 16)
        for (i in 0 until tempString.length step 2) {
            listDataDevice.add(tempString.substring(i, i + 2))
        }
        index += 16
    }
    // добавить индекс признака от какого устройства приняты данные
        listDataDevice.add(CAN_OLD)

    return listDataDevice
}

//извлечение данных из принятого сообщения PP3S (возвращает список байт в виде строк или пустой список)
fun extractDataPp3s(device: Int): List<String> {
    TODO("Not yet implemented")
}

//извлечение данных из принятого сообщения GP3S (возвращает список байт в виде строк или пустой список)
fun extractDataGp3s(device: Int): List<String> {
    TODO("Not yet implemented")
}

//преобразование массива данных из сохраненного файла в список строк
fun arrayByteToListString(data: ByteArray): List<String> {
    var typeDevice = ""
    val list = mutableListOf<String>()
    try {
        when {
            String(
                data, data.size - CAN_NEW.length,
                CAN_NEW.length, Charset.forName("windows-1251")
            ).trim { it <= ' ' } == CAN_NEW -> typeDevice = CAN_NEW

            String(
                data, data.size - CAN_OLD.length,
                CAN_OLD.length, Charset.forName("windows-1251")
            ).trim { it <= ' ' } == CAN_OLD -> typeDevice = CAN_OLD

            String(
                data, data.size - GP3S.length,
                GP3S.length, Charset.forName("windows-1251")
            ).trim { it <= ' ' } == GP3S -> typeDevice = GP3S

            String(
                data, data.size - PP3S.length,
                PP3S.length, Charset.forName("windows-1251")
            ).trim { it <= ' ' } == PP3S -> typeDevice = PP3S
        }
        for (i in 0 until data.size - typeDevice.length) {
            //list.add(data[i].toUByte().toString(16))
            list.add(byteToStrHex(data[i]))
        }
        list.add(typeDevice)
        return list

    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        return listOf()
    }
    return listOf()
}


