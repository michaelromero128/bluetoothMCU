package com.example.bluetoothmcu

import android.content.Context
import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import java.nio.charset.StandardCharsets


class MqttHelper(context: Context,zeCallback:(String)->Unit) {

    companion object {
        const val TAG = "CUSTOMA"
    }
    lateinit var reallyZeCallback:(String)->Unit;
    lateinit var client: Mqtt5AsyncClient
    init{
        reallyZeCallback = zeCallback
        client = MqttClient.builder().useMqttVersion5().identifier("android-Michael-R")
                //.serverAddress(InetSocketAddress("1d76043bee1b4d56afd3bd5c150f4157.s1.eu.hivemq.cloud",443))
            //.sslWithDefaultConfig()
            //.webSocketConfig(MqttWebSocketConfig.builder().subprotocol("mqtt").serverPath("/mqtt").build()).buildAsync();
            //.serverHost("1d76043bee1b4d56afd3bd5c150f4157.s1.eu.hivemq.cloud")
            .serverHost("broker.hivemq.com")
            //.sslWithDefaultConfig()
            .serverPort(1883)
        .buildAsync()
        //client.connectWith().simpleAuth()
            //.username("android")
          //  .password(UTF_8.encode("powerAndroid1")).applySimpleAuth()
        //.send()
        client.connect()
            .whenComplete {connAck, throwable ->
            if(throwable != null) {
                Log.i(TAG, "connect threw exception${throwable.stackTraceToString()}")
            }else{
                Log.i(TAG,"mqtt connected")
        }
        }

        subscribe()
    }
    fun subscribe(){

        client.toAsync().subscribeWith()
            .topicFilter("MichaelRFAU-AfterLock")
            .callback{ mqtt5Publish ->
                val message: String = String(mqtt5Publish.payloadAsBytes, StandardCharsets.UTF_8)
                Log.i(TAG, "message got got: ${message}")
                reallyZeCallback(message)
                if(message.equals("a")){
                    Log.i(TAG,"unlocked");

                    }
                if(message.equals("b")){
                    Log.i(TAG,"locked");
                }

            }.send().whenComplete{ subAck, throwable ->
            if(throwable !=null){
                Log.i(TAG,"subscribe messed up")
            }else{
                Log.i(TAG,"subscribe succeded")
            }
            }

    }
    fun publish(message:String){
        client.publishWith().topic("MichaelRFAU-SendLock").payload(message.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE).send()
            .whenComplete{ mqtt5Publish,throwable ->
                if(throwable != null) {
                    Log.i(TAG, throwable.stackTraceToString())
                } else{
                    Log.i(TAG, "lock status sent");
            }}


    }


}