package com.daniluk

//import android.R
import com.daniluk.R
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.daniluk.bluetooth.BluetoothService
import com.daniluk.bluetooth.BluetoothService.Companion.TAG_BT_CONNECT
import com.daniluk.utils.constants.COMMAND_BT_DO_NOTHING
import com.daniluk.utils.constants.STATE_NO_CONNECT
import com.daniluk.utils.constants.STATE_READ_CODE
import java.io.IOException


class MainViewModel(application: Application) : AndroidViewModel(application) {
    var statePCHZS = MutableLiveData<Int>(STATE_NO_CONNECT)
    var managerBluetooth = MutableLiveData<Int>(COMMAND_BT_DO_NOTHING)
    var stateBluetoothAdapter = MutableLiveData<Int>()
    var eePromMaster = MutableLiveData<List<String>>()
    var eePromSlave = MutableLiveData<List<String>>()
    var textTvCodeMaster = MutableLiveData<String>("")
    var textTvCodeSlave = MutableLiveData<String>("")
    val context = getApplication<Application>() as Context
    val bluetoothService: BluetoothService by lazy { BluetoothService(context) }
    var adressPchzs: String

    var flagfirstConnect = false        //true - кнопка подключить нажималась хотя бы один раз, false - еще не нажималась
    var flagActionDisconnect = false    //намерение отключиться
    var flagActionConnect = false       //намерение подключиться
    var numberOfConnectionAttempts = 0  //количество попыток подключения


    //var countAttemptsDevice = 1

    companion object {
        lateinit var instansViewModel: MainViewModel
    }

    init {
        val sharedPref = context.getSharedPreferences(context.getString(R.string.ADRESS_PCHZS), MODE_PRIVATE)
        adressPchzs = sharedPref.getString("adressPchzs", "") ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothService.unregisterBluetoothBroadcastReceiver()
        try {
            bluetoothService.clientSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG_BT_CONNECT, "Не удалось закрыть клиентский сокет после завершения приложения", e)
        }
        //Чтение записанного ранее адреса ПЧЗС
        val sharedPref = context.getSharedPreferences(context.getString(R.string.ADRESS_PCHZS), MODE_PRIVATE)
        val savedAdress = sharedPref.getString("adressPchzs", "") ?: ""
        //Если ранее записаный адрес не совпадает с текущим - перезаписываем

        if (savedAdress != adressPchzs) {
            val editor = sharedPref.edit()
            editor.putString("adressPchzs", adressPchzs)
            editor.commit()
        }



        //TODO("Очистить все ресурсы")
    }

    fun clickReadButton() {
        val state = statePCHZS.value ?: STATE_NO_CONNECT
        when (state) {
            STATE_NO_CONNECT -> {
                clearDataScreen()
                flagActionConnect = true
                numberOfConnectionAttempts = 1
                flagfirstConnect = true
                bluetoothService.connectPCHZS()

                bluetoothService.logConnect = ""
            }
            STATE_READ_CODE -> {
                clearDataScreen()
                bluetoothService.logConnect = ""
                bluetoothService.readProtectState()
                //bluetoothService.disconnectPCHZS()
            }
        }
    }

    fun clearDataScreen() {
        textTvCodeMaster.value = ""
        textTvCodeSlave.value = ""
    }
}