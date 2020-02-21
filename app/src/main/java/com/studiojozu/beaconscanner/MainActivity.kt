package com.studiojozu.beaconscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.studiojozu.beaconscanner.beacon.BeaconManager
import com.studiojozu.beaconscanner.beacon.BeaconManagerFactory
import com.studiojozu.beaconscanner.event.AddLogEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
    }

    private val eventBus: EventBus = EventBus.getDefault()
    private val logLines = mutableListOf<String>()
    private lateinit var beaconManager: BeaconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        beaconManager = BeaconManagerFactory.getInstance(applicationContext)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        setEvents()
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    override fun onPause() {
        eventBus.unregister(this)
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) return

        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
    }

    @Subscribe
    fun addLogs(event: AddLogEvent) {
        val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        logLines.add("${formatter.format(Calendar.getInstance().time)} ${event.message}")
        if (logLines.count() > 100) {
            logLines.removeAt(10)
        }
        textLog.text = ""
        for (log in logLines) {
            textLog.append("$log\n")
        }
        scrollLog.post { scrollLog.fullScroll(View.FOCUS_DOWN) }
    }

    private fun setEvents() {
        buttonStartService.setOnClickListener {
            beaconManager.onStart()
        }
        buttonStopService.setOnClickListener {
            beaconManager.onStop()
        }

        viewStartDonate.setOnClickListener {
            beaconManager.onStop()

            val intent = Intent(this, DonateActivity::class.java)
            startActivity(intent)
        }
    }
}
