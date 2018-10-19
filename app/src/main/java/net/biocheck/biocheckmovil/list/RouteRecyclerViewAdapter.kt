package net.biocheck.biocheckmovil.list

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.biocheck.biocheckmovil.R


import net.biocheck.biocheckmovil.model.ApiData
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RouteRecyclerViewAdapter(private val mValues: List<Any>) : RecyclerView.Adapter<RouteRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_route, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var value = mValues[position]

        if(value is ApiData.RouteLocation){
            holder.layoutRouteLocation.visibility = View.VISIBLE
            holder.lblTitle.text = value.location.title?.orEmpty()
            holder.lblAddress1.text = value.location.street?.orEmpty()
                    .plus(" ")
                    .plus(value.location.externalNumber?.orEmpty())
                    .plus(" ")
                    .plus(value.location.internalNumber?.orEmpty())

            holder.lblAddress2.text = value.location.suburb?.orEmpty()
                    .plus(" ")
                    .plus(value.location.zipcode?.orEmpty())
                    .plus(", ")
                    .plus(value.location.municipality?.orEmpty())

            if(value.checkIn) holder.imgChekIn.visibility = View.VISIBLE
            if(value.checkOut) holder.imgChekOut.visibility = View.VISIBLE

            holder.mView.setOnClickListener {
                v ->
                val uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s+%s)",
                        value.location.latitude, value.location.longitude,
                        value.location.latitude, value.location.longitude,
                        value.location.title?.orEmpty(),
                        holder.lblAddress1.text)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                v.context.startActivity(intent)
            }
        }else if (value is String){
            holder.layoutLabel.visibility = View.VISIBLE
            holder.lblString.text = value
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val btnLocation: Button = mView.findViewById(R.id.btnLocation)
        val lblTitle: TextView = mView.findViewById(R.id.lblTItle)
        val lblAddress1: TextView = mView.findViewById(R.id.lblAddress1)
        val lblAddress2: TextView = mView.findViewById(R.id.lblAddress2)
        val lblString: TextView = mView.findViewById(R.id.lblString)

        val imgChekIn: ImageView  = mView.findViewById(R.id.imgCheckIn)
        val imgChekOut: ImageView  = mView.findViewById(R.id.imgCheckOut)

        val layoutLabel: LinearLayout = mView.findViewById(R.id.layoutLabel)
        val layoutRouteLocation: LinearLayout = mView.findViewById(R.id.layoutRouteLocation)

        override fun toString(): String {
            //TODO hacer un buen to string...
            return super.toString() + " '"
        }
    }
}
