package yq.treasureHunt

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var userMarker: Marker
    private lateinit var taskMarker: Marker
    private lateinit var taskLoc: LatLng

    private var taskIndex = 0
    private var gameStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        play_btn.setOnClickListener() {
            startTrackUserLoc()
            initializeTask()
            gameStatus = true;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(TAG, "User last known location: $location")

                    val homeLatLng = LatLng(location.latitude, location.longitude)

                    if (!::userMarker.isInitialized) {
                        userMarker = map.addMarker(
                            MarkerOptions().position(homeLatLng).title("You Are Here!")
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))
                    } else {
                        userMarker.setPosition(homeLatLng)
                    }

                    // Check task accomplishment
                    if (gameStatus) {
                        checkFinishTask(location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        setLocRequest()
        getUserLoc()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initializeTask() {
        if (taskIndex < TasksConstants.NUM_TASKS) {
            taskLoc = TasksConstants.TASK_DATA[taskIndex].latLong


            if (!::taskMarker.isInitialized) {
                taskMarker = map.addMarker(
                    MarkerOptions()
                        .position(taskLoc)
                        .title(TasksConstants.TASK_DATA[taskIndex].id)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            } else {
                taskMarker.setPosition(taskLoc)
                if (!taskMarker.isVisible) {
                    taskMarker.setVisible(true)
                }
            }
        } else {
            gameStatus = false
            stopLocationUpdates()
            taskMarker.setVisible(false)
            taskIndex = 0

            Snackbar.make(
                findViewById(R.id.linearLayout),
                "You Finished All tasks Successfully",
                Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private fun checkFinishTask(lat: Double, lng: Double) {
        var dis = getAirDistance(
            TasksConstants.TASK_DATA[taskIndex].latLong.latitude,
            TasksConstants.TASK_DATA[taskIndex].latLong.longitude,
            lat,
            lng
        )
        Log.d(TAG, "Distance= " + dis)

        if (dis < 150) {
            Snackbar.make(
                findViewById(R.id.linearLayout),
                "You finished task: " + (taskIndex + 1) + "/" + TasksConstants.NUM_TASKS,
                Snackbar.LENGTH_LONG
            ).show();

            ++taskIndex
            initializeTask()
        }
    }

    private fun getUserLoc() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                var homeLatLng = LatLng(location!!.latitude, location.longitude)
                userMarker =
                    map.addMarker(MarkerOptions().position(homeLatLng).title("You Are Here!"))

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))

            }
    }


    // Y.Q.
    private fun startTrackUserLoc() {
        fusedLocationClient.requestLocationUpdates(
            userLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Y.Q.
    fun setLocRequest() {
        userLocationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(userLocationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this@MapsActivity, 29)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, "Error: addOnFailureListener")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

    }
}

private const val TAG = "MapsActivity"
