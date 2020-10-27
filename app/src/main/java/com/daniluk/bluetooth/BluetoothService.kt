package com.daniluk.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.utils.constants.BLUETOOTH_DEVICE_NAME
import com.daniluk.utils.constants.COMMAND_BT_ON
import com.daniluk.utils.constants.COMMAND_SEARCH_PERMISSION_REQUEST
import com.daniluk.utils.constants.STATE_CONNECTION_IN_PROGRESS
import com.daniluk.utils.constants.STATE_DISCONNECTION_IN_PROGRESS
import com.daniluk.utils.constants.STATE_ERROR_BLUETOOTH_INIT
import com.daniluk.utils.constants.STATE_NO_CONNECT
import com.daniluk.utils.constants.STATE_READ_CODE
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.util.*

class BluetoothService(val context: Context) {
    companion object {
        const val REQUEST_CODE_BLUETOOTH_TUNE_ON = 101
        const val PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION = 201
        const val TAG_BT_CONNECT = "BT_CONNECT"
    }

    var logConnect = ""
    var flagWriteLogConnect = true
    fun showBtLog(textLog: String) {
        Log.d(
            TAG_BT_CONNECT,
            textLog
        )
        if (flagWriteLogConnect) {
            logConnect = logConnect.plus(textLog).plus("\n\n")
        }
    }

    private val viewModel = MainViewModel.instansViewModel
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    //private var listBondedDevices = bluetoothAdapter?.bondedDevices ?: setOf()
    var clientSocket: BluetoothSocket? = null
    private var bluetoothPCHZS: BluetoothDevice? = null
    private var bluetoothBroadcastReceiver: BroadcastReceiver? = null
    private var bluetoothPairingBroadcastReceiver: BroadcastReceiver? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var timeDisconnect = Date().time


    //private val uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66") //SECURE
    //private val uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66") //INSECURE tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);

    init {
        bluetoothInitialization()
    }

    //Чтение защитного состояния
    fun readProtectState() {
        //проверить а есть ли соединение....
    }

    //Инициализация Bluetooth
    fun bluetoothInitialization(): Boolean {
        //если bluetoothAdapter = null - выход
        if (bluetoothAdapter == null) {
            viewModel.textTvCodeMaster.value = context.getString(R.string.NO_BLUETOOTH)
            viewModel.textTvCodeSlave.value = context.getString(R.string.NO_BLUETOOTH)
            viewModel.statePCHZS.value = STATE_ERROR_BLUETOOTH_INIT
            return false
        }

        //создаем широковещательный приемник состояния bluetooth
        //showBtLog("bluetoothInitialization():  регистрация bluetoothBroadcastReceiver")
        registerBluetoothBroadcastReceiver()
        return true
    }

    // Включение Bluetooth
    private suspend fun bluetoothEnable(): Boolean {
        showBtLog("bluetoothEnable() послать команду включения Bluetooth")
        viewModel.managerBluetooth.value = COMMAND_BT_ON
        showBtLog("bluetoothEnable() Вход в suspend включения Bluetooth, ждем пока включится")
        withContext(Dispatchers.IO) {
            while (bluetoothAdapter?.isEnabled == false) {
                delay(1000)
                showBtLog("searchPchzs(): Ждем... bluetoothAdapter включен??? = ${bluetoothAdapter.isEnabled}")
            }
        }
        return bluetoothAdapter?.isEnabled ?: false
    }

    // Выключение Bluetooth
    fun bluetoothDisable() {
        bluetoothAdapter?.disable()
    }

