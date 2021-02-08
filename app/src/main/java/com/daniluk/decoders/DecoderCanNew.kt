package com.daniluk.decoders

import android.content.Context
import com.daniluk.R
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.END_BLOCK
import com.daniluk.utils.Constants.FLAG_DECODE_CODE
import com.daniluk.utils.Constants.FLAG_DECODE_NON_ERASABLE_CODE
import com.daniluk.utils.Constants.MASTER_STR
import com.daniluk.utils.Constants.NO_PROTECT_STATE
import com.daniluk.utils.Constants.SLAVE_STR
import com.daniluk.utils.Constants.TYPE_DEFECT
import com.daniluk.utils.Constants.adressCodProtectState1
import com.daniluk.utils.Constants.adressCodProtectState4
import com.daniluk.utils.Constants.defectFileNameEndAdress
import com.daniluk.utils.Constants.defectFileNameStartAdress
import com.daniluk.utils.Constants.defectStringNumberAdress
import com.daniluk.utils.Constants.deviceNameEndAdress
import com.daniluk.utils.Constants.deviceNameStartAdress
import com.daniluk.utils.Constants.pocessorIdStartAdress
import com.daniluk.utils.Constants.programDateReleaseEndAdress
import com.daniluk.utils.Constants.programDateReleaseStartAdress
import com.daniluk.utils.Constants.programVersionEndAdress
import com.daniluk.utils.Constants.programVersionStartAdress
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import kotlin.experimental.and

//Код отказа в HEX (CAN_NEW)
fun getStringProtectCodeCanNew(data: List<String>): String {
    var codProtectState1: String = data.getOrElse(adressCodProtectState1) { "" } + data.getOrElse(
        adressCodProtectState1 + 1
    ) { "" }
    if (codProtectState1 == NO_PROTECT_STATE) {
        codProtectState1 = "Прибор не находится в защитном состоянии"
    }
    return codProtectState1
}

//Не стираемый код отказа в HEX (CAN_NEW)
fun getStringProtectCodeNotErasableCanNew(data: List<String>): String {
    return data.getOrElse(adressCodProtectState4) { "" } + data.getOrElse(adressCodProtectState4 + 1) { "" }
}

//=====Методы дешифрации CAN_NEW====================================================================
//номер строки в файле где произошел отказ (CAN_NEW)
fun getStringDefectStringNumberCanNew(data: List<String>): String {
    val str = data.getOrElse(defectStringNumberAdress) { "" } +
            data.getOrElse(defectStringNumberAdress + 1) { "" }
    return str.toInt(16).toString()
}

//имя файла где произошел отказ (CAN_NEW)
fun getStringDefectFileNameCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, defectFileNameStartAdress, defectFileNameEndAdress)
}

//код отказа декодированный (CAN_NEW)
fun getStringDecodedProtectCodeCanNew(context: Context, data: List<String>, typeCode: Int): String {
    val codProtectState: String =
        when (typeCode) {
            FLAG_DECODE_CODE -> data.getOrElse(adressCodProtectState1) { "" } +
                    data.getOrElse(adressCodProtectState1 + 1) { "" }
            FLAG_DECODE_NON_ERASABLE_CODE -> data.getOrElse(adressCodProtectState4) { "" } +
                    data.getOrElse(adressCodProtectState4 + 1) { "" }
            else -> ""
        }
    val deviceName = getStringDeviceNameCanNew(data)
    return decodingProtectCodeCanNew(context, codProtectState, deviceName)
}

//метод декодирования CAN_NEW
fun decodingProtectCodeCanNew(context: Context, codProtectState: String, deviceName: String): String {

    var result: String
    var protectStateHighbyte = "0x" + codProtectState.substring(0, 2)
    var protectStateLowbyte = "0x" + codProtectState.substring(2)
    var line = ""
    var subLine: String
    try {
        val inputStream = context.assets.open(deviceName + "_mapCode.txt")
        val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        val bufferedReader = BufferedReader(inputStreamReader)
        //Находим начало блока "тип отказа"
        while (!line.equals(TYPE_DEFECT, ignoreCase = true)) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                throw FileNotFoundException()
            }
            line = line.trim { it <= ' ' }
        }
        //проходим блок "тип отказа" в поисках старшего байта кода отказа
        while (true) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return context.getString(R.string.ERROR_FILE_DECODER)
            }
            if (line.equals(END_BLOCK + TYPE_DEFECT, ignoreCase = true)) {
                result = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                return result
            }
            if (line.length > 6) {
                subLine = line.substring(0, 4)
                if (subLine.equals(protectStateHighbyte, ignoreCase = true)) {
                    protectStateHighbyte = line.substring(4 + 1).trim { it <= ' ' }
                    break
                }
            }
        }
        //Если мы здесь значит код отказа найден и начинаем поиск расшифровки младшего байта кода отказа
        //protectStateHighbyte содержит название блока компонента
        while (!line.equals(protectStateHighbyte, ignoreCase = true)) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return "Тип отказа: $protectStateHighbyte\nКод отказа: не декодирован"
            }
            line = line.trim { it <= ' ' }
        }
        //проходим блок с отказом в поисках младшего байта кода отказа
        while (true) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return context.getString(R.string.ERROR_FILE_DECODER)
            }
            if (line.equals(END_BLOCK + protectStateLowbyte, ignoreCase = true)) {
                result = context.getString(R.string.FAILURE_CODE_NOT_FOUND)
                return result
            }
            if (line.length > 6) {
                subLine = line.substring(0, 4)
                if (subLine.equals(protectStateLowbyte, ignoreCase = true)) {
                    protectStateLowbyte = line.substring(4 + 1).trim { it <= ' ' }
                    break
                }
            }
        }
        result = "$protectStateHighbyte\n$protectStateLowbyte"
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        result = context.getString(R.string.FILE_DECODER_NOT_FOUND)
    } catch (e: IOException) {
        e.printStackTrace()
        result = context.getString(R.string.ERROR_READ_FILE)
    } catch (e: NullPointerException) {
        result = context.getString(R.string.REFERENCE_NULL_POINTER)
    }
    return result

}


// Имя устройства (CAN_NEW)
fun getStringDeviceNameCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, deviceNameStartAdress, deviceNameEndAdress)
}

//ID процессора (CAN_NEW)
fun getStringProcessorIdCanNew(data: List<String>): String {
    val id = data[pocessorIdStartAdress + 1].toInt(16).toByte().and(1).toInt()
    return when (id) {
        0 -> SLAVE_STR
        1 -> MASTER_STR
        else -> "NO_ID"
    }
}

//Версия ПО (CAN_NEW)
fun getStringProgramVersionCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, programVersionStartAdress, programVersionEndAdress)
}

//Дата релиза ПО (CAN_NEW)
fun getStringProgramDateReleaseCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, programDateReleaseStartAdress, programDateReleaseEndAdress)
}

//извлечение строки
fun getStringFromArrayByte(data: List<String>, startAdress: Int, endAdress: Int): String {
    val list = mutableListOf<Byte>()
    for (i in startAdress until endAdress) {
        val byte = data[i].toInt(16).toByte()
        if (byte.toInt() == 0) {
            break
        }
        list.add(data[i].toInt(16).toByte())
    }
    return String(list.toByteArray(), Charset.forName("windows-1251"))
}


