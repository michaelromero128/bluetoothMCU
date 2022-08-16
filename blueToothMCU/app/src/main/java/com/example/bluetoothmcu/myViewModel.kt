package com.example.bluetoothmcu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.*

const val nullByte: Byte = 0
class myViewModel: ViewModel() {
    public var device: BluetoothDevice? = null
    public var socket: BluetoothSocket? = null
    private lateinit var  mqttHelper: MqttHelper
    private lateinit var zeButton: Button;
    public val status = MutableLiveData<String>();


    @SuppressLint("MissingPermission")
    fun listen(textView: TextView) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("CUSTOMA", "Inside listening thread")
            val mmBuffer: ByteArray = ByteArray(1024)


            var mmInStream: InputStream? = socket?.inputStream

            Log.i("CUSTOMA", "${socket.toString()}")
            Log.i("CUSTOMA", "${device?.name}")
            Log.i("CUSTOMA", "${mmInStream?.available()}")
            while (true) {
                try {
                    val bytesAvailable: Int? = mmInStream?.available()
                    val builder: StringBuilder = StringBuilder()
                    if (14 < bytesAvailable!!) {
                        val bytes: Int? = mmInStream?.read(mmBuffer)
                        Log.i("CUSTOMER", "Got a message")
                        for (byte: Byte in mmBuffer) {
                            val char: Int = byte.toInt()
                            Log.i("CUSTOMERb", char.toString())
                            if (char == 0 || char.toChar() == '\t') {
                                continue
                            } else if (char == 10) {
                                Log.i("CUSTOMERa", builder.toString())
                                textView.text = "Output from board:\n ${builder.toString()}"
                                builder.setLength(0)
                            } else {
                                builder.append(char.toChar())
                            }

                        }

                    }
                } catch (e: Exception) {
                    Log.i("CUSTOMA", "bad read")
                }
            }
        }
    }

    fun send(string: String, textView: TextView) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val out = string.padEnd(15, ('\t')).encodeToByteArray()
                var mmOutStream = socket?.outputStream
                mmOutStream?.write(out)
            } catch (e: IOException) {
                Log.i("CUSTOMA", "write failed");
            }
        }
    }

    fun startMqtt(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                 val callback: (String) -> Unit =  { message ->
                     if(message == "a"){
                         status.postValue("a")
                     }else if(message =="b"){
                         status.postValue("b")
                     }else{
                         Log.i("CUSTOMA", "invalid message received")
                         status.postValue("unknown state")
                     }

                 };
                val buttonCallBack: ()->Unit = {
                    status.postValue("a")
                    Log.i("CUSTOMA","status update via button call back fired")
                }
                 mqttHelper  = MqttHelper(context,buttonCallBack,callback)

            } catch (e: IOException) {
                Log.i("CUSTOMA", "write failed");
            }
        }
    }
    fun sendLock(message:String){
        viewModelScope.launch(Dispatchers.IO){
            mqttHelper.publish(message);
        }
    }



}