    //Подключение найденному устройству
    private suspend fun connectDevice() {
        withContext(Dispatchers.IO) {
            showBtLog("connectDevice(): Вход в suspend подключения")
            try {
                //clientSocket = bluetoothPCHZS?.createRfcommSocketToServiceRecord(uuid) //создать сокет для соединения
                clientSocket = bluetoothPCHZS?.createInsecureRfcommSocketToServiceRecord(uuid) //создать сокет для соединения без требования сопряжения
                showBtLog("connectDevice():  создали Socket")
                bluetoothAdapter?.cancelDiscovery()
                showBtLog("connectDevice():  выключили поиск и ждем отключения поиска")
                while (bluetoothAdapter?.isDiscovering == true) {
                    showBtLog("ждем...")
                }
                showBtLog("connectDevice():  старт подключения.")
                while (Date().time - timeDisconnect < 3000){
                    //ожидается время гарантированного отключения удаленного устройства (имеенно чтобы ПЧЗС успел отключиться)
                    showBtLog("connectDevice():  ожидаем время перед повторным подключением")
                }

                clientSocket?.connect()
                //Если подключение не успешо метод clientSocket.connect() выбросит исключение
                //При этом сработает BluetoothBroadcastReceiver:  ACTION_ACL_DISCONNECTED
                viewModel.statePCHZS.postValue(STATE_READ_CODE)
                showBtLog("connectDevice():  после подключения. Установили готовность к чтению ЗС")
                viewModel.adressPchzs = bluetoothPCHZS?.address ?: ""
                showBtLog("connectDevice():  после подключения. сохранили адрес ПЧЗС")
            } catch (e: IOException) {
                e.printStackTrace()
                showBtLog("connectDevice():  Ошибка подключения. Выброшено исключение: ${e.message}, Установить STATE_NO_CONNECT")
                viewModel.statePCHZS.postValue(STATE_NO_CONNECT)
            }
        }
    }

