package net.biocheck.biocheckmovil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.LensPosition
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.parameter.selector.LensPositionSelectors
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.File
import java.io.FileOutputStream

class PhotoActivity : AppCompatActivity() {

    private lateinit var  fotoapparat:Fotoapparat
    private var checkInOut:Boolean = false
    val FILENAME = Environment.getExternalStorageDirectory().absolutePath +"/biocheck_face.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInOut = intent.getBooleanExtra("IsCheckInOut", false)
        setContentView(R.layout.activity_photo)
        fotoapparat = Fotoapparat.with(this)
                .into(camera_view)
                .lensPosition(LensPositionSelectors.lensPosition(LensPosition.FRONT))
                .previewScaleType(ScaleType.CENTER_CROP)
                .build()
        btn_take_picture.setOnClickListener{
            var photoResult = fotoapparat.takePicture()
            var file = File(FILENAME)
            photoResult.saveToFile(file)
            photoResult
                    .toBitmap()
                    .whenAvailable({ t ->
                        var intent = Intent()
                        intent.putExtra("bitmap", FILENAME)
                        intent.putExtra("IsCheckInOut", checkInOut)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    })
        }
    }


    override fun onStart() {
        super.onStart()
        fotoapparat.start()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat.stop()
    }
}
