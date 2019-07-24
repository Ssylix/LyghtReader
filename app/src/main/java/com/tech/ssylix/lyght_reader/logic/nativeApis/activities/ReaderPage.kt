package com.tech.ssylix.lyght_reader.logic.nativeApis.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.logic.utitlities.Storage
import com.tech.ssylix.lyght_reader.data.models.*
import com.tech.ssylix.lyght_reader.logic.utitlities.debugLog
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.NewUpload.Companion.REQ_DOC
import com.tech.ssylix.lyght_reader.logic.nativeApis.fragments.*
import com.tech.ssylix.lyght_reader.logic.utitlities.copyInputStreamToFile
import com.tech.ssylix.lyght_reader.logic.utitlities.toast
import com.tech.ssylix.lyght_reader.logic.viewmodels.ReaderViewModel
import com.tech.ssylix.lyght_reader.logic.viewmodels.ReaderViewModel.*
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.android.synthetic.main.app_bar_reader_page.*
import kotlinx.android.synthetic.main.content_reader_page.*
import kotlinx.android.synthetic.main.model_empty_list_text.view.*
import kotlinx.android.synthetic.main.model_interpret_recycler.view.*
import kotlinx.android.synthetic.main.model_summary_recycler.view.*
import kotlinx.android.synthetic.main.page_drawer_interpret.*
import kotlinx.android.synthetic.main.page_drawer_summary_outline.*
import kotlinx.android.synthetic.main.page_show_reference.view.*
import java.io.File



