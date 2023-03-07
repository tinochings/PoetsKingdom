package com.wendorochena.poetskingdom.recyclerViews

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.BackgroundType
import java.io.File
import kotlin.collections.set

class SearchResultsRecyclerViewAdapter(
    val context: Context,
    private val pair: Pair<ArrayList<Pair<String, String>>, HashMap<String, ArrayList<Pair<Int, String>>>>
) : RecyclerView.Adapter<SearchResultsRecyclerViewAdapter.ViewHolder>() {

    private var searchResultsAdapter: Pair<ArrayList<Pair<String, String>>, HashMap<String, ArrayList<Pair<Int, String>>>> =
        this.pair

    private val poemBackgroundTypeArrayList = ArrayList<Pair<BackgroundType, Int>>()
    var onItemClick: ((String) -> Unit)? = null

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var searchTitle: TextView
        var searchText: TextView
        var searchStanzas: TextView
        var frameLayoutParent: FrameLayout
        var backgroundImage : ImageView

        init {
            searchTitle = view.findViewById(R.id.searchTitle)
            searchText = view.findViewById(R.id.searchText)
            searchStanzas = view.findViewById(R.id.searchStanzas)
            frameLayoutParent = view.findViewById(R.id.parent)
            frameLayoutParent.setOnClickListener {
                onItemClick?.invoke(frameLayoutParent.tag.toString())
            }
            backgroundImage = view.findViewById(R.id.backgroundImage)
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
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_results_layout, parent, false)

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
        val subStringLocations = searchResultsAdapter.first[position]
        val searchTitle = subStringLocations.first.split(".")[0]
        val underlineSpan = SpannableString(searchTitle.replace('_', ' '))
        underlineSpan.setSpan(UnderlineSpan(), 0, underlineSpan.length, 0)
        holder.searchTitle.text = underlineSpan
        holder.searchTitle.setTextColor(poemBackgroundTypeArrayList[position].second)
        //take into account the newline character
        var stanzaNumbersText = ""
        val stanzaIndexAndText = searchResultsAdapter.second[subStringLocations.first]

        if (stanzaIndexAndText != null) {
            val tripleArrayList = HashMap<Int, ArrayList<Pair<Int, Int>>>()
            for (substringLocation in subStringLocations.second.lines()) {
                val delimitedString = substringLocation.split(" ")

                // must be an error
                if (delimitedString.size != 3)
                    break

                val stanzaNum = delimitedString[0].toIntOrNull()
                val startIndex = delimitedString[1].toIntOrNull()
                val endIndex = delimitedString[2].toIntOrNull()

                if (stanzaNum != null && startIndex != null && endIndex != null) {
                    if (tripleArrayList[stanzaNum] == null)
                        tripleArrayList[stanzaNum] = ArrayList()

                    tripleArrayList[stanzaNum]?.add(Pair(startIndex, endIndex))
                }
            }

            var holderText: Spannable = SpannableString("")
            for ((counter, pair) in stanzaIndexAndText.withIndex()) {
                val spannableString = if (counter != stanzaIndexAndText.lastIndex)
                    SpannableString(pair.second + "\n\n")
                else
                    SpannableString(pair.second)
                val foregroundColor = BackgroundColorSpan(Color.YELLOW)

                for (indices in tripleArrayList[pair.first]!!) {
                    spannableString.setSpan(
                        CharacterStyle.wrap(foregroundColor),
                        indices.first,
                        indices.second,
                        0
                    )
                }
                holderText += spannableString
            }
            holder.searchText.setTextColor(poemBackgroundTypeArrayList[position].second)
            if (poemBackgroundTypeArrayList[position].first.toString().contains("OUTLINE")){
                val linearLayoutParams = holder.searchText.layoutParams as FrameLayout.LayoutParams
                linearLayoutParams.marginStart = context.resources.getDimensionPixelSize(R.dimen.portraitStrokeSizeMargin)
                linearLayoutParams.marginEnd = context.resources.getDimensionPixelSize(R.dimen.portraitStrokeSizeMargin)
                holder.searchText.layoutParams = linearLayoutParams
            }
            holder.searchText.text = holderText
        }

        // obtain locations
        for (line in subStringLocations.second.lines()) {
            val number = line.split(" ")[0].toIntOrNull()
            if (number != null) {
                if (stanzaNumbersText.isEmpty() || stanzaNumbersText[stanzaNumbersText.length - 2].digitToInt() != number) {
                    stanzaNumbersText += "$number "
                }
            }
        }

        holder.searchStanzas.setTextColor(poemBackgroundTypeArrayList[position].second)
        holder.searchStanzas.text = "Stanzas: " + stanzaNumbersText

        try {
            val backgroundImageDrawableFolder = context.getDir(
                context.getString(R.string.background_image_drawable_folder),
                Context.MODE_PRIVATE
            )
            val backgroundFileImage =
                File(backgroundImageDrawableFolder.absolutePath + File.separator + searchTitle + ".png")

            if (backgroundFileImage.exists()) {

                Glide.with(context).load(backgroundFileImage.absolutePath).into(holder.backgroundImage)
                holder.backgroundImage.contentDescription = holder.searchText.text.toString() + "background image"
//                val bitmapImage = BitmapFactory.decodeFile(backgroundFileImage.absolutePath)
//                holder.linearParent.background = bitmapImage.toDrawable(context.resources)

            } else {
                holder.frameLayoutParent.background = ColorDrawable(Color.WHITE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.frameLayoutParent.tag = searchTitle
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return searchResultsAdapter.first.size
    }

    /**
     *
     */
    fun addBackgroundTypePair(pair: Pair<BackgroundType, Int>) {
        poemBackgroundTypeArrayList.add(pair)
    }

    operator fun Spannable.plus(other: Spannable): Spannable {
        return SpannableStringBuilder(this).append(other)
    }

    /**
     * Attempts to clear data from the currently saved search
     */
    fun clearData(): Int {
        try {
            searchResultsAdapter.first.clear()
            searchResultsAdapter.second.clear()
            poemBackgroundTypeArrayList.clear()
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        return 0
    }
}