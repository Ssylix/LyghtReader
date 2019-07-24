package com.tech.ssylix.lyght_reader.logic.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tech.ssylix.lyght_reader.logic.utitlities.Storage

class NewContentViewModel(application: Application) : AndroidViewModel(application){
    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var mDatabaseReference: DatabaseReference
    var mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    var mStorageReference: StorageReference

    val mStoreUtils by lazy {
        Storage(mFirebaseAuth.uid!!)
    }

    init {
        mDatabaseReference = mFirebaseDatabase.reference
        mStorageReference = mFirebaseStorage.reference
    }
}