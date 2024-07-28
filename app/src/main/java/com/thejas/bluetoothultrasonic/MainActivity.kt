package com.thejas.bluetoothultrasonic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val TAG = "BluetoothApp"
    private val DEVICE_ADDRESS = "98:D3:61:F7:1C:FC" // Replace with your module's MAC address
    private val UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var btSocket: BluetoothSocket? = null
    private var isBluetoothConnected = false
    private lateinit var dataTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var buttonOn: Button
    private lateinit var buttonOff: Button
    private var outputStream: OutputStream? = null
    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataTextView = findViewById(R.id.textView)
        distanceTextView = findViewById(R.id.distanceTextView)


        textToSpeech = TextToSpeech(this, this)

        ConnectBluetoothTask().start()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
        }
    }

    private inner class ConnectBluetoothTask : Thread() {
        override fun run() {
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)

            try {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                    btSocket = device?.createRfcommSocketToServiceRecord(UUID_INSECURE)
                    btSocket?.connect()
                    isBluetoothConnected = true
                    Log.d(TAG, "Connected to Bluetooth device")
                    outputStream = btSocket?.outputStream

                    ReceiveDataTask().start()
                } else {
                    Log.e(TAG, "Bluetooth permissions are not granted")
                }
            } catch (e: IOException) {
                isBluetoothConnected = false
                Log.e(TAG, "Error connecting to Bluetooth device", e)
            }
        }
    }

    private inner class ReceiveDataTask : Thread() {
        override fun run() {
            val inputStream: InputStream? = btSocket?.inputStream
            val buffer = ByteArray(1024)
            var bytes: Int
            var message = ""

            while (isBluetoothConnected) {
                try {
                    bytes = inputStream?.read(buffer) ?: 0
                    if (bytes > 0) {
                        message += String(buffer, 0, bytes)
                        if (message.contains("\n")) {
                            val completeMessage = message.trim()
                            message = "" // Reset message buffer

                            Log.d(TAG, "Received: $completeMessage")

                            runOnUiThread {
                                val distance = completeMessage.toDoubleOrNull()
                                if (distance != null) {
                                    distanceTextView.text = "Distance: $completeMessage cm"
                                    if (distance < 15 && !textToSpeech!!.isSpeaking) {
                                        textToSpeech?.speak("Object in front", TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading from Bluetooth device", e)
                    isBluetoothConnected = false
                    break
                }
            }
        }
    }

    private fun sendCommand(command: String) {
        if (isBluetoothConnected) {
            try {
                outputStream?.write(command.toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, "Error sending command", e)
                Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        btSocket?.close()
        textToSpeech?.shutdown()
    }
}
