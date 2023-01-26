package com.wendorochena.poetskingdom

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.wendorochena.poetskingdom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setTheme(R.style.Theme_PoetsKingdom_PurpleText)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val extras = intent.extras
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        if (extras?.containsKey("imagePath") == true) {
            supportFragmentManager.setFragmentResultListener("imagePath", this) {
                requestKey, bundle ->
                val result = bundle.getString(requestKey)
                val returnIntent = Intent()
                if (result != null) {
                    returnIntent.putExtra(requestKey, result)
                    setResult(RESULT_OK, returnIntent)
                }
                else {
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
            val bundleExtras = Bundle()
            bundleExtras.putBoolean(getString(R.string.image_for_result_key), true)
            navController.navigate(R.id.action_FirstFragment_toMyImages,bundleExtras)
        }
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}