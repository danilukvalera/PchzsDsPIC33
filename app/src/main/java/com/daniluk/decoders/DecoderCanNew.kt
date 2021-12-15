package com.daniluk.decoders

import android.content.Context
import com.daniluk.R
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.END_BLOCK
import com.daniluk.utils.Constants.FILE_IS_FUN_DEVICE_NUMBER
import com.daniluk.utils.Constants.FLAG_DECODE_CODE
import com.daniluk.utils.Constants.FLAG_DECODE_NON_ERASABLE_CODE
import com.daniluk.utils.Constants.MANUFACTURERS_NUMBER_START_ADDRESS
import com.daniluk.utils.Constants.MASTER_STR
import com.daniluk.utils.Constants.NO_PROTECT_STATE
import com.daniluk.utils.Constants.SLAVE_STR
import com.daniluk.utils.Constants.TYPE_DEFECT
import com.daniluk.utils.Constants.addressCodProtectState1
import com.daniluk.utils.Constants.addressCodProtectState4
import com.daniluk.utils.Constants.addressParam1
import com.daniluk.utils.Constants.addressParam2
import com.daniluk.utils.Constants.addressParam3
import com.daniluk.utils.Constants.addressParam4
import com.daniluk.utils.Constants.defectFileNameEndAddress
import com.daniluk.utils.Constants.defectFileNameStartAddress
import com.daniluk.utils.Constants.defectStringNumberAddress
import com.daniluk.utils.Constants.deviceNameStartAddress
import com.daniluk.utils.Constants.pocessorIdStartAddress
import com.daniluk.utils.Constants.programDateReleaseEndAddress
import com.daniluk.utils.Constants.programDateReleaseStartAddress
import com.daniluk.utils.Constants.programVersionEndAddress
import com.daniluk.utils.Constants.programVersionStartAddress
import java.io.*
import java.nio.charset.Charset
import kotlin.experimental.and

//Код отказа в HEX (CAN_NEW)
fun getStringProtectCodeCanNew(data: List<String>): String {
    var codProtectState1: String = data.getOrElse(addressCodProtectState1) { "" } + data.getOrElse(
        addressCodProtectState1 + 1
    ) { "" }
    if (codProtectState1 == NO_PROTECT_STATE) {
        codProtectState1 = "Прибор не находится в защитном состоянии"
    }
    return codProtectState1
}

//Не стираемый код отказа в HEX (CAN_NEW)
fun getStringProtectCodeNotErasableCanNew(data: List<String>): String {
    return data.getOrElse(addressCodProtectState4) { "" } + data.getOrElse(addressCodProtectState4 + 1) { "" }
}

//=====Методы дешифрации CAN_NEW====================================================================
//номер строки в файле где произошел отказ (CAN_NEW)
fun getStringDefectStringNumberCanNew(data: List<String>): String {
    val str = data.getOrElse(defectStringNumberAddress) { "" } +
            data.getOrElse(defectStringNumberAddress + 1) { "" }
    return str.toInt(16).toString()
}

//имя файла где произошел отказ (CAN_NEW)
fun getStringDefectFileNameCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, defectFileNameStartAddress, defectFileNameEndAddress)
}

//код отказа декодированный (CAN_NEW)
fun getStringDecodedProtectCodeCanNew(context: Context, data: List<String>, typeCode: Int): String {
    val codProtectState: String =
        when (typeCode) {
            FLAG_DECODE_CODE -> data.getOrElse(addressCodProtectState1) { "" } +
                    data.getOrElse(addressCodProtectState1 + 1) { "" }
            FLAG_DECODE_NON_ERASABLE_CODE -> data.getOrElse(addressCodProtectState4) { "" } +
                    data.getOrElse(addressCodProtectState4 + 1) { "" }
            else -> ""
        }
    val deviceName = getStringDeviceNameCanNew(context, data)
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
    var textProtectStateHighbyte = ""
    var textProtectStateLowbyte = ""

    try {
        //var inputStream = context.assets.open(deviceName + "_mapCode.txt")
        var inputStream = context.assets.open("$deviceName/mapCode.txt")

        var inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        var bufferedReader = BufferedReader(inputStreamReader)

        //переход к блоку кодов соответствующему protectStateHighbyte
        fun getBlockHighbyte(): Boolean {
            while (!line.equals("##$protectStateHighbyte", ignoreCase = true)) {
                try {
                    line = bufferedReader.readLine()
                } catch (e: RuntimeException) {
                    return false
                }
                line = line.trim { it <= ' ' }
            }
            return true
        }

        //декодирование младшего байта
        fun decoderLowbyte(): Boolean{
            //проходим блок с отказом в поисках младшего байта кода отказа
            while (true) {
                try {
                    line = bufferedReader.readLine().trim { it <= ' ' }
                } catch (e: RuntimeException) {
                    textProtectStateLowbyte = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                    return false
                }
                //Если дошли до конца блока и отказ не найден
                //line.replace(Regex("[\\s]{2,}"), " ") - оставляет только по одному пробелу между словами
                if (line.replace(Regex("[\\s]{2,}"), " ").equals("##" + END_BLOCK + protectStateHighbyte, ignoreCase = true)){
                    textProtectStateLowbyte = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                    return false
                }
                //извлекаем строку расшифровки младшего байта
                if (line.length > 6) {
                    subLine = line.substring(0, 4)
                    if (subLine.equals(protectStateLowbyte, ignoreCase = true)) {
                        textProtectStateLowbyte = line.substring(4 + 1).trim { it <= ' ' }
                        break
                    }
                }
            }
            return true
        }

        //Находим начало блока "тип отказа"
        while (! line.replace(Regex("[\\s]{2,}"), " ").equals(TYPE_DEFECT, ignoreCase = true)) {
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
                line = bufferedReader.readLine().trim { it <= ' ' }
            } catch (e: RuntimeException) {
                return context.getString(R.string.ERROR_FILE_DECODER)
            }
            if (line.replace(Regex("[\\s]{2,}"), " ").equals(END_BLOCK + TYPE_DEFECT, ignoreCase = true)) {
                result = context.getString(R.string.FAILURE_TYPE_NOT_FOUND)
                return result
            }
            if (line.length > 6) {
                subLine = line.substring(0, 4)
                if (subLine.equals(protectStateHighbyte, ignoreCase = true)) {
                    textProtectStateHighbyte = line.substring(4 + 1).trim { it <= ' ' }
                    break
                }
            }
        }

        //Если мы здесь значит код отказа найден и начинаем поиск расшифровки младшего байта кода отказа
        if(getBlockHighbyte() && decoderLowbyte()){

        }else{
            bufferedReader.close()
            inputStream = context.assets.open("$deviceName/mapCode.txt")
            inputStreamReader = InputStreamReader(inputStream, "windows-1251")
            bufferedReader = BufferedReader(inputStreamReader)

            protectStateHighbyte = "0x00"
            if(getBlockHighbyte()){
                decoderLowbyte()
            }
        }




        //result = "$protectStateHighbyte\textProtectStateLowbyte"
        result = "$textProtectStateHighbyte\n$textProtectStateLowbyte"

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

    var extraOptions = java.lang.StringBuilder()
    val param1 = data.getOrElse(addressParam1) { "" } + data.getOrElse(addressParam1 + 1) { "" }
    val param2 = data.getOrElse(addressParam2) { "" } + data.getOrElse(addressParam2 + 1) { "" }
    val param3 = data.getOrElse(addressParam3) { "" } + data.getOrElse(addressParam3 + 1) { "" }
    val param4 = data.getOrElse(addressParam4) { "" } + data.getOrElse(addressParam4 + 1) { "" }

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
            extraOptions.append("\n\nВнимание!!!\nФайл InterChannelId.h отсутствует для данного устройства\n")
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
        }
        //line содержит название идентификатора
        extraOptions.append( """
                                |
                                |Идентификатор - 0x$param1
                                |$line
                                |Значение в своем канале - 0x$param4
                                |Значение в соседнем канале - 0x$param3
                               """.trimMargin()
        )

    } else {
        extraOptions.append("\nОшибка ПО\nИдентификатор  - 0x$param1")
    }
    return extraOptions.toString()
}

//декодер черного ящика
fun decodeBlackBox(
    context: Context,
    data: List<String>
): String {

    //val tableParametrs = mutableListOf<List<String>>()
    val deviceName = getStringDeviceNameCanNew(context, data)
    val result = StringBuilder("\n")

    //Заполнить tableParametrs из файла mapBlackBox.txt
    var line: String = ""
    try {
        val inputStream = context.assets.open("$deviceName/mapBlackBox.txt")
        val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        val bufferedReader = BufferedReader(inputStreamReader)
        bufferedReader.use {
            for (line in it.readLines()) {
                //Пропустить пустую линию и комментарии
                if (line.isEmpty() || line.trim().substring(0, 2) == "//") {
                    continue
                }
                //Вывести заголовок параметров
                if (line.trim().substring(0, 2) == "##") {
                    val title = line.trim().substring(2)
                    //val boldTitle = "<b>$title</b>"
                    //val boldTitle = "<i><b>$title</b></i>"
                    //val boldTitle = "<strong>$title</strong>"
                    //val boldTitle = "<h1><font color=\"red\">$title</font></h1>"
                    val boldTitle = "<u><i><b><font color=\"red\" font-size=\"24sp\">$title</font></b></i></u>"
                    result.append(boldTitle)
                    result.append("<br>")
                    continue
                }
                //Дешифратор параметра
                val param = line.split("%%").toMutableList()
                val paramAddress = param.getOrNull(0)?.substring(2)?.trim()?.toIntOrNull(16) ?: continue
                val paramBitL = param.getOrNull(1)?.trim()?.toIntOrNull()
                val paramBitH = param.getOrNull(2)?.trim()?.toIntOrNull()
                val paramKeyValue = param.getOrNull(3)?.trim()?.toIntOrNull()
                val paramNotation = param.getOrNull(4)?.trim()?.toIntOrNull()
                val paramName = param.getOrNull(5)?.trim() ?: continue
                //если в параметре отсутсвует ключ для дешифрации
                if(paramKeyValue == null){
                    val dataInt = (data[paramAddress] + data[paramAddress + 1]).toIntOrNull(16) ?: continue
                    val paramValue:Int =
                        if (paramBitL == null || paramBitH == null){
                            dataInt
                        }else if (paramBitL == paramBitH ) {
                            (dataInt ushr paramBitL) and 1
                        }else{
                            var mask = 1
                            for (i in 0 until (paramBitH - paramBitL) ) {
                                mask = mask shl 1
                                mask++
                            }
                            (dataInt ushr paramBitL) and mask
                        }

                    result.append("${paramName} ")
                    result.append("<font color=\"green\">${
                        if (paramNotation == 16){
                            val builder = java.lang.StringBuilder()
                            builder.append("0x")
                            val strValue = paramValue.toString(16).toUpperCase()
                            var len = 4
                            if (paramBitL != null && paramBitH != null && paramBitH >= paramBitL) {
                                len = (paramBitH - paramBitL + 1)/4 
                            }
                            
                            
                            for (i in strValue.length until len) {
                                builder.append("0")
                            }
                            builder.append(strValue)
                            builder.toString()
                        }else{
                            paramValue.toString()
                        }
                    }</font>")
                    result.append("<br>")
                    result.append("<br>")
                    //если в параметре имеется ключ для дешифрации
                }else{
                    val paramValue:Int = (data[paramAddress] + data[paramAddress + 1]).toIntOrNull(16) ?: continue
                    if (paramValue == paramKeyValue){
                        result.append(paramName)
                        result.append("<br>")
                        result.append("<br>")
                    }
                }
            }
        }

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }

    //Заполняем список результатов декодирования черного ящика
    //val result = StringBuilder("\n")
    return result.toString()
}


// Имя устройства (CAN_NEW)
fun getStringDeviceNameCanNew(context: Context, data: List<String>): String {
    //return getStringFromArrayByte(data, deviceNameStartAdress, deviceNameEndAdress)
    var result: String
    val codeDevice: String = data[deviceNameStartAddress] + data[deviceNameStartAddress + 1]

    //ищем по коду название прибора в файле mapDevice.txt
    try {
        val inputStream = context.assets.open(Constants.FILE_NAME_MAP_DEVICE)
        val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        val bufferedReader = BufferedReader(inputStreamReader)
        //проходим блок "тип прибора" в поисках имени устройства
        var line: String?
        while (true) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return "Ошибка в файле декодера"
            }
            line = line.trim { it <= ' ' }
            if (line.equals(Constants.LABEL_END_TYPE_DEVICE, ignoreCase = true)) {
                result = context.getString(R.string.DEVICE_TYPE_NOT_DEFINED)
                return result
            }
            if (line.length > 9) {
                if (line.substring(0, 6).equals("0x$codeDevice", ignoreCase = true)) {
                    result = line.substring(8).trim { it <= ' ' }
                    break
                }
            }
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

//ID процессора (CAN_NEW)
fun getStringProcessorIdCanNew(data: List<String>): String {
    val id = data[pocessorIdStartAddress + 1].toInt(16).toByte().and(1).toInt()
    return when (id) {
        0 -> SLAVE_STR
        1 -> MASTER_STR
        else -> "NO_ID"
    }
}

//Версия ПО (CAN_NEW)
fun getStringProgramVersionCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, programVersionStartAddress, programVersionEndAddress)
}

//Дата релиза ПО (CAN_NEW)
fun getStringProgramDateReleaseCanNew(data: List<String>): String {
    return getStringFromArrayByte(data, programDateReleaseStartAddress, programDateReleaseEndAddress)
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
    val str = String(list.toByteArray(), Charset.forName("windows-1251"))
    //val matchedResults = Regex("""[A-Za-z0-9-_.,*]""").findAll(str)
    val matchedResults = Regex(
        """
    [A-Za-z0-9 \[\]\.\-\?\\,_!@#$%:;&*(){}+=/]
    """.trimIndent()
    ).findAll(str)
    val result = StringBuilder()
    for (matchedText in matchedResults){
        result.append(matchedText.value)
    }
    return result.toString()
}

//извлечение строки
fun getStringFromArrayByte_1111111111111(data: List<String>, startAdress: Int, endAdress: Int): String {
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

//Получить заводской номер
fun getManufacturersNumberCanNew(context: Context, deviceName: String, data: List<String>): String {
    return getStringFromArrayByte(data, MANUFACTURERS_NUMBER_START_ADDRESS, MANUFACTURERS_NUMBER_START_ADDRESS + 20)
}



