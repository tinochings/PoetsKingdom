package com.wendorochena.poetskingdom.poemdata

import android.content.Context
import android.graphics.Bitmap
import android.util.Xml
import com.wendorochena.poetskingdom.R
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringWriter

class PoemXMLParser(private val poem: PoemDataContainer, val context: Context) {

    /**
     * Saves the poem as an XML file
     *
     * @return 0 is returned if operation was successful
     * @return -1 if operation failed
     */
    fun saveToXmlFile(): Int {
        try {
            val poemFolder =
                context.getDir(context.getString(R.string.poems_folder_name), Context.MODE_PRIVATE)
            if (poemFolder.exists()) {
                val fileName = poem.poemTheme.getTitle().replace(' ', '_')
                val fileToCreate =
                    File(poemFolder.absolutePath + File.separator + fileName + ".xml")

                val outputStream = FileOutputStream(fileToCreate)

                val stringWriter = StringWriter()
                val xmlSerializer = Xml.newSerializer()
                xmlSerializer.setOutput(stringWriter)
                xmlSerializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output", true
                )
                xmlSerializer.startDocument("UTF-8", true)
                xmlSerializer.startTag(null, "root")

                xmlSerializer.startTag(null, "pages")
                xmlSerializer.text(poem.getPages().toString())
                xmlSerializer.endTag(null, "pages")

                xmlSerializer.startTag(null, "category")
                xmlSerializer.text(poem.category.toString())
                xmlSerializer.endTag(null, "category")

                xmlSerializer.startTag(null, "title")
                xmlSerializer.text(poem.poemTheme.getTitle())
                xmlSerializer.endTag(null, "title")

                var counter = 1

                for (editable in poem.getPoemTextArray()) {
                    xmlSerializer.startTag(null, "stanza$counter")
                    xmlSerializer.text(editable.toString())
                    xmlSerializer.endTag(null, "stanza$counter")
                    counter++
                }

                xmlSerializer.endTag(null, "root")

                xmlSerializer.endDocument()
                xmlSerializer.flush()

                val dataWritten = stringWriter.toString()
                outputStream.write(dataWritten.toByteArray())
                outputStream.close()
                return 0
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return -1
        }

        return -1
    }

    /**
     * Saves the background image as a bitmap. This is necessary for the search poem recycler view
     *
     * @param toBitmap the bitmap to save
     * @return true if it was saved false if it was not
     */
    fun saveBackgroundImageDrawable(toBitmap: Bitmap) : Boolean {
        val backgroundImageDrawableFolder = context.getDir(context.getString(R.string.background_image_drawable_folder), Context.MODE_PRIVATE)

        if (backgroundImageDrawableFolder.exists()) {
            val poemTitle = poem.poemTheme.getTitle().replace(" ","_")
            try {
                val fileToSave = File(backgroundImageDrawableFolder.absolutePath + File.separator + poemTitle + ".png")

                if  (fileToSave.exists() || fileToSave.createNewFile()) {
                    val outStream = FileOutputStream(fileToSave)
                    val toRet = toBitmap.compress(Bitmap.CompressFormat.PNG,100, outStream)
                    outStream.close()
                    return toRet
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    companion object PoemXMLFileParserHelper {

        /**
         * @param poemTitle : The title of the poem to obtain saved stanzas
         * @param applicationContext : the context of the calling class
         *
         * @return the arraylist containing each stanza as an element
         */
        fun parseSavedPoem(poemTitle: String, applicationContext : Context) : ArrayList<String> {
            val stanzas = ArrayList<String>()
            val poemsFolder = applicationContext.getDir("poems", Context.MODE_PRIVATE)
            val inputStream =
                FileInputStream(
                    File(
                        poemsFolder?.absolutePath + File.separator + poemTitle.replace(
                            ' ',
                            '_'
                        ) + ".xml"
                    )
                )

            try {
                inputStream.use { input ->
                    val parser: XmlPullParser = Xml.newPullParser()
                    parser.setInput(input, null)
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

                    parser.nextTag()
                    var numOfPages = 0
                    parser.require(XmlPullParser.START_TAG, null, "root")
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType != XmlPullParser.START_TAG) {
                            continue
                        }
                        when (parser.name) {
                            "pages" -> {
                                parser.require(XmlPullParser.START_TAG, null, "pages")
                                if (parser.next() == XmlPullParser.TEXT) {
                                    numOfPages = parser.text.toInt()
                                }
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "pages")
                            }
                            "category" -> {
                                parser.require(XmlPullParser.START_TAG, null, "category")
                                if (parser.next() == XmlPullParser.TEXT) {
                                }
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "category")
                            }
                            "title" -> {
                                parser.require(XmlPullParser.START_TAG, null, "title")
                                parser.next()
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "title")
                            }
                            "stanza1" -> {
                                parser.require(XmlPullParser.START_TAG, null, "stanza1")
                                if (parser.next() == XmlPullParser.TEXT) {
                                    stanzas.add(parser.text)
                                    parser.nextTag()
                                }
                                parser.require(XmlPullParser.END_TAG, null, "stanza1")

                                if (numOfPages > 1) {
                                    parser.nextTag()
                                    var counter = 2
                                    while (counter <= numOfPages) {
                                        parser.require(XmlPullParser.START_TAG, null, "stanza$counter")
                                        if (parser.next() == XmlPullParser.TEXT) {
                                            stanzas.add(parser.text)
                                            parser.nextTag()
                                        } else {
                                            stanzas.add("")
                                        }
                                        parser.require(XmlPullParser.END_TAG, null, "stanza$counter")
                                        if (counter < numOfPages)
                                            parser.nextTag()
                                        counter++
                                    }
                                }
                            }
                        }
                    }
                    parser.require(XmlPullParser.END_TAG, null, "root")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream.close()
            }
            return stanzas
        }
    }
}