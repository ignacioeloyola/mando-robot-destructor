package com.ignacioeloyola.mandodestructor

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ignacioeloyola.mandodestructor.databinding.ActivityMainBinding
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var bluetoothManager: BluetoothManager? = null
    private var socket: BluetoothSocket? = null

    companion object {
        private const val PERMISSION_CODE = 1001
        private const val BLUETOOTH_MODULE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = MainViewModel()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        initBluetooth()

        initListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.close()
    }

    private fun initBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            val permissionsToRequest = mutableListOf<String>()
            requiredPermissions.forEach { perm ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        perm
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(perm)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_CODE)
            } else {
                // All permissions are already granted
            }


            var robotBluetoothDevice: BluetoothDevice? = null
            val pairedDevices: Set<BluetoothDevice>? = bluetoothManager?.adapter?.bondedDevices
            pairedDevices?.forEach { device ->
                if (device.address == "MAC ADDRESS FROM MODULE TODO") { // TODO MAC ADDRESS
                    robotBluetoothDevice = device
                }
            }

            robotBluetoothDevice?.let {
                connectDevice(it)
            }
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        val uuid: UUID = UUID.fromString(BLUETOOTH_MODULE_UUID)
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        PERMISSION_CODE
                    )
                }
                return
            }
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            // You now have an open Bluetooth socket to the Arduino
        } catch (e: IOException) {
            // Handle the exception
        }
    }

    private fun initListeners() {
        binding.buttonSettings.setOnClickListener {
            // TODO GO TO SETTINGS
        }

        binding.buttonVolumeDown.setOnClickListener {
            sendCommand("D")
        }

        binding.buttonVolumeUp.setOnClickListener {
            sendCommand("U")
        }

        binding.buttonLaser.setOnClickListener {

        }

        binding.buttonBlades.setOnClickListener {

        }

        binding.buttonFlamethrower.setOnClickListener {

        }

        binding.buttonSmoke.setOnClickListener {

        }

        binding.buttonHackerspace.setOnClickListener {

        }
    }

    private fun sendCommand(command: String) {
        socket?.let {
            if (it.isConnected) {
                try {
                    val outputStream: OutputStream = it.outputStream
                    outputStream.write(command.toByteArray())
                } catch (e: IOException) {
                    // Handle the exception
                }
            }
        }
    }
}