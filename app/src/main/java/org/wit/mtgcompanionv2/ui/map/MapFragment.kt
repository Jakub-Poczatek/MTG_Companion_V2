package org.wit.mtgcompanionv2.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.wit.mtgcompanionv2.BuildConfig.MAPS_API_KEY
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.databinding.FragmentMapBinding
import org.wit.mtgcompanionv2.main.MTGCompanion
import org.wit.mtgcompanionv2.models.PlaceModel
import org.wit.mtgcompanionv2.ui.card.CardFragment
import timber.log.Timber
import timber.log.Timber.e
import timber.log.Timber.i

class MapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    lateinit var app: MTGCompanion
    private var _fragBinding: FragmentMapBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var mapViewModel: MapViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var loc: LatLng
    private lateinit var map: GoogleMap
    private lateinit var client: HttpClient
    private val places = ArrayList<PlaceModel>()
    private var polyLine: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MTGCompanion
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        activity?.title = "Map"

        _fragBinding = FragmentMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        //_contentBinding = ContentMapBinding.bind(fragBinding.root)
        fragBinding.mapMpVw.onCreate(savedInstanceState)

        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        mapViewModel.text.observe(viewLifecycleOwner, Observer{})

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    retrieveLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    retrieveLocation()
                } else -> {
                retrieveDefaultLocation()
            }
            }
        }

        fragBinding.mapMpVw.getMapAsync {
            map = it
            configureMap()
            checkLocPermissions()
        }

        fragBinding.place = PlaceModel()

        return root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CardFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onMarkerClick(marker: Marker): Boolean {
        val placeId = marker.tag
        val foundPlace: PlaceModel? = places.find { p -> p.id == placeId}
        fragBinding.place = foundPlace
        fragBinding.directionsButton.setOnClickListener{
            calculatePath(places[0].loc, foundPlace!!.loc)
        }
        return false
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragBinding.mapMpVw.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        fragBinding.mapMpVw.onPause()
    }

    override fun onResume() {
        super.onResume()
        fragBinding.mapMpVw.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragBinding.mapMpVw.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    private fun retrieveLocation(){
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { result ->
                    loc = LatLng(result.latitude, result.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12f))
                    val usersPosition: PlaceModel = PlaceModel(0, "You", "Your current position", loc, 0.0, 0, false)
                    places.add(usersPosition)
                    map.addMarker(MarkerOptions().title(usersPosition.name).position(usersPosition.loc)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))!!.tag = usersPosition.id
                    findCardShops()
                }
                .addOnFailureListener { error ->
                    Timber.e("Error occurred whilst retrieving current location: $error")
                    retrieveDefaultLocation()
                }
        } catch (ex: SecurityException){
            Timber.e("Error getting permissions: $ex")
        }
    }

    private fun findCardShops(){
        runBlocking {
            client = HttpClient()
            val url =
                "https://maps.googleapis.com/maps/api/place/textsearch/json?query=card%20game%20shops%20near%20me&location=${loc.latitude}%2C${loc.longitude}&radius=10000&key=$MAPS_API_KEY"
            val response = client.get(url)
            i("${response.body<String>()}")
            val copy = JSONObject(response.body<String>())
            //i("${copy.getJSONArray("results").getJSONObject(0)}")
            for(j in 0 until copy.getJSONArray("results").length()){
                val docPlace = copy.getJSONArray("results").getJSONObject(j)
                try {
                    val place = PlaceModel(
                        id = j+1,
                        name = docPlace.getString("name"),
                        address = docPlace.getString("formatted_address"),
                        loc = LatLng(
                            docPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                            docPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                        ),
                        rating = docPlace.getDouble("rating"),
                        totalUserRatings = docPlace.getInt("user_ratings_total"),
                        open = docPlace.getJSONObject("opening_hours").getBoolean("open_now")
                    )
                    places.add(place)
                } catch (e: java.lang.Exception) {
                    e("Can't fetch place")
                }
                //i("${copy.getJSONArray("results").getJSONObject(j).get("name")}")
            }
            places.forEach{
                if(it.id == 0)
                    return@forEach
                //Timber.i("$it")
                val options = MarkerOptions().title(it.name).position(it.loc)
                map.addMarker(options)?.tag = it.id
            }
            val foundPlace: PlaceModel? = places.find { p -> p.id == places[0].id}
            fragBinding.place = foundPlace
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(places[0].loc, 12f))
        }
    }

    private fun retrieveDefaultLocation(){
        loc = LatLng(52.245696, -7.139102)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12f))
        findCardShops()
    }

    private fun configureMap() {
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
    }

    private fun checkLocPermissions(){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            retrieveLocation()
        }
    }

    private fun calculatePath(origin: LatLng, destination: LatLng){
        runBlocking {
            Timber.i("Starting directions retrieve")
            val url =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$MAPS_API_KEY"
            val response = client.get(url)
            val responseJson = JSONObject(response.body<String>())
            val points = mutableListOf<LatLng>()
            val routes = responseJson.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val polyline = step.getJSONObject("polyline")
                val encodedPoints = polyline.getString("points")
                val decodedPoints = PolyUtil.decode(encodedPoints)

                points.addAll(decodedPoints)
            }

            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(5f)
                .color(Color.RED)

            polyLine?.remove()
            polyLine = map.addPolyline(polylineOptions)
        }
    }
}