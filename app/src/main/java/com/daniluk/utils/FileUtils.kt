package com.daniluk.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daniluk.decoders.getManufacturersNumberCanNew
import com.daniluk.decoders.getStringDeviceNameCanNew
import com.daniluk.decoders.getStringDeviceNameCanOld
import com.daniluk.utils.Constants.CAN_NEW
import com.daniluk.utils.Constants.CAN_OLD
import com.daniluk.utils.Constants.GP3S
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.NAME_FILE_MASTER_HEX
import com.daniluk.utils.Constants.NAME_FILE_SLAVE_HEX
import com.daniluk.utils.Constants.PP3S
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

const val PERMISSION_REQUEST_CODE_WRITE_READ_EXTERNAL_STORAGE = 301
const val PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE = 302
const val REQUEST_CODE_SELECTION_FILE_TO_READ = 303
const val REQUEST_CODE_SELECTION_FILE_TO_SEND = 304
var uri: Uri? = null


/**
 * Запись массива data в файл nameFile в папку nameDirectory внутреннего хранилища
 */
suspend fun saveFile(
    data: List<String>,
    idProcessor: Int,
//    nameDirectory: String,
    context: Context
): String {
    var patch = ""
//    withContext(Dispatchers.IO) {
    if (data.isEmpty()) {
//            return@withContext
        return patch
    }
    var nameFile: String
    var nameController =
        if (idProcessor == MASTER) {
            NAME_FILE_MASTER_HEX
        } else {
            NAME_FILE_SLAVE_HEX
        }

    // проверяем доступность SD
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
        Toast.makeText(context, "SD-карта не доступна", Toast.LENGTH_SHORT).show()
//            return@withContext
        return patch
    }
    //получить имя устройства
    var deviceName = ""
    when (data.lastOrNull()) {
        CAN_NEW -> {
            deviceName = getStringDeviceNameCanNew(context, data)
        }
        CAN_OLD -> {
            deviceName = getStringDeviceNameCanOld(context, data, idProcessor)
        }
        PP3S -> {
        }
        GP3S -> {
        }
        else -> {
//                return@withContext
            return patch
        }
    }
    //deviceName += "_"


    // получаем путь к SD
    var sdPath = Environment.getExternalStorageDirectory()


    // добавляем свой каталог к пути
    sdPath = File(sdPath.absolutePath + "/" + Constants.NAME_DIRECTORY)
    // создаем каталог
    if (!sdPath.isDirectory) {
        sdPath.mkdirs()
    }

    // получаем строку пути
    val path = sdPath.absolutePath

    //получить заводской номер
    val number = getManufacturersNumberCanNew(context, deviceName, data)
    // получаем дату и время и добавляем к названию файла
    val calendar = Calendar.getInstance()
    val day = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
    val month = SimpleDateFormat("MM", Locale.getDefault()).format(calendar.time)
    val year = SimpleDateFormat("yy", Locale.getDefault()).format(calendar.time)
    val hours = SimpleDateFormat("HH", Locale.getDefault()).format(calendar.time)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)
    nameFile = deviceName + "_" + number + "_" + day + month + year + "_" + hours + minutes + "_" + nameController

    //добавляем зав номер к имени
    deviceName += "_" + number

    // получаем полный путь к файлу (добавляем директорию и имя файла)
    nameFile = "$path/$nameFile.hex"
    //преобразовать строки в байты
    val typeDevice = data.last()    //тип устройства (CAN_NEW, CAN_OLD, PP3S, GP3S)
    data.toMutableList().remove(data.last())
    val dataByte = mutableListOf<Byte>()
    for (i in 0 until data.size - 1) {
        dataByte.add(data[i].toInt(16).toByte())
    }
    typeDevice.forEach {
        dataByte.add(it.toByte())
    }
    try {
        val fileOutputStream = FileOutputStream(nameFile)
        fileOutputStream.write(dataByte.toByteArray())
        fileOutputStream.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
//            return@withContext
        return patch
    } catch (e: IOException) {
        e.printStackTrace()
        //return@withContext
        return patch
    }
    patch = nameFile
//    }

    return patch
}

/**
 * Чтение файла nameFile из внутреннего хранилища после выбора в проводнике
 */
suspend fun readFile(
    uriFile: Uri?,
    contentResolver: ContentResolver,
    context: Context?
): ByteArray {
    var data = ByteArray(0)
    if (uriFile == null) {
        return data
    }
    //Запоминаем uri чтобы считать файл в onActivityResult после выдачи разрешения
    uri = uriFile
    //Запрос разрешения
    if (!requestReadStorage(context)) {
        return data
    }
    var fileInputStream: FileInputStream? = null
    try {
        fileInputStream = contentResolver.openInputStream(uriFile) as FileInputStream?
        if (fileInputStream != null) {
            data = ByteArray(fileInputStream.available())
            fileInputStream.read(data)
        }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return data
    } catch (e: IOException) {
        e.printStackTrace()
        return data
    } finally {
        if (fileInputStream != null) {
            try {
                fileInputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return data
}

/**
 * Чтение файла из защищенной папки программы
 */
fun readFile(nameFile: String?, context: Context): ByteArray? {
    var fileInputStream: FileInputStream? = null
    return try {
        fileInputStream = context.openFileInput(nameFile)
        val data = ByteArray(fileInputStream.available())
        fileInputStream.read(data)
        data
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        if (fileInputStream != null) {
            try {
                fileInputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Запрос разрешения чтения файлов
 */
fun requestReadStorage(context: Context?): Boolean {
    val permissionStatus =
        ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
    //если разрешения нет, запрос на получение разрешения
    //ответ на запрос обрабатывается в функции onRequestPermissionsResult()
    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            (context as Activity?)!!,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE
        )
        return false
    }
    return true
}


