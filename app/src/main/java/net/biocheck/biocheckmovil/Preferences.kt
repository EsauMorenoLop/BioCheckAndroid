package net.biocheck.biocheckmovil

import android.content.ContextWrapper
import android.content.SharedPreferences
import net.biocheck.biocheckmovil.model.ApiData

/**
 * Created by Gabriel Martinez on 09/01/2018.
 */
class Preferences(sharedPreferences: SharedPreferences){
    var sharedPreferences:SharedPreferences =  sharedPreferences

    companion object {
        val PREFS_FILENAME = "net.biocheck.biocheckmovil.prefs"
//        val BASE_URL = "https://api-m.biocheck.net/"
        val BASE_URL = "http://192.168.100.109:8080/"

        lateinit var instance: Preferences
            private set

        fun buildInstance(sharedPreferences: SharedPreferences){
            instance = Preferences(sharedPreferences)
        }

    }
    var token:String
        get() = sharedPreferences.getString("token", "")
        set(value) {
            sharedPreferences.edit().putString("token", value).apply()
        }

    var checks:Int
    get() = sharedPreferences.getInt("checks", 0)
    set(value) {
        sharedPreferences.edit().putInt("checks", value).apply()
    }

    var checksWithPhoto:Int
        get() = sharedPreferences.getInt("checks_w_photo", 0)
        set(value) {
            sharedPreferences.edit().putInt("checks_w_photo", value).apply()
        }

    var formatedToken:String = ""
        get() = "Bearer " + sharedPreferences.getString("token", "")

    var employee:ApiData.Employee? = null

    val checkWithPhoto:Boolean
    get() {
        if( employee?.mobilePercentage!! == 0) return false
        var percent:Int = if (checks < 1) 0 else (checksWithPhoto * 100 / checks)
        return (percent <= employee?.mobilePercentage!!)}

    fun logout(){
        checks = 0
        checksWithPhoto = 0
        token = ""
    }
}