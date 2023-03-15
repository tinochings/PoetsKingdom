package com.wendorochena.poetskingdom.recyclerViews

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.wendorochena.poetskingdom.R

class CreatePoemRecyclerViewAdapter(
    val frameLayoutArrayList: ArrayList<FrameLayout>,
    val context: Context
) :
    RecyclerView.Adapter<CreatePoemRecyclerViewAdapter.ViewHolder>() {
    var onItemClick: ((FrameLayout) -> Unit)? = null
    var onItemLongClick: ((FrameLayout) -> Unit)? = null

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var frameLayout: FrameLayout
        var imageView: ShapeableImageView
        var textView: EditText
        var pageNumber: TextView

        init {
            frameLayout = view.findViewById(R.id.frameLayoutRecyclerView)
            imageView = view.findViewById(R.id.frameLayoutImageBackground)
            textView = view.findViewById(R.id.frameLayoutTextView)
            pageNumber = view.findViewById(R.id.pageNumberTextView)
            view.findViewById<FrameLayout>(R.id.clickableArea).bringToFront()
            view.findViewById<FrameLayout>(R.id.clickableArea).setOnClickListener {
                onItemClick?.invoke(frameLayoutArrayList[absoluteAdapterPosition])
            }
            if (frameLayout.id != R.id.addPage) {
                view.findViewById<FrameLayout>(R.id.clickableArea).setOnLongClickListener {
                    onItemLongClick?.invoke(frameLayoutArrayList[absoluteAdapterPosition])
                    true
                }
            }
        }

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
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.poem_framelayout_view, parent, false)
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
     * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
     * have the updated adapter position.
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currentFrameLayout = frameLayoutArrayList[position]
        var currentImageLayout: ShapeableImageView? = null
        var editText: EditText? = null

        for (child in currentFrameLayout.children) {
            if (child is ShapeableImageView) {
                currentImageLayout = child
            } else if (child is EditText) {
                editText = child
            }
        }

        if (currentFrameLayout.id == R.id.addPage) {
            holder.frameLayout.id = R.id.addPageRecyclerViewId
            holder.frameLayout.background = currentFrameLayout.background.mutate()
            holder.imageView.layoutParams = currentImageLayout?.layoutParams
            holder.imageView.setImageDrawable(ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.baseline_add_circle_outline_24,
                null
            ))
        } else {
            if (currentFrameLayout.background != null) {
                val drawable = currentFrameLayout.background.constantState?.newDrawable()
                drawable?.setBounds(
                    currentFrameLayout.left,
                    currentFrameLayout.top,
                    currentFrameLayout.right,
                    currentFrameLayout.bottom
                )
                holder.frameLayout.background = drawable
            }
            if (currentImageLayout?.tag.toString().startsWith("/")) {
                holder.imageView.layoutParams = currentImageLayout?.layoutParams
                holder.imageView.shapeAppearanceModel = currentImageLayout?.shapeAppearanceModel!!
                Glide.with(context).load(currentImageLayout.tag.toString()).into(holder.imageView)
//                holder.imageView.setImageBitmap(BitmapFactory.decodeFile(currentImageLayout.tag.toString()))
                holder.imageView.tag = currentImageLayout.tag
            }
            holder.textView.text = editText?.text

            editText?.currentTextColor?.let { holder.textView.setTextColor(it) }
            holder.textView.typeface = editText?.typeface
            holder.textView.textAlignment = editText?.textAlignment!!
            holder.textView.gravity = editText.gravity
            if (holder.frameLayout.background != null) {
                val params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(context.resources.getDimensionPixelSize(R.dimen.portraitStrokeSize))
                holder.textView.layoutParams = params
            }
            holder.pageNumber.text = position.toString()
        }
        holder.frameLayout.tag = currentFrameLayout.tag
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return frameLayoutArrayList.size
    }

    /**
     * Creates a new frame layout display of a new page to be added
     * @param newFrameLayout the frame layout to duplicate
     */
    private fun createNewFrameLayout(newFrameLayout: FrameLayout): FrameLayout {
        val frameToRet = FrameLayout(context)
        val imageViewToAdd = ShapeableImageView(context)
        val editTextToAdd = EditText(context)

        val currentImageLayout = newFrameLayout.getChildAt(0) as ShapeableImageView
        val editText = newFrameLayout.getChildAt(1) as EditText

        if (newFrameLayout.background != null) {
            val drawable = newFrameLayout.background.constantState?.newDrawable()
            drawable?.setBounds(
                newFrameLayout.left,
                newFrameLayout.top,
                newFrameLayout.right,
                newFrameLayout.bottom
            )
            frameToRet.background = drawable
        }

        if (currentImageLayout.tag != null && currentImageLayout.tag.toString().startsWith("/")) {
            imageViewToAdd.layoutParams = currentImageLayout.layoutParams
            imageViewToAdd.shapeAppearanceModel = currentImageLayout.shapeAppearanceModel
            Glide.with(context).load(currentImageLayout.tag.toString()).into(imageViewToAdd)
            imageViewToAdd.tag = currentImageLayout.tag
        }
        editTextToAdd.text = editText.text
        editText.currentTextColor.let { editTextToAdd.setTextColor(it) }
        editTextToAdd.typeface = editText.typeface
        editTextToAdd.textAlignment = editText.textAlignment
        editText.gravity = editText.gravity
        frameToRet.tag = newFrameLayout.tag

        frameToRet.addView(imageViewToAdd)
        frameToRet.addView(editTextToAdd)

        return frameToRet
    }

    /**
     * Adds a new element to array
     */
    fun addElement(frameLayout: FrameLayout, index: Int) {
        if (frameLayoutArrayList.size == index) {
            frameLayoutArrayList.add(index, createNewFrameLayout(frameLayout))
            notifyItemInserted(index)
        }
    }

    /**
     * Updates the editText views text alignment
     * @param alignment The alignment of the text
     * @param index The index number of the frame layout
     */
    fun addElement(alignment: Int, gravityAlignment: Int, index: Int) {

        if (frameLayoutArrayList.size > index) {
            val currentFrame = frameLayoutArrayList[index]
            val childEditText = currentFrame.getChildAt(1) as EditText
            childEditText.textAlignment = alignment
            childEditText.gravity = gravityAlignment
            notifyItemChanged(index)
        }
    }

    /**
     * Updates the frame layouts editText view
     * @param updatedText The updated text
     * @param index The index number of the frame layout
     */
    fun addElement(updatedText: Editable, index: Int) {

        if (frameLayoutArrayList.size > index) {
            val currentFrame = frameLayoutArrayList[index]
            val childEditText = currentFrame.getChildAt(1) as EditText
            childEditText.text = updatedText
            notifyItemChanged(index)
        }
    }

    /**
     * Changes the text size of all preview pages
     */
    fun addElement(textSize: Float) {

        for (frame in frameLayoutArrayList) {
            for (child in frame.children) {
                if (child is EditText) {
                    child.textSize = textSize
                }
            }
        }
        notifyItemChanged(1, itemCount - 1)
    }

    /**
     * Changes the color of all preview pages
     *
     * @param color the updated color to add to all elements
     */
    fun addElement(color: Int) {
        for (frame in frameLayoutArrayList) {
            for (child in frame.children) {
                if (child is EditText) {
                    child.setTextColor(color)
                }
            }
        }
        notifyItemChanged(1, itemCount - 1)
    }

    /**
     * Updates the page indexes of each frame layout
     *
     * @param index the index to update
     */
    private fun updatePageNumbersAfterIndex(index: Int) {
        if (frameLayoutArrayList.lastIndex != index) {
            if (index == 1) {
                val page1EditText = frameLayoutArrayList[1].getChildAt(1) as EditText
                page1EditText.text = (frameLayoutArrayList[2].getChildAt(1) as EditText).text
                updatePageNumbersAfterIndex(2)
                notifyItemChanged(1)
            }
            if (index != 1) {
                var counter = index + 1
                while (counter < frameLayoutArrayList.size) {
                    val previousTag = frameLayoutArrayList[counter].tag as Int
                    val newIndex = previousTag - 1
                    frameLayoutArrayList[counter].tag = newIndex
                    counter++
                }
            }
        }
    }

    /**
     * Removes element from page view
     * @param index the index to remove
     */
    fun removeElement(index: Int) {
        if (index == 1 && itemCount == 2) {
            val editText = frameLayoutArrayList[index].getChildAt(1) as EditText
            if (editText.text.toString() != "") {
                editText.text = SpannableStringBuilder("")
                notifyItemChanged(1)
            }
        } else if (index == 1 && itemCount > 2) {
            updatePageNumbersAfterIndex(index)
            frameLayoutArrayList.removeAt(2)
            notifyItemRemoved(2)
        } else {
            updatePageNumbersAfterIndex(index)
            frameLayoutArrayList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * This is for testing purposes I REPEAT DO NOT UNCOMMENT IT IS HIGHLY INSECURE
     */
    fun getElement(index: Int) : FrameLayout {
        return frameLayoutArrayList[index]
    }
}