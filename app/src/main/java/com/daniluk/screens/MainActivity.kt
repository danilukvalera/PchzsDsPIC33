package com.daniluk.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.daniluk.MainViewModel
import com.daniluk.R
import com.daniluk.bluetooth.BluetoothService.Companion.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION
import com.daniluk.bluetooth.BluetoothService.Companion.REQUEST_CODE_BLUETOOTH_TUNE_ON
import com.daniluk.eePromMaster
import com.daniluk.eePromSlave
import com.daniluk.utils.Constants.COMMAND_BT_ON
import com.daniluk.utils.Constants.COMMAND_SEARCH_PERMISSION_REQUEST
import com.daniluk.utils.Constants.COMMAND_WRITE_PERMISSION_REQUEST
import com.daniluk.utils.Constants.FLAG_DECODE_CODE
import com.daniluk.utils.Constants.FLAG_DECODE_NON_ERASABLE_CODE
import com.daniluk.utils.Constants.MASTER
import com.daniluk.utils.Constants.NUMBER_ATTEMPTS_AUTO_CONNECT
import com.daniluk.utils.Constants.SLAVE
import com.daniluk.utils.Constants.STATE_CONNECTION_IN_PROGRESS
import com.daniluk.utils.Constants.STATE_DISCONNECTION_IN_PROGRESS
import com.daniluk.utils.Constants.STATE_ERROR_BLUETOOTH_INIT
import com.daniluk.utils.Constants.STATE_NO_CONNECT
import com.daniluk.utils.Constants.STATE_READY_READ_CODE
import com.daniluk.utils.Constants.STATE_READ_CODE_IN_PROGRESS
import com.daniluk.utils.Constants.STATE_REMOVE_PROTECT_CODE_IN_PROGRESS
import com.daniluk.utils.Constants.colorBLUE
import com.daniluk.utils.Constants.colorGREEN
import com.daniluk.utils.Constants.colorGray
import com.daniluk.utils.Constants.colorRED
import com.daniluk.utils.PERMISSION_REQUEST_CODE_WRITE_READ_EXTERNAL_STORAGE
import com.daniluk.utils.REQUEST_CODE_SELECTION_FILE_TO_READ
import com.daniluk.utils.REQUEST_CODE_SELECTION_FILE_TO_SEND
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onBackPressed() {
        //Диалог "Вы действительно хотите выйти?"
        viewModel.setDataDialog(
            title = "Выход из приложения",
            message = "Вы действительно хотите выйти?",
            negativeButton = "Нет",
            positiveButton = "Да",
            positiveFun = if (viewModel.flagSaveNeed) { //если данные не сохранены диалог о сохранении
                {
                    viewModel.setDataDialog(
                        title = "ЗС считано но данные не сохранены",
                        message = "Сохранить?",
                        negativeButton = "Нет",
                        negativeFun = {
                            viewModel.flagAppOff.value = true
                        },
                        positiveButton = "Да",
                        positiveFun = {
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                viewModel.saveDataToFiles()
                                viewModel.flagAppOff.postValue(true)
                            }
                        }

                    )
                    viewModel.flagCreateDialog.value = true
                }
            } else {  //если данные сохранены или нечего сохранять просто выходим
                {
                    viewModel.flagAppOff.value = true
                }
            }

        )
        viewModel.flagCreateDialog.value = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.handDecoder -> {
                val intent = Intent(this, HandDecoderActivity::class.java)
                startActivity(intent)
            }
            R.id.sendTo -> {
                sendFile()
            }
            R.id.saveEEPROM -> {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.saveDataToFiles()
                }
            }
            R.id.readEEPROM -> {
                downloadFileFromMemory()
            }
            R.id.displayEEpromMaster -> {
                val intent = Intent(this, HexDataActivity::class.java)
                intent.putExtra("idProcessor", MASTER)
                startActivity(intent)
            }
            R.id.displayEEpromSlave -> {
                val intent = Intent(this, HexDataActivity::class.java)
                intent.putExtra("idProcessor", SLAVE)
                startActivity(intent)
            }
            R.id.removeProtectState -> {
                if (viewModel.statePCHZS.value == STATE_READY_READ_CODE) {
                    viewModel.removeProtectState()
                } else {
                    viewModel.textTvCodeMaster.value = getString(R.string.ERROR_CONNECT)
                }
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
            when (viewModel.statePCHZS.value ?: STATE_NO_CONNECT) {
                STATE_NO_CONNECT -> {
                    viewModel.clearDataScreen()
                    viewModel.flagActionConnect = true
                    viewModel.numberOfConnectionAttempts = 1
                    viewModel.flagfirstConnect = true
                    viewModel.bluetoothService.connectPCHZS()
                }
                STATE_READY_READ_CODE -> {
                    //если есть считанные и не сохраненные данные, показать диалог о необходимости сохранения
                    if (viewModel.flagSaveNeed) {
                        viewModel.setDataDialog(
                            title = "ЗС считано но данные не сохранены",
                            message = "Сохранить?",
                            negativeButton = "Нет",
                            negativeFun = {
                                viewModel.flagSaveNeed = false
                                viewModel.readProtectState()
                            },
                            positiveButton = "Да",
                            positiveFun = {
                                viewModel.viewModelScope.launch(Dispatchers.IO) {
                                    viewModel.saveDataToFiles()
                                    viewModel.readProtectState()
                                }
                            }
                        )
                        viewModel.flagCreateDialog.value = true
                    } else {
                        viewModel.readProtectState()
                    }
                }
            }
        }
        //обработчик нажатия на окна tvCodeMaster и tvCodeSlave
        tvCodeMaster.setOnClickListener {
            val intent = Intent(this, ExtendedDataActivity::class.java)
            intent.putExtra("idProcessor", MASTER)
            intent.putExtra("typeCode", FLAG_DECODE_CODE)
            startActivity(intent)
        }
        tvCodeSlave.setOnClickListener {
            val intent = Intent(this, ExtendedDataActivity::class.java)
            intent.putExtra("idProcessor", SLAVE)
            intent.putExtra("typeCode", FLAG_DECODE_CODE)
            startActivity(intent)
        }
        tvCodeMaster.setOnLongClickListener {
            val intent = Intent(this, ExtendedDataActivity::class.java)
            intent.putExtra("idProcessor", MASTER)
            intent.putExtra("typeCode", FLAG_DECODE_NON_ERASABLE_CODE)
            startActivity(intent)
            true
        }
        tvCodeSlave.setOnLongClickListener {
            val intent = Intent(this, ExtendedDataActivity::class.java)
            intent.putExtra("idProcessor", SLAVE)
            intent.putExtra("typeCode", FLAG_DECODE_NON_ERASABLE_CODE)
            startActivity(intent)
            true
        }


        //Подписаться на данные из viewModel
        viewModel.statePCHZS.observe(this, { state -> setStateView(state) })
        viewModel.managerBluetooth.observe(this, { command ->
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
                    viewModel.bluetoothService.showBtLog("MainActivity:  Запрос разрешения поиска (из активити)")
                }
                COMMAND_WRITE_PERMISSION_REQUEST -> {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PERMISSION_REQUEST_CODE_WRITE_READ_EXTERNAL_STORAGE
                    )
                    viewModel.bluetoothService.showBtLog("MainActivity:  Запрос разрешения записи и чтения памяти (из активити)")
                }
            }
        })
        viewModel.stateBluetoothAdapter.observe(this, { state ->
            when (state) {
//                BluetoothAdapter.STATE_TURNING_ON -> progressBarMainActivity.visibility = View.VISIBLE
//                BluetoothAdapter.STATE_TURNING_OFF -> progressBarMainActivity.visibility = View.VISIBLE
//                BluetoothAdapter.STATE_ON -> progressBarMainActivity.visibility = View.INVISIBLE
//                BluetoothAdapter.STATE_OFF -> progressBarMainActivity.visibility = View.INVISIBLE
            }
        })
        viewModel.textTvCodeMaster.observe(this, { text -> tvCodeMaster.text = text })
        viewModel.textTvCodeSlave.observe(this, { text -> tvCodeSlave.text = text })
        viewModel.textTvIdDeviceMaster.observe(this, { text -> tvIdDeviceMaster.text = text })
        viewModel.textTvIdDeviceSlave.observe(this, { text -> tvIdDeviceSlave.text = text })
        viewModel.flagAppOff.observe(this, {
            if (it) {
                super.onBackPressed()
            }
        })
        viewModel.flagCreateDialog.observe(this, {
            if (it) {
                viewModel.flagCreateDialog.value = false
                ConfirmationDialogFragment().show(supportFragmentManager, ConfirmationDialogFragment.TAG)
            }
        })
        viewModel.toast.observe(this, {
            if (!it.equals("")) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })


        MainViewModel.instansViewModel = viewModel   //Сохранить ссылку в экземпляре viewModel


        //vvvvvvvvvv
        //viewModel.readDataFromFile(Uri.fromFile(File("/storage/emulated/0/PCHZS/ГАРС-С__261121_1550_master.hex")))
        //viewModel.readDataFromFile(Uri.fromFile(File("/storage/emulated/0/PCHZS/ГАРС-С__slave_200721_1822.hex")))

        //vvvvvvvvvv
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentIn: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentIn)

        if (requestCode == REQUEST_CODE_BLUETOOTH_TUNE_ON) {
            //Ответ на запрос включения Bluetooth
            if (resultCode == Activity.RESULT_CANCELED) {
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

        if (requestCode == REQUEST_CODE_SELECTION_FILE_TO_READ) {
            //ответ на выбор файла для чтения
            if (resultCode == RESULT_OK) {
                val uriFile = intentIn?.data ?: return
                //val contentResolver = contentResolver
                //viewModel.readDataFromFile(uriFile, contentResolver, applicationContext)
                //viewModel.readDataFromFile(uriFile, applicationContext)
                viewModel.readDataFromFile(uriFile)
            }
        }

        if (requestCode == REQUEST_CODE_SELECTION_FILE_TO_SEND) {
            if (resultCode == RESULT_OK) {
                val uri = intentIn?.data
                if (uri != null) {
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        //val uri = uri
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "image/jpeg"
                        //type = "file/*"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Выберите приложение для отправки"))
                } else {
                    Toast.makeText(this, "Ошибка выбора файла Uri = null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ошибка выбора файла. resultCode = CANCELED", Toast.LENGTH_SHORT).show()
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
        //если пришел ответ на запрос разрешения чтения или записи памяти
        if (requestCode == PERMISSION_REQUEST_CODE_WRITE_READ_EXTERNAL_STORAGE && grantResults.size == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.bluetoothService.showBtLog("MainActivity:  Разрешение поиска получено")
            } else {
                //прервать выполнение коррутин
                viewModel.viewModelScope.coroutineContext.cancelChildren()

                viewModel.bluetoothService.showBtLog("MainActivity:  Запрос разрешения отклонен")
                Toast.makeText(
                    applicationContext,
                    "Без выдачи разрешения чтение и запись не возможна",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    //Установка свойств обектов на экране в зависимости  сосотяния системы ПЧЗС
    private fun setStateView(statePCHZS: Int?) {
        statePCHZS.let {
            when (statePCHZS) {
                STATE_ERROR_BLUETOOTH_INIT -> {
                    btReadProtectState.text = getString(R.string.ERROR_INIT_BLUETOOTH)
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
                        if (viewModel.numberOfConnectionAttempts < NUMBER_ATTEMPTS_AUTO_CONNECT) {
                            viewModel.clearDataScreen()
                            viewModel.flagActionConnect = true
                            //если уже было три попытки соединения по сохраненному адресу
                            if (viewModel.adressPchzs.isNotEmpty() && viewModel.numberOfConnectionAttempts == 3) {
                                //сбросить адрес
                                viewModel.adressPchzs = ""
                            }
                            viewModel.numberOfConnectionAttempts++
                            viewModel.bluetoothService.showBtLog(
                                "setStateView():  повторное подключение - старт connectPCHZS(), " +
                                        "попытка № ${viewModel.numberOfConnectionAttempts}"
                            )
                            viewModel.bluetoothService.connectPCHZS()

                        } else {
                            viewModel.bluetoothService.showBtLog("setStateView():  Колличество попыток подключения превысило норму, повторного подключения не нужно")
                            viewModel.textTvCodeMaster.postValue(getString(R.string.ERROR_CONNECT))
                            viewModel.flagActionConnect = false
                            viewModel.numberOfConnectionAttempts = 0     //ddd сбросить счетчик автоподключений

                        }
                    }
                }
                STATE_READY_READ_CODE -> {
                    btReadProtectState.setText(R.string.READY_READ_CODE)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorGREEN))
                    progressBarMainActivity.visibility = View.INVISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_READY_READ_CODE")
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
                STATE_READ_CODE_IN_PROGRESS -> {
                    btReadProtectState.setText(R.string.READ_CODE_IN_PROGRESS)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorBLUE))
                    progressBarMainActivity.visibility = View.VISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_READ_CODE_IN_PROGRESS")
                }
                STATE_REMOVE_PROTECT_CODE_IN_PROGRESS -> {
                    btReadProtectState.setText(R.string.REMOVE_PROTECT_CODE_IN_PROGRESS)
                    btReadProtectState.setBackgroundColor(resources.getColor(colorBLUE))
                    progressBarMainActivity.visibility = View.VISIBLE
                    viewModel.bluetoothService.showBtLog("MainActivity:  Сработало STATE_REMOVE_PROTECT_CODE_IN_PROGRESS")
                }
            }
        }
    }

    //Отправить файл с EEPROM через E-mail или мессенджер
    private fun sendFile() {
        //если считанных данных нет то выбор файлов для отправки
        if (eePromMaster.value?.isEmpty() ?: true && eePromSlave.value?.isEmpty() ?: true) {
            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                Toast.makeText(this, "SD-карта не доступна", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("*/*")
            startActivityForResult(intent, REQUEST_CODE_SELECTION_FILE_TO_SEND)
        } else {// ели есть считанные данные, то диалог что отправлять, считанные данные или сохраненные ранее
            viewModel.setDataDialog(
                title = "Отправить данные об отказе",
                message = "Какие данные отправлять?",
                positiveButton = "Считанные данные",
                positiveFun = {
                    //viewModel.sendFiles()
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        viewModel.saveDataToFiles()
                        val shareIntent: Intent = Intent()
                        shareIntent.action = Intent.ACTION_SEND_MULTIPLE
                        shareIntent.type = "image/jpeg"

                        val uris = arrayListOf<Uri>()
                        if (!viewModel.patchFileMaster.isEmpty()) {
                            var uriMmaster = Uri.fromFile(File(viewModel.patchFileMaster))
                            uris.add(uriMmaster)
                        }
                        if (!viewModel.patchFileMaster.isEmpty()) {
                            var uriSlave = Uri.fromFile(File(viewModel.patchFileSlave))
                            uris.add(uriSlave)
                        }
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

                        startActivity(Intent.createChooser(shareIntent, "Выберите приложение для отправки"))
                    }
                },
                negativeButton = "Сохраненные ранее",
                negativeFun = {
//                val intent = Intent(Intent.ACTION_PICK)
//                val uri = Uri.parse(Environment.getExternalStorageDirectory().path + "/${Constants.NAME_DIRECTORY}/")
//                intent.setDataAndType(uri, "*/*")
//                startActivityForResult(
//                    Intent.createChooser(intent, "Выберете приложение для поиска файла"),
//                    REQUEST_CODE_SELECTION_FILE_TO_SEND
//                )
                    // проверяем доступность SD
                    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                        Toast.makeText(this, "SD-карта не доступна", Toast.LENGTH_SHORT).show()
                        return@setDataDialog
                    }

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("*/*")
                    startActivityForResult(intent, REQUEST_CODE_SELECTION_FILE_TO_SEND)
                }
            )
            ConfirmationDialogFragment().show(supportFragmentManager, ConfirmationDialogFragment.TAG)
        }

    }

    //чтение файла ЗС из памяти
    private fun downloadFileFromMemory() {
        // проверяем доступность SD
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Toast.makeText(this, "SD-карта не доступна", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        //если есть считанные и не сохраненные данные, показать диалог о необходимости сохранения
        if (viewModel.flagSaveNeed) {
            viewModel.setDataDialog(
                title = "ЗС считано но данные не сохранены",
                message = "Сохранить?",
                negativeButton = "Нет",
                negativeFun = {
                    viewModel.flagSaveNeed = false
                    startActivityForResult(intent, REQUEST_CODE_SELECTION_FILE_TO_READ)
                },
                positiveButton = "Да",
                positiveFun = {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        viewModel.saveDataToFiles()
                        startActivityForResult(intent, REQUEST_CODE_SELECTION_FILE_TO_READ)
                    }
                }
            )
            viewModel.flagCreateDialog.value = true
        } else {
            startActivityForResult(intent, REQUEST_CODE_SELECTION_FILE_TO_READ)
        }
    }


}