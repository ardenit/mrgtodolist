package com.mirage.todolist.ui.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.mirage.todolist.BuildConfig
import com.mirage.todolist.R
import com.mirage.todolist.util.showToast
import timber.log.Timber

/**
 * Activity for selecting a specific location for the task using Google Maps API.
 */
class LocationActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private lateinit var placesClient: PlacesClient

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    /** Default starting location for the map if user does not grant location permission */
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    /**
     * The geographical location where the device is currently located. That is, the last-known
     * location retrieved by the Fused Location Provider.
     */
    private var lastKnownLocation: Location? = null

    private var currentMarker: Marker? = null
    /** Location of user-selected marker */
    private var currentMarkerLocation: LatLng? = null
    /** Name of the place of user-selected marker */
    private var currentPlaceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            lastKnownLocation = it.getParcelable(KEY_LOCATION)
            cameraPosition = it.getParcelable(KEY_CAMERA_POSITION)
        }
        intent.extras?.let {
            val markerLatitude = it.getDouble(KEY_MARKER_LATITUDE, 0.0)
            val markerLongitude = it.getDouble(KEY_MARKER_LONGITUDE, 0.0)
            val markerPlaceName = it.getString(KEY_MARKER_PLACE_NAME, "")
            if (markerLatitude != 0.0 && markerLongitude != 0.0) {
                currentMarkerLocation = LatLng(markerLatitude, markerLongitude)
            }
            if (markerPlaceName.isNotEmpty()) {
                currentPlaceName = markerPlaceName
            }
        }
        setContentView(R.layout.activity_location)
        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        placesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let {
            outState.putParcelable(KEY_CAMERA_POSITION, it.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            Timber.v("Saving place name $currentPlaceName into result")
            currentPlaceName?.let {
                val resultIntent = Intent()
                resultIntent.putExtra(KEY_RESULT_MARKER_PLACE_NAME, it)
                resultIntent.putExtra(KEY_RESULT_MARKER_LATITUDE, currentMarkerLocation?.latitude ?: 0.0)
                resultIntent.putExtra(KEY_RESULT_MARKER_LONGITUDE, currentMarkerLocation?.longitude ?: 0.0)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } ?: showToast(R.string.location_select_toast)
        }
        return true
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            // Return null here, so that getInfoContents() is called next.
            override fun getInfoContents(marker: Marker): View? = null

            override fun getInfoWindow(marker: Marker): View? {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById(R.id.fragment_map),
                    false
                )
                val title: TextView = infoWindow.findViewById(R.id.title)
                title.text = marker.title
                val snippet: TextView = infoWindow.findViewById(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })
        map.setOnMapClickListener(this)
        // Prompt the user for permission.
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
        currentMarkerLocation?.let {
            map.addMarker(MarkerOptions().position(it).title(getString(R.string.location_current_title)))
            map.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    override fun onMapClick(position: LatLng) {
        map?.apply {
            currentMarker?.let {
                it.position = position
            } ?: run {
                val marker = addMarker(MarkerOptions().position(position).title(getString(R.string.location_current_title)))
                currentMarker = marker
            }
            currentMarkerLocation = position
            currentPlaceName = getPlaceName(position)
            Toast.makeText(this@LocationActivity, currentPlaceName!!, Toast.LENGTH_SHORT).show()
            Timber.v(currentPlaceName)
            moveCamera(CameraUpdateFactory.newLatLng(position))
        }
        Timber.v("Map Click: $position")
    }

    private fun getPlaceName(position: LatLng): String {
        val geocoder = Geocoder(this)
        val addressMatches = geocoder.getFromLocation(position.latitude, position.longitude, 1)
        val bestMatch = addressMatches[0]
        return bestMatch.getAddressLine(0)
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                @SuppressLint("MissingPermission")
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(it.latitude, it.longitude), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Timber.v("Current location is null. Using defaults.")
                        Timber.e(task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                defaultLocation, DEFAULT_ZOOM.toFloat()
                            )
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (ex: SecurityException) {
            Timber.e(ex)
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
            }
        }
        updateLocationUI()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        map?.run {
            try {
                if (locationPermissionGranted) {
                    @SuppressWarnings("MissingPermission")
                    isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true
                } else {
                    @SuppressWarnings("MissingPermission")
                    isMyLocationEnabled = false
                    uiSettings.isMyLocationButtonEnabled = false
                    lastKnownLocation = null
                    getLocationPermission()
                }
            } catch (ex: SecurityException) {
                Timber.e(ex)
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for saved state Bundle
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Keys for Intent parameters for selected place
        const val KEY_MARKER_LATITUDE = "marker_latitude"
        const val KEY_MARKER_LONGITUDE = "marker_longitude"
        const val KEY_MARKER_PLACE_NAME = "marker_place_name"

        // Key for activity Result
        const val KEY_RESULT_MARKER_LATITUDE = "result_marker_latitude"
        const val KEY_RESULT_MARKER_LONGITUDE = "result_marker_longitude"
        const val KEY_RESULT_MARKER_PLACE_NAME = "result_marker_place_name"
    }
}