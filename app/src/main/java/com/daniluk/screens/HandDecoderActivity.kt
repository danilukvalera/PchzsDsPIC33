package com.daniluk.screens

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.daniluk.R
import com.daniluk.decoders.decodingProtectCodeCanNew
import com.daniluk.decoders.decodingProtectCodeCanOld
import kotlinx.android.synthetic.main.activity_hand_decoder.*

class HandDecoderActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hand_decoder)
        supportActionBar?.hide()        //убрать ActionBar

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.device_array,
            //android.R.layout.simple_spinner_item
            R.layout.device_spinner_item
        )
        //adapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1)
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(R.layout.device_spinner_dropdown_item)
        spDevice.setAdapter(adapter)
        spDevice.setOnItemSelectedListener(this)


    }

    fun executeClick(view: View) {
        val typeDevice = spDevice.selectedItem.toString()
        val code = etProtectCode.text.toString().toUpperCase()

        if (code.isEmpty()) {
            tvDecodeData.text = "Введите код защитного состояния"
        }
        var decodeCode = ""
        decodeCode = if (typeDevice.equals("ГАРС-С", ignoreCase = true) ||
            typeDevice.equals("ГП-Е", ignoreCase = true)
        ) {
            decodingProtectCodeCanNew(applicationContext, code, typeDevice, listOf())
        } else if (typeDevice.equals("ГКЛС-Е", ignoreCase = true) ||
            typeDevice.equals("ГАРС-Е", ignoreCase = true) ||
            typeDevice.equals("ОКД", ignoreCase = true)
        ) {
            decodingProtectCodeCanOld(applicationContext, "0x$code", typeDevice)
        } else {
            "Выберите тип прибора"
        }

        tvDecodeData.text = decodeCode

    }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        tvDecodeData.text = ""
        etProtectCode.setText("")
        etProtectCode.isFocusable = true

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }
}