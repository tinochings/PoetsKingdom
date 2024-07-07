package com.wendorochena.poetskingdom.recyclerViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imageLocations.size

    /**
     * Add file element to adapter
     *
     * @param file the file to add
     */
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

    /**
     * Updates long image
     *
     * @param index the index to update when there is a long click
     * @param string the tag to be updated can be "circle" or "check"
     *
     * @return 0 if tag was circle before -1 if tag was check
     */
    fun updateLongImage(index : Int, string: String) : Int {
        var toRet = 0
        val longClickImage = longClickImageViews[index]
        if (longClickImage.tag != null) {
            if (longClickImage.tag == context.getString(R.string.check) && string ==  context.getString(R.string.check)) {
                longClickImage.tag = context.getString(R.string.circle)
                toRet =  -1
            }
        }
        if (toRet != -1) {
            longClickImage.tag = string
            longClickImage.visibility = View.VISIBLE
        }
        notifyItemChanged(index)

        return toRet
    }
    /**
     * @param index index that is selected
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

    /**
     * Turns of the selected elements image buttons
     */
    fun turnOffLongClick() {
        for ((index,imageView) in longClickImageViews.withIndex()) {
            imageView.tag = "circle"
            imageView.visibility= View.GONE
            notifyItemChanged(index)
        }
    }

    /**
     * @param index the index to delete
     */
    fun deleteElem(index: Int) {
        longClickImageViews.removeAt(index)
        imageLocations.removeAt(index)
    }

    /**
     * @return the images that are currently selected
     */
    fun getLongClickImages() : ArrayList<ImageView> {
        return longClickImageViews
    }
    /**
     * This is for testing purposes I REPEAT DO NOT UNCOMMENT IT IS HIGHLY INSECURE
     */
//    fun getElement(elem : Int) : File {
//        return imageLocations[elem]
//    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
            try {
                Glide.with(context).clear(holder.imageView)
            } catch (e : java.lang.IllegalArgumentException) {
            }
    }
}