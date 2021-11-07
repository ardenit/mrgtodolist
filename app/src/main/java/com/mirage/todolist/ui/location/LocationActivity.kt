package com.mirage.todolist.ui.location

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.mirage.todolist.BuildConfig
import com.mirage.todolist.R
import timber.log.Timber
import kotlin.math.min


class LocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private lateinit var placesClient: PlacesClient

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"

    // Used for selecting the current place.
    private val M_MAX_ENTRIES = 5
    private lateinit var likelyPlaceNames: Array<String>
    private lateinit var likelyPlaceAddresses: Array<String>
    private lateinit var likelyPlaceAttributions: Array<List<*>?>
    private lateinit var likelyPlaceLatLngs: Array<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            lastKnownLocation = it.getParcelable(KEY_LOCATION)
            cameraPosition = it.getParcelable(KEY_CAMERA_POSITION)
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
            showCurrentPlace()
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
        // Prompt the user for permission.
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    override fun onMapClick(location: LatLng) {
        //TODO

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
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private fun showCurrentPlace() {
        map ?: return
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result
                    // Set the count, handling cases where less than 5 entries are returned.
                    val count = min(likelyPlaces.placeLikelihoods.size, M_MAX_ENTRIES)
                    val likelihoods = likelyPlaces.placeLikelihoods.take(count)
                    likelyPlaceNames = likelihoods.mapNotNull { it.place.name }.toTypedArray()
                    likelyPlaceAddresses = likelihoods.mapNotNull { it.place.address }.toTypedArray()
                    likelyPlaceAttributions = likelihoods.mapNotNull { it.place.attributions }.toTypedArray()
                    likelyPlaceLatLngs = likelihoods.mapNotNull { it.place.latLng }.toTypedArray()
                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    openPlacesDialog()
                } else {
                    Timber.e(task.exception)
                }
            }
        } else {
            // The user has not granted permission.
            Timber.v("The user did not grant location permission.")
            // Add a default marker, because the user hasn't selected a place.
            map?.addMarker(MarkerOptions()
                .title(getString(R.string.default_info_title))
                .position(defaultLocation)
                .snippet(getString(R.string.default_info_snippet)))
            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private fun openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        val listener = DialogInterface.OnClickListener { dialog, which ->
            // The "which" argument contains the position of the selected item.
            val markerLatLng = likelyPlaceLatLngs[which]
            var markerSnippet = likelyPlaceAddresses[which]
            if (likelyPlaceAttributions[which] != null) {
                markerSnippet += "\n" + likelyPlaceAttributions[which]
            }
            // Add a marker for the selected place, with an info window
            // showing information about that place.
            map?.addMarker(MarkerOptions()
                .title(likelyPlaceNames[which])
                .position(markerLatLng)
                .snippet(markerSnippet))
            // Position the map's camera at the location of the marker.
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, DEFAULT_ZOOM.toFloat()))
        }
        // Display the dialog.
        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
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
        // Keys for Intent parameters for selected place
        const val KEY_MARKER_LATITUDE = "marker_latitude"
        const val KEY_MARKER_LONGITUDE = "marker_longitude"
        const val KEY_MARKER_PLACE_NAME = "marker_place_name"
    }
}