    //Поик устройства
    private suspend fun searchPchzs(): BluetoothDevice? {
        withContext(Dispatchers.IO) {
            showBtLog("searchPchzs(): Вход в suspend поиска")
            //статус разрешение на запуск поискс Bluetooth
            var permissionStatus =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            showBtLog("searchPchzs(): проверка разрешения permissionStatus = $permissionStatus")
            //если разрешение есть, старт поиска устройств
            //если разрешения нет, запрос на получение разрешения
            //ответ на запрос обрабатывается в функции onRequestPermissionsResult()
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                showBtLog("searchPchzs(): Разрешения поиска нет. Запрос разрешения")
                viewModel.managerBluetooth.postValue(COMMAND_SEARCH_PERMISSION_REQUEST)
                //ожидание разрешения поиска
                while (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    permissionStatus =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    showBtLog("searchPchzs(): Ждем разрешения...")
                    delay(1000)
                }
            }

            bluetoothAdapter?.cancelDiscovery()
            showBtLog("searchPchzs(): выключили поиск, если вдруг он запущен системой  и ждем отключения поиска")
            while (bluetoothAdapter?.isDiscovering == true) {
                showBtLog("searchPchzs(): ждем...")
            }
            showBtLog("searchPchzs(): Старт поиска")
            bluetoothAdapter?.startDiscovery()
            showBtLog("searchPchzs(): Поиск запущен. Ждем обнаружения ПЧЗС")
            //ожидание обнаружения bluetoothPCHZS
            var i = 0
            while (bluetoothPCHZS == null && i < 15) {
                delay(1000)
                i++
                showBtLog("searchPchzs(): Идет поиск. Прошло $i секунд. bluetoothPCHZS = $bluetoothPCHZS")
            }
            showBtLog("searchPchzs():  поиск принес результат, ПЧЗС найден, идем на подключение")
        }
        return bluetoothPCHZS
    }

    //Подключение к ПЧЗС
    fun connectPCHZS() {
        viewModel.statePCHZS.value = STATE_CONNECTION_IN_PROGRESS
        showBtLog("connectPCHZS():  Вход в функцию connectPCHZS():  запустил прогрессбар. Проверка что старый сокет закрыт и если нужно ожидание закрытия")
        while (clientSocket?.isConnected() == true) {
            showBtLog("Ждем...")
        }
        //коррутина подключения
        val job = viewModel.viewModelScope.launch {
            showBtLog("connectPCHZS():  вход в коррутину подключения")
            //проверка включен ли Bluetooth, если нет то включение
            showBtLog("connectPCHZS():  Включен ли Bluetooth? -  bluetoothAdapter = ${bluetoothAdapter?.isEnabled}")
            if (bluetoothAdapter?.isEnabled == false) {
                bluetoothEnable()
            }

            //Получить bluetoothPCHZS из сохраненного адреса
            try {
                bluetoothPCHZS = bluetoothAdapter?.getRemoteDevice(viewModel.adressPchzs)
                showBtLog("connectPCHZS():  ПЧЗС получен из сохраненного адреса}")
            } catch (e: Exception) {
                bluetoothPCHZS = null
                showBtLog("connectPCHZS():  Сохраненный адрес ПЧЗС не действительный, установил bluetoothPCHZS = null")
            }

/*
            //получить bluetoothPCHZS из списка сопряженных устройств
            bluetoothPCHZS = getDeviceFromBondedDevices()
            showBtLog("connectPCHZS():  bluetoothPCHZS из списка сопряженных устойств bluetoothPCHZS = $bluetoothPCHZS")
*/

            //Поиск устройств, если bluetoothPCHZS = null
            if (bluetoothPCHZS == null) {
                bluetoothPCHZS = searchPchzs()
                if (bluetoothPCHZS == null) {
                    showBtLog("connectPCHZS():  ПЧЗС не найден, прервать выполнение коррутин, установить STATE_NO_CONNECT")
                    //прервать выполнение коррутин
                    viewModel.viewModelScope.coroutineContext.cancelChildren()
                    viewModel.statePCHZS.value = STATE_NO_CONNECT
                    viewModel.textTvCodeMaster.value = context.getString(R.string.PCHZS_NOT_FOUND)
                }
            }
            //если нужно узнать поддерываемый UUID у устройства
/*
            val bluetoothPCHZSParcelUuid = bluetoothPCHZS?.uuids
            val array: Array<ParcelUuid> = bluetoothPCHZSParcelUuid ?: arrayOf()
            for(parcelUuid in array) {
                val uuid = parcelUuid.uuid
                showBtLog("name = ${bluetoothPCHZS?.name}, adress = ${bluetoothPCHZS?.address}, ParcelUuid = $parcelUuid,  UUID = $uuid")
            }
            // В результате получил
            // ParcelUuid = 00001101-0000-1000-8000-00805f9b34fb,  UUID = 00001101-0000-1000-8000-00805f9b34fb
            // ParcelUuid = 00000000-0000-1000-8000-00805f9b34fb,  UUID = 00000000-0000-1000-8000-00805f9b34fb   !!!! с этим не работает
            // ParcelUuid = 00000000-0000-1000-8000-00805f9b34fb,  UUID = 00000000-0000-1000-8000-00805f9b34fb
*/

            //подключение к bluetooth ПЧЗС
            connectDevice()
        }

    }

    //Отключение от ПЧЗС
    fun disconnectPCHZS() {
        clientSocket.let {
            it?.close()
            viewModel.statePCHZS.value = STATE_DISCONNECTION_IN_PROGRESS
        }
    }

    //Получение bluetoothPCHZS из списка сопряженных устройств
    //private fun getDeviceFromBondedDevices(): BluetoothDevice? {
    public fun getDeviceFromBondedDevices(): BluetoothDevice? {
        var listBondedDevices = bluetoothAdapter?.bondedDevices ?: setOf()
        listBondedDevices?.forEach { device ->
            device.name.let {
                if (it == BLUETOOTH_DEVICE_NAME)
                    return device
            }
        }
        return null
    }

    //регистрация широковещательного приемника
    private fun registerBluetoothBroadcastReceiver() {
        //фильтр для отслеживания обнаружения доступных устройств и отслеживания режима поиска
        val filterBroadcastReceiver = IntentFilter()
        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_FOUND) //ACTION обнаружения нового устройства
        filterBroadcastReceiver.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //ACTION старта поиска
        filterBroadcastReceiver.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //ACTION завершения поиска
        filterBroadcastReceiver.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //ACTION изменения состояния блютзадаптера
        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //ACTION соединения с удаленным устройством блютуз
        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //ACTION разрыва блютуз соединения
        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) //ACTION запроса на отключение
        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) //ACTION зизменения состояния сопряжения с подключаемым устройством
