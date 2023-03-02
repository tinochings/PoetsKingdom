package com.wendorochena.poetskingdom.poemdata

import android.content.Context
import android.util.Xml
import android.view.View
import com.wendorochena.poetskingdom.R
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringWriter

class PoemThemeXmlParser(
    private var poemTheme: PoemTheme,
    private val applicationContext: Context
) {
    private val stanzas = ArrayList<String>()

    private lateinit var backgroundTypeAsString: String

    fun getPoemTheme(): PoemTheme {
        return poemTheme
    }

    fun getStanzasArraylist(): ArrayList<String> {
        return stanzas
    }

    /**
     * @return 0 if successful -1 otherwise
     */
    fun parseTheme(poemTitle: String?): Int {
        val poemThemeFolder = applicationContext.getDir("poemThemes", Context.MODE_PRIVATE)
        val inputStream =
            FileInputStream(
                File(
                    poemThemeFolder?.absolutePath + File.separator + poemTitle?.replace(
                        ' ',
                        '_'
                    ) + ".xml"
                )
            )
        try {
            inputStream.use { input ->
                val parser: XmlPullParser = Xml.newPullParser()

                parser.setInput(input, null)
                parser.nextTag()

                parser.require(XmlPullParser.START_TAG, null, "root")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) {
                        continue
                    }
                    when (parser.name) {
                        "backgroundType" -> {
                            parser.require(XmlPullParser.START_TAG, null, "backgroundType")
                            if (parser.next() == XmlPullParser.TEXT) {
                                val backgroundType = PoemTheme.parseBackgroundType(parser.text)
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "backgroundType")
                                parser.nextTag()
                                poemTheme = PoemTheme(backgroundType, applicationContext)
                                poemTheme.setTitle(poemTitle as String)
                                backgroundTypeAsString =
                                    PoemTheme.determineBackgroundTypeAsString(
                                        backgroundType
                                    )
                                parseBackgroundType(parser)
                            }
                        }
                        "textSize" -> {
                            parser.require(XmlPullParser.START_TAG, null, "textSize")
                            if (parser.next() == XmlPullParser.TEXT) {
                                poemTheme.setTextSize(parser.text.toInt())
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "textSize")
                            }
                        }
                        "textColor" -> {
                            parser.require(XmlPullParser.START_TAG, null, "textColor")
                            if (parser.next() == XmlPullParser.TEXT) {
                                poemTheme.setTextColor(parser.text)
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "textColor")
                            }
                        }
                        "textColorAsInt" -> {
                            parser.require(XmlPullParser.START_TAG, null, "textColorAsInt")
                            if (parser.next() == XmlPullParser.TEXT) {
                                poemTheme.setTextColorAsInt(parser.text.toInt())
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "textColorAsInt")
                            }
                        }
                        "textAlignment" -> {
                            parser.require(XmlPullParser.START_TAG, null, "textAlignment")
                            if (parser.next() == XmlPullParser.TEXT) {
                                poemTheme.setTextAlignment(PoemTheme.determineTextAlignment(parser.text))
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "textAlignment")
                            }
                        }
                        "textFont" -> {
                            parser.require(XmlPullParser.START_TAG, null, "textFont")
                            if (parser.next() == XmlPullParser.TEXT) {
                                poemTheme.setTextFont(parser.text)
                                parser.nextTag()
                                parser.require(XmlPullParser.END_TAG, null, "textFont")
                            }
                        }
                    }
                }
                parser.require(XmlPullParser.END_TAG, null, "root")
                poemTheme.setTitle(poemTheme.getTitle().replace('_', ' '))
                return 0
            }
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
            return -1
        }
    }

    /**
     * Parses the background type
     */
    private fun parseBackgroundType(parser: XmlPullParser) {
        try {
            //it can only be the image path
            if (backgroundTypeAsString.split(" ").size <= 1) {
                parser.require(XmlPullParser.START_TAG, null, backgroundTypeAsString)
                parser.next()
                poemTheme.setImagePath(parser.text)
                parser.nextTag()
                parser.require(XmlPullParser.END_TAG, null, backgroundTypeAsString)
            } else {
                val delimitedString = backgroundTypeAsString.split(" ")
                when (delimitedString[0]) {
                    "imagePath" -> {
                        parser.require(XmlPullParser.START_TAG, null, delimitedString[0])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setImagePath(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[0])
                            parser.nextTag()
                        }
                        parser.require(XmlPullParser.START_TAG, null, delimitedString[1])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutline(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[1])
                            parser.nextTag()
                        }

                        parser.require(XmlPullParser.START_TAG, null, delimitedString[2])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutlineColor(parser.text.toInt())
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[2])
                        }
                    }
                    "backgroundColor" -> {
                        parser.require(XmlPullParser.START_TAG, null, delimitedString[0])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setBackgroundColor(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[0])
                            parser.nextTag()
                        }
                        parser.require(XmlPullParser.START_TAG, null, delimitedString[1])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setBackgroundColorAsInt(parser.text.toInt())
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[1])
                        }
                    }
                    "backgroundOutline" -> {
                        parser.require(XmlPullParser.START_TAG, null, delimitedString[0])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutline(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[0])
                            parser.nextTag()
                        }

                        parser.require(XmlPullParser.START_TAG, null, delimitedString[1])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutlineColor(parser.text.toInt())
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[1])
                        }
                    }
                    "backgroundOutlineWithColor" -> {
                        parser.require(XmlPullParser.START_TAG, null, "backgroundOutline")
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutline(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, "backgroundOutline")
                            parser.nextTag()
                        }

                        parser.require(XmlPullParser.START_TAG, null, delimitedString[1])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setOutlineColor(parser.text.toInt())
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[1])
                            parser.nextTag()
                        }

                        parser.require(XmlPullParser.START_TAG, null, delimitedString[2])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setBackgroundColor(parser.text)
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[2])
                            parser.nextTag()
                        }

                        parser.require(XmlPullParser.START_TAG, null, delimitedString[3])
                        if (parser.next() == XmlPullParser.TEXT) {
                            poemTheme.setBackgroundColorAsInt(parser.text.toInt())
                            parser.nextTag()
                            parser.require(XmlPullParser.END_TAG, null, delimitedString[3])
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Encodes an xml file with the entire poem theme
     * @return returns 0 on success and -1 otherwise
     */
    fun savePoemThemeToLocalFile(
        backgroundImageChosen: String?,
        backgroundColorChosen: String?,
        outlineChosen: View?,
    ): Int {
        val poemThemeFolder = applicationContext.getDir(applicationContext.getString(R.string.poem_themes_folder_name), Context.MODE_PRIVATE)
        val poemFolder = applicationContext.getDir(applicationContext.getString(R.string.poems_folder_name), Context.MODE_PRIVATE)

        if (poemThemeFolder != null) {
            if (poemThemeFolder.exists()) {
                try {
                    val poemFileName = poemTheme.getTitle().replace(' ', '_') + ".xml"
                    val savedPoem = File(poemFolder.absolutePath + File.separator + poemFileName)
                    val poemFile =
                        File(poemThemeFolder.absolutePath + File.separator + poemFileName)
                    if (!savedPoem.exists() || poemFile.createNewFile()) {
                        val fileOutStream = FileOutputStream(poemFile)

                        val stringWriter = StringWriter()
                        val xmlSerializer = Xml.newSerializer()
                        xmlSerializer.setOutput(stringWriter)
                        xmlSerializer.setFeature(
                            "http://xmlpull.org/v1/doc/features.html#indent-output",
                            true
                        )
                        xmlSerializer.startDocument("UTF-8", true)
                        xmlSerializer.startTag(null, "root")

                        xmlSerializer.startTag(null, "backgroundType")
                        xmlSerializer.text(poemTheme.backgroundType.toString())
                        xmlSerializer.endTag(null, "backgroundType")

                        when (poemTheme.backgroundType) {
                            BackgroundType.IMAGE -> {
                                xmlSerializer.startTag(null, "imagePath")
                                xmlSerializer.text(backgroundImageChosen)
                                xmlSerializer.endTag(null, "imagePath")
                            }
                            BackgroundType.COLOR -> {
                                xmlSerializer.startTag(null, "backgroundColor")
                                xmlSerializer.text(backgroundColorChosen)
                                xmlSerializer.endTag(null, "backgroundColor")

                                xmlSerializer.startTag(null, "backgroundColorAsInt")
                                xmlSerializer.text(poemTheme.getBackgroundColorAsInt().toString())
                                xmlSerializer.endTag(null, "backgroundColorAsInt")
                            }
                            BackgroundType.OUTLINE -> {
                                xmlSerializer.startTag(null, "backgroundOutline")
                                if (outlineChosen == null)
                                    xmlSerializer.text(poemTheme.getOutline())
                                else
                                    xmlSerializer.text(outlineChosen.contentDescription.toString())
                                xmlSerializer.endTag(null, "backgroundOutline")

                                xmlSerializer.startTag(null, "backgroundOutlineColor")
                                xmlSerializer.text(poemTheme.getOutlineColor().toString())
                                xmlSerializer.endTag(null, "backgroundOutlineColor")
                            }
                            BackgroundType.OUTLINE_WITH_IMAGE -> {
                                xmlSerializer.startTag(null, "imagePath")
                                xmlSerializer.text(backgroundImageChosen)
                                xmlSerializer.endTag(null, "imagePath")
                                xmlSerializer.startTag(null, "backgroundOutline")
                                if (outlineChosen == null)
                                    xmlSerializer.text(poemTheme.getOutline())
                                else
                                    xmlSerializer.text(outlineChosen.contentDescription.toString())
                                xmlSerializer.endTag(null, "backgroundOutline")

                                xmlSerializer.startTag(null, "backgroundOutlineColor")
                                xmlSerializer.text(poemTheme.getOutlineColor().toString())
                                xmlSerializer.endTag(null, "backgroundOutlineColor")
                            }
                            BackgroundType.OUTLINE_WITH_COLOR -> {
                                xmlSerializer.startTag(null, "backgroundOutline")
                                if (outlineChosen == null)
                                    xmlSerializer.text(poemTheme.getOutline())
                                else
                                    xmlSerializer.text(outlineChosen.contentDescription.toString())
                                xmlSerializer.endTag(null, "backgroundOutline")

                                xmlSerializer.startTag(null, "backgroundOutlineColor")
                                xmlSerializer.text(poemTheme.getOutlineColor().toString())
                                xmlSerializer.endTag(null, "backgroundOutlineColor")

                                xmlSerializer.startTag(null, "backgroundColor")
                                xmlSerializer.text(backgroundColorChosen)
                                xmlSerializer.endTag(null, "backgroundColor")

                                xmlSerializer.startTag(null, "backgroundColorAsInt")
                                xmlSerializer.text(poemTheme.getBackgroundColorAsInt().toString())
                                xmlSerializer.endTag(null, "backgroundColorAsInt")
                            }
                            BackgroundType.DEFAULT -> {
                                xmlSerializer.startTag(null, "backgroundColor")
                                xmlSerializer.text("#FFFFFF")
                                xmlSerializer.endTag(null, "backgroundColor")

                                xmlSerializer.startTag(null, "backgroundColorAsInt")
                                xmlSerializer.text(poemTheme.getBackgroundColorAsInt().toString())
                                xmlSerializer.endTag(null, "backgroundColorAsInt")
                            }
                        }

                        xmlSerializer.startTag(null, "textSize")
                        xmlSerializer.text(poemTheme.getTextSize().toString())
                        xmlSerializer.endTag(null, "textSize")

                        xmlSerializer.startTag(null, "textColor")
                        xmlSerializer.text(poemTheme.getTextColor())
                        xmlSerializer.endTag(null, "textColor")

                        xmlSerializer.startTag(null, "textColorAsInt")
                        xmlSerializer.text(poemTheme.getTextColorAsInt().toString())
                        xmlSerializer.endTag(null, "textColorAsInt")

                        xmlSerializer.startTag(null, "textAlignment")
                        xmlSerializer.text(poemTheme.getTextAlignment().toString())
                        xmlSerializer.endTag(null, "textAlignment")

                        xmlSerializer.startTag(null, "textFont")
                        xmlSerializer.text(poemTheme.getTextFont())
                        xmlSerializer.endTag(null, "textFont")

                        xmlSerializer.endTag(null, "root")

                        xmlSerializer.endDocument()
                        xmlSerializer.flush()

                        val dataWritten = stringWriter.toString()
                        fileOutStream.write(dataWritten.toByteArray())
                        fileOutStream.close()
                        return 0
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    return -1
                }
            }
        }
        return -1
    }

}