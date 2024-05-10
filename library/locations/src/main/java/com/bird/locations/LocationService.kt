package com.bird.locations

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bird.locations.client.LocationClient
import com.bird.locations.data.repository.LocationsRepository
import com.bird.locations.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LocationService : Service() {

    private var interval: Long = defaultInterval

    private val locationsRepository: LocationsRepository = ServiceLocator.locationsRepository

    private val locationClient: LocationClient = ServiceLocator.locationClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.provideApplicationContext(applicationContext)
        setupNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            interval = intent.getLongExtra(INTERVAL_EXTRA, defaultInterval)
        }
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, LocationService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.notification_stop_text),
                stopPendingIntent
            )
            .setOngoing(true)

        locationClient
            .getLocationUpdates(interval)
            .catch { e ->
                e.printStackTrace()
            }
            .onEach { location ->
                serviceScope.launch {
                    locationsRepository.updateLocation(location.longitude, location.latitude)
                        .collect {
                            Log.e("LocationService", it.toString())
                        }
                }
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val INTERVAL_EXTRA = "interval_extra"
        const val NOTIFICATION_CHANNEL = "location_notification_channel"

        const val defaultInterval = 5000L
    }
}
