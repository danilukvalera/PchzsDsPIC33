package com.daniluk.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.decoders.*
import com.daniluk.eePromMaster
import com.daniluk.eePromSlave
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.CAN_NEW
import com.daniluk.utils.Constants.CAN_OLD
import com.daniluk.utils.Constants.GP3S
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.PP3S
import com.daniluk.utils.Constants.SLAVE
import kotlinx.android.synthetic.main.activity_extended_data.*
import kotlinx.android.synthetic.main.activity_main.*

class ExtendedDataActivity : AppCompatActivity() {
    private var deviceName = ""
    private var manufacturersNumber = ""
    private var processorId = ""
    private var programVersion = ""
    private var programVersionBuild = ""
    private var programDateRelease = ""
    private var protectCode = ""
    private var protectCodeNotErasable = ""
    private var decodedProtectCode = ""
    private var defectFileName = ""
    private var defectStringNumber = ""
    private var typeCode = 1


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extended_data)

        //supportActionBar?.title = ""   //убрать текст из ActionBar
        supportActionBar?.hide()        //убрать ActionBar

        //val viewModel = MainViewModel.instansViewModel
        val idProcessor = intent.getIntExtra("idProcessor", 0)
        typeCode = intent.getIntExtra("typeCode", 1) //тип кода - стираемый или не стираемый
        var currentData = listOf<String>()
        when (idProcessor) {
            MASTER -> {
                eePromMaster.observe(this, { getData(eePromMaster.value ?: listOf(), MASTER) })
                tvPocessorNameExtended.text = "Master"
                currentData = eePromMaster.value ?: listOf()
            }
            SLAVE -> {
                eePromSlave.observe(this, { getData(eePromSlave.value ?: listOf(), SLAVE) })
                tvPocessorNameExtended.text = "Slave"
                currentData = eePromSlave.value ?: listOf()
            }
        }

        //обработчик ндлинного нажания на экран для перехода на экран расшифровки черного ящика
        tvDataExtended.setOnLongClickListener {
            if (currentData.lastOrNull() == CAN_NEW) {
                val intent = Intent(this, BlackBox::class.java)
                intent.putExtra("idProcessor", idProcessor)
                startActivity(intent)
            }
            true
        }

    }

    //получить данные в зависимости от признака MASTER или SLAVE
    private fun getData(data: List<String>, idProcessor: Int) {
        if (data.isEmpty()) {
            tvDataExtended.text = "Данные не загружены"
            return
        }

        //Декодирование в зависимости от какого устройства приняты данные
        val result = when (data.lastOrNull()) {
            CAN_NEW -> decoderCanNew(data)
            CAN_OLD -> decoderCanOld(data, idProcessor)
            PP3S -> decoderPP3S(data)
            GP3S -> decoderGP3S(data)
            else -> "Данные не приняты"
        }
        tvDataExtended.text = result
    }

    //декодер CAN_NEW
    private fun decoderCanNew(data: List<String>): String {
        deviceName = getStringDeviceNameCanNew(applicationContext, data)
        manufacturersNumber = getManufacturersNumberCanNew(applicationContext, deviceName, data)
        processorId = getStringProcessorIdCanNew(data)
        programVersion = getStringProgramVersionCanNew(data)
        programDateRelease = getStringProgramDateReleaseCanNew(data)
        protectCode = getStringProtectCodeCanNew(data)
        protectCodeNotErasable = getStringProtectCodeNotErasableCanNew(data)
        decodedProtectCode = getStringDecodedProtectCodeCanNew(applicationContext, data, typeCode)
        defectFileName = getStringDefectFileNameCanNew(data)
        defectStringNumber = getStringDefectStringNumberCanNew(data)
        return printFormat()
    }

    //декодер CAN_OLD
    private fun decoderCanOld(data: List<String>, processorName: Int): String {
        deviceName = getStringDeviceNameCanOld(applicationContext, data, processorName)
        processorId = getStringProcessorIdCanOld(data)
        programVersion = getStringProgramVersionCanOld(data, processorName)
        programVersionBuild = getStringProgramVersionBuildCanOld(data, processorName)
        programDateRelease = getStringProgramDateReleaseCanOld(data, processorName)
        protectCode = getStringProtectCodeCanOld(data, processorName)
        protectCodeNotErasable = getStringProtectCodeNotErasableCanOld(data, processorName)
        decodedProtectCode = getStringDecodedProtectCodeCanOld(
            applicationContext,
            data,
            processorName,
            typeCode
        )
        return printFormat()
    }


    //декодер PP3S
    private fun decoderPP3S(data: List<String>): String {
        return "PP3S еще не реализован"
    }

    //декодер GP3S
    private fun decoderGP3S(data: List<String>): String {
        return "GP3S еще не реализован"
    }

    private fun printFormat(): String {
        var result = ""
        if (deviceName.isNotEmpty()) {
            val begin = "Имя устройства:"
            result = "$result${String.format("%-18s", begin)}$deviceName\n"
        }

        if (manufacturersNumber.isNotEmpty()) {
            val begin = "Заводской номер:"
            result = "$result${String.format("%-18s", begin)}$manufacturersNumber\n"
        }

        if (processorId.isNotEmpty()) {
            val begin = "ID процессора:"
            result = "$result${String.format("%-18s", begin)}$processorId\n\n"
        }

        if (programVersion.isNotEmpty()) {
            val begin = "Версия ПО:"
            result = "$result${String.format("%-18s", begin)}$programVersion\n"
        }

        if (programVersionBuild.isNotEmpty()) {
            val begin = "Версия сборки ПО:"
            result = "$result${String.format("%-18s", begin)}$programVersionBuild\n\n"
        }

        if (programDateRelease.isNotEmpty()) {
            val begin = "Дата релиза ПО:"
            result = "$result${String.format("%-18s", begin)}$programDateRelease\n\n"
        }

        if (protectCode.isNotEmpty()) {
            val begin = "Код отказа:"
            result = "$result${String.format("%-24s", begin)}$protectCode\n"
        }

        if (protectCodeNotErasable.isNotEmpty()) {
            val begin = "Не стираемый код отказа:"
            result = "$result${String.format("%-24s", begin)}$protectCodeNotErasable\n"
        }

        if (decodedProtectCode.isNotEmpty()) {
            val begin = when (typeCode) {
                Constants.FLAG_DECODE_CODE -> "Расшифровка кода отказа:"
                Constants.FLAG_DECODE_NON_ERASABLE_CODE -> "Расшифровка не стираемого кода отказа:"
                else -> ""
            }
            result = "$result\n$begin\n$decodedProtectCode\n\n"
        }

        if (defectFileName.isNotEmpty()) {
            val begin = "Имя файла:"
            result = "$result${String.format("%-18s", begin)}$defectFileName\n"
        }

        if (defectStringNumber.isNotEmpty() && defectFileName.isNotEmpty()) {
            val begin = "Номер строки:"
            result = "$result${String.format("%-18s", begin)}$defectStringNumber\n"
        }

        return result
    }

}