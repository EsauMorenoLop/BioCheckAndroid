package net.biocheck.biocheckmovil.list

import android.content.Context
import android.icu.text.AlphabeticIndex
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.biocheck.biocheckmovil.Preferences

import net.biocheck.biocheckmovil.R
import net.biocheck.biocheckmovil.list.dummy.DummyContent
import net.biocheck.biocheckmovil.list.dummy.DummyContent.DummyItem
import net.biocheck.biocheckmovil.model.ApiData
import net.biocheck.biocheckmovil.service.ApiServiceGenerator
import net.biocheck.biocheckmovil.service.MobileService
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecordFragment : Fragment() {

//    private lateinit var mAdapter: RecordRecyclerViewAdapter
    private var listToView = ArrayList<ApiData.RecordView>()
    private var listOfRecord = ArrayList<ApiData.Record>()
    val dtz = DateTimeZone.getDefault()!!
    private var lastDateTime = DateTime(2000,1,1,1,1, dtz)
    private lateinit var adapter:RecordRecyclerViewAdapter
    private var refreshing = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_record_list, container, false)
        adapter = RecordRecyclerViewAdapter(listToView, context)
        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = GridLayoutManager(context, 1)
            view.adapter = adapter
            if(listToView.isEmpty()){
                refresh()
            }else{
                buildListView()
            }
        }

        return view
    }

    fun buildListView(){
        var iteratorLocalDate = LocalDate(2000,1,1)
        var list = ArrayList<ApiData.RecordView>()
        val today = DateTime.now(dtz)
        listOfRecord.forEach { r ->
            //TODO.. Con las etiquetas no queda, pone todos los datos como quiere y repetidos, tratar de corregirlo
//            val dt = DateTime(r?.recTime, DateTimeZone.UTC).toDateTime(dtz)
//            if(!dt.toLocalDate().isEqual(iteratorLocalDate)){
//                if(dt.toLocalDate().isEqual(today.toLocalDate())){
//                    list.add(ApiData.RecordView(false, null, getString(R.string.lbl_today)))
//                }else {
//                    list.add(ApiData.RecordView(false, null, DateTimeFormat.mediumDate().print(dt.toLocalDate())))
//                }
//                iteratorLocalDate = dt.toLocalDate()
//            }
            list.add(ApiData.RecordView(true, r, null))
            if(r.recTime > lastDateTime.toDate()) lastDateTime = DateTime(r.recTime)
        }
        listToView = list
        adapter.updateValues(listToView)
        if(view != null) {
            val v = view as RecyclerView
            v.adapter = adapter
            v.adapter.notifyDataSetChanged()
        }
    }

    fun refresh(){

        if(refreshing) return

        refreshing = true

        var mobileService2 = ApiServiceGenerator.createService(MobileService::class.java)
        var callGetRecords = mobileService2.getRecords(Preferences.instance.formatedToken, lastDateTime.plusSeconds(10).toDate(),
                DateTime.now().toDate())
        callGetRecords.enqueue(object: Callback<List<ApiData.Record>?> {
            override fun onFailure(call: Call<List<ApiData.Record>?>?, t: Throwable?) {
                //TODO poner un mensaje de que no hay conexión de red
                refreshing = false
            }
            override fun onResponse(call: Call<List<ApiData.Record>?>?, response: Response<List<ApiData.Record>?>?) {
                response?.body()?.forEach {
                    r -> listOfRecord.add(r)
                }

//                if(listOfRecord.isNotEmpty()) {
//                    lastDateTime = DateTime(listOfRecord[0].recTime, dtz)
//                }

                //Ahora sortemos la lista..
                listOfRecord = ArrayList(listOfRecord.sortedByDescending { record ->  record.recTime})

                listOfRecord.forEach { record ->
                    if(record.recTime > lastDateTime.toDate()) lastDateTime = DateTime(record.recTime)
                }

                buildListView()
                refreshing = false
            }
        })
    }
}
