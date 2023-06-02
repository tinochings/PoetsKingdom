package com.wendorochena.poetskingdom

import android.content.Context
import com.wendorochena.poetskingdom.poemdata.PoemXMLParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PoemParserTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mock {
            on {
                getDir(
                    "index",
                    Context.MODE_PRIVATE
                )
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/mock_index")
            on {
                getDir(
                    this.getString(R.string.poems_folder_name),
                    Context.MODE_PRIVATE
                )
            } doReturn File("../app/src/test/java/com/wendorochena/poetskingdom/MockFiles/poems")
        }
    }

    /**
     * Simple Poems have 1 stanza only namely:
     * Search_Test_1.xml, This_is_search_1.xml, Boundary_Test_1.xml, HELLO.xml, Load_Default_Test
     */
    @Test
    fun testParseSimplePoem(): Unit = runTest {
        var result =
            PoemXMLParser.parseSavedPoem("Search Test 1", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 1)
        assert(result[0].lines().size == 1)
        assert(result[0] == "I want you")

        result = PoemXMLParser.parseSavedPoem("This is search 1", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 1)
        assert(result[0].lines().size == 1)
        assert(
            result[0] == "This is an example of a very long exact search string that will be " +
                    "tested in the same manner as the previous tests"
        )

        result = PoemXMLParser.parseSavedPoem("Boundary Test 1", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 1)
        assert(result[0].lines().size == 1)
        assert(
            result[0] == "K-nearest neighbors, abbreviated to KNN for future mentions is a " +
                    "classification technique that assigns a class to the k nearest points in regards to" +
                    " a point x. The prevailing class in all chosen k points is the class that is" +
                    " assigned. Before we start attempting to classify with the KNN method we split " +
                    "training data and test data. This helps us avoid over-fitting, in this way when we " +
                    "test the data it is tested on data that has never been seen before and as such gives " +
                    "us more conclusive results. For our KNN classifier we split the data to 70% training " +
                    "data and 30% test data. Generally to find the most optimal value of k to use we use" +
                    " the formula k = n0.5, where k is the number of nearest points and n is the number " +
                    "of instances. However for our hyper parameter k we decided for a much lower value " +
                    "than such k that would have been deduced by square rooting the number of our " +
                    "instances. Due to a general higher bias the larger the value k is we decided to go " +
                    "with the value 5."
        )

        result = PoemXMLParser.parseSavedPoem("HELLO", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 1)
        assert(result[0].lines().size == 1)
        assert(result[0] == "Sdfdsfsd dsf")

        result =
            PoemXMLParser.parseSavedPoem("Load Default Test", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 1)
        assert(result[0].lines().size == 1)
        assert(result[0] == "HAHAHAHA")
    }

    /**
     * Complex poems are poems with relatively a lot of text for a poem and have 10 stanzas
     *
     * This is an expensive test so I will just test the boundary test 2 case
     */
    @Test
    fun testComplexPoem(): Unit = runTest {
        val result =
            PoemXMLParser.parseSavedPoem("Boundary Test 2", mockContext, Dispatchers.IO, null)
        assert(result.isNotEmpty())
        assert(result.size == 10)
        assert(result[0].lines().size == 34)
        assert(
            result[0] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )
        assert(result[1].lines().size == 34)
        assert(
            result[1] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[2].lines().size == 34)
        assert(
            result[2] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[3].lines().size == 34)
        assert(
            result[3] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[4].lines().size == 34)
        assert(
            result[4] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[5].lines().size == 34)
        assert(
            result[5] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[6].lines().size == 34)
        assert(
            result[6] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[7].lines().size == 34)
        assert(
            result[7] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[8].lines().size == 34)
        assert(
            result[8] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(result[9].lines().size == 35)
        assert(
            result[9] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg \n" +
                    "K-nearest neighbors, abbreviated to KNN for future mentions is a classification technique that assigns a class to the k nearest points in regards to a point x. The prevailing class in all chosen k points is the class that is assigned. Before we start attempting to classify with the KNN method we split training data and test data. This helps us avoid over-fitting, in this way when we test the data it is tested on data that has never been seen before and as such gives us more conclusive results. For our KNN classifier we split the data to 70% training data and 30% test data. Generally to find the most optimal value of k to use we use the formula k = n0.5, where k is the number of nearest points and n is the number of instances. However for our hyper parameter k we decided for a much lower value than such k that would have been deduced by square rooting the number of our instances. Due to a general higher bias the larger the value k is we decided to go with the value 5."
        )
    }

    /**
     * Tests if multiple poems are correctly parsed
     */
    @Test
    fun parseMultipleSimplePoems(): Unit = runTest() {
        val names = ArrayList<String>()
        names.add("Search Test 1")
        names.add("Boundary Test 1")
        names.add("Load Default Test")
        names.add("HELLO")
        val results = PoemXMLParser.parseMultiplePoems(names, mockContext, Dispatchers.IO)

        assert(results[0].first == "Search Test 1")
        assert(results[0].second[0] == "I want you")

        assert(results[1].first == "Boundary Test 1")
        assert(
            results[1].second[0] == "K-nearest neighbors, abbreviated to KNN for future mentions " +
                    "is a classification technique that assigns a class to the k nearest points in " +
                    "regards to a point x. The prevailing class in all chosen k points is the class" +
                    " that is assigned. Before we start attempting to classify with the KNN method we " +
                    "split training data and test data. This helps us avoid over-fitting, in this way " +
                    "when we test the data it is tested on data that has never been seen before and as " +
                    "such gives us more conclusive results. For our KNN classifier we split the data to " +
                    "70% training data and 30% test data. Generally to find the most optimal value of k " +
                    "to use we use the formula k = n0.5, where k is the number of nearest points and n " +
                    "is the number of instances. However for our hyper parameter k we decided for a much" +
                    " lower value than such k that would have been deduced by square rooting the number " +
                    "of our instances. Due to a general higher bias the larger the value k is we decided " +
                    "to go with the value 5."
        )

        assert(results[2].first == "Load Default Test")
        assert(results[2].second[0] == "HAHAHAHA")

        assert(results[3].first == "HELLO")
        assert(results[3].second[0] == "Sdfdsfsd dsf")
    }

    /**
     * Tests if multiple poems in the album test folder are correctly parsed
     */
    @Test
    fun parseMultipleSimplePoemsInAlbums(): Unit = runTest {
        val names = ArrayList<String>()
        val albumName = "Album_test_folder"
        names.add(albumName + File.separator + "Album Test 1")
        names.add(albumName + File.separator + "Album Test 2")
        names.add(albumName + File.separator + "Album Test 3")

        val results = PoemXMLParser.parseMultiplePoems(names, mockContext, Dispatchers.IO)

        assert(results.size == 3)
        assert(results[0].first == "Album Test 1")
        assert(results[0].second[0] == "I want you")

        assert(results[1].first == "Album Test 2")
        assert(
            results[1].second[0] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )

        assert(results[2].first == "Album Test 3")
        assert(
            results[2].second[0] == "Bcvbvnbsdfdsfdsfdsfdsfdsf\n" +
                    "kkkdsfdsfdsfdsfsfsd fss\n" +
                    "ksdfdsfdsfsdfdsfsdfsdfdsfsdfdsfsdfsdfsdfsdfsdfsdfdsdfsdfsdfdsf\n" +
                    "kdsfsdfdsfdsfdsfdsfdsfdsfdsfdsfsdfds\n" +
                    "kfsdfsdfsdfsdfds\n" +
                    "kfsdfsdfdsfsdfsdfsdf\n" +
                    "kdsfsdfsdf\n" +
                    "dsfsdfdsfsdfsdfdsf\n" +
                    "kdsfdsfsdfdsfdsfdsfsdfdsfsdfdsfsdfsdfsd\n" +
                    "kkfsdfsdfsdfdsfdsfsdfdsfsdfdsfds\n" +
                    "kfdssdfdsfsdfdsfdsfdsfds dsf dsfsdfdsfsdfsdfdsf do dfsdf \n" +
                    "k\n" +
                    "dsfdsfdsf. sdgg fdg fgd so sd SF DG SG \n" +
                    "kk dsfdsfdsf sdfdsfds dsfdsfsdfsd dsfdsfsf\n" +
                    "k sdfds\n" +
                    " sdfdsfds sdfds for ssdss a a a a aef rfefdgd\n" +
                    "kk fdgdfgfdg fdgfdg for cxvsvs d\n" +
                    "k sd dsf sdf dsf sdf fdsgfgd DG sf\n" +
                    "k ds dsf dsf dsfdsfdsf ds dsf dsf dsfdsfdsf dsf\n" +
                    "k sdfds fdsf for sd\n" +
                    "dsfdsfdsfsdfsfdfdds\n" +
                    "kkksdfkjdsfkdjsfkjsdfksjdfndksjfndksjfndskjfsdfs\n" +
                    "k sfdsdfdsfdsfdsfdssfdsdfdsfdsf\n" +
                    "kdfsdfdsfsdfdsfdsfdsfdsfdsfdsf\n" +
                    "ksdfdsfdsf\n" +
                    "sd. fdss\n" +
                    "kk fdsf dfsfd DF. fdfdf DF DF FD so ghgb vdf dsf \n" +
                    " dsfdsfdsf ds. bbfdb FB if FB FD d\n" +
                    "lkdsv so vdsf sdf sdf \n" +
                    "sdfdsfds DF jdjd do do do do do do do d\n" +
                    " do do do do do do do do d\n" +
                    "fgf\n" +
                    "gfg\n" +
                    "fgfgfgfgfg"
        )
    }
}