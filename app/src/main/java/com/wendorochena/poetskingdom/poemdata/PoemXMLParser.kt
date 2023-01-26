package com.wendorochena.poetskingdom.poemdata

import android.content.Context
import android.util.Xml
import com.wendorochena.poetskingdom.R
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter

class PoemXMLParser(private val poem: PoemDataContainer, val context: Context) {

    /**
     *
     * @param overwrite true if we want to create a new file and false if we want to overwrite a file
     * @return 0 is returned if operation was successful
     * @return 1 if file exist
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
//                if (overwrite && fileToCreate.exists()) {
//                    return 1
//                } else if ((overwrite && fileToCreate.createNewFile()) || !overwrite) {
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
//            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return -1
        }

        return -1
    }
}