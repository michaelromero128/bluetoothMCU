package com.example.bluetoothmcu

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.companion.CompanionDeviceManager
import android.content.pm.PackageManager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.os.Handler
import android.os.Message
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import java.util.*
private const val BLUETOOTH_CONNECT_REQUEST = 3

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
const val uuid: String = "00001101-0000-1000-8000-00805F9B34FB"


class getDevices : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var device : BluetoothDevice? = null
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var  tv: TextView
    private lateinit var  button: Button
    private lateinit var bluetoothService: MyBluetoothService
    private lateinit var handler: Handler
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("CUSTOMA","get devices activity started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_devices)
        device = intent.getParcelableExtra("bt")
        tv = findViewById(R.id.tvResponse)
        button = findViewById(R.id.button)
        tv.setText("Information from bluetooth")
        val model = ViewModelProvider(this)[myViewModel::class.java]
        model.device = device
        val socket = device?.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        socket?.connect()
        model.socket = socket

        //Log.i("CUSTOMA","${socket.toString()}")
        Log.i("CUSTOMA","${device?.name}")

        model.listen(tv)

        button.setOnClickListener {
            model.send("wuzzle\n",tv)
        }






    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(BLUETOOTH_CONNECT_REQUEST)
    fun getConnection(){
        if(EasyPermissions.hasPermissions(this,Manifest.permission.BLUETOOTH_ADMIN)){
            Log.i("CUSTOMA", "device name: ${device?.name}")
            Log.i(" CUSTOMA","newActivity started: waaa")


        }else{
            Log.i("CUSTOMA","need permission")
            EasyPermissions.requestPermissions(this,"I need bluetooth scan", BLUETOOTH_CONNECT_REQUEST, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH)
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray

    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== BLUETOOTH_CONNECT_REQUEST){
            Log.i("CUSTOMA","permission request returned")
            grantResults.forEach { ele-> Log.i("CUSTOMA",ele.toString()) }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }








}
@SuppressLint("MissingPermission")
class MyBluetoothService(activity: MainActivity): Thread(){
    private var  cancelled: Boolean = false
    private var serverSocket: BluetoothServerSocket?
    private val activity = activity

    init {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            this.serverSocket =
                btAdapter.listenUsingRfcommWithServiceRecord("phone", UUID.fromString(uuid))
            this.cancelled = false
        } else {
            this.serverSocket = null
            this.cancelled = true
        }
    }

    override fun run(){
        var socket: BluetoothSocket
        while(true){
            if(this.cancelled){
                break
            }
            try{
                socket = serverSocket!!.accept()
            }catch(e:IOException){
                Log.i("CUSTOMA","server socket accept failed")
                break
            }
            if(!this.cancelled && socket != null){
                Log.i("server","Connecting")
                //BluetoothServer(this.activity,socket).start()
            }
        }
    }
    fun cancel() {
        this.cancelled = true
        this.serverSocket!!.close()
    }
}


