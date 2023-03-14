package com.wendorochena.poetskingdom.utils

import android.content.Context
import androidx.databinding.ObservableArrayList
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.PoemXMLParser
import kotlinx.coroutines.*
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
import java.io.FileReader

/**
 * The search phrase input by the user is run through Lucene with a boosted scoring of 2.
 * Lucene searches and finds the most relevant files containing the search phrase.
 * The subset of files containing the search phrase is further passed down the pipeline to hit highlight
 * and present to the user
 *
 * NOTE SCORING NEEDS TO BE LOOKED INTO
 */
class SearchUtil(private val searchPhrase: String, val applicationContext: Context) {
    private val analyzer: StandardAnalyzer = StandardAnalyzer(Version.LUCENE_43)
    private lateinit var indexWriter: IndexWriter
    private lateinit var titleSearchResults: ArrayList<String>
    private lateinit var subStringLocations : ObservableArrayList<Pair<String, String>>
    private lateinit var stanzaIndexAndText : HashMap<String, ArrayList<Pair<Int, String>>>
    private var itemCount = -1

    /**
     * @return An arraylist containing sub-string locations of the search results
     */
    fun getSubStringLocations() : ObservableArrayList<Pair<String, String>> {
        if (!this::subStringLocations.isInitialized)
            return ObservableArrayList()

        return subStringLocations
    }

    fun getItemCount () : Int {

        return itemCount
    }

    /**
     * @return A Hash Map containing the name of the file as a key. The value is an arrayList where
     * the first element is the stanza number and the second element is the actual stanza
     */
    fun getStanzaAndText() : HashMap<String, ArrayList<Pair<Int, String>>> {
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

            if (poemFolder.exists()) {
                try {
                    indexWriter.deleteAll()
                    val allPoemFiles = poemFolder.listFiles()
                    if (allPoemFiles != null) {
                        for (poemFile in allPoemFiles) {
                            val fileReader = FileReader(poemFile)
                            try {
                                val document = Document()
                                val field = TextField("xmlFile", fileReader)
                                // the idea behind this is that the scoring probability should be higher
                                // or equal to 0.5 when boosted. This will help filter really low probabilities
                                // of finding a sub phrase
                                field.setBoost(2f)
                                document.add(field)
                                document.add(
                                    StringField(
                                        "fileName",
                                        poemFile.name,
                                        Field.Store.YES
                                    )
                                )

                                indexWriter.addDocument(document)
                                println("added : ${poemFile.name}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                fileReader.close()
                            }
                        }

                        indexWriter.commit()
                        indexWriter.close()
                        val indexReader = DirectoryReader.open(FSDirectory.open(indexFolder))
                        val indexSearcher = IndexSearcher(indexReader)
                        val collector = TopScoreDocCollector.create(10, true)

                        try {
                            val query = QueryParser(Version.LUCENE_43, "xmlFile", analyzer).parse(
                                searchPhrase
                            )
                            indexSearcher.search(query, collector)
                            val scoreDocHits = collector.topDocs().scoreDocs

                            println("${scoreDocHits.size} hits have been found")

                            for (hit in scoreDocHits) {
                                val currDocument = indexSearcher.doc(hit.doc)

                                // if convoluted with a lot of text a result of 0.1 should be sufficient
                                if (hit.score >= 0.1) {
                                    if (!this::titleSearchResults.isInitialized)
                                        titleSearchResults = ArrayList()

                                    titleSearchResults.add(currDocument.get("fileName"))
                                }

                                println(currDocument.get("fileName") + " score= ${hit.score}")
                            }
                        } catch (e: Exception) {
                            println("Error Searching for $searchPhrase and error ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
            exception.printStackTrace()
        }

        GlobalScope.launch(Dispatchers.Main + handler) {
            val stanzasArrayList = PoemXMLParser.parseMultiplePoems(titleSearchResults, applicationContext)

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

                                    if (!stanzaIndexAndText.containsKey(poemFileName)){
                                        val arrayListPair = ArrayList<Pair<Int, String>>()
                                        arrayListPair.add(Pair(stanzaIndex + 1, stanza))
                                        stanzaIndexAndText[poemFileName] = arrayListPair
                                    } else{
                                        val arrayListPair = stanzaIndexAndText[poemFileName]
                                        if (arrayListPair?.last()?.first != stanzaIndex + 1)
                                            arrayListPair?.add(Pair(stanzaIndex + 1,stanza))
                                    }
                                }
                                else if (searchPhrase[searchCharCounter] == stanza[counter]) {
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
                temp.add(Pair(poemFileName, preciseLocation))
            }
            subStringLocations.addAll(temp)
        }
    }
}