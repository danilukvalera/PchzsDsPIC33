package com.daniluk.decoders

import android.content.Context
import com.daniluk.R
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.END_BLOCK
import com.daniluk.utils.Constants.FILE_NAME_MAP_DEVICE_CAN_OLD
import com.daniluk.utils.Constants.ID_COD_DATE_COMPIL
import com.daniluk.utils.Constants.ID_COD_DEVICE
import com.daniluk.utils.Constants.ID_COD_PROTECT_STATE
import com.daniluk.utils.Constants.ID_COD_PROTECT_STATE_NOT_ERASABLE
import com.daniluk.utils.Constants.ID_COD_VERSION_BUILD
import com.daniluk.utils.Constants.ID_COD_VERSION_PO
import com.daniluk.utils.Constants.ID_MASTER_OLD_CAN
import com.daniluk.utils.Constants.ID_SLAVE_OLD_CAN
import com.daniluk.utils.Constants.LABEL_END_TYPE_DEVICE
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.MASTER_STR
import com.daniluk.utils.Constants.NO_PROTECT_STATE_OLD_CAN
import com.daniluk.utils.Constants.SLAVE
import com.daniluk.utils.Constants.SLAVE_STR
import com.daniluk.utils.Constants.TYPE_DEFECT
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

//=====Методы дешифрации CAN_OLD====================================================================
//Код отказа в HEX (CAN_OLD)
fun getStringProtectCodeCanOld(data: List<String>, idController: Int): String {
    var codProtectState: String
    //ищем код отказа
    codProtectState = getStringParametr(data, idController, ID_COD_PROTECT_STATE)
    if (codProtectState == NO_PROTECT_STATE_OLD_CAN) {
        codProtectState = "Прибор не находится в защитном состоянии"
    }
    return codProtectState
}

//Не стираемый Код отказа в HEX (CAN_OLD)
fun getStringProtectCodeNotErasableCanOld(data: List<String>, idController: Int): String {
    return getStringParametr(data, idController, ID_COD_PROTECT_STATE_NOT_ERASABLE)
}


//код отказа декодированный (CAN_OLD)
fun getStringDecodedProtectCodeCanOld(context: Context, data: List<String>, idMicrocontroller: Int, typeCode: Int): String {
    val deviceName = getStringDeviceNameCanOld(context, data, idMicrocontroller)
    var codProtectState: String =
        when(typeCode){
            Constants.FLAG_DECODE_CODE -> "0x${getStringParametr(data, idMicrocontroller, ID_COD_PROTECT_STATE)}"
            Constants.FLAG_DECODE_NON_ERASABLE_CODE -> "0x${getStringProtectCodeNotErasableCanOld(data, idMicrocontroller)}"
            else -> ""
        }
    return decodingProtectCodeCanOld(context, codProtectState, deviceName)
}

//метод декодирования CAN_OLD
fun decodingProtectCodeCanOld(context: Context, codProtectState: String, deviceName: String): String{
    var result = ""
    var line = ""
    var subLine: String
    try {
        val inputStream = context.assets.open("$deviceName/mapCode.txt")
        val inputStreamReader = InputStreamReader(inputStream, "windows-1251")
        val bufferedReader = BufferedReader(inputStreamReader)
        //Находим начало блока "тип отказа"
        while (!line.equals(TYPE_DEFECT, ignoreCase = true)) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return "Ошибка в файле декодера"
            }
            line = line.trim { it <= ' ' }
        }
        //проходим блок "тип отказа" в поисках кода отказа
        while (true) {
            try {
                line = bufferedReader.readLine()
            } catch (e: RuntimeException) {
                return "Ошибка в файле декодера"
            }
            if (line.equals(END_BLOCK + TYPE_DEFECT, ignoreCase = true)) {
                result = "код отказа не найден"
                break
            }
            if (line.length > 8) {
                subLine = line.substring(0, 6)
                if (subLine.equals(codProtectState, ignoreCase = true)) {
                    result = line.substring(6 + 1).trim { it <= ' ' }
                    break
                }
            }
        }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        result = "Ошибка декодирования.\nФайл декодера не найден\n\n"
    } catch (e: IOException) {
        e.printStackTrace()
        result = "ошибка декодирования"
    } catch (e: java.lang.NullPointerException) {
        result = "Ссылка на нулевой указатель\n\n"
    }
    return result
}


