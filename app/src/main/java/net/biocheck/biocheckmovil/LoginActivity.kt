package net.biocheck.biocheckmovil

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.LoginService
import net.biocheck.biocheckmovil.service.MobileService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    var TAG = LoginActivity::class.java.name

    fun loggin(isLoggin:Boolean){
        if(isLoggin) {
            progress_bar.visibility = View.VISIBLE
            ti_employee_id.isEnabled = false
            ti_company.isEnabled = false
            ti_nip.isEnabled = false
            btn_login.isEnabled = false
        }else{
            progress_bar.visibility = View.INVISIBLE
            ti_employee_id.isEnabled = true
            ti_company.isEnabled = true
            ti_nip.isEnabled = true
            btn_login.isEnabled = true
        }
    }

    private fun goToEnroll(){
        var intent = Intent(this, EnrollActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        btn_login.setOnClickListener{

            ti_employee_id.isErrorEnabled = false
            ti_company.isErrorEnabled = false
            ti_nip.isErrorEnabled = false

            loggin(true)

            if(ti_company.editText?.text.isNullOrEmpty()){
                ti_company.error = getString(R.string.lbl_error_cant_be_empty)
                ti_company.isErrorEnabled = true
                loggin(false)
                return@setOnClickListener
            }

            if(ti_employee_id.editText?.text.isNullOrEmpty()){
                ti_employee_id.error = getString(R.string.lbl_error_cant_be_empty)
                ti_employee_id.isErrorEnabled = true
                loggin(false)
                return@setOnClickListener
            }

            if(ti_nip.editText?.text.isNullOrEmpty()){
                ti_nip.error = getString(R.string.lbl_error_cant_be_empty)
                ti_nip.isErrorEnabled = true
                loggin(false)
                return@setOnClickListener
            }

            var company =  ti_company.editText?.text.toString().trim()

            var user = ApiData.User(ti_employee_id.editText?.text.toString().toLong(),
                    ti_nip.editText?.text.toString().toLong(),
                    company)
            //Call to login function
            var loginService = LoginService(user)
            loginService.setOnLoginEventListener(object: LoginService.LoginEvent{
                override fun onLogin(token: String?) {
                    Preferences.instance.token = token!!
                    var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
                    var callEmployee = mobileService.getEmployee(Preferences.instance.formatedToken)

                    callEmployee.enqueue(object : Callback<ApiData.Employee?>{
                        override fun onResponse(call: Call<ApiData.Employee?>?, response: Response<ApiData.Employee?>?) {
                            loggin(false)
                            if(response?.code() == 200){
                                Preferences.instance.employee = response?.body()
                                if(! Preferences.instance.employee?.enrolled!!){
                                    goToEnroll()
                                }else{
                                    goToMain()
                                }
                            }else{
                                Log.e(TAG, response?.raw().toString())
                                Snackbar.make(findViewById<View>(android.R.id.content),
                                        getString(R.string.lbl_error_unknown),
                                        Snackbar.LENGTH_LONG ).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiData.Employee?>?, t: Throwable?) {
                            Log.e(TAG, t.toString())
                            Snackbar.make(findViewById<View>(android.R.id.content),
                                    getString(R.string.lbl_error_network),
                                    Snackbar.LENGTH_LONG ).show()
                            loggin(false)
                        }
                    })

                }

                override fun onCheckIdError() {
                    ti_employee_id.error = getString(R.string.lbl_error_employe_id_not_found)
                    ti_employee_id.isErrorEnabled = true
                    loggin(false)
                }

                override fun onNipError() {
                    ti_nip.error = getString(R.string.lbl_error_nip_incorrect)
                    ti_nip.isErrorEnabled = true
                    loggin(false)
                }

                override fun onSubdomainError() {
                    ti_company.error = getString(R.string.lbl_error_company_not_found)
                    ti_company.isErrorEnabled = true
                    loggin(false)
                }

                override fun onInvalidUser() {
                    Snackbar.make(findViewById<View>(android.R.id.content),
                            getString(R.string.lbl_error_invalid_user),
                            Snackbar.LENGTH_LONG ).show()
                    loggin(false)
                }

                override fun onUnknownError(t: Throwable?) {
                    Log.e(TAG, t.toString())
                    Snackbar.make(findViewById<View>(android.R.id.content),
                            getString(R.string.lbl_error_invalid_user),
                            Snackbar.LENGTH_LONG ).show()
                    loggin(false)
                }
            })
            loginService.login()
        }
    }
    private fun goToMain(){
        var intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
