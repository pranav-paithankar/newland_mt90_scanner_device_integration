package com.example.newland_mt90_device_integration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin
import android.util.Log

class MainActivity : FlutterActivity() {
    private val channelName = "scanQR"
    private val scannerAction = "nlscan.action.SCANNER_TRIG"

    private var flutterEngine: FlutterEngine? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("MyApp", "On Receive on native end ${intent.action}")

            if (intent.action == "nlscan.action.SCANNER_RESULT") {
                val barcode1 = intent.getStringExtra("SCAN_BARCODE1")
                val barcodeType = intent.getIntExtra("SCAN_BARCODE_TYPE", -1)
                val scanStatus = intent.getStringExtra("SCAN_STATE")
                Log.d("MyApp", "status: ${scanStatus}")

                if ("ok" == scanStatus) {
                    Toast.makeText(
                        baseContext, "Scanned successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("MyApp", "Barcode1 $barcode1")
                    Log.d("MyApp", "BarcodeType $barcodeType")

                    // Success
                    sendBarcodeDataToFlutter(barcode1, barcodeType)
                } else {
                    Toast.makeText(
                        baseContext, "Scanning failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Failure, e.g., operation timed out
                    sendBarcodeDataToFlutter(null, -1)
                }
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        this.flutterEngine = flutterEngine

        val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)

        // Register the BroadcastReceiver to listen for the broadcast
        val filter = IntentFilter("nlscan.action.SCANNER_RESULT")

        channel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result? ->
            try {
                Log.d("MyApp", "Method call received: ${call.method}")
                if ("showQRData" == call.method) {
                    val intent = Intent(scannerAction)
                    intent.putExtra("SCAN_TIMEOUT", 5)
                    intent.putExtra("SCAN_TYPE", 2)
                    Log.d("MyApp", "Method call received inside ${intent.action}")
                    sendBroadcast(intent)
                }
            } catch (err: Exception) {
                Log.d("MyApp", "Method call received: ${call.method} and $err")
            }
        }

        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun sendBarcodeDataToFlutter(barcode1: String?, barcodeType: Int) {
        val messenger = flutterEngine?.dartExecutor?.binaryMessenger
        messenger?.let {
            val channel = MethodChannel(it, channelName)
            val dataMap = mapOf(
                "barcode1" to (barcode1 ?: ""),
                "barcodeType" to barcodeType
            )
            Log.d("MyApp", "Data is: $dataMap")

            channel.invokeMethod("onBarcodeScanned", dataMap)
        }
    }
}