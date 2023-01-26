package com.wendorochena.poetskingdom.recyclerViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import com.wendorochena.poetskingdom.R
import java.io.File

class ImageRecyclerViewAdapter(
    private var callingClassName: String,
    private var imageLocations: ArrayList<File>,
    private val context : Context
) :
    RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder>() {
    var onItemClick: ((File,Int) -> Unit)? = null
    var onItemLongClick: ((File, Int) -> Unit)? = null
    private val longClickImageViews = ArrayList<ImageView>()
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView
        lateinit var checkImageView : ImageView

        init {
            imageView = view.findViewById(R.id.idIVImage)
            val splitString = callingClassName.split(".")
            if (splitString[splitString.size - 1] != "PoemThemeActivity") {
                checkImageView = view.findViewById(R.id.longClickImageView)
                view.setOnLongClickListener {
                    checkImageView.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.check_mark,
                            null
                        )
                    )
                    checkImageView.tag = "check"
                    checkImageView.visibility = View.VISIBLE
                    checkImageView.z = 1f
                    onItemLongClick?.invoke(
                        imageLocations[absoluteAdapterPosition],
                        absoluteAdapterPosition
                    )
                    true
                }
            }
            view.setOnClickListener {
                onItemClick?.invoke(imageLocations[absoluteAdapterPosition],absoluteAdapterPosition)
            }
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
       val splitString = callingClassName.split(".")
        val view : View = if (splitString[splitString.size - 1] == "PoemThemeActivity")
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.poem_themer_image_view, viewGroup, false)
        else
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.image_card_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        try {
            val currentImageLocation = imageLocations[position]

            if (currentImageLocation.exists()) {
                viewHolder.imageView.scaleType = ImageView.ScaleType.FIT_XY
                Glide.with(context).load(currentImageLocation).placeholder(R.drawable.ic_launcher_background).into(viewHolder.imageView)
//                Picasso.get().load(currentImageLocation).fit().placeholder(R.drawable.ic_launcher_background)
//                    .into(viewHolder.imageView)
            }

            val splitString = callingClassName.split(".")
            if (splitString[splitString.size - 1] != "PoemThemeActivity" && longClickImageViews.size > 0){
                val longClickImageView = longClickImageViews[position]
                if (longClickImageView.tag != null && longClickImageView.isVisible) {
                    when(longClickImageView.tag as String){
                        "circle" -> {
                            viewHolder.checkImageView.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.circle_svg,null))
                        }
                        "check" -> {
                            viewHolder.checkImageView.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.check_mark,null))
                        }
                    }
                    viewHolder.checkImageView.z = 1f
                    viewHolder.checkImageView.visibility = View.VISIBLE
                } else {
                    viewHolder.checkImageView.z = 0f
                    viewHolder.checkImageView.visibility = View.GONE
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imageLocations.size

    fun addElement(file: File) {
        imageLocations.add(file)
    }

    /**
     * adds image to the image arraylist
     */
    fun addElement(imageView: ImageView, index: Int) {
        longClickImageViews.add(imageView)
        notifyItemChanged(index)
    }

    fun updateLongImage(index : Int, string: String) {
        val longClickImage = longClickImageViews[index]
        longClickImage.tag = string
        longClickImage.visibility = View.VISIBLE
        notifyItemChanged(index)
    }
    /**
     *
     */
    fun initiateOnLongClickImage(index : Int) {
        for ((currIndex,imageView) in longClickImageViews.withIndex()) {
            if (index != currIndex) {
                imageView.tag = "circle"
                imageView.visibility= View.VISIBLE
                notifyItemChanged(index)
            }
        }
    }
    fun turnOffLongClick() {
        for ((index,imageView) in longClickImageViews.withIndex()) {
            imageView.tag = "circle"
            imageView.visibility= View.GONE
            notifyItemChanged(index)
        }
    }

    fun deleteElem(index: Int) {
        longClickImageViews.removeAt(index)
        imageLocations.removeAt(index)
    }

    fun getLongClickImages() : ArrayList<ImageView> {
        return longClickImageViews
    }
    /**
     * This is for testing purposes I REPEAT DO NOT UNCOMMENT IT IS HIGHLY INSECURE
     */
//    fun getElement(elem : Int) : File {
//        return imageLocations[elem]
//    }

}