//Имя устройства (CAN_OLD)
fun getStringDeviceNameCanOld(context: Context, data: List<String>, idMicrocontroller: Int): String {
    var result: String
    val codeDevice: String = getStringParametr(data, idMicrocontroller, ID_COD_DEVICE)

    //ищем по коду название прибора в файле mapDevice.txt
    try {
        val inputStream = context.assets.open(FILE_NAME_MAP_DEVICE_CAN_OLD)
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
            if (line.equals(LABEL_END_TYPE_DEVICE, ignoreCase = true)) {
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

//тип процессора (master slave) (CAN_OLD)
fun getStringProcessorIdCanOld(data: List<String>): String {
    return when (data.getOrElse(0){""} + data.getOrElse(1){""}) {
        ID_MASTER_OLD_CAN -> {
            MASTER_STR
        }
        ID_SLAVE_OLD_CAN -> {
            SLAVE_STR
        }
        else -> "не определен"
    }
}

//Версия ПО (CAN_OLD)
fun getStringProgramVersionCanOld(data: List<String>, idMicrocontroller: Int): String {
    //ищем номер версии ПО
    val ver = getDigitalParametr(data, idMicrocontroller, ID_COD_VERSION_PO)
    if(ver[0] != -1 && ver[1] != -1){
        return "${ver[1]}.${ver[0]} "
    }
    return ""
}

//Версия сборки ПО (CAN_OLD)
fun getStringProgramVersionBuildCanOld(data: List<String>, idMicrocontroller: Int): String {
    //ищем версию сборки ПО
    val  build = getDigitalParametr(data, idMicrocontroller, ID_COD_VERSION_BUILD)
    if(build[0] == -1 && build[1] == -1){
        return "нет данных"
    }
    return (build[1]shl(2) + build[0]).toString()
}

//дата компиляции (CAN_OLD)
fun getStringProgramDateReleaseCanOld(data: List<String>, idMicrocontroller: Int): String {
    return getStringParametr(data, idMicrocontroller, ID_COD_DATE_COMPIL)
}


//получить параметр по idCod в виде строки (CAN_OLD)
private fun getStringParametr(data: List<String>, idController: Int, idCod: String): String {
    val idProcessor = when (idController) {
        MASTER -> {
            ID_MASTER_OLD_CAN
        }
        SLAVE -> {
            ID_SLAVE_OLD_CAN
        }
        else -> return ""
    }
    var index = 0
    while (index < data.size) {
        if (data.getOrElse(index){""} + data.getOrElse(index + 1){""} == idProcessor) {
            when(idCod){
                data.getOrElse(index + 2) { "" } -> {
                    return data.getOrElse(index + 4) { "" } + data.getOrElse(index + 3) { "" }
                }
                data.getOrElse(index + 5) { "" } -> {
                    return data.getOrElse(index + 7) { "" } + data.getOrElse(index + 6) { "" }
                }
            }
        }
        index += 8
    }
    return ""
}

//получить параметр по idCod в виде массива из двух байт [0]-младший байт [1]-старший байт
private fun getDigitalParametr(data: List<String>, idController: Int, idCod: String): List<Int> {
    val result = MutableList(2){-1}
    val idProcessor = when (idController) {
        MASTER -> {
            ID_MASTER_OLD_CAN
        }
        SLAVE -> {
            ID_SLAVE_OLD_CAN
        }
        else -> return result
    }
    var index = 0
    while (index < data.size) {
        if (data.getOrElse(index){""} + data.getOrElse(index + 1){""} == idProcessor) {
            when(idCod){
                data.getOrElse(index + 2) { "" } -> {
                    val hByte = data.getOrElse(index + 4) { "" }.toIntOrNull()
                    result[1] = hByte ?: -1
                    val lByte = data.getOrElse(index + 3) { "" }.toIntOrNull()
                    result[0] = lByte ?: -1
                }
                data.getOrElse(index + 5) { "" } -> {
                    val hByte = data.getOrElse(index + 7) { "" }.toIntOrNull()
                    result[1] = hByte ?: -1
                    val lByte = data.getOrElse(index + 6) { "" }.toIntOrNull()
                    result[0] = lByte ?: -1
                }
            }
        }
        index += 8
    }
    return result

}



