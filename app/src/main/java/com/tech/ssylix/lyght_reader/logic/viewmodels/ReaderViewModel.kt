package com.tech.ssylix.lyght_reader.logic.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tech.ssylix.lyght_reader.data.models.Interpretation
import com.tech.ssylix.lyght_reader.data.models.Reference
import com.tech.ssylix.lyght_reader.data.models.Summary
import com.tech.ssylix.lyght_reader.data.models.Type
import com.tech.ssylix.lyght_reader.logic.utitlities.Storage
import com.tech.ssylix.lyght_reader.logic.utitlities.debugLog
import java.io.File
import java.lang.Thread.sleep

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var mDatabaseReference: DatabaseReference
    var mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    var mStorageReference: StorageReference

    lateinit var mBookLoadListener: OnBookLoaded
    lateinit var mInterpretationLoadListener : OnInterpretationLoadListener
    lateinit var mOutlineLoadListener: OnOutlineLoadListener
    lateinit var mReferenceLoadListener: OnReferenceLoadListener


    val mStoreUtils by lazy {
        Storage(mFirebaseAuth.uid!!)
    }

    init {
        mDatabaseReference = mFirebaseDatabase.reference
        mStorageReference = mFirebaseStorage.reference
    }

    lateinit var mBookName : String
    var mBookLoaded = false
    var mCurrentPageNumber : Int = 0

    var mMinVal = Long.MAX_VALUE

    private val mBook: File by lazy {
        File.createTempFile("Temp", ".pdf").also{
            mStorageReference.child(mStoreUtils.DOCS).child(mBookName).getFile(it)
                .addOnSuccessListener {_ ->
                    mBookLoaded = true
                    mBookLoadListener.onBookSuccessfullyLoaded(it)
                }.addOnFailureListener {_ ->
                    mBookLoadListener.onBookFailedLoad()
                }.addOnProgressListener { task ->
                    if(mMinVal > task.bytesTransferred && task.bytesTransferred != 0.toLong()){
                        mMinVal = task.bytesTransferred
                    }

                    if(task.totalByteCount <= mMinVal){
                        val tT = (task.totalByteCount - mMinVal)/100
                        Thread{
                            sleep(500)
                            val pG = (task.totalByteCount - task.bytesTransferred)/tT
                            mBookLoadListener.onBookLoadProgress(100 - pG.toInt())
                        }
                    }else {
                        val tT = (task.totalByteCount - mMinVal) / 100
                        val pG = (task.totalByteCount - task.bytesTransferred) / tT
                        mBookLoadListener.onBookLoadProgress(100 - pG.toInt())
                    }
                }
        }
    }

    fun initializeListeners(context: Context){
        mBookLoadListener = context as OnBookLoaded
        mInterpretationLoadListener = context as OnInterpretationLoadListener
        mOutlineLoadListener = context as OnOutlineLoadListener
        mReferenceLoadListener = context as OnReferenceLoadListener
    }

    fun getBook(bookName : String): File {
        mBookName = bookName
        return mBook
    }

    lateinit var mBookId : String

    private val mInterpretationList by lazy {
        ArrayList<Pair<Int, Interpretation>>().also{
            listOf(0,1,2,3,4).forEach {pageId ->
                loadPageInterpretation(pageId, false)
            }
        }
    }

    fun getInterpretationList(bookId : String): ArrayList<Pair<Int, Interpretation>> {
        mBookId = bookId
        return mInterpretationList
    }

    fun loadPageInterpretation(pageId: Int, returnArray : Boolean = true): ArrayList<Pair<Int, Interpretation>>? {
        mDatabaseReference.child(mStoreUtils.INTERPRETN.invoke(mBookId, pageId)).addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val interpret = p0.getValue(Interpretation::class.java)
                interpret?.mPageId = pageId
                interpret?.mDocId = mBookId
                if(interpret != null) {
                    val curiousPair = Pair(pageId, interpret)
                    if(!mInterpretationList.contains(curiousPair)){
                        mInterpretationList.add(curiousPair)
                        mInterpretationLoadListener.onPageInterpretationsChanged()
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
        return if(returnArray) mInterpretationList else null
    }

    fun getPageInterpretations(pageId: Int): List<Pair<Int, Interpretation>> {
        return mInterpretationList.filter {
            it.first == pageId
        }
    }

    private val mOutlineList by lazy {
        ArrayList<Pair<Int, Summary>>().also {
            listOf(0,1,2,3,4).forEach {pageId ->
                loadPageOutline(pageId, false)
            }
        }
    }

    fun getOutlineList(bookId: String): ArrayList<Pair<Int, Summary>> {
        mBookId = bookId
        return mOutlineList
    }

    fun loadPageOutline(pageId: Int, returnArray : Boolean = true) : ArrayList<Pair<Int, Summary>>? {
        mDatabaseReference
            .child(mStoreUtils.SUMMARY_OUTLINE.invoke(mBookId, pageId))
            .addChildEventListener(object : ChildEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val summary = p0.getValue(Summary::class.java)
                    if(summary != null){
                        val curiousPair = Pair(pageId, summary)
                        if(!mOutlineList.contains(curiousPair)){
                            mOutlineList.add(curiousPair)
                            mOutlineLoadListener.onPageOutlinesChanged()
                        }
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }

            })
        return if(returnArray) mOutlineList else null
    }

    fun getPageOutline(pageId: Int): List<Pair<Int, Summary>> {
        return mOutlineList.filter {
            it.first == pageId
        }
    }

    private val mReferenceList by lazy {
        ArrayList<Pair<Int, Reference>>().also {
            listOf(0,1,2,3,4).forEach {pageId ->
                loadPageReferences(pageId, false)
            }
        }
    }

    fun getReferenceList(bookId: String): ArrayList<Pair<Int, Reference>> {
        mBookId = bookId
        return mReferenceList
    }

    fun loadPageReferences(pageId: Int, returnArray : Boolean = true) : ArrayList<Pair<Int, Reference>>?{
        mDatabaseReference.child(mStoreUtils.REFERENCE.invoke(mBookId, pageId)).addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val reference = p0.getValue(Reference::class.java)
                reference?.mPageId = pageId
                reference?.mDocId = mBookId
                if(reference != null) {
                    val curiousPair = Pair(pageId, reference)
                    if(!mReferenceList.contains(curiousPair)){
                        mReferenceList.add(curiousPair)
                        mReferenceLoadListener.onPageReferencesChanged()
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
        return if(returnArray) mReferenceList else null
    }

    fun getPageReferences(pageId: Int): List<Pair<Int, Reference>> {

        return ArrayList<Pair<Int, Reference>>().also { dummy_array ->
            arrayOf(Type.MP4, Type.URL, Type.MP3, Type.JPG, Type.OTHER).forEach {
                dummy_array.add(Pair(pageId, Reference().apply { this.type = it }))
            }
        }
        mReferenceList.filter {
            it.first == pageId
        }
    }

    interface OnBookLoaded{

        fun onBookSuccessfullyLoaded(book: File, startPage : Int = 0)

        fun onBookFailedLoad()

        fun onBookLoadProgress(percentageLoad : Int)
    }

    interface OnInterpretationLoadListener{

        fun onCurrentPageChangeInterpretation(){}

        fun onSaveNewInterpretation(interpretation: Interpretation)

        fun onPageInterpretationsChanged()
    }

    interface OnOutlineLoadListener{

        fun onCurrentPageChangeOutline(){}

        fun onSaveNewOutline(summary: Summary)

        fun onPageOutlinesChanged()
    }

    interface OnReferenceLoadListener{

        fun onCurrentPageChangeReference(pageId : Int){}

        fun onSaveNewReference(reference: Reference)

        fun onPageReferencesChanged()
    }
}