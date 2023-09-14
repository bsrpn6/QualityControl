package info.onesandzeros.qualitycontrol.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerInfo
import com.symbol.emdk.barcode.ScannerResults
import info.onesandzeros.qualitycontrol.databinding.ActivityEmdkBarcodeScannerBinding

class EMDKBarcodeScannerActivity : BaseActivity(), EMDKListener,
    BarcodeManager.ScannerConnectionListener, Scanner.DataListener {
    private lateinit var binding: ActivityEmdkBarcodeScannerBinding

    private var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmdkBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeHardwareScanner()
    }

    private fun initializeHardwareScanner() {
        val results = EMDKManager.getEMDKManager(applicationContext, this)
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "EMDKManager object request failed!")
            return
        }
    }

    override fun onOpened(emdkManager: EMDKManager?) {
        binding.scannerProgressBar.visibility = View.VISIBLE

        this.emdkManager = emdkManager
        barcodeManager =
            emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager?
        scanner = barcodeManager?.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
        scanner?.addDataListener(this)
        scanner?.apply {
            addDataListener(this@EMDKBarcodeScannerActivity)
            try {
                enable()
                val config = scanner?.config
                config?.decoderParams?.code39?.enabled = true
                scanner?.config = config
                scanner?.read()
            } catch (e: ScannerException) {
                Log.e(TAG, "Exception while enabling scanner: ${e.message}")
            }
        }
    }

    override fun onClosed() {
        emdkManager?.let {
            if (barcodeManager != null) {
                barcodeManager = null
            }
            it.release()
            emdkManager = null
        }
    }

    override fun onData(scanDataCollection: ScanDataCollection?) {
        scanDataCollection?.let {
            if (it.result == ScannerResults.SUCCESS) {
                for (data in it.scanData) {
                    val barcodeData = data.data
                    val resultIntent = Intent()
                    resultIntent.putExtra("barcode_data", barcodeData)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    override fun onConnectionChange(
        scannerInfo: ScannerInfo?,
        connectionState: BarcodeManager.ConnectionState?
    ) {
        when (connectionState) {
            BarcodeManager.ConnectionState.CONNECTED -> {
                try {
                    scanner?.read()
                } catch (e: ScannerException) {
                    Log.e(TAG, "Error starting scanner read: ${e.message}")
                }
            }

            BarcodeManager.ConnectionState.DISCONNECTED -> {
                scanner?.removeDataListener(this)
                scanner = null
                binding.scannerProgressBar.visibility = View.GONE
            }

            else -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner?.let {
            try {
                it.cancelRead()
                it.disable()
            } catch (e: ScannerException) {
                Log.e(TAG, "Error disabling scanner: ${e.message}")
            }
        }
        onClosed()
    }

    companion object {
        private const val TAG = "EMDKBarcodeScannerActivity"
    }
}
