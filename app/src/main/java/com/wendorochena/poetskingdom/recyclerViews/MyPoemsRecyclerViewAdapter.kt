package com.wendorochena.poetskingdom.recyclerViews

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.R
import java.io.File

class MyPoemsRecyclerViewAdapter(val context: android.content.Context) :
    RecyclerView.Adapter<MyPoemsRecyclerViewAdapter.ViewHolder>() {
    private val listAdapterArrayList = ArrayList<FrameLayout>()
    var onItemClick: ((FrameLayout, Int) -> Unit)? = null
    var onItemLongClick: ((FrameLayout, Int) -> Unit)? = null


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var checkImageView: ImageView
        var imageView: ShapeableImageView
        var textView: TextView

        init {
            checkImageView = view.findViewById(R.id.longClickImageView)
            imageView = view.findViewById(R.id.listViewImage)
            imageView.setOnClickListener {
                onItemClick?.invoke(listAdapterArrayList[absoluteAdapterPosition], absoluteAdapterPosition)
            }
            imageView.setOnLongClickListener {
                checkImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.check_mark,
                        null
                    )
                )
                checkImageView.visibility = View.VISIBLE
                checkImageView.z = 1f
                onItemLongClick?.invoke(
                    listAdapterArrayList[absoluteAdapterPosition],
                    absoluteAdapterPosition
                )
                true
            }
            textView = view.findViewById(R.id.listViewText)
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return listAdapterArrayList.size
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_view_layout, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [ViewHolder.getBindingAdapterPosition] which
     * will have the updated adapter position.
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val frameLayout = listAdapterArrayList[position]
        val longClickImageView = frameLayout.getChildAt(0) as ImageView
        val shapeableImageView = frameLayout.getChildAt(1) as ShapeableImageView
        val textView = frameLayout.getChildAt(2) as TextView
        if (shapeableImageView.tag != null && shapeableImageView.tag.toString().startsWith("/")) {
            val file = File(shapeableImageView.tag as String)
            if (file.exists()) {
                val drawable = BitmapDrawable(context.resources, file.absolutePath)
                val rippledImage = RippleDrawable(
                    ColorStateList.valueOf(context.getColor(R.color.ripple_color)),
                    drawable,
                    null
                )
//                Picasso.get().load(file).into(holder.imageView)
                holder.imageView.setImageDrawable(rippledImage)
                holder.imageView.tag = shapeableImageView.tag
            }
        }

        holder.textView.text = textView.text
        if (longClickImageView.tag != null && longClickImageView.isVisible) {
            when (longClickImageView.tag as String) {
                "circle" -> {
                    holder.checkImageView.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.circle_svg,
                            null
                        )
                    )
                }
                "check" -> {
                    holder.checkImageView.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.check_mark,
                            null
                        )
                    )
                }
            }

            holder.checkImageView.z = 1f
            holder.checkImageView.visibility = View.VISIBLE
        } else {
            holder.checkImageView.z = 0f
            holder.checkImageView.visibility = View.GONE
        }
    }

    /**
     * Add an element to the list adapter
     */
    fun addItem(frameToAdd: FrameLayout) {
        listAdapterArrayList.add(frameToAdd)
    }

    /**
     *
     */
    fun updateLongImage(index: Int, string: String) {
        val longClickImage = listAdapterArrayList[index].getChildAt(0)
        longClickImage.tag = string
        longClickImage.visibility = View.VISIBLE
        notifyItemChanged(index)
    }

    fun turnOffLongClick() {
        for ((index, frame) in listAdapterArrayList.withIndex()) {
            val imageView = frame.getChildAt(0) as ImageView
            imageView.tag = "circle"
            imageView.visibility = View.GONE
            imageView.z = 0f
            notifyItemChanged(index)
        }
    }

    /**
     * Initiates the image views that indicate selected images and non selected images
     */
    fun initiateOnLongClickImage(id: Int) {

        for ((index, frame) in listAdapterArrayList.withIndex()) {
            if (frame.id != id) {
                val imageView = frame.getChildAt(0) as ImageView
                imageView.tag = "circle"
                imageView.visibility = View.VISIBLE
                imageView.z = 1f
                notifyItemChanged(index)
            }
        }
    }

    fun getFrameAtIndex(index: Int): FrameLayout {
        return listAdapterArrayList[index]
    }

    fun removeAtIndex(index: Int) {
        listAdapterArrayList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun deleteElem(index: Int) {
        listAdapterArrayList.removeAt(index)
    }
}