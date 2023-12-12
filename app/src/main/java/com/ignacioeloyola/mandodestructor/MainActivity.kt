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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
    private var previous = true

    companion object {
        private const val PERMISSION_CODE = 1001
        private const val BLUETOOTH_MODULE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var dynamicCommand: (() -> String)? = null

    private val commandRunnable = object : Runnable {
        override fun run() {
            // Check if the dynamicCommand function is not null before sending
            dynamicCommand?.let { commandFunction ->
                val command = commandFunction.invoke()
                sendCommand(command)
            }

            // Schedule the next command after 50 milliseconds
            handler.postDelayed(this, 50)
        }
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
        handler.removeCallbacks(commandRunnable)
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
                if (device.address == "A8:42:E3:90:6A:9A") { // TODO MAC ADDRESS
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

    private val buttonLaserTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                sendCommand("U")
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonFlamethrowerTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                sendCommand("X")
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonSmokeTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                sendCommand("W")
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonBladesTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                if (previous) {
                    sendCommand("V")
                } else {
                    sendCommand("v")
                }
                previous = !previous
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonForwardTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                dynamicCommand = { "F" }
                handler.post(commandRunnable)
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonBackTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                dynamicCommand = { "B" }
                handler.post(commandRunnable)
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonLeftTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                dynamicCommand = { "L" }
                handler.post(commandRunnable)
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }

    private val buttonRightTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start sending "U" commands when the button is pressed
                dynamicCommand = { "R" }
                handler.post(commandRunnable)
            }
            MotionEvent.ACTION_UP -> {
                // Stop sending commands when the button is released
                handler.removeCallbacks(commandRunnable)
                dynamicCommand = null
                dynamicCommand = { "S" }
                handler.post(commandRunnable)
            }
        }
        true
    }


    private fun initListeners() {
        binding.buttonSettings.setOnClickListener {
            // TODO GO TO SETTINGS
        }

        binding.buttonVolumeDown.setOnClickListener {

        }

        binding.buttonVolumeUp.setOnClickListener {

        }

        binding.buttonBlades.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonBladesTouchListener.onTouch(v, event)
        }

        binding.buttonFlamethrower.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonFlamethrowerTouchListener.onTouch(v, event)
        }

        binding.buttonSmoke.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonSmokeTouchListener.onTouch(v, event)
        }

        binding.buttonLaser.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonLaserTouchListener.onTouch(v, event)
        }

        binding.buttonForward.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonForwardTouchListener.onTouch(v, event)
        }

        binding.buttonBack.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonBackTouchListener.onTouch(v, event)
        }

        binding.buttonLeft.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonLeftTouchListener.onTouch(v, event)
        }

        binding.buttonRight.setOnTouchListener { v, event ->
            // Call performClick to handle the click event
            v.performClick()
            buttonRightTouchListener.onTouch(v, event)
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