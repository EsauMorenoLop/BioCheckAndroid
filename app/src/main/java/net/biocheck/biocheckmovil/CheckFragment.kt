package net.biocheck.biocheckmovil

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.android.synthetic.main.fragment_check.*
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.MobileService
import okhttp3.MediaType
import okhttp3.RequestBody
import org.apache.commons.net.ntp.NTPUDPClient
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.timerTask


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CheckFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CheckFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CheckFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null

    private var timer:Timer? = null
    private var lastTime = DateTime.now()
    private var daySettings: ApiData.DaySettings? = null

    private var loc:ApiData.Location? = null
    private var route:ApiData.Route? = null
    private var routeLocation:ApiData.RouteLocation? = null
    private var location:Location? = null

    private val ACTIVITY_REQUEST_CODE = 128

    private var layout:View? = null

    private var isBusy = false

    private var pDialog: SweetAlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        layout = inflater?.inflate(R.layout.fragment_check, container, false)

        layout?.findViewById<Button>(R.id.btnCheckIn)?.setOnClickListener{
           checkInOut(false)
        }

        layout?.findViewById<Button>(R.id.btnCheckOut)?.setOnClickListener{
            checkInOut(true)
        }

        layout?.findViewById<TextView>(R.id.lbl_employee)?.text = Preferences.instance.employee?.name.
                plus(" ").
                plus(Preferences.instance.employee?.lastName)
        return layout
    }

    private fun checkInOut(checkInOut: Boolean){
        busy(true)
        if (Preferences.instance.checkWithPhoto){
            busy(true)
            dispatchTakePictureIntent(checkInOut)
        }else{
            doCheck(checkInOut)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startClock()
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun startClock(){
        updateTime(DateTime.now())

        var dt = DateTime.now().minusMillis(DateTime.now().millisOfSecond).minusSeconds(DateTime.now().secondOfMinute).plusMinutes(1)

        timer = Timer("clock", true)
        timer?.schedule(timerTask {
            activity?.runOnUiThread({
                updateTime(DateTime.now())
                if(lastTime.toLocalDate().isAfter(DateTime.now().toLocalDate())){
                    var mainActivity = activity as MainActivity
                    mainActivity.refresh()
                }
            })
        }, dt.toDate(), 60000)
    }

    private fun updateTime(dateTime: DateTime){
        lbl_time?.text = DateTimeFormat.forPattern("hh:mm aa").print(dateTime)
        lbl_date?.text = DateTimeFormat.mediumDate().print(dateTime)
    }

    //Función para refrescar los datos que se necesitan...
    fun refresh(daySettings: ApiData.DaySettings?, location: Location?){
        //busy(isBusy)
        this.daySettings = daySettings

        updateTime(DateTime.now())

        loc = null
        route = null
        routeLocation = null
        this.location = location

        if(location != null) {
            //Revisamos primeramente si estamos en alguna ruta...
            daySettings?.routes?.forEach { r ->
                r.routeLocations.forEach{rl ->
                    var locToEvaluate = Location(rl.id.toString())
                    locToEvaluate.latitude = rl.location.latitude
                    locToEvaluate.longitude = rl.location.longitude
                    if(location?.distanceTo(locToEvaluate)!! <= rl.location.distance){
                        if(loc == null && !rl.location.deleted){
                            loc = rl.location
                            route = r
                            routeLocation = rl
                        }
                    }
                }
            }
            //Si no hubo nada en la ruta, revisemos si tenemos algo en las locaciones donde podemos trabajar
            daySettings?.locations?.forEach{ l ->
                if(loc == null) loc = l
            }
        }

        //Ahora ya sabemos que si está o no en una locación...
        lbl_details_title?.text = loc?.title.orEmpty()

        //Revisemos, si tenemos una localidad
        if(loc != null){
            val sbDetails = StringBuilder()
            sbDetails.append(loc?.street.orEmpty().plus(" "))
            sbDetails.append(loc?.externalNumber.orEmpty().plus(" "))
            sbDetails.append(loc?.internalNumber.orEmpty())
            sbDetails.append("\r\n")
            sbDetails.append(loc?.suburb.orEmpty().plus(" "))
            sbDetails.append(loc?.zipcode.orEmpty())
            sbDetails.append("\r\n")
            sbDetails.append(loc?.municipality.orEmpty())

            lbl_details_content?.text = sbDetails.toString()
        }else {
            lbl_details_content?.text = ""
        }

        //Revisemos si tenemos una ruta
        if(loc != null){
            btnViewRoute?.visibility = View.VISIBLE
            btnViewRoute?.setOnClickListener{
                v ->
                val uri = String.format(Locale.ENGLISH, "geo:%f,%f", loc?.latitude, loc?.longitude)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                v.context.startActivity(intent)
            }
        }else btnViewRoute?.visibility = View.INVISIBLE

        //Revisemos si hay una route Location, para setear los botones como queremos
        //TODO ver si lo acaban de hacer hace 5 minutos, o algo asi, para desactivarlo ese tiempo.
        if(routeLocation != null){
            btnCheckIn?.isEnabled = routeLocation!!.checkIn
            btnCheckOut?.isEnabled = routeLocation!!.checkOut
        }else{
            btnCheckIn?.isEnabled = true
            btnCheckOut?.isEnabled = true
        }

    }
    private fun busy(isBusy:Boolean){
        if(pDialog == null && context != null){
            pDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
            pDialog?.titleText = "Loading"
            pDialog?.setCancelable(false)
        }

        if(pDialog == null) return

        if(isBusy && !pDialog?.isShowing!!){
            pDialog?.show()
        }else if(pDialog?.isShowing!!){
            pDialog?.dismiss()
            pDialog = null
        }
//        ProgressDialog.show(context, "Loading", "Wait while Loading")
//
//        this.isBusy = isBusy
//        if(isBusy){
//            progressBar?.visibility = View.VISIBLE
//            btnCheckIn?.isEnabled = false
//            btnCheckOut?.isEnabled = false
//            btnViewRoute?.isEnabled = false
//            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//        }else{
//            progressBar?.visibility = View.INVISIBLE
//            btnCheckIn?.isEnabled = true
//            btnCheckOut?.isEnabled = true
//            btnViewRoute?.isEnabled = true
//            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//        }
    }

    private fun dispatchTakePictureIntent(checkInOut: Boolean) {
        var takePictureIntet = Intent(activity, PhotoActivity::class.java)
        takePictureIntet.putExtra("IsCheckInOut", checkInOut)
        startActivityForResult(takePictureIntet, ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            busy(true)
            val bitmap = data?.extras?.getString("bitmap")
            val checkInOut = data?.extras?.getBoolean("IsCheckInOut") as Boolean
            //Ahora, hay que hacer la llamada a que por fin se registre con la foto...
            val file = File(bitmap)
            val rBody = RequestBody.create(MediaType.parse("image/*"), file)

            val mobileService = ApiServiceGenerator.createService(MobileService::class.java)
            val callValidate = mobileService.doValidate(Preferences.instance.formatedToken, rBody)

            callValidate.enqueue(object: Callback<String?>{

                override fun onFailure(call: Call<String?>?, t: Throwable?) {
                    busy(false)

                    Snackbar.make(view!!, getString(R.string.lbl_error_network), Snackbar.LENGTH_INDEFINITE ).show()

                }

                override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
                    when(response?.code()){
                        200 -> {
                            when(response?.body()){
                                "OK" -> doCheck(checkInOut)
                            }
                        }
                        406 -> {
                            busy(false)
                            Snackbar.make(view!!, resources.getText(R.string.lbl_error_invalid_picture), Snackbar.LENGTH_SHORT).show()

                        }
                        404 -> {
                            busy(false)
                            Snackbar.make(view!!, resources.getText(R.string.lbl_error_wrong_picture), Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
                            busy(false)
                            var snackbar = Snackbar.make(view!!, R.string.lbl_error_unknown, Snackbar.LENGTH_INDEFINITE)
                            snackbar.setAction(android.R.string.yes, {
                                onActivityResult(requestCode, resultCode, data)
                            })
                            Log.v("Error", response?.message());
                            snackbar.show()
                        }
                    }
                }

            })


        }else{
            busy(false)
        }
    }


    fun doCheck(checkInOut:Boolean) {

        var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
        var timezone = TimeZone.getDefault().getOffset(DateTime.now().millis)

        var check = ApiData.Check(location?.latitude!!, location?.longitude!!, 0, timezone, checkInOut)

        var callCheck = mobileService.putCheckRecord(Preferences.instance.formatedToken, check)

        if(loc != null && routeLocation != null){
            callCheck = mobileService.putCheckRecordRoute(Preferences.instance.formatedToken,
                    check, routeLocation?.id!!, loc?.id!!)
        }

        //Para revisar si viene con mocklocation
        if(location?.isFromMockProvider!!){
            //Mandar un mensaje que es una ubicación fake
            busy(false)
            Snackbar.make(view!!, resources.getText(R.string.snack_invalid_gps_position), Snackbar.LENGTH_LONG).show()
        }else{
            //Lo Subimos al servidor!!
            callCheck.enqueue(object : Callback<ApiData.Check?>{
                override fun onResponse(call: Call<ApiData.Check?>?, response: Response<ApiData.Check?>?) {
                    busy(false)
                    when(response?.code()){
                        200 -> {
                            if(Preferences.instance.checkWithPhoto) Preferences.instance.checksWithPhoto++
                            Preferences.instance.checks++
                            Snackbar.make(view!!, resources.getText(R.string.lbl_check_successful), Snackbar.LENGTH_LONG).show()
                        }
                        else -> Snackbar.make(view!!, resources.getText(R.string.lbl_error_cant_check), Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiData.Check?>?, t: Throwable?) {
                    busy(false)

                    var snackbar = Snackbar.make(view!!, R.string.snack_no_conection_retry, Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(android.R.string.yes, {
                        doCheck(checkInOut)
                    })
                    snackbar.show()
                }

            })
        }
    }

}// Required empty public constructor