//        filterBroadcastReceiver.addAction(BluetoothDevice.ACTION_UUID) //ACTION получения UUID (SDP - Service Discovery Protocol )
        //приемник для отслеживания обнаружения доступных устройств и отслеживания режима поиска
        bluetoothBroadcastReceiver = BluetoothBroadcastReceiver()
        context.registerReceiver(bluetoothBroadcastReceiver, filterBroadcastReceiver)

        //фильтр для отслеживания запроса сопряжения
        val filterBluetoothPairingBroadcastReceiver = IntentFilter()
        //filterBluetoothPairingBroadcastReceiver.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        filterBluetoothPairingBroadcastReceiver.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            filterBluetoothPairingBroadcastReceiver.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST) //ACTION запроса пин кода
        }
        //приемник для отслеживания запроса сопряжения
        bluetoothPairingBroadcastReceiver = BluetoothPairingBroadcastReceiver()
        context.registerReceiver(bluetoothPairingBroadcastReceiver, filterBluetoothPairingBroadcastReceiver)
    }

    //отмена регистрации широковещательного приемника
    fun unregisterBluetoothBroadcastReceiver() {
        context.unregisterReceiver(bluetoothBroadcastReceiver)
        context.unregisterReceiver(bluetoothPairingBroadcastReceiver)
    }

    //широковещательный приемник для отслеживания состояния BluetoothAdapter
    private inner class BluetoothBroadcastReceiver : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        override fun onReceive(
            contextApp: Context,
            intent: Intent
        ) {
            val action = intent.action
            //showBtLog("BluetoothBroadcastReceiver:  action = $action")

            when {
                //ACTION старт поиска
                BluetoothAdapter.ACTION_DISCOVERY_STARTED == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_DISCOVERY_STARTED")
                }

                //ACTION завершения поиска
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action -> {
                    //если поиск запустило приложение
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_DISCOVERY_FINISHED")
                }

                //ACTION обнаружения нового устройства
                BluetoothDevice.ACTION_FOUND == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_FOUND")
                    //bluetooth устройство найдено
                    val device =
                        (intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))!!
                    if (device.name != null && device.name == BLUETOOTH_DEVICE_NAME) {
                        bluetoothPCHZS = device
                        showBtLog("ПЧЗС найден: =  $device.name")
                    } else {
                        showBtLog("Найденное устройство не ПЧЗС")
                    }
                }

                //ACTION отслеживание состояния блютуз адаптера - вкл, выкл.
                BluetoothAdapter.ACTION_STATE_CHANGED == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_STATE_CHANGED")
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    when (state) {
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            viewModel.stateBluetoothAdapter.value = BluetoothAdapter.STATE_TURNING_ON
                            showBtLog("BluetoothAdapter.STATE_TURNING_ON")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            viewModel.stateBluetoothAdapter.value = BluetoothAdapter.STATE_ON
                            showBtLog("BluetoothAdapter.STATE_ON")
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            viewModel.stateBluetoothAdapter.value = BluetoothAdapter.STATE_TURNING_OFF
                            showBtLog("BluetoothAdapter.STATE_TURNING_OFF")
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            viewModel.stateBluetoothAdapter.value = BluetoothAdapter.STATE_OFF
                            showBtLog("BluetoothAdapter.STATE_OFF")
                            viewModel.statePCHZS.value = STATE_NO_CONNECT
                            showBtLog("установить STATE_NO_CONNECT")
                        }
                    }
                }

                //ACTION отслеживание момента подключения удаленного устройства
                BluetoothDevice.ACTION_ACL_CONNECTED == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_ACL_CONNECTED")
                    val device = (intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))!!
                    showBtLog("Момент подключения к устройству ${device.name}")
                }

                //ACTION отслеживание момента отключения удаленного устройства
                BluetoothDevice.ACTION_ACL_DISCONNECTED == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_ACL_DISCONNECTED")
                    val device = (intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))!!
                    showBtLog("device = ${device.name}")
                    if (device.name == BLUETOOTH_DEVICE_NAME) {
                        timeDisconnect = Date().time
                        showBtLog("установить STATE_NO_CONNECT")
                        viewModel.statePCHZS.value = STATE_NO_CONNECT
                        if(viewModel.flagActionDisconnect){
                            viewModel.flagActionDisconnect = false
                        }
                    }
                }

                //ACTION запроса на отключение
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_ACL_DISCONNECT_REQUESTED")
                    showBtLog("Был запрос на отключение")
                }

                // ACTION изменения состояния сопряжения с подключаемым устройством
                // срабатывает при сопряжении или отмене сопряжения с подключаемым устройством
                // или при попытке подкоючения и отсутствии сопряжения
                BluetoothDevice.ACTION_BOND_STATE_CHANGED == action -> {
                    showBtLog("BluetoothBroadcastReceiver: ACTION_BOND_STATE_CHANGED")
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    showBtLog("Состояние сопряжения с подключаемым устройством bondState = $bondState")
                }

/*
                //ACTION получения UUID
                BluetoothDevice.ACTION_UUID == action -> {
                    showBtLog("BluetoothBroadcastReceiver:  ACTION_UUID")
                    val device = (intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))!!
                    val detectedParcelUuid = (intent.getParcelableExtra<ParcelUuid>(BluetoothDevice.EXTRA_UUID))
                    val detectedUUID = detectedParcelUuid?.uuid
                    showBtLog("device = $device, name = ${device.name}, adress = ${device.address}, ParcelUuid = $detectedParcelUuid,  UUID = $detectedUUID")
                }
*/
            }
        }
    }

    //широковещательный приемник для сопряжения устройств
    private inner class BluetoothPairingBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //ACTION запрос пинкода на сопряжение от подключаемого устройства
            //срабатывает если сопряжение ранее не было установлено
            if (intent?.action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                showBtLog("BluetoothPairingBroadcastReceiver:  ACTION_PAIRING_REQUEST")
                try {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234)
                    val pinBytes: ByteArray
                    pinBytes = ("" + pin).toByteArray(StandardCharsets.UTF_8)
                    //установить пинкод
                    showBtLog("Старт автосопряжения setPin(). PIN = $pin")
                    val resultPairing = device?.setPin(pinBytes)
                    //если сопряжение успешно, setPin вернет true, прервать broadcast чтобы система не показала окно запроса пинкода
                    if (resultPairing == true) {
                        showBtLog("Автосопряжение НОРМА")
                        abortBroadcast()
                    } else {
                        showBtLog("Автосопряжение ОШИБКА")
                    }
                } catch (e: Exception) {
                    showBtLog("Автосопряжение выбросило исключение: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }


    //Удалить спаренное устройство из списка сопряженных
    //не работает ни type 1, ни type 2. Выдает result = false
    //Это свойство Xiaomi!!! Работает если и сопряжение и удаление сопряжения выполняются в одном приложении
    //На других устройствах работает!!! Проверил на Холмове (Huavey 9)
    fun unpairDevice(type: Int) {
        val device = getDeviceFromBondedDevices()
        //метод одиночной рефлексии
        if (type == 1) {
            try {
                val someHiddenClass1 = device?.javaClass
                showBtLog("unpairDevice():  someHiddenClass = $someHiddenClass1")
                val someHiddenMethod1 = someHiddenClass1?.getMethod("removeBond")
                showBtLog("unpairDevice():  someHiddenMethod = $someHiddenMethod1")

                val result1 = someHiddenMethod1?.invoke(device).toString()
                showBtLog("unpairDevice():  result1 = $result1")
                viewModel.textTvCodeMaster.value = result1

            } catch (e: java.lang.Exception) {
                viewModel.bluetoothService.showBtLog("teunpairDevicest(): e = ${e.message}")
            }
        }

        //метод двойной рефлексии (система запрашивает скрытый метод, а не приложение)
        if (type == 2) {
            try {
                val reflexClass = Class::class.java
                showBtLog("unpairDevice():  reflexClass = $reflexClass")
                val forName = reflexClass.getMethod("forName", String::class.java)
                showBtLog("unpairDevice():  forName = $forName")
                val getMethod =
                    Class::class.java.getMethod("getMethod", String::class.java, arrayOf<Class<*>>()::class.java)
                showBtLog("unpairDevice():  getMethod = $getMethod")
                val someHiddenClass2 = forName.invoke(null, "android.bluetooth.BluetoothDevice") as Class<*>
                showBtLog("unpairDevice():  getMethod = $getMethod")
                val someHiddenMethod2 = getMethod.invoke(someHiddenClass2, "removeBond", null) as Method
                showBtLog("unpairDevice():  someHiddenMethod2 = $someHiddenMethod2")

                val result2 = someHiddenMethod2.invoke(device)
                showBtLog("unpairDevice():  result2 = $result2")
                viewModel.textTvCodeMaster.value = result2.toString()
            } catch (e: Exception) {
                viewModel.bluetoothService.showBtLog("teunpairDevicest(): e = ${e.message}")
            }

/*
            //работоспособность метода двойной рефлексии проверена на таком коде (вызывать из MainActivity)
            val reflexClass = Class::class.java
            val forName = reflexClass.getMethod("forName", String::class.java)
            val getMethod = Class::class.java.getMethod("getMethod", String::class.java, arrayOf<Class<*>>()::class.java)
            val someHiddenClass = forName.invoke(null, "android.widget.TextView") as Class<*>
            val someHiddenMethod = getMethod.invoke(someHiddenClass, "getText", null) as Method
            val text = someHiddenMethod.invoke(tvLabelMaster)
*/


        }
    }

}