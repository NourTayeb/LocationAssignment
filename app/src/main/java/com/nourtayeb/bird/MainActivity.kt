package com.nourtayeb.bird

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bird.locations.LocationUpdateUseCase
import com.bird.locations.api.LocationUpdatesSdk
import com.nourtayeb.bird.ui.theme.BirdAssignmentTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    class MyViewModel(private val locationUpdateUseCase: LocationUpdateUseCase = LocationUpdatesSdk.getLocationUpdateUseCase()) :
        ViewModel() {
        fun onSendLocationOneTime() {
            viewModelScope.launch {
                locationUpdateUseCase.sendUpdatedLocation().collect {
                    Log.e("MyViewModel", "result ${it}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocationUpdatesSdk.init(applicationContext)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )
        setContent {
            BirdAssignmentTheme {
                MainScreen(MyViewModel())
            }
        }
    }


    @Composable
    fun MainScreen(myViewModel: MyViewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = {
                LocationUpdatesSdk.startLocationUpdatesService(8000L)
            }) {
                Text(text = "Start")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                LocationUpdatesSdk.stopLocationUpdatesService()
            }) {
                Text(text = "Stop")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                myViewModel.onSendLocationOneTime()
            }) {
                Text(text = "Send onetime")
            }
        }
    }
}
