package renetik.android.sample.view

import android.annotation.SuppressLint
import android.graphics.Color.RED
import android.location.Location
import android.view.View
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.JointType.ROUND
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import renetik.android.base.layout
import renetik.android.controller.base.CSViewController
import renetik.android.extensions.title
import renetik.android.java.common.CSConstants.SECOND
import renetik.android.java.extensions.collections.previousLastItem
import renetik.android.location.asLatLng
import renetik.android.maps.CSMapClientController
import renetik.android.maps.CSMapController
import renetik.android.sample.R
import renetik.android.sample.model.model

@SuppressLint("MissingPermission")
class SampleMapPathController(title: String, private val mapController: CSMapController)
    : CSViewController<View>(navigation, layout(R.layout.sample_map_path)) {

    private val mapClient = CSMapClientController(this, R.id.SampleMap_Map, mapController)
    private val locationRequest = LocationRequest().apply {
        priority = PRIORITY_HIGH_ACCURACY
        interval = 2L * SECOND
        fastestInterval = 2L * SECOND
        smallestDisplacement = 5F
    }
    private val lineOptions
        get() = PolylineOptions().jointType(ROUND)
                .startCap(RoundCap()).endCap(RoundCap()).width(12F).color(RED)

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            model.mapRoute.add(result.lastLocation.asLatLng())
            model.mapRoute.previousLastItem?.let {
                mapController.map?.addPolyline(lineOptions.add(it).add(model.mapRoute.last()))
            }
            mapController.camera(result.lastLocation, 11f)
        }
    }

    init {
        title(R.id.SampleMap_Title, title)
        mapClient.onMapShowing { map ->
            map.isMyLocationEnabled = true
            mapController.map?.addPolyline(lineOptions.addAll(model.mapRoute))
            getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onViewHiding() {
        super.onViewHiding()
        getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback)
    }
}