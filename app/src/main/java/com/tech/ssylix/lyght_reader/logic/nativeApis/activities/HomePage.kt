package com.tech.ssylix.lyght_reader.logic.nativeApis.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.data.models.Document
import com.tech.ssylix.lyght_reader.data.models.UserData
import com.tech.ssylix.lyght_reader.logic.nativeApis.fragments.HomeFragment
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel.*
import kotlinx.android.synthetic.main.activity_home.*

class HomePage : AppCompatActivity(), OnParamsInitialized, HomeFragment.OnInitialize, UX, OnContinueItemListener,
    OnRecommendedListListener {
    override fun onFirebaseParamsInitialized() {
        mHomeFragment.onFirebaseParamsInitialized()
    }

    private val mViewModel : HomeViewModel by lazy {
        ViewModelProviders.of(this)[HomeViewModel::class.java]
    }

    lateinit var mHomeFragment: HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        
        mViewModel.initializeFireBase(this)

        Navigation.setViewNavController(fab, findNavController(R.id.fragment))
        fab.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_newUpload))
        findNavController(R.id.fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.newUpload -> {
                    fab.isEnabled = false
                }

                else -> {
                    fab.isEnabled = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemLoadedProgress(increment: Int) {
        mHomeFragment.onItemLoadedProgress(increment)
    }

    override fun onItemAdded(document: Document) {
        mHomeFragment.onItemAdded(document)
    }

    override fun onItemRemoved(document: Document) {
        mHomeFragment.onItemRemoved(document)
    }

    override fun onItemChanged(document: Document) {
        mHomeFragment.onItemChanged(document)
    }

    override fun referInterfaces(homeFragment: HomeFragment) {
        mHomeFragment = homeFragment
    }

    override fun onBioDataLoaded(userData: UserData?) {
        mHomeFragment.onBioDataLoaded(userData)
    }

    override fun onRecommendationAdded(document: Document) {
        mHomeFragment.onRecommendationAdded(document)
    }

    override fun onRecommendationChanged(document: Document) {
        mHomeFragment.onRecommendationChanged(document)
    }

    override fun onRecommendationsLoaded(document: Document) {
        mHomeFragment.onRecommendationsLoaded(document)
    }
}

