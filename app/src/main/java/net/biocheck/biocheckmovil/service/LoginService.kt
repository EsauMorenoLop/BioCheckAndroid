package net.biocheck.biocheckmovil.service

import android.util.Log
import com.google.gson.Gson
import net.biocheck.biocheckmovil.model.ApiData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Gabriel Martinez on 10/01/2018.
 */
class LoginService(user:ApiData.User) {

    var user:ApiData.User = user
    var loginEvent: LoginEvent? = null

    fun login(){
        var mobileService = ApiServiceGenerator.createService(MobileService::class.java)
        var callLogin = mobileService.login(user)

        callLogin.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>?, t: Throwable?) {
                loginEvent?.onUnknownError(t)
            }
            override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
                if(response?.code() == 200){
                    loginEvent?.onLogin(response?.body())
                }else{
                    var gson = Gson()
                    var apiError = gson.fromJson(response?.errorBody()?.string(), ApiData.ApiError::class.java)

                    if (apiError?.message != null) {
                        Log.i("API ERROR", apiError?.message)
                        when(apiError.message){
                            "INVALID_SUBDOMAIN" -> loginEvent?.onSubdomainError()
                            "INVALID_USER" -> loginEvent?.onCheckIdError()
                            "INVALID_NIP" -> loginEvent?.onNipError()
                        }
                    }
                    else
                        loginEvent?.onInvalidUser()

                }
            }
        })
    }

    fun setOnLoginEventListener(loginEvent: LoginEvent){
        this.loginEvent = loginEvent
    }


    interface LoginEvent{
        fun onLogin(token:String?)
        fun onSubdomainError()
        fun onNipError()
        fun onCheckIdError()
        fun onInvalidUser()
        fun onUnknownError(t:Throwable?)
    }
}