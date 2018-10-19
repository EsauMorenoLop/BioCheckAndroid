package net.biocheck.biocheckmovil

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_enroll.*
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.MobileService
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EnrollActivity : AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 128

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enroll)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        btn_take.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        btn_take.isEnabled = false
        progressBar.visibility = View.VISIBLE
        var takePictureIntet = Intent(this, PhotoActivity::class.java)
        startActivityForResult(takePictureIntet, ACTIVITY_REQUEST_CODE)
    }


    override fun onBackPressed() {
        var intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            var bitmap = data?.extras?.getString("bitmap")
            //Ahora, hay que hacer la llamada a que por fin se registre con la foto...
            enroll(bitmap)
        }
    }

    fun enroll(filename:String?){
        var file = File(filename)
        var rBody = RequestBody.create(MediaType.parse("image/*"), file)

        var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
        var callEnroll = mobileService.doEnroll(Preferences.instance.formatedToken, rBody)

        callEnroll.enqueue(object : Callback<ApiData.EnrollState?>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ApiData.EnrollState?>?, response: Response<ApiData.EnrollState?>?) {
                var gson = Gson()
                when(response?.code()){
                    201 -> {
                        var enrollState = response.body()
                        if(enrollState?.verified!!){
                            btn_take.text = resources.getText(R.string.lbl_finish)
                            goToMain()
                        }else{
                            lbl_photo.text = resources.getText(R.string.lbl_photo).toString() + " " + (enrollState?.imagesCount + 1) + "/" + enrollState?.imagesNedeed
                        }
                    }
                    403 -> {
                        Log.i("Error", "Forbidden")
                    }
                    406 -> {
                        var apiError = gson.fromJson(response?.errorBody()?.string(), ApiData.ApiError::class.java)
                        Snackbar.make(findViewById<View>(android.R.id.content),
                                resources.getText(R.string.lbl_error_invalid_picture), Snackbar.LENGTH_LONG).show()
                    }
                    500 -> {
                        var apiError = gson.fromJson(response?.errorBody()?.string(), ApiData.ApiError::class.java)
                        Snackbar.make(findViewById<View>(android.R.id.content),
                                resources.getText(R.string.lbl_error_unknown).toString() + " " + apiError.message, Snackbar.LENGTH_LONG).show()
                    }

                }
                progressBar.visibility = View.GONE
                btn_take.isEnabled = true
            }

            override fun onFailure(call: Call<ApiData.EnrollState?>?, t: Throwable?) {
                Snackbar.make(findViewById<View>(android.R.id.content),
                        resources.getText(R.string.lbl_error_network).toString(), Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                btn_take.isEnabled = true
            }

        })

    }
    private fun goToMain(){
        var intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
