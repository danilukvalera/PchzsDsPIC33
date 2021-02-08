package com.daniluk.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.CAN_NEW
import com.daniluk.utils.Constants.TAG_CAN_NEW
import com.daniluk.utils.byteToStrHex
import com.daniluk.utils.wordToStrHex
import kotlinx.android.synthetic.main.activity_extended_data.*
import kotlinx.android.synthetic.main.activity_hex_data.*

class HexDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hex_data)

        supportActionBar?.hide()        //убрать ActionBar

        val viewModel = MainViewModel.instansViewModel
        val idProcessor = intent.getIntExtra("idProcessor", 0)
        val data: List<String>
        when (idProcessor) {
            Constants.MASTER -> {
                data = viewModel.eePromMaster.value ?: listOf()
            }
            Constants.SLAVE -> {
                data = viewModel.eePromSlave.value ?: listOf()
            }
            else -> data = listOf()
        }
        if (data.isEmpty()) {
            //tvDataExtended.text = "Данные не загружены"
            return
        }

        if (data.lastOrNull() == CAN_NEW) {
            var str: StringBuilder
            val result = StringBuilder()
            var word: String
            tvAdress.setText("")
            //tvAdressColumn.setText("   0000 0002 0004 0006 0008 000A 000C 000E")
            tvAdressColumn.text = String.format("%42s", "0000 0002 0004 0006 0008 000A 000C 000E")
            val length: Int = data.size
            var i = 0
            while (i < length-1) {
                str = StringBuilder("  ")
                tvAdress.setText(tvAdress.getText().toString() + wordToStrHex(i).toString() + "\n")
                for (m in 0..7) {
                    if (i < length-1 ) {
                        word = data.getOrElse(i) {""} + data.getOrElse(i+1){""}
                        str.append(String.format("%5s", word))
                        i += 2
                    }
                }
                str.append("\n")
                result.append(str)
            }
            tvEEPROMhex.setText(result.toString())
        } else {
            val builder = StringBuilder("Полученные данные не являются содержимым EEPROM\n")
            builder.append("Полученные данные:\n")
            builder.append(data.toString())
            tvEEPROMhex.setText(builder.toString())
        }

    }
}