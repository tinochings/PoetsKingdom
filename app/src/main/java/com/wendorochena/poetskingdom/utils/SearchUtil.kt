package com.wendorochena.poetskingdom.utils

import android.content.Context
import com.wendorochena.poetskingdom.R
import com.wendorochena.poetskingdom.poemdata.PoemXMLParser
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
 */
class SearchUtil(private val searchPhrase: String, val applicationContext: Context) {
    private val analyzer: StandardAnalyzer = StandardAnalyzer(Version.LUCENE_43)
    private lateinit var indexWriter: IndexWriter
    private lateinit var titleSearchResults: ArrayList<String>
    private lateinit var subStringLocations : ArrayList<Pair<String, String>>

    /**
     * @return An arraylist containing sub-string locations of the search results
     */
    fun getSubStringLocations() : ArrayList<Pair<String, String>> {
        if (!this::subStringLocations.isInitialized)
            return ArrayList()

        return subStringLocations
    }
    /**
     * This function creates meta data for each poem file. The file name is the key of the hash map
     * the value is the stanzas to be iterated over.
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

                                if (hit.score >= 0.4) {
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

            if (this::titleSearchResults.isInitialized)
                locatePreciseLocation()
        }
    }

    /**
     * This is a costly algorithm, albeit necessary.
     * The exact locations of the searched sub-phrase are found by matching the exact char sequence
     * The resulting locations are then returned back to class MyPoems which will then display the results
     */
    private fun locatePreciseLocation() {
        subStringLocations = ArrayList()

        for (poemFileName in titleSearchResults) {
            val fileName = poemFileName.split(".")
            val stanzas = PoemXMLParser.parseSavedPoem(fileName[0], applicationContext)

            for ((stanzaCounter, stanza) in stanzas.withIndex()) {
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
                                val stanzaNum = stanzaCounter + 1
                                val preciseLocation = "$stanzaNum $startIndex $counter"
                                subStringLocations.add(Pair(poemFileName, preciseLocation))
                                searchCharCounter = 0
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
        }
        println(subStringLocations)
    }
}