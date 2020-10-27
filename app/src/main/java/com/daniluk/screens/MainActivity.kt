package com.daniluk.screens

//import java.lang.Class
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.bluetooth.BluetoothService.Companion.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION
import com.daniluk.bluetooth.BluetoothService.Companion.REQUEST_CODE_BLUETOOTH_TUNE_ON
import com.daniluk.utils.constants.COMMAND_BT_ON
import com.daniluk.utils.constants.COMMAND_SEARCH_PERMISSION_REQUEST
import com.daniluk.utils.constants.NUMBER_ATTEMPTS_AUTO_CONNECT
import com.daniluk.utils.constants.STATE_CONNECTION_IN_PROGRESS
import com.daniluk.utils.constants.STATE_DISCONNECTION_IN_PROGRESS
import com.daniluk.utils.constants.STATE_ERROR_BLUETOOTH_INIT
import com.daniluk.utils.constants.STATE_NO_CONNECT
import com.daniluk.utils.constants.STATE_READ_CODE
import com.daniluk.utils.constants.colorBLUE
import com.daniluk.utils.constants.colorGREEN
import com.daniluk.utils.constants.colorGray
import com.daniluk.utils.constants.colorRED
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.cancelChildren

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.handDecoder -> {
                //viewModel.bluetoothService.unpairDevice(2)
            }
            R.id.disconnect -> {
                viewModel.flagActionDisconnect = true
                viewModel.clearDataScreen()
                viewModel.bluetoothService.disconnectPCHZS()
                viewModel.bluetoothService.logConnect = ""
            }
            R.id.saveEEPROM -> {

            }
            R.id.readEEPROM -> {

            }
            R.id.displayEEpromMaster -> {

            }
            R.id.displayEEpromSlave -> {

            }
            R.id.removeProtectState -> {

            }

        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = ""   //убрать текст из ActionBar

        //Обработчик кнопки btReadProtectState
        btReadProtectState.setOnClickListener {
            viewModel.clickReadButton()
        }
        //обработчик нажатия на окна tvCodeMaster и tvCodeSlave
        tvCodeMaster.setOnClickListener {
            val intent = Intent(this, ExtendedDataActivity::class.java)
            intent.putExtra("log", viewModel.bluetoothService.logConnect)
            startActivity(intent)
        }

        //Подписаться на данные из viewModel
        viewModel.statePCHZS.observe(this, Observer { state -> setStateView(state) })
        viewModel.managerBluetooth.observe(this, Observer { command ->
            when (command) {
                COMMAND_BT_ON -> {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_CODE_BLUETOOTH_TUNE_ON)
                }
                COMMAND_SEARCH_PERMISSION_REQUEST -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION
                    )
                    viewModel.bluetoothService.showBtLog("MainActivity:  Запрос разрешения из активити")
                }
            }
        })
        viewModel.stateBluetoothAdapter.observe(this, Observer { state ->
            when (state) {
//                BluetoothAdapter.STATE_TURNING_ON -> progressBarMainActivity.visibility = View.VISIBLE
//                BluetoothAdapter.STATE_TURNING_OFF -> progressBarMainActivity.visibility = View.VISIBLE
//                BluetoothAdapter.STATE_ON -> progressBarMainActivity.visibility = View.INVISIBLE
//                BluetoothAdapter.STATE_OFF -> progressBarMainActivity.visibility = View.INVISIBLE
            }
        })
        viewModel.textTvCodeMaster.observe(this, Observer { text -> tvCodeMaster.setText(text) })
        viewModel.textTvCodeSlave.observe(this, Observer { text -> tvCodeSlave.setText(text) })


        MainViewModel.instansViewModel = viewModel   //Сохранить ссылку в экземпляре viewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BLUETOOTH_TUNE_ON) {
            //Ответ на запрос включения Bluetooth
            if (resultCode == Activity.RESULT_OK) {
            } else if (resultCode == Activity.RESULT_CANCELED) {
                /*
                 * В этом случае блютуз не включится и опереатор либо вручную запускает подключение заново
                 * либо выходит из приложения. Стоп подключение и информация для оператора
                 */
                viewModel.statePCHZS.value = STATE_NO_CONNECT
                viewModel.textTvCodeMaster.value =
                    "Без включения Bluetooth работа не возможна.\nДля продолжения запустите подключение повторно и выберите в диалоге включения блютуз\"Разрешить\""
                //прервать выполнение коррутин
                viewModel.viewModelScope.coroutineContext.cancelChildren()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //если пришел ответ на запрос разрешения сканирования
        if (requestCode == PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults.size == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.bluetoothService.showBtLog("MainActivity:  Разрешение поиска получено")
            } else {
                viewModel.statePCHZS.value = STATE_NO_CONNECT
                viewModel.textTvCodeMaster.value =
                    "Разрешите поиск Bluetooth устройств иначе устройство не будет работать.\n" +
                            "Для продолжения запустите подключение повторно и выберите в диалоге разрешения поиска\"Разрешить\""
                //прервать выполнение коррутин
                viewModel.viewModelScope.coroutineContext.cancelChildren()
                viewModel.bluetoothService.showBtLog("MainActivity:  Запрос разрешения отклонен")
            }
        }
    }

    private fun setStateView(statePCHZS: Int?) {
        statePCHZS.let {
            when (statePCHZS) {
                STATE_ERROR_BLUETOOTH_INIT -> {
                    btReadProtectState.setText(getString(R.string.ERROR_INIT_BLUETOOTH))
                    btReadProtectState.setBackgroundColor(resources.getColor(colorGray))
                    progressBarMainActivity.visibility = View.INVISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_ERROR_BLUETOOTH_INIT")
                }
                STATE_NO_CONNECT -> {
                    viewModel.bluetoothService.clientSocket?.close()
                    btReadProtectState.setText(R.string.NO_CONNECTION)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorRED))
                    progressBarMainActivity.visibility = View.INVISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_NO_CONNECT")
                    viewModel.flagActionConnect = false
                    viewModel.viewModelScope.coroutineContext.cancelChildren()

                    //проверка что это первой попытки подключения еще не было
                    if (!viewModel.flagfirstConnect) {
                        return
                    }

                    //****Далее логика переподключения*****
                    //если не было намерения отключаться
                    if (!viewModel.flagActionDisconnect) {
                        //если кол-вл попыток соединения не превышает ограничение
                        if(viewModel.numberOfConnectionAttempts < NUMBER_ATTEMPTS_AUTO_CONNECT){
                            viewModel.clearDataScreen()
                            viewModel.flagActionConnect = true
                            //если уже было три попытки соединения по сохраненному адресу
                            if(! viewModel.adressPchzs.isEmpty() && viewModel.numberOfConnectionAttempts == 3){
                                //сбросить адрес
                                viewModel.adressPchzs = ""
                            }
                            viewModel.numberOfConnectionAttempts++
                            viewModel.bluetoothService.showBtLog("setStateView():  повторное подключение - старт connectPCHZS(), " +
                                    "попытка № ${viewModel.numberOfConnectionAttempts}")
                            viewModel.bluetoothService.connectPCHZS()

                        }else{
                            viewModel.bluetoothService.showBtLog("setStateView():  Колличество попыток подключения превысило норму, повторного подключения не нужно")
                            viewModel.textTvCodeMaster.postValue(getString(R.string.ERROR_CONNECT))
                            viewModel.flagActionConnect = false
                        }
                    }
                }
                STATE_READ_CODE -> {
                    btReadProtectState.setText(R.string.READ_CODE)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorGREEN))
                    progressBarMainActivity.visibility = View.INVISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_READ_CODE")
                    viewModel.flagActionConnect = false
                }
                STATE_CONNECTION_IN_PROGRESS -> {
                    btReadProtectState.setText(R.string.CONNECTION_IN_PROGRESS)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorBLUE))
                    progressBarMainActivity.visibility = View.VISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_CONNECTION_IN_PROGRESS")
                }
                STATE_DISCONNECTION_IN_PROGRESS -> {
                    btReadProtectState.setText(R.string.DISCONNECTION_IN_PROGRESS)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorBLUE))
                    progressBarMainActivity.visibility = View.VISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_DISCONNECTION_IN_PROGRESS")
                }
            }
        }

    }

}