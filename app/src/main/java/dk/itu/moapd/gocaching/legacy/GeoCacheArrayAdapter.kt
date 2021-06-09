package dk.itu.moapd.gocaching.legacy

import android.content.Context
import android.widget.ArrayAdapter
import dk.itu.moapd.gocaching.R


/*class GeoCacheArrayAdapter(context: Context, geoCaches: List<GeoCache>):
    ArrayAdapter<GeoCache>(context, R.layout.list_geo_cache, geoCaches) {*/

    // Viewholder represent each variable you wanna present in your user interface.
    /*private class GeoCacheViewHolder {
        internal var cache: TextView? = null
        internal var where: TextView? = null
        internal var createdDate: TextView? = null
        internal var updatedDate: TextView? = null
    }*/

    // used to populate the adapter.
    /*override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: GeoCacheViewHolder
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_geo_cache, parent, false)

            viewHolder = GeoCacheViewHolder()
            viewHolder.cache = view.findViewById<View>(R.id.cache_text) as TextView
            viewHolder.where = view.findViewById<View>(R.id.where_text) as TextView
            viewHolder.createdDate = view.findViewById<View>(R.id.createdDate_text) as TextView
            viewHolder.updatedDate = view.findViewById<View>(R.id.updatedDate_text) as TextView

        } else
            viewHolder = view.tag as GeoCacheViewHolder

        val geoCache = getItem(position)
        viewHolder.cache!!.text = geoCache?.cache
        viewHolder.where!!.text = geoCache?.where
        viewHolder.createdDate!!.text = geoCache?.createdDate.toString()
        viewHolder.updatedDate!!.text = geoCache?.updatedDate.toString()


        view?.tag = viewHolder
        return view!!
    }
    }*/


