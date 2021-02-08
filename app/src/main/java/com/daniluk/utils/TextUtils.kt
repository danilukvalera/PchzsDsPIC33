package com.daniluk.utils

import java.util.*

//***ОБЩЕЕ****************************************************************
//один байт в строку в HEX
fun byteToStrHex(i: Byte): String {
    val absI: Int = (i.toInt()).and(0x000000FF)
    var rezult = absI.toString(16).toUpperCase(Locale.getDefault())
    if (rezult.length == 1) {
        rezult = "0$rezult"
    }
    return rezult
}

//word (два байта) в строку в HEX
fun wordToStrHex(i: Int): String? {
    var result = i.toString(16).toUpperCase(Locale.getDefault())
    while (result.length != 4) {
        result = "0$result"
    }
    return result
}

//массив байт данных в строку HEX символов
fun ArrayByteToString(data: ByteArray, attribute: String): String? {
    var dataCode = ""
    for (i in 0 until data.size - attribute.length) {
        dataCode += byteToStrHex(data[i])
    }
    return dataCode
}

//Форматирование текста для окна сообщений о коде отказа в MainActivity
fun formattingTextForMessengeProtectCode(protectCode: String, protectCodeNotErasable: String): String {
    return """
             ${String.format("%-24s", "Код отказа:")}$protectCode
             ${String.format("%-24s", "Не стираемый код отказа:")}$protectCodeNotErasable
             
             """.trimIndent()
}

