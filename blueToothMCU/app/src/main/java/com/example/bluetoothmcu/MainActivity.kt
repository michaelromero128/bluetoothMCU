package com.example.bluetoothmcu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.regex.Pattern


private const val SELECT_DEVICE_REQUEST_CODE = 4
private const val REQUEST_ENABLE_BT = 1
private const val PERMISSION_REQUEST_BT = 2
private const val PERMISSION_REQUEST_BT_SCAN = 3


class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {


    private lateinit var layout: View
    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.main_layout)


        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            //start activity saying you need bluetooth
        }
        Log.i("CUSTOMA","GOT TO THIS POINT")
        if(bluetoothAdapter != null && bluetoothAdapter?.isEnabled == false){
            enableBT()
        }
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .setNamePattern(Pattern.compile("ESP32test"))
            .build()

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of them appears.
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(true)
            .build()

        // When the app tries to pair with a Bluetooth device, show the
        // corresponding dialog box to the user.
        deviceManager.associate(pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    Log.i("CUSTOMA","devicefound")
                    startIntentSenderForResult(chooserLauncher,
                        SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
                }

                override fun onFailure(error: CharSequence?) {
                    // Handle the failure.
                }
            }, null)

    }
    @SuppressLint("MissingPermission")
    fun enableBT(){
        Log.i("CUSTOM","bluetooth not set triggered")
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
                    REQUEST_ENABLE_BT -> when(resultCode) {
                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth device.
                        Log.i("CUSTOMA","bt enabled")
                        // Maintain continuous interaction with a paired device.
                    }
                }
                Activity.RESULT_CANCELED -> {
                    enableBT()
                }

            SELECT_DEVICE_REQUEST_CODE ->when(resultCode){
                Activity.RESULT_OK -> {
                    Log.i("CUSTOMA","starting function to load intent")
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->


                        Log.i("CUSTOMA", "Start new intent")
                        device.createBond()
                        val zeIntent = Intent(this,getDevices::class.java)
                        zeIntent.putExtra("bt",device)
                        startActivity(zeIntent)
                        Log.i("CUSTOMA","End branch")
                    }
                }
            }
            else -> {

                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }



    fun View.showSnackbar(msgId: Int, length: Int) {
        showSnackbar(context.getString(msgId), length)
    }

    fun View.showSnackbar(msg: String, length: Int) {
        showSnackbar(msg, length, null, {})
    }

    fun View.showSnackbar(
        msgId: Int,
        length: Int,
        actionMessageId: Int,
        action: (View) -> Unit
    ) {
        showSnackbar(context.getString(msgId), length, context.getString(actionMessageId), action)
    }

    fun View.showSnackbar(
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(this, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray

    ) {
        Log.i("CUSTOMA", "request permission call back fired")
        if(requestCode ==PERMISSION_REQUEST_BT_SCAN){
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("CUSTOMA","Scan permission granted")
            }else{
               Log.i("CUSTOMA","Scan permission denied")
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}