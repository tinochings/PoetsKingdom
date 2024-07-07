package com.wendorochena.poetskingdom.utils

import android.content.Context
import androidx.databinding.ObservableArrayList
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.PoemXMLParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * The search phrase input by the user is run through Lucene with a boosted scoring of 2.
 * Lucene searches and finds the most relevant files containing the search phrase.
 * The subset of files containing the search phrase is further passed down the pipeline to hit highlight
 * and present to the user
 *
 * NOTE Lucene uses keyword AND to make sure an exact sub-phrase is matched together, without it the
 * default is the OR operator where it returns results for a document that could contain any of the
 * keywords
 *
 *
 * NOTE SPECIAL CHARS ARE NOT ESCAPED
 *
 * @see <a href="http://web.cs.ucla.edu/classes/winter15/cs144/projects/lucene/index.html">Lucene Intro</a>
 */
class SearchUtil(
    private val searchPhrase: String,
    val applicationContext: Context,
    private val searchType: String,
    private val mainDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val analyzer: StandardAnalyzer = StandardAnalyzer(Version.LUCENE_43)
    private lateinit var indexWriter: IndexWriter
    private lateinit var titleSearchResults: ArrayList<String>
    private lateinit var subStringLocations: ObservableArrayList<Pair<String, String>>
    private lateinit var stanzaIndexAndText: HashMap<String, ArrayList<Pair<Int, String>>>
    private var itemCount = -1
    private var fileNameAndAlbum: HashMap<String, String> = HashMap()

    /**
     * @return An arraylist containing sub-string locations of the search results
     */
    fun getSubStringLocations(): ObservableArrayList<Pair<String, String>> {
        if (!this::subStringLocations.isInitialized)
            return ObservableArrayList()

        return subStringLocations
    }

    fun getItemCount(): Int {
        return itemCount
    }

    /**
     * @return the title names of all search docs
     */
    fun getTitleSearchResults(): ArrayList<String> {
        if (!this::titleSearchResults.isInitialized)
            return ArrayList()

        return titleSearchResults
    }

    /**
     * @return A Hash Map containing the name of the file as a key. The value is an arrayList where
     * the first element is the stanza number and the second element is the actual stanza
     */
    fun getStanzaAndText(): HashMap<String, ArrayList<Pair<Int, String>>> {
        if (!this::stanzaIndexAndText.isInitialized) {
            return HashMap()
        }

        return stanzaIndexAndText
    }

    /**
     * This function initiates Lucene to search
     */
    fun initiateLuceneSearch() {
        val poemFolder = applicationContext.getDir(
            applicationContext.getString(R.string.poems_folder_name),
            Context.MODE_PRIVATE
        )
        val indexFolder = applicationContext.getDir("index", Context.MODE_PRIVATE)

        if (indexFolder.exists()) {
            val configuration = IndexWriterConfig(Version.LUCENE_43, analyzer)
            val directory = FSDirectory.open(indexFolder)
            indexWriter = IndexWriter(directory, configuration)
            var shouldContinueSearch = true

            if (poemFolder.exists()) {
                try {
                    indexWriter.deleteAll()
                    val allPoemFiles = poemFolder.listFiles()
                    if (allPoemFiles != null) {
                        for (poemFile in allPoemFiles) {
                            try {
                                // add poems in an album
                                if (poemFile.isDirectory) {
                                    val subPoems = poemFile.listFiles()
                                    if (subPoems != null) {
                                        for (subPoemFile in subPoems) {
                                            val fileReader = FileReader(subPoemFile)
                                            val document = Document()
                                            val subPoemFileName =
                                                subPoemFile.name.split(".")[0].replace('_', ' ')
                                            val field = TextField("xmlFile", fileReader)
                                            fileNameAndAlbum[subPoemFileName] = poemFile.name
                                            field.setBoost(2f)
                                            document.add(field)
                                            document.add(
                                                StringField(
                                                    "fileName",
                                                    subPoemFileName,
                                                    Field.Store.YES
                                                )
                                            )
                                            indexWriter.addDocument(document)
                                            fileReader.close()
                                        }
                                    }
                                } else {
                                    val fileReader = FileReader(poemFile)
                                    val document = Document()
                                    val field = TextField("xmlFile", fileReader)
                                    field.setBoost(2f)
                                    document.add(field)
                                    document.add(
                                        StringField(
                                            "fileName",
                                            poemFile.name.split(".")[0].replace('_', ' '),
                                            Field.Store.YES
                                        )
                                    )
                                    indexWriter.addDocument(document)
                                    fileReader.close()
                                }
                            } catch (e: IOException) {
                                indexWriter.close()
                                shouldContinueSearch = false
                            }
                        }
                        if (shouldContinueSearch) {
                            indexWriter.commit()
                            indexWriter.close()
                            val indexReader = DirectoryReader.open(FSDirectory.open(indexFolder))
                            val indexSearcher = IndexSearcher(indexReader)
                            val collector = TopScoreDocCollector.create(10, true)

                            try {


                                val queryParser =
                                    QueryParser(Version.LUCENE_43, "xmlFile", analyzer)

                                when (searchType) {
                                    applicationContext.getString(R.string.exact_phrase_search) -> {
                                        val exactSearchSubPhrase = "\"$searchPhrase\""
                                        val query = queryParser.parse(exactSearchSubPhrase)
                                        indexSearcher.search(query, collector)
                                    }

                                    applicationContext.getString(R.string.approximate_phrase_search) -> {
                                        val approximatePhrase = "\"$searchPhrase\"~"
                                        val query = queryParser.parse(approximatePhrase)
                                        indexSearcher.search(query, collector)
                                    }

                                    else -> {
                                        val query = queryParser.parse(searchPhrase)
                                        indexSearcher.search(query, collector)
                                    }
                                }

                                val scoreDocHits = collector.topDocs().scoreDocs

                                for (hit in scoreDocHits) {
                                    val currDocument = indexSearcher.doc(hit.doc)

                                    if (!this::titleSearchResults.isInitialized)
                                        titleSearchResults = ArrayList()
                                    val albumName =
                                        if (fileNameAndAlbum[currDocument.get("fileName")] == null)
                                            ""
                                        else
                                            fileNameAndAlbum[currDocument.get("fileName")] + File.separator

                                    titleSearchResults.add(
                                        albumName + currDocument.get("fileName")
                                            .replace(' ', '_') + ".xml"
                                    )
                                }
                            } catch (e: IOException) {
                            }
                        }
                    }
                } catch (e: IOException) {
                    indexWriter.close()
                }
            }

            if (this::titleSearchResults.isInitialized) {
                subStringLocations = ObservableArrayList()
                itemCount = titleSearchResults.size
                locatePreciseLocation()
            }
        }
    }

    /**
     * This is a costly algorithm, albeit necessary.
     * The exact locations of the searched sub-phrase are found by matching the exact char sequence
     * The resulting locations are then returned back to class MyPoems which will then display the results
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun locatePreciseLocation() {
        val temp = ObservableArrayList<Pair<String, String>>()
        stanzaIndexAndText = HashMap()

        val handler = CoroutineExceptionHandler { _, exception ->

        }

        GlobalScope.launch(mainDispatcher + handler) {
            val stanzasArrayList =
                PoemXMLParser.parseMultiplePoems(
                    titleSearchResults,
                    applicationContext,
                    ioDispatcher
                )

            for (fileNameAndStanzas in stanzasArrayList) {
                val poemFileName = fileNameAndStanzas.first.split(".")[0]
                var preciseLocation = ""

                for ((stanzaIndex, stanza) in fileNameAndStanzas.second.withIndex()) {
                    var counter = 0
                    var searchCharCounter = 0


                    while (counter < stanza.length) {

                        if (searchPhrase[searchCharCounter] == stanza[counter] && (counter + searchPhrase.length <= stanza.length)) {
                            val startIndex = counter
                            searchCharCounter++
                            counter++

                            var isExactSubString = true

                            while (isExactSubString) {
                                // found exact sub-string
                                // The precise location is stored as a string delimited by space
                                // StanzaNum BeginningIndexNum EndIndexNum
                                if (searchCharCounter == searchPhrase.length) {
                                    isExactSubString = false
                                    val stanzaNum = stanzaIndex + 1
                                    preciseLocation += "$stanzaNum $startIndex $counter\n"
                                    searchCharCounter = 0

                                    if (!stanzaIndexAndText.containsKey(poemFileName)) {
                                        val arrayListPair = ArrayList<Pair<Int, String>>()
                                        arrayListPair.add(Pair(stanzaIndex + 1, stanza))
                                        stanzaIndexAndText[poemFileName] = arrayListPair
                                    } else {
                                        val arrayListPair = stanzaIndexAndText[poemFileName]
                                        if (arrayListPair?.last()?.first != stanzaIndex + 1)
                                            arrayListPair?.add(Pair(stanzaIndex + 1, stanza))
                                    }
                                } else if (searchPhrase[searchCharCounter] == stanza[counter]) {
                                    searchCharCounter++
                                    counter++
                                } else {
                                    searchCharCounter = 0
                                    isExactSubString = false
                                }
                            }
                        } else {
                            counter++
                        }
                    }
                }
                //filter results in case of false positives
                if (preciseLocation.isEmpty()) {
                    if (poemFileName.replace('_',' ').lowercase().contains(searchPhrase.lowercase())) {
                        val toAdd = Pair(1, fileNameAndStanzas.second[0])
                        val arrayListPair = ArrayList<Pair<Int, String>>()
                        arrayListPair.add(toAdd)
                        stanzaIndexAndText[poemFileName] = arrayListPair
                        preciseLocation = "1 -1 -1"
                        temp.add(Pair(poemFileName, preciseLocation))
                    }
                }
                else
                    temp.add(Pair(poemFileName, preciseLocation))
            }
            if (temp.isEmpty())
                subStringLocations.add(Pair("null","null"))
            else
                subStringLocations.addAll(temp)
        }
    }
}