class ReaderPage : AppCompatActivity(),
    ReferencesFragment.OnFragmentInitializedListener,
    OnBookLoaded, OnInterpretationLoadListener,
    OnOutlineLoadListener, OnReferenceLoadListener {

    private var mNewMarkerPosition: Int? = null
    private lateinit var mStore: Storage
    private lateinit var mShare : SharedPreferences
    private lateinit var mBook: File
    private var mRootViewSize: Int = 0
    private lateinit var interpretationList: ArrayList<Pair<Int, Interpretation>>
    private lateinit var summaryList: ArrayList<Pair<Int, Summary>>
    private lateinit var referenceList: ArrayList<Pair<Int, Reference>>
    private lateinit var mDocKey: String
    private lateinit var mDocTitle: String
    private lateinit var mDocDownLoadURL: String
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var mViewModel: ReaderViewModel
    private lateinit var mReferencesFragment: ReferencesFragment

    private val initializePeriferalContent = {
        interpretationList = mViewModel.getInterpretationList(mDocKey)
        summaryList = mViewModel.getOutlineList(mDocKey)
        referenceList = mViewModel.getReferenceList(mDocKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)
        val bottomBar: BottomAppBar = findViewById(R.id.bottomBar)
        setSupportActionBar(bottomBar)

        mShare = getSharedPreferences(READER_PREF_KEY, Context.MODE_PRIVATE)

        mBottomSheetBehavior = BottomSheetBehavior.from(bottom_nav_sheet)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                when (newState){
                    BottomSheetBehavior.STATE_HIDDEN -> "State Hidden".debugLog()

                    BottomSheetBehavior.STATE_EXPANDED -> "State Expanded".debugLog()

                    BottomSheetBehavior.STATE_COLLAPSED -> "State Collapsed".debugLog()

                    BottomSheetBehavior.STATE_DRAGGING -> "State Dragging".debugLog()

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> "State Half-Expanded".debugLog()

                    BottomSheetBehavior.STATE_SETTLING -> "State Settling".debugLog()

                    else -> "State Other".debugLog()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                "Slide".debugLog()
            }
        })

        getIntentDetails()

        mViewModel = ViewModelProviders.of(this)[ReaderViewModel::class.java]
        mViewModel.initializeListeners(this)
        mBook = mViewModel.getBook(mDocTitle)
        initializePeriferalContent.invoke()

        fab.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                fab.setImageResource(R.drawable.ic_add_new)
            } else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(intent, "Get Documents"), REQ_DOC)
            }
        }

        root.viewTreeObserver.apply {
            if (this.isAlive) {
                this.addOnGlobalLayoutListener {
                    mRootViewSize = root.height
                }
            }
        }

        val toggle = object : ActionBarDrawerToggle(
            this, drawer_layout, bottomBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            val searcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val newArray: ArrayList<Pair<Int, Interpretation>> = ArrayList()

                    searchForSimilar(newArray, s.toString())

                    if (newArray.isNotEmpty()) {
                        interpret_recycler.adapter = InterpretationRecycler(newArray)
                    } else {
                        setupUiToAddNewInterpretation(s.toString())
                    }
                }

            }
            override fun onDrawerOpened(drawerView: View) {
                when {
                    drawer_layout.isDrawerOpen(GravityCompat.START) -> {
                        summary_recycler.layoutManager = LinearLayoutManager(this@ReaderPage)
                        summary_recycler.setHasFixedSize(true)
                        summary_recycler.adapter = SummaryRecycler()

                        add_new_summary.setOnClickListener {
                            onStartSummaryUISetup()
                            save_summary.setOnClickListener {
                                if (summary_text.text.isNotEmpty()) {
                                    val summaryText = summary_text.text.toString()
                                    val pos = (summary_recycler.adapter as? SummaryRecycler)?.newArray?.size ?: 0
                                    val summary = Summary(summaryText, null, pos)
                                    try {
                                        onSaveNewOutline(summary)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    toast("Type a note first")
                                }
                            }

                            close_summary.setOnClickListener {
                                onFinishSummaryUISetup()
                            }
                        }
                    }

                    drawer_layout.isDrawerOpen(GravityCompat.END) -> {
                        interpret_recycler.layoutManager = LinearLayoutManager(this@ReaderPage)
                        interpret_recycler.setHasFixedSize(true)
                        interpret_recycler.adapter = InterpretationRecycler()

                        search_icon.setOnClickListener {
                            val newArray: ArrayList<Pair<Int, Interpretation>> = ArrayList()
                            val searchText = search_text.text.toString()

                            searchForSimilarOnline(newArray, searchText)

                            if (newArray.isNotEmpty()) {
                                interpret_recycler.adapter = InterpretationRecycler(newArray)
                            } else {
                                setupUiToAddNewInterpretation(searchText)
                            }
                        }

                        search_text.addTextChangedListener(searcher)
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                when{
                    !drawer_layout.isDrawerOpen(GravityCompat.END) -> {
                        search_text.removeTextChangedListener(searcher)
                    }
                }
            }
        }

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        bottomBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_summary_outline -> {
                    drawer_layout.openDrawer(nav_outline)
                    true
                }

                R.id.action_reference -> {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    fab.setImageResource(R.drawable.ic_down_arrow)
                    true
                }

                R.id.action_interpretation -> {
                    drawer_layout.openDrawer(nav_interpreter)
                    true
                }

                R.id.action_night -> {
                    bottomBar.menu.findItem(R.id.action_night).isChecked = mShare.getBoolean(NIGHT_MODE_KEY, true)
                    it.isChecked = !it.isChecked
                    val pageNum = pdfViewer.currentPage
                    mShare.edit().putBoolean(NIGHT_MODE_KEY, it.isChecked).commit()
                    pdfViewer.defaultSetup(mBook, pageNum, mShare.getBoolean(NIGHT_MODE_KEY, true)) { nightMode ->
                        bottomBar.menu.findItem(R.id.action_night).isChecked = nightMode
                    }
                    true
                }

                else -> true
            }
        }

        /*reference_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reference_recycler.setHasFixedSize(true)
        reference_recycler.adapter = ExtensionsAndUtils().DefaultRecyclerAdapter(this, R.layout.model_dummy_reference)
        GravitySnapHelper(Gravity.START, true, GravitySnapHelper.SnapListener {
        }).attachToRecyclerView(reference_recycler)*/
    }

    override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        bottomBar.menu.findItem(R.id.action_night).isChecked = mShare.getBoolean(NIGHT_MODE_KEY, true)
        return super.onMenuOpened(featureId, menu)
    }

    override fun onResume() {
        super.onResume()
        if(mViewModel.mBookLoaded){
            onBookSuccessfullyLoaded(mViewModel.getBook(mDocTitle), mViewModel.mCurrentPageNumber)
        }
    }

    private fun setupUiToAddNewInterpretation(searchText: String) {
        var position: Float = -1f
        add_new_interpret.visibility = View.VISIBLE
        add_new_interpret.isEnabled = true
        add_new_interpret.setOnClickListener {
            onStartInterpretationUISetup()
            if (mNewMarkerPosition == null) {
                position_marker.isChecked = false
            }
            position_marker.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    buttonView.text = getString(R.string.marker_set)
                } else {
                    buttonView.text = getString(R.string.marker_unset)
                }
            }
            position_marker.setOnClickListener {
                position_frame.visibility = View.VISIBLE
                position_frame.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            position = event.y / mRootViewSize
                            toast("Close or Save to end the process")
                            drawer_layout.openDrawer(GravityCompat.END)
                            position_frame.visibility = View.GONE
                            true
                        }

                        else -> false
                    }
                }
                drawer_layout.closeDrawer(GravityCompat.END)
            }
            save_interpretation.setOnClickListener {
                if (position_marker.isChecked && interpretation_text.text.isNotEmpty()) {
                    val interpretation =
                        Interpretation(
                            searchText,
                            interpretation_text.text.toString(),
                            null,
                            position,
                            0
                        )

                    onSaveNewInterpretation(interpretation)
                } else {
                    toast("Marker unset")
                }
            }

            close_interpretation.setOnClickListener {
                onFinishInterpretationUISetup()
            }
        }
    }

    private fun onStartInterpretationUISetup() {
        interpretation_text.visibility = View.VISIBLE
        position_marker.visibility = View.VISIBLE
        close_interpretation.visibility = View.VISIBLE
        save_interpretation.visibility = View.VISIBLE
        marker_tag_icon.visibility = View.VISIBLE
        add_new_interpret.isEnabled = false
    }

    private fun onFinishInterpretationUISetup() {
        interpretation_text.visibility = View.GONE
        position_marker.visibility = View.GONE
        close_interpretation.visibility = View.GONE
        save_interpretation.visibility = View.GONE
        marker_tag_icon.visibility = View.GONE
        add_new_interpret.isEnabled = true
        add_new_interpret.visibility = View.GONE
        position_marker.text = getString(R.string.marker_unset)
        search_text.setText("")
        interpretation_text.setText("")
        position_frame.visibility = View.GONE
    }

    private fun onStartSummaryUISetup() {
        summary_text.visibility = View.VISIBLE
        close_summary.visibility = View.VISIBLE
        save_summary.visibility = View.VISIBLE
    }

    private fun onFinishSummaryUISetup() {
        toast("Saved")
        summary_text.visibility = View.GONE
        close_summary.visibility = View.GONE
        save_summary.visibility = View.GONE
    }

    private fun searchForSimilar(newArray: ArrayList<Pair<Int, Interpretation>>, searchText: String) {
        interpretationList.forEach {
            if (it.second.capText.contains(searchText) || searchText.contains(it.second.capText) ||
                it.second.subText.contains(searchText) || searchText.contains(it.second.subText)
            ) {
                newArray.add(it)
            }
        }
    }

    private fun searchForSimilarOnline(
        newArray: ArrayList<Pair<Int, Interpretation>>,
        searchText: String
    ) {

    }

    private fun getIntentDetails() {
        mDocKey = intent.getStringExtra(HomeFragment.DOC_KEY)
        mDocTitle = intent.getStringExtra(HomeFragment.DOC_TITLE)
        mDocDownLoadURL = intent.getStringExtra(HomeFragment.DOC_URL)
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            drawer_layout.isDrawerOpen(GravityCompat.END) -> drawer_layout.closeDrawer(GravityCompat.END)
            mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED ->
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reader_page, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_DOC -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri: Uri = data?.data!!
                    mBook = File.createTempFile("Temp", ".pdf")
                    mBook.copyInputStreamToFile(baseContext.contentResolver?.openInputStream(uri)!!)
                    pdfViewer.defaultSetup(mBook, isDark = mShare.getBoolean(NIGHT_MODE_KEY, true)) { nightMode ->
                        bottomBar.menu.findItem(R.id.action_night).isChecked = nightMode
                    }
                }
            }
        }
    }

    inner class InterpretationRecycler(private var newArray: List<Pair<Int, Interpretation>>? =
                                           mViewModel.getPageInterpretations(pdfViewer.currentPage)) :
        RecyclerView.Adapter<InterpretationRecycler.MyViewHolder>() {
        private val mDefaultEmpty = 1001

        init {
            if (newArray == null) {
                newArray = ArrayList()
                try {
                    val page = pdfViewer.currentPage
                    interpretationList.forEach {
                        if (it.first == page) {
                            (newArray as ArrayList<Pair<Int, Interpretation>>).add(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                mDefaultEmpty -> MyViewHolder(
                    LayoutInflater.from(this@ReaderPage).inflate(R.layout.model_empty_list_text, parent, false)
                )

                else -> MyViewHolder(
                    LayoutInflater.from(this@ReaderPage).inflate(R.layout.model_interpret_recycler, parent, false)
                )
            }
        }

        override fun getItemCount(): Int {
            return newArray?.size ?: 1
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if ((newArray?.size ?: 0) >= 1) {
                holder.itemView.cap_text.text = newArray?.get(position)?.second?.capText
                holder.itemView.interpret_text.text = newArray?.get(position)?.second?.subText
            } else {
                holder.itemView.empty_list_text.text = getString(R.string.empty_list_text)
            }

            if(newArray?.get(position)?.second?.subInterpretations?.isEmpty() != false){
                holder.itemView.more_interpretations.visibility = View.GONE
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if ((newArray?.size ?: 0) < 1) {
                mDefaultEmpty
            } else {
                -1
            }
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    inner class SummaryRecycler(var newArray: List<Pair<Int, Summary>>? = mViewModel.getPageOutline(pdfViewer.currentPage)) :
        RecyclerView.Adapter<SummaryRecycler.MyViewHolder>() {

        private val mDefaultEmpty = 1001

        init {
            if (newArray == null) {
                newArray = ArrayList()
                try {
                    val page = pdfViewer.currentPage
                    summaryList.forEach {
                        if (it.first == page) {
                            (newArray as ArrayList<Pair<Int, Summary>>).add(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                mDefaultEmpty -> MyViewHolder(
                    LayoutInflater.from(this@ReaderPage).inflate(R.layout.model_empty_list_text, parent, false)
                )
                else -> MyViewHolder(
                    LayoutInflater.from(this@ReaderPage).inflate(R.layout.model_summary_recycler, parent, false)
                )
            }
        }

        override fun getItemCount(): Int {
            return newArray?.size ?: 1
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if ((newArray?.size ?: 0) >= 1) {
                holder.itemView.summaryText.text = newArray?.get(position)?.second?.sumText
            } else {
                holder.itemView.empty_list_text.text = getString(R.string.empty_list_text)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if ((newArray?.size ?: 0) < 1) {
                mDefaultEmpty
            } else {
                -1
            }
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    private fun PDFView.defaultSetup(
        file: File,
        openPageNumber: Int = 0,
        isDark: Boolean = true,
        action: ((Boolean) -> Unit)? = null
    ) {
        this.fromFile(file).enableSwipe(true).swipeHorizontal(false).enableDoubletap(true)
            .defaultPage(openPageNumber).enableAnnotationRendering(true).password(null)
            .scrollHandle(null).enableAntialiasing(true).spacing(8).autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH).pageSnap(false).pageFling(false).nightMode(isDark)
            .onPageChange { page, pageCount ->
                if ((page + 5) < pageCount) {
                    mViewModel.loadPageInterpretation(page + 5)
                    mViewModel.loadPageOutline(page + 5)
                    mViewModel.loadPageReferences(page + 5)
                }
                mViewModel.mCurrentPageNumber = page
                mReferencesFragment.onCurrentPageChangeReference(page)
            }.load()
        action?.invoke(isDark)
    }

    override fun onPageInterpretationsChanged() {
        if(drawer_layout.isDrawerOpen(GravityCompat.END)) {
            interpret_recycler.adapter = InterpretationRecycler()
        }
    }

    override fun onPageOutlinesChanged() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            summary_recycler.adapter = SummaryRecycler()
        }
    }

    override fun onPageReferencesChanged() {
        mReferencesFragment.onPageReferencesChanged()
    }

    override fun onSaveNewInterpretation(interpretation: Interpretation) {
        mViewModel.mDatabaseReference
            .child(mStore.INTERPRETN.invoke(intent.getStringExtra(HomeFragment.DOC_KEY), pdfViewer.currentPage))
            .push().setValue(interpretation).addOnSuccessListener {
                interpret_recycler.adapter = InterpretationRecycler()
                toast("Saved")
                onFinishInterpretationUISetup()
            }.addOnFailureListener {
                if (!position_marker.isChecked) toast("Failed to add interpretation")
                if (interpretation_text.text.isEmpty()) toast("Type an interpretation please")
            }
    }

    override fun onSaveNewReference(reference: Reference) {
        mViewModel.mDatabaseReference.child(
            mStore.REFERENCE.invoke(
                intent.getStringExtra(HomeFragment.DOC_KEY),
                pdfViewer.currentPage
            )
        ).push().setValue(reference)
            .addOnSuccessListener {
                toast("Saved")
            }
    }

    override fun onSaveNewOutline(summary: Summary) {
        mViewModel.mDatabaseReference.child(
            mStore.SUMMARY_OUTLINE.invoke(
                intent.getStringExtra(HomeFragment.DOC_KEY),
                pdfViewer.currentPage
            )
        ).push().setValue(summary)
            .addOnSuccessListener {
                onFinishSummaryUISetup()
            }
    }

    override fun onBookSuccessfullyLoaded(book : File, startPage : Int) {
        file_download_progress.visibility = View.GONE
        pdfViewer.defaultSetup(book, startPage, mShare.getBoolean(NIGHT_MODE_KEY, true))
    }

    override fun onBookFailedLoad() {
        file_download_progress.visibility = View.GONE
        toast("Failed to load document, 🤔please check your internet connection")
        toast("Meanwhile, we will keep trying")
    }

    override fun onBookLoadProgress(percentageLoad : Int) {
        file_download_progress.isIndeterminate = false
        file_download_progress.max = 100
        file_download_progress.progress = percentageLoad
    }

    override fun onReferenceFragmentInit(fragment: ReferencesFragment) {
        mReferencesFragment = fragment
    }

    companion object {
        private val NIGHT_MODE_KEY = "nightMode"

        private val READER_PREF_KEY = "Reader Preferences"
    }
}
