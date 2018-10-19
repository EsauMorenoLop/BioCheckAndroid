package net.biocheck.biocheckmovil.list

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.biocheck.biocheckmovil.R
import net.biocheck.biocheckmovil.list.dummy.DummyContent.DummyItem
import net.biocheck.biocheckmovil.model.ApiData
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RecordRecyclerViewAdapter(private var mValues: ArrayList<ApiData.RecordView>, private val context: Context) : RecyclerView.Adapter<RecordRecyclerViewAdapter.ViewHolder>() {

    val dtz = DateTimeZone.getDefault()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_record, parent, false)
        return ViewHolder(view)
    }

    fun updateValues(values: ArrayList<ApiData.RecordView>){
        this.mValues = values
    }

    //TODO.. Con las etiquetas no queda, pone todos los datos como quiere y repetidos, tratar de corregirlo
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(mValues[position].isRecord){
            holder.layoutRecord.visibility = View.VISIBLE
//            holder.lblTime.text = DateTimeFormat.forPattern("hh:mm aa").print(DateTime(mValues[position].record?.recTime, DateTimeZone.UTC).toDateTime(dtz))
//            val dateTime = DateTime(mValues[position].record?.recTime, DateTimeZone.UTC).toDateTime(dtz)
            val dateTime = DateTime(mValues[position].record?.recTime, DateTimeZone.UTC)
//            val dateTime = DateTime(mValues[position].record?.recTime)
            holder.lblTime.text = DateTimeFormat.forPattern("HH:mm:ss").print(dateTime)
            holder.lblDate.text = DateTimeFormat.forPattern("dd-MM-yyyy").print(dateTime)
            holder.lblSerialNumber.text = mValues[position].record?.serialNumber
            if (mValues[position].record?.checkInOut!!){
                holder.imgCheckInOut.setImageResource(R.drawable.btn_checkout_green)
            }
            if (mValues[position].record?.serialNumber.equals("mobile")){
                holder.btnLocation.visibility = View.VISIBLE
                holder.icType.setImageResource(R.drawable.ic_mobile_blue)
                if (!mValues[position].record?.coordinates.isNullOrEmpty())
                    holder.btnLocation.setOnClickListener { v ->
                    val uri = String.format(Locale.ENGLISH, "geo:%s?q=%s(%s+%s)",
                            mValues[position].record?.coordinates,
                            mValues[position].record?.coordinates,
                            holder.lblTime.text,
                            holder.lblDate.text)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    v.context.startActivity(intent)
                }
            }
        }else{
            holder.layoutLabel.visibility = View.VISIBLE
            holder.lblHeader.text = mValues[position].label?.orEmpty()
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val layoutLabel: LinearLayout = mView.findViewById(R.id.layoutLabel)
        val lblHeader: TextView = mView.findViewById(R.id.lblHeader)

        val layoutRecord: LinearLayout = mView.findViewById(R.id.layoutRecord)
        val lblTime: TextView = mView.findViewById(R.id.lblTime)
        val lblSerialNumber: TextView = mView.findViewById(R.id.lblSerialNumber)
        val lblDate: TextView = mView.findViewById(R.id.lblDate)

        val btnLocation: Button = mView.findViewById(R.id.btnLocation)

        val imgCheckInOut: ImageView = mView.findViewById(R.id.imgCheckInOut)

        val icType: ImageView = mView.findViewById(R.id.icType)
    }
}
