package com.wendorochena.poetskingdom.poemdata

import android.content.Context
import android.util.Xml
import com.wendorochena.poetskingdom.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun saveToXmlFile(albumName: String?): Int {
        return withContext(Dispatchers.IO) {
            try {
                val poemFolder =
                    context.getDir(
                        context.getString(R.string.poems_folder_name),
                        Context.MODE_PRIVATE
                    )
                if (poemFolder.exists()) {
                    val fileName = poem.poemTheme.poemTitle.replace(' ', '_')

                    val fileToCreate = if (albumName != null)
                        File(poemFolder.absolutePath + File.separator + albumName + File.separator + fileName + ".xml")
                    else
                        File(poemFolder.absolutePath + File.separator + fileName + ".xml")
                    if (fileToCreate.exists() || fileToCreate.createNewFile()) {
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
                        xmlSerializer.text(poem.poemTheme.poemTitle)
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
                        return@withContext 0
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                return@withContext -1
            }

            return@withContext -1
        }
    }

    companion object PoemXMLFileParserHelper {

        /**
         * @param poemTitle : The title of the poem to obtain saved stanzas
         * @param applicationContext : the context of the calling class
         *
         * @return the arraylist containing each stanza as an element
         */
        suspend fun parseSavedPoem(
            poemTitle: String,
            applicationContext: Context,
            ioDispatcher: CoroutineDispatcher,
            albumName: String?
        ): ArrayList<String> {
            return withContext(ioDispatcher) {
                val stanzas = ArrayList<String>()
                val poemsFolder = applicationContext.getDir(
                    applicationContext.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
                val fileToUse = if (albumName != null)
                    File(
                        poemsFolder.absolutePath + File.separator + albumName.replace(' ', '_') + File.separator + poemTitle.replace(
                            ' ',
                            '_'
                        ) + ".xml"
                    )
                else
                    File(
                        poemsFolder.absolutePath + File.separator + poemTitle.replace(
                            ' ',
                            '_'
                        ) + ".xml"
                    )

                if (fileToUse.exists()) {
                    val inputStream =
                        FileInputStream(
                            fileToUse
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
                                                parser.require(
                                                    XmlPullParser.START_TAG,
                                                    null,
                                                    "stanza$counter"
                                                )
                                                if (parser.next() == XmlPullParser.TEXT) {
                                                    stanzas.add(parser.text)
                                                    parser.nextTag()
                                                } else {
                                                    stanzas.add("")
                                                }
                                                parser.require(
                                                    XmlPullParser.END_TAG,
                                                    null,
                                                    "stanza$counter"
                                                )
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
                }
                return@withContext stanzas
            }
        }

        /**
         * Parses multiple poems
         *
         * @param fileNames the file names to parse
         * @param applicationContext the context of the application
         */
        suspend fun parseMultiplePoems(
            fileNames: ArrayList<String>,
            applicationContext: Context,
            ioDispatcher: CoroutineDispatcher
        ): ArrayList<Pair<String, ArrayList<String>>> {
            return withContext(ioDispatcher) {
                val stanzasArrayList = ArrayList<Pair<String, ArrayList<String>>>()

                for (fileName in fileNames) {
                    val isAlbumPoem = fileName.contains(File.separator)
                    val albumName = if (isAlbumPoem)
                        fileName.split(File.separator)[0]
                    else
                        null

                    val fileNameSplit = if (isAlbumPoem)
                        fileName.split(File.separator)[1].split(".")[0]
                    else
                        fileName.split(".")[0]

                    stanzasArrayList.add(
                        Pair(
                            fileNameSplit,
                            parseSavedPoem(
                                fileNameSplit,
                                applicationContext,
                                ioDispatcher,
                                albumName
                            )
                        )
                    )
                }

                return@withContext stanzasArrayList
            }
        }
    }
}