package renetik.android.maps

import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import renetik.android.controller.base.CSViewController
import renetik.android.dialog.extensions.dialog
import renetik.android.java.event.CSEvent
import renetik.android.java.event.CSEvent.CSEventRegistration
import renetik.android.java.event.event
import renetik.android.java.event.execute
import renetik.android.logging.CSLog.logError
import renetik.android.maps.extensions.asLatLng
import kotlin.system.exitProcess

private const val DEFAULT_ZOOM = 13f

open class CSMapController(parent: CSViewController<*>, private val options: GoogleMapOptions) : CSViewController<MapView>(parent) {

    var map: GoogleMap? = null
    private val onMapReadyEvent: CSEvent<GoogleMap> = event()
    private var animatingCamera = false
    private var onCameraMoveStartedByUser = event<GoogleMap>()
    fun onCameraMoveStartedByUser(function: (GoogleMap) -> Unit) = onCameraMoveStartedByUser.execute(function)
    var onCameraStopped = event<GoogleMap>()
    fun onCameraStopped(function: (GoogleMap) -> Unit) = onCameraStopped.execute(function)
    private var onInfoWindowClick = event<Marker>()
    fun onMarkerInfoClick(function: (Marker) -> Unit) = onInfoWindowClick.execute(function)

    override fun obtainView() = MapView(this, options)

    constructor(parent: CSViewController<*>) : this(parent, GoogleMapOptions())

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        catchApiKeyNotFound {
            view.onCreate(state)
        }
    }

    private fun catchApiKeyNotFound(function: () -> Unit) {
        try {
            function()
        } catch (ex: RuntimeException) {
            logError(ex)
            if (ex.message?.contains("API key not found") == true)
                dialog("Error", ex.message!!).cancelable(false).show(onPositive = {
                    exitProcess(1)
                })
            else throw ex
        }
    }

    override fun onResume() {
        super.onResume()
        catchApiKeyNotFound {
            view.onResume()
            view.getMapAsync { onInitializeMap(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        catchApiKeyNotFound {
            view.onPause()
        }
    }

    override fun onStart() {
        super.onStart()
        catchApiKeyNotFound {
            view.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        catchApiKeyNotFound {
            view.onStop()
        }
    }

    override fun onDestroy() {
        catchApiKeyNotFound {
            view.onDestroy()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        catchApiKeyNotFound {
            view.onLowMemory()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        catchApiKeyNotFound {
            view.onSaveInstanceState(outState)
        }
    }

    private fun onInitializeMap(map: GoogleMap) {
        this.map = map
        onMapReadyEvent.fire(map)
        map.setOnCameraMoveStartedListener { onCameraMoveStarted() }
        map.setOnCameraIdleListener { onCameraMoveStopped() }
        map.setOnCameraMoveCanceledListener { onCameraMoveStopped() }
        map.setOnInfoWindowClickListener { onInfoWindowClick.fire(it) }
    }

    fun camera(location: Location, zoom: Float) = camera(location.asLatLng(), zoom)

    fun camera(latLng: LatLng) = camera(latLng, DEFAULT_ZOOM)

    fun camera(location: Location) = camera(location.asLatLng())

    fun camera(latLng: LatLng, zoom: Float) {
        animatingCamera = true
        map?.animateCamera(newLatLngZoom(latLng, zoom), object : GoogleMap.CancelableCallback {
            override fun onCancel() {
                onAnimateCameraDone()
            }

            override fun onFinish() {
                onAnimateCameraDone()
            }
        })
    }

    fun camera(latLng: LatLng, zoom: Float, onFinished: () -> Unit) {
        animatingCamera = true
        map?.animateCamera(newLatLngZoom(latLng, zoom), object : GoogleMap.CancelableCallback {
            override fun onCancel() {
                onAnimateCameraDone()
                onFinished()
            }

            override fun onFinish() {
                onAnimateCameraDone()
                onFinished()
            }
        })
    }

    private fun onAnimateCameraDone() {
        animatingCamera = false
    }

    fun onMapAvailable(onMapReady: (GoogleMap) -> Unit): CSEventRegistration? {
        map?.let { onMapReady(it) }
                ?: let {
                    return onMapReadyEvent.run { registration, map ->
                        onMapReady(map)
                        registration.cancel()
                    }
                }
        return null
    }

    private fun onCameraMoveStarted() {
        if (animatingCamera) return
        onCameraMoveStartedByUser.fire(map!!)
    }

    private fun onCameraMoveStopped() {
        onCameraStopped.fire(map!!)
    }

    fun clearMap() {
        map?.clear()
        map?.setOnMapLongClickListener(null)
        map?.setOnMapClickListener(null)
    }


}