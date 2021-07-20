package com.daniluk.decoders

import android.content.Context
import com.daniluk.R
import com.daniluk.utils.Constants.END_BLOCK
import com.daniluk.utils.Constants.FLAG_DECODE_CODE
import com.daniluk.utils.Constants.FLAG_DECODE_NON_ERASABLE_CODE
import com.daniluk.utils.Constants.MASTER_STR
import com.daniluk.utils.Constants.NO_PROTECT_STATE
import com.daniluk.utils.Constants.SLAVE_STR
import com.daniluk.utils.Constants.TYPE_DEFECT
import com.daniluk.utils.Constants.TYPICAL_FAILURE_CODES
import com.daniluk.utils.Constants.adressCodProtectState1
import com.daniluk.utils.Constants.adressCodProtectState4
import com.daniluk.utils.Constants.adressParam1
import com.daniluk.utils.Constants.adressParam2
import com.daniluk.utils.Constants.adressParam3
import com.daniluk.utils.Constants.adressParam4
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
import java.io.*
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
    //val stringDecodingProtectCode = decodingProtectCodeCanNew(context, codProtectState, deviceName)
    return decodingProtectCodeCanNew(context, codProtectState, deviceName, data)
}

//метод декодирования CAN_NEW
fun decodingProtectCodeCanNew(
    context: Context,
    codProtectState: String,
    deviceName: String,
    data: List<String>
): String {

    var result: String
    var protectStateHighbyte = "0x" + codProtectState.substring(0, 2)
    var protectStateLowbyte = "0x" + codProtectState.substring(2)
    var line = ""
    var subLine: String
    try {
        //var inputStream = context.assets.open(deviceName + "_mapCode.txt")
        var inputStream = context.assets.open("$deviceName/mapCode.txt")

        var inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        var bufferedReader = BufferedReader(inputStreamReader)
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

        var textProtectStateHighbyte = ""
        //Если мы здесь значит код отказа найден и начинаем поиск расшифровки младшего байта кода отказа
        //Если младший байт кода отказа находится в диапазоне "Типовые коды отказов" (от 0 до 0x21 (до 33 DEC))
        val lowBate = protectStateLowbyte.substring(2, 4).toInt(16)
        if (lowBate < 0x21) {
            textProtectStateHighbyte = TYPICAL_FAILURE_CODES
        } else {
            textProtectStateHighbyte = protectStateHighbyte
        }


        while (!line.equals(textProtectStateHighbyte, ignoreCase = true)) {
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
                protectStateLowbyte = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                break
            }
            if (line.equals(END_BLOCK + textProtectStateHighbyte, ignoreCase = true)) {
                protectStateLowbyte = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                break
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

        //если "отказы компонента InterChannel.h"
        if (codProtectState.substring(0, 2).equals("02", ignoreCase = true) && !data.isEmpty()) {
            result += decodeInterChannel(context, codProtectState, deviceName, data)
        }

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

//декодер отказа типа InterChannel
fun decodeInterChannel(
    context: Context,
    codProtectState: String,
    deviceName: String,
    data: List<String>
): String {

    var extraOptions = ""
    val param1 = data.getOrElse(adressParam1) { "" } + data.getOrElse(adressParam1 + 1) { "" }
    val param2 = data.getOrElse(adressParam2) { "" } + data.getOrElse(adressParam2 + 1) { "" }
    val param3 = data.getOrElse(adressParam3) { "" } + data.getOrElse(adressParam3 + 1) { "" }
    val param4 = data.getOrElse(adressParam4) { "" } + data.getOrElse(adressParam4 + 1) { "" }

    //если младший байт = 2B или 2C
    if (codProtectState.substring(2).equals("2B", ignoreCase = true) ||
        codProtectState.substring(2).equals("2C", ignoreCase = true)
    ) {
        //Расшифровка идентификатора
        var line: String = ""
        try {
            val decId = param1.toIntOrNull(16)
            val inputStream = context.assets.open("$deviceName/InterChannelId.h")
            val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
            val bufferedReader = BufferedReader(inputStreamReader)
            //Находим начало блока typedef enum
            line = ""
            while (line.indexOf("typedef enum") < 0) {
                try {
                    line = bufferedReader.readLine()
                } catch (e: RuntimeException) {
                    throw FileNotFoundException()
                }
            }
            //Находим строку с номером идентификатора
            while (line.indexOf("#endif") < 0) {
                try {
                    line = bufferedReader.readLine()
                } catch (e: RuntimeException) {
                    throw FileNotFoundException()
                }
                var n = line.indexOf(decId.toString())
                if (n > 0) {
                    line = line.substring(n + decId.toString().length).trim(' ', ',', '/', '<', '>')
                    break
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
        }
        //line содержит название идентификатора
        extraOptions = """
                                |
                                |Идентификатор - 0x$param1
                                |$line
                                |Значение в своем канале - 0x$param4
                                |Значение в соседнем канале - 0x$param3
                               """.trimMargin()

    } else {
        extraOptions = "\nОшибка ПО\nИденификатор  - 0x$param1"
    }
    return extraOptions
}

//декодер черного ящика
fun decodeBlackBox(
    context: Context,
    data: List<String>
): String {

    val tableParametrs = mutableListOf<List<String>>()
    val deviceName = getStringDeviceNameCanNew(data)

    //Заполнить tableParametrs из файла mapBlackBox.txt
    var line: String = ""
    try {
        val inputStream = context.assets.open("$deviceName/mapBlackBox.txt")
        val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        val bufferedReader = BufferedReader(inputStreamReader)
        bufferedReader.use {
            for (line in it.readLines()) {
                if (line.isEmpty() || line.trim().substring(0, 2) == "//") {
                    continue
                }
                val param = line.split("%%").toMutableList()
                for (i in 0 until param.size){
                    param[i] = param[i].trim()
                }
                tableParametrs.add(param)
            }
        }

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
    }

    //Заполняем список результатов декодирования черного ящика
    val result = StringBuilder("\n")
    for (param in tableParametrs) {
        val paramAddress = param.getOrNull(0)?.substring(2)?.toIntOrNull(16) ?: continue
        val paramBit = param.getOrElse(1, { null })?.toIntOrNull()
        val paramName = param.getOrNull(2) ?: continue
        val paramValue:Int =
            if (paramBit == null){
                (data[paramAddress] + data[paramAddress + 1]).toIntOrNull(16) ?: continue
            }else{
                val value = (data[paramAddress] + data[paramAddress + 1]).toIntOrNull(16) ?: continue
                (value ushr paramBit) and 1
            }

        result.append("${paramName}")
        result.append("\n")
        result.append(paramValue)
        result.append("\n")
        result.append("\n")
    }

    return result.toString()
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


