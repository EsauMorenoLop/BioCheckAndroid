package net.biocheck.biocheckmovil

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import android.view.View
import android.view.WindowManager
import com.google.android.gms.security.ProviderInstaller
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.MobileService
import net.danlew.android.joda.JodaTimeAndroid
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {

    var permissionsWorking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JodaTimeAndroid.init(this)
        Preferences.buildInstance(getSharedPreferences(Preferences.PREFS_FILENAME, Context.MODE_PRIVATE))
        setContentView(R.layout.activity_splash)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onStart() {
        super.onStart()
        ProviderInstaller.installIfNeeded(applicationContext)
        if(!permissionsWorking) requestPermissionsApp()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasPermissions = false
        grantResults.forEach { g ->
            run {
                if(g != PackageManager.PERMISSION_DENIED) {
                    hasPermissions = true
                }
            }
        }

        if(!hasPermissions){
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Pemissions Error")
                    .setMessage("Biocheck needs your permission to run, please grant")
                    .setPositiveButton(android.R.string.ok, { dialog, which ->
                        requestPermissionsApp()
                    })
                    .setNegativeButton(android.R.string.cancel, { dialog, which ->
                        finish()
                        System.exit(0)
                    })
            builder.create().show()
        }else{
            permissionsWorking = false
            //Ahora hay que llamar a ver si esta loggeado...
//            Preferences.buildInstance(getSharedPreferences(Preferences.PREFS_FILENAME, Context.MODE_PRIVATE))
            if(Preferences.instance.token.isEmpty()){
                goToLogin()
            }else {
                //Try to get employee
                getEmployee()
            }
        }
    }

    private fun getEmployee(){
        //Try to get employee
        var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
        var callGetEmployee = mobileService.getEmployee(Preferences.instance.formatedToken)

        callGetEmployee.enqueue(object : Callback<ApiData.Employee?>{
            override fun onFailure(call: Call<ApiData.Employee?>?, t: Throwable?) {

                var snackbar = Snackbar.make(findViewById<View>(android.R.id.content), R.string.snack_no_conection_retry, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(android.R.string.yes, {
                    getEmployee()

                })
                snackbar.show()
            }
            override fun onResponse(call: Call<ApiData.Employee?>?, response: Response<ApiData.Employee?>?) {
                if(response?.code() == 200){
                    Preferences.instance.employee = response?.body()
                    if(! Preferences.instance.employee?.enrolled!!){
                        goToEnroll()
                    }else{
                        goToMain()
                    }
                }else{
                    goToLogin()
                }
            }
        })
    }

    private fun showErrorSnackbar(menssage:String){
        var errorSnackBar = Snackbar.make(this.findViewById<View>(android.R.id.content),
                menssage, Snackbar.LENGTH_LONG)
        errorSnackBar.show()
    }

    private fun goToLogin(){
        var intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToEnroll(){
        var intent = Intent(this, EnrollActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToMain(){
        var intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun requestPermissionsApp(){
        permissionsWorking = true
        ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1024)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        moveTaskToBack(true)
    }
}
