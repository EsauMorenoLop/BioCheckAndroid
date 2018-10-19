package net.biocheck.biocheckmovil

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import net.biocheck.biocheckmovil.list.RecordFragment
import net.biocheck.biocheckmovil.list.RouteFragment
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.MobileService
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mLocationManager:LocationManager? = null
    private var daySettings: ApiData.DaySettings? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocation: Location? = null

    private var checkFragment = CheckFragment()
    private var routeFragment = RouteFragment()
    private var recordFragment = RecordFragment()
    private var recordList = ArrayList<Any>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        //Set up the tabs
        tabLayout.setupWithViewPager(container)


        //Set up Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(mFusedLocationClient != null) {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                mLocation = location
                checkFragment?.refresh(daySettings, mLocation)
            }
        }

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

        if(Preferences.instance == null) {
            Preferences.buildInstance(getSharedPreferences(Preferences.PREFS_FILENAME, Context.MODE_PRIVATE))
        }

        lbl_company.text = Preferences.instance.employee?.company
        lbl_employe_name.text = Preferences.instance.employee?.name.plus(" ").plus(Preferences.instance.employee?.lastName)

        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            //Refrescamos los parámetros de los fragmentos.
            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> checkFragment.refresh(daySettings, mLocation)
                    1 -> routeFragment.refresh(getLocationsOfTheRoute())
                    2 -> recordFragment.refresh()
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        startRtLocation()
        refresh()
    }

    override fun onPause() {
        super.onPause()
        stopRtLocation()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return when(id){
            R.id.action_settings -> true
            R.id.action_logout -> {
                Preferences.instance.logout()
                var intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        var pageTitles = listOf<String>(getString(R.string.lbl_check), getString(R.string.lbl_routes),
                getString(R.string.lbl_checks))

        override fun getPageTitle(position: Int): CharSequence {
            return pageTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return when(position){
                0 -> checkFragment
                1 -> routeFragment
                2 -> recordFragment
                else -> PlaceholderFragment.newInstance(position + 1)
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    private fun getLocationsOfTheRoute():List<Any>{
        var list = ArrayList<Any>()

        daySettings?.routes?.forEach { r ->
            list.add(r.name)
            r.routeLocations?.forEach { rl ->
                list.add(rl)
            }
        }

        if(list.size < 1) list.add(getString(R.string.lbl_no_have_routes))

        return list
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            //rootView.section_label.text = getString(R.string.section_format, arguments.getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }


//    private fun isMockLocationOn(location:Location, context: Context): Boolean{
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
//            return location.isFromMockProvider();
//        }else{
//
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun startRtLocation(){
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        criteria.isCostAllowed = false
        criteria.powerRequirement = Criteria.POWER_LOW
        val provider = mLocationManager?.getBestProvider(criteria, true)
        if(provider != null) {
            mLocationManager?.requestLocationUpdates(provider, (1000 * 5), 30F , mLocationListener )
        }
    }

    private fun stopRtLocation(){
        mLocationManager?.removeUpdates(mLocationListener)
        if(mLocationManager != null) { mLocationManager = null }
    }

    val mLocationListener:LocationListener =  object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            mLocation = location
            checkFragment.refresh(daySettings, mLocation)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }

    //Función para refrescar los datos que se necesitan...
    fun refresh() {
        var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
        var callDaySettings = mobileService.getDaySettings(Preferences.instance.formatedToken, Date())
        callDaySettings.enqueue(object : Callback<ApiData.DaySettings?> {
            override fun onResponse(call: Call<ApiData.DaySettings?>?, response: Response<ApiData.DaySettings?>?) {
                if (response?.code() == 200) {
                    daySettings = response?.body()
                    //Ahora revisar, que es lo que se tiene que hacer...
                    if(daySettings != null) {
                        checkFragment.refresh(daySettings, mLocation)
                        routeFragment.refresh(getLocationsOfTheRoute())
                    }
                } else {
                    var snackbar = Snackbar.make(findViewById<View>(android.R.id.content), R.string.lbl_error_network, Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(R.string.lbl_retry, {
                        refresh()
                    })
                    snackbar.show()
                }
            }

            override fun onFailure(call: Call<ApiData.DaySettings?>?, t: Throwable?) {

                var snackbar = Snackbar.make(findViewById<View>(android.R.id.content), R.string.snack_no_conection_retry, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(android.R.string.yes, {
                    refresh()
                })
                snackbar.show()
            }

        })
    }

}
