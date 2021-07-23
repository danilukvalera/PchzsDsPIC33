package com.daniluk.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.decoders.decodeBlackBox
import com.daniluk.eePromMaster
import com.daniluk.eePromSlave
import com.daniluk.utils.Constants
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.MASTER_STR
import com.daniluk.utils.Constants.SLAVE_STR
import kotlinx.android.synthetic.main.activity_black_box.*

class BlackBox : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black_box)

        //supportActionBar?.title = ""   //убрать текст из ActionBar
        supportActionBar?.hide()        //убрать ActionBar

        //val viewModel = MainViewModel.instansViewModel
        val idProcessor = intent.getIntExtra("idProcessor", 0)

        //var data = listOf<String>()
        eePromMaster.observe(this, {
            if (idProcessor == MASTER) {
                //tvBlacBox.text = decodeBlackBox(this, it)
                tvBlacBox.text = Html.fromHtml(decodeBlackBox(this, it))
                tvIdProcessor.text = MASTER_STR
            }
        })

        eePromSlave.observe(this, {
            if (idProcessor == Constants.SLAVE) {
                //tvBlacBox.text = decodeBlackBox(this, it)
                tvBlacBox.text = Html.fromHtml(decodeBlackBox(this, it))
                tvIdProcessor.text = SLAVE_STR
            }
        })
    }
}