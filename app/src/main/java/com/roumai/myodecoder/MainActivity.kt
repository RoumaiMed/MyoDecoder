package com.roumai.myodecoder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.core.app.ActivityCompat
import com.roumai.myodecoder.core.DataManager
import com.roumai.myodecoder.device.ble.MyoBleFinder
import com.roumai.myodecoder.device.ble.impl.BleDelegateDefaultImpl
import com.roumai.myodecoder.ui.main.Main
import com.roumai.myodecoder.ui.theme.MyoDecoderTheme

class MainActivity : AppCompatActivity() {
    private val bleFinder = mutableStateOf<MyoBleFinder?>(null)
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBlePermission()
        checkFilePermission()
        hideSystemUI()
        setContent {
            MyoDecoderTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                hideSystemUI()
                            }
                            false
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main(bleFinder.value)
                }
            }
        }
    }

    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun checkBlePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_PRIVILEGED
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_PRIVILEGED,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                ),
                1
            )
            return false
        }
        return true
    }

    fun checkFilePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                2
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            bleFinder.value = MyoBleFinder(true)
            BleDelegateDefaultImpl.init(this.application)
            checkFilePermission()
        }
        if (requestCode == 2) {
            // file auth...
            // init file path...
            DataManager.setRecordingDir(getExternalFilesDir(null)!!.absolutePath)
        }
    }
}