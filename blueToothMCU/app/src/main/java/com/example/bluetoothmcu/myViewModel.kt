package com.example.bluetoothmcu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.TextView
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
                    if ( 14 < bytesAvailable!!) {
                        val bytes: Int?= mmInStream?.read(mmBuffer)
                        Log.i("CUSTOMER", "Got a message")
                        for (byte: Byte in mmBuffer) {
                            val char: Int = byte.toInt()
                            Log.i("CUSTOMERb",char.toString())
                            if(char== 0|| char.toChar() =='\t'){
                                continue
                            }else if(char == 10){
                                Log.i("CUSTOMERa",builder.toString())
                                textView.text = "Output from board:\n ${builder.toString()}"
                                builder.setLength(0)
                            }else{
                                builder.append(char.toChar())
                            }

                        }

                    }
                }catch(e:Exception){
                    Log.i("CUSTOMA","bad read")
                }
            }
        }
    }
    fun send(string:String,textView:TextView){
        viewModelScope.launch(Dispatchers.IO){
            while(true){

            }
        }
    }

    fun readMessage(builder: StringBuilder,textView:TextView){

    }
}