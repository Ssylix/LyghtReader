package com.tech.ssylix.lyght_reader.logic.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tech.ssylix.lyght_reader.data.models.ContentInfo
import com.tech.ssylix.lyght_reader.logic.utitlities.Storage
import com.tech.ssylix.lyght_reader.data.models.Document
import com.tech.ssylix.lyght_reader.data.models.UserData
import com.tech.ssylix.lyght_reader.logic.utitlities.Auth

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirebaseStorage: FirebaseStorage
    lateinit var mStorageReference: StorageReference

    lateinit var mContinueListListener: OnContinueItemListener
    lateinit var mParamLoadListener : OnParamsInitialized
    lateinit var mRecommendationListListener: OnRecommendedListListener

    val mRecommendedList = ArrayList<Document>()

    private val mAuthUtils = Auth()
    val mStoreUtils by lazy {
        Storage(mFirebaseAuth.uid!!)
    }

    val mUserData : UserData by lazy {
        UserData().also {
            it.contentInfo = ContentInfo()
            it.loadBioData(this, mParamLoadListener)
            it.loadViewedHistoryIntoList(this, mContinueListListener)
            it.loadRecommendationsList(this, mRecommendationListListener)
        }
    }

    fun initializeFireBase(activity: Activity) : Boolean{
        return try{
            mFirebaseAuth = FirebaseAuth.getInstance()
            mFirebaseAuth.addAuthStateListener {
                if(it.currentUser != null){
                    mFirebaseDatabase = FirebaseDatabase.getInstance()
                    mDatabaseReference = mFirebaseDatabase.reference
                    mFirebaseStorage = FirebaseStorage.getInstance()
                    mStorageReference = mFirebaseStorage.reference
                    mParamLoadListener = activity as OnParamsInitialized
                    mParamLoadListener.onFirebaseParamsInitialized()
                }else{
                    mFirebaseAuth.removeAuthStateListener {}
                    mAuthUtils.beginUserLogin(activity, VM_SIGN_IN)
                }
            }
            true
        }catch (e : Exception){
            e.printStackTrace()
            false
        }
    }

    fun getUserData (context : Context): UserData {
        mContinueListListener = context as OnContinueItemListener
        mRecommendationListListener = context as OnRecommendedListListener
        return mUserData
    }

    fun getContinueList() {

    }

    override fun onCleared() {
        super.onCleared()
    }

    interface UX {

        fun onItemLoadedProgress (increment : Int)

        fun onPopularListLoaded () {}
    }

    interface OnParamsInitialized{

        fun onBioDataLoaded(userData: UserData?)

        fun onFirebaseParamsInitialized()
    }

    interface OnContinueItemListener {

        fun onItemLoadedProgress (increment : Int)

        fun onItemAdded (document: Document)

        fun onItemRemoved (document: Document)

        fun onItemChanged (document: Document)
    }

    interface OnRecommendedListListener{

        fun onRecommendationAdded (document: Document){}

        fun onRecommendationChanged (document: Document){}

        fun onRecommendationsLoaded (document: Document){}
    }

    interface OnPopularListListener {

        fun onPopularListLoaded (documents : ArrayList<Document>)
    }

    companion object {
        const val VM_SIGN_IN : Int = 101
    }
}