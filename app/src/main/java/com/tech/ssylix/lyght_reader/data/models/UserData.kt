package com.tech.ssylix.lyght_reader.data.models

import com.google.firebase.database.*
import com.tech.ssylix.lyght_reader.logic.utitlities.debugLog
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel

data class UserData(
    var name: Names? = null,
    var email: String? = null,
    var phone: String? = null,
    var contentInfo: ContentInfo? = null,
    var financialInfo: Any? = null
) {

    constructor() : this(null)

    fun loadViewedHistoryIntoList(viewModel: HomeViewModel, listener: HomeViewModel.OnContinueItemListener) {
        viewModel.mDatabaseReference.child(viewModel.mStoreUtils.VIEWED_DOCS).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val doc = p0.getValue(Document::class.java)
                if (doc != null) {
                    doc.stringId = p0.key
                    listener.onItemChanged(doc)
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val doc = p0.getValue(Document::class.java)
                if (doc != null) {
                    listener.onItemAdded(doc)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val doc = p0.getValue(Document::class.java)
                if (doc != null) {
                    doc.stringId = p0.key
                    listener.onItemRemoved(doc)
                }
            }

        })
    }

    fun loadBioData(viewModel: HomeViewModel, listener: HomeViewModel.OnParamsInitialized) {
        viewModel.mDatabaseReference.child(viewModel.mStoreUtils.USERINFO).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(user_info: DataSnapshot) {
                val userInfo = user_info.getValue(UserData::class.java)
                listener.onBioDataLoaded(userInfo)
            }

        })
        //TODO("N.I") Check for changes since last sync and local availability
    }

    fun loadRecommendationsList(viewModel: HomeViewModel, listener: HomeViewModel.OnRecommendedListListener) {
        //FIXME("HACK") THis listener currently loads all the files in the database, set it to load actual preferences.
        viewModel.mDatabaseReference.child(viewModel.mStoreUtils.REG_DOCS).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val doc = p0.getValue(Document::class.java)
                if(doc != null) {
                    doc.stringId = p0.key
                    listener.onRecommendationChanged(doc)
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val doc = p0.getValue(Document::class.java)
                if(doc != null) {
                    listener.onRecommendationAdded(doc)
                }

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    /*fun loadContentInfo(
        store: Storage,
        dBReference: DatabaseReference
    ) {
        dBReference.child(store.UPLOAD_VIDEOS).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                contentInfo?.uploads?.Docs?.find {
                    it.stringId == p0.key
                }.apply {
                    val doc = p0.getValue(Media::class.java)
                    if (doc != null) {
                        this?.title = doc.title
                        this?.storageLocation = doc.storageLocation
                        this?.rating = doc.rating
                        this?.metadata = doc.metadata
                        this?.tagData = doc.tagData
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val doc = p0.getValue(Document::class.java)
                if (doc != null) {
                    doc.stringId = p0.key ?: "null"
                    contentInfo?.uploads?.Docs?.add(doc)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                contentInfo?.uploads?.Docs?.remove(
                    contentInfo?.uploads?.Docs?.find {
                        it.stringId == p0.key
                    }
                )
            }
        })
    }*/
}