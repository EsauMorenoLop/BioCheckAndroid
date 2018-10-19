package net.biocheck.biocheckmovil.list

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_list.*

import net.biocheck.biocheckmovil.R


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
class RouteFragment : Fragment() {

//    private lateinit var mRouteList: List<ApiData.Location>
    private lateinit var mAdapter: RouteRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //TODO Crear divider para el listado.
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_route_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = GridLayoutManager(context, 1)
        }
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun refresh(routeList: List<Any>){
        mAdapter = RouteRecyclerViewAdapter(routeList)
        if(view != null) {
            var v = view as RecyclerView
            v.adapter = mAdapter
            mAdapter.notifyDataSetChanged()
        }
    }
}
