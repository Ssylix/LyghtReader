package com.tech.ssylix.lyght_reader.logic.nativeApis.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.data.models.Reference
import com.tech.ssylix.lyght_reader.data.models.Type
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.NewUpload.Companion.REQ_IMG
import com.tech.ssylix.lyght_reader.logic.utitlities.*
import com.tech.ssylix.lyght_reader.logic.utitlities.helpers.ReferenceOnlineHelper
import com.tech.ssylix.lyght_reader.logic.utitlities.helpers.ReferenceUiHelper
import com.tech.ssylix.lyght_reader.logic.viewmodels.ReaderViewModel
import kotlinx.android.synthetic.main.fragment_references.view.*
import kotlinx.android.synthetic.main.model_image_reference.view.*
import kotlinx.android.synthetic.main.model_video_reference.*
import kotlinx.android.synthetic.main.model_video_reference.view.*
import kotlinx.android.synthetic.main.model_web_reference.view.*
import kotlinx.android.synthetic.main.page_new_audio_reference.view.*
import kotlinx.android.synthetic.main.page_new_image_reference.view.*
import kotlinx.android.synthetic.main.page_new_web_reference.view.*
import kotlinx.android.synthetic.main.page_new_video_reference.view.*
import kotlinx.android.synthetic.main.page_show_reference.view.*
import java.lang.Thread.sleep

class ReferencesFragment : Fragment(), ReaderViewModel.OnReferenceLoadListener {

    private var listener: OnFragmentInitializedListener? = null
    private var mReferenceLoadListener: ReaderViewModel.OnReferenceLoadListener? = null
    private lateinit var mViewModel: ReaderViewModel

    lateinit var mShowReferencePage: View
    lateinit var mNewWebReferencePage: View
    lateinit var mNewVideoReferencePage: View
    lateinit var mNewAudioReferencePage: View
    lateinit var mNewImageReferencePage: View

    val mReferenceOnlineHelper : ReferenceOnlineHelper = ReferenceOnlineHelper()
    val mReferenceUiHelper : ReferenceUiHelper = ReferenceUiHelper(this)

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_references, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        mReferenceUiHelper.getPages()

        mReferenceUiHelper.addPagesToLayout(rootView)

        mReferenceUiHelper.showView(mShowReferencePage)

        mReferenceUiHelper.setupShowReferencePage()

        mReferenceUiHelper.setDescriptionVisibilityChangeListener()

        rootView.add_new_reference.setOnClickListener {
            it.animateClicks(50) {
                mReferenceUiHelper.toggleAddNewReferenceUI(rootView)
            }
        }

        rootView.new_web_reference.setOnClickListener {
            it.animateClicks(50) {
                mReferenceUiHelper.showView(mNewWebReferencePage)
                mNewWebReferencePage.save_reference_webpage.setOnClickListener { save_button ->
                    save_button.animateClicks(50) {
                        mReferenceOnlineHelper.WebReferenceOnlineHelper().saveNewReference(
                            mNewWebReferencePage,
                            mReferenceUiHelper.WebReferenceUiHelper()
                        ){ reference ->
                            onSaveNewReference(reference)
                        }
                    }
                }
            }
        }

        rootView.new_image_reference.setOnClickListener {
            it.animateClicks(50) {
                mReferenceUiHelper.showView(mNewImageReferencePage)
                mNewImageReferencePage.select_reference_image.setOnClickListener { choose_image_button ->
                    choose_image_button.animateClicks {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        startActivityForResult(Intent.createChooser(intent, "Get Images"), REQ_IMG)
                    }
                }
            }
        }

        rootView.new_video_reference.setOnClickListener {
            it.animateClicks(50) {
                "Check Point".debugLog()
                mReferenceUiHelper.showView(mNewVideoReferencePage)
                mReferenceUiHelper.VideoReferenceUiHelper().setupVideoReferencePage(mNewVideoReferencePage, activity!!)
                mNewVideoReferencePage.save_reference_video.setOnClickListener { save_button ->
                    save_button.animateClicks {
                        mReferenceOnlineHelper.VideoReferenceOnlineHelper().saveNewReference(
                            mNewVideoReferencePage,
                            mReferenceUiHelper.VideoReferenceUiHelper())
                        {reference ->
                            onSaveNewReference(reference)
                        }
                    }
                }
            }
        }

        rootView.new_audio_reference.setOnClickListener {
            it.animateClicks(50) {
                mReferenceUiHelper.showView(mNewAudioReferencePage)
                mNewAudioReferencePage.select_reference_audio.setOnClickListener { save_button ->
                    save_button.animateClicks {
                        context?.toast("You are not on a paid plan")
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(activity!!)[ReaderViewModel::class.java]
        if (context is OnFragmentInitializedListener && context is ReaderViewModel.OnReferenceLoadListener) {
            listener = context
            mReferenceLoadListener = context
            listener?.onReferenceFragmentInit(this)
        } else {
            throw RuntimeException("$context must implement OnFragmentInitializedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_IMG -> {
                var mMinVal = 0.toLong()
                if (resultCode == RESULT_OK) {
                    val uri = data?.data
                    if (uri != null) {
                        mNewImageReferencePage.image_reference_thumbnail.setImageURI(uri)
                        mNewImageReferencePage.save_reference_image.isEnabled = true
                        mNewImageReferencePage.save_reference_image.setOnClickListener {
                            mNewImageReferencePage.save_reference_image.animateClicks(50) {
                                mViewModel.mStorageReference.child(mViewModel.mStoreUtils.PHOTOS)
                                    .child(generateRandomKey(24))
                                    .apply {
                                        val epiPhantomReference = Reference()
                                        this.putFile(uri).addOnSuccessListener {
                                            mNewImageReferencePage.image_reference_upload_progress.visibility =
                                                View.INVISIBLE
                                            epiPhantomReference.type = Type.getMimeType(context!!, uri)
                                            this.downloadUrl.addOnSuccessListener {
                                                epiPhantomReference.url = it.toString()
                                                onSaveNewReference(epiPhantomReference)
                                            }.addOnFailureListener {
                                                context?.toast("Failed, Try again")
                                            }
                                        }.addOnProgressListener { task ->
                                            if (mNewImageReferencePage.image_reference_upload_progress.visibility == View.INVISIBLE) {
                                                mNewImageReferencePage.image_reference_upload_progress.visibility =
                                                    View.VISIBLE
                                            }
                                            if (mMinVal > task.bytesTransferred.debugLog() && task.bytesTransferred != 0.toLong()) {
                                                mMinVal = task.bytesTransferred
                                            }

                                            if (task.totalByteCount <= mMinVal) {
                                                val tT = (task.totalByteCount - mMinVal) / 100
                                                Thread {
                                                    Thread.sleep(500)
                                                    val pG = (task.totalByteCount - task.bytesTransferred) / tT
                                                    mNewImageReferencePage.image_reference_upload_progress.isIndeterminate =
                                                        false
                                                    mNewImageReferencePage.image_reference_upload_progress.max = 100
                                                    mNewImageReferencePage.image_reference_upload_progress.progress =
                                                        100 - pG.toInt()
                                                }
                                            } else {
                                                val tT = (task.totalByteCount - mMinVal) / 100
                                                val pG = (task.totalByteCount - task.bytesTransferred) / tT
                                                mNewImageReferencePage.image_reference_upload_progress.isIndeterminate =
                                                    false
                                                mNewImageReferencePage.image_reference_upload_progress.max = 100
                                                mNewImageReferencePage.image_reference_upload_progress.progress =
                                                    100 - pG.toInt()
                                            }
                                        }.addOnFailureListener {
                                            mNewImageReferencePage.image_reference_upload_progress.visibility =
                                                View.INVISIBLE
                                            context?.toast("Failed, Try again")
                                        }
                                    }
                            }
                        }
                    }
                }
            }

        }
    }

    inner class ReferenceRecycler(var referenceList: List<Pair<Int, Reference>> = mViewModel.getPageReferences(1)) :
        RecyclerView.Adapter<ReferenceRecycler.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                URL -> MyViewHolder(LayoutInflater.from(context!!).inflate(R.layout.model_web_reference, parent, false))

                IMAGE -> MyViewHolder(
                    LayoutInflater.from(context!!).inflate(
                        R.layout.model_image_reference,
                        parent,
                        false
                    )
                )

                VIDEO -> MyViewHolder(
                    LayoutInflater.from(context!!).inflate(
                        R.layout.model_video_reference,
                        parent,
                        false
                    )
                )

                AUDIO -> MyViewHolder(
                    LayoutInflater.from(context!!).inflate(
                        R.layout.model_audio_reference,
                        parent,
                        false
                    )
                )

                else -> MyViewHolder(
                    LayoutInflater.from(context!!).inflate(
                        R.layout.model_error_reference,
                        parent,
                        false
                    )
                )
            }
        }

        override fun getItemCount(): Int {
            return referenceList.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            when(referenceList[position].second.type) {
                in arrayOf(Type.URL) -> {
                    Thread{
                        sleep(500)
                        activity!!.runOnUiThread {
                            holder.itemView.reference_webview.settings.javaScriptEnabled = true
                            val webViewClient = object: WebViewClient(){

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    holder.itemView.page_load_progress.visibility = View.VISIBLE
                                    super.onPageStarted(view, url, favicon)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    holder.itemView.page_load_progress.visibility = View.INVISIBLE
                                    super.onPageFinished(view, url)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    context!!.toast("Failed to load web page at ${referenceList[position].second.url}")
                                    super.onReceivedError(view, request, error)
                                }
                            }
                            holder.itemView.reference_webview.webViewClient = webViewClient
                            holder.itemView.reference_webview.loadUrl(referenceList[position].second.url)
                        }
                    }.start()

                    holder.itemView.reference_link_text.text = referenceList[position].second.url
                    mShowReferencePage.reference_title_text.text = referenceList[position].second.title ?: ""
                    mShowReferencePage.reference_notes_text.text = referenceList[position].second.notes ?: ""
                }

                in arrayOf(Type.BMP, Type.JPEG, Type.GIF, Type.JPG, Type.PNG, Type.WBMP, Type.WEBP) -> {
                    mReferenceUiHelper.ImageReferenceUiHelper().showReferenceRecyclerBindAction(
                        this@ReferencesFragment, this@ReferenceRecycler, position, holder
                    )
                }

                in arrayOf(Type.AAC, Type.MP3, Type.MWA) -> {
                    mShowReferencePage.reference_title_text.text = referenceList[position].second.title ?: ""
                    mShowReferencePage.reference_notes_text.text = referenceList[position].second.notes ?: ""
                }

                in arrayOf(Type.MKV, Type.MP4, Type.YOUTUBE) -> {

                    holder.itemView.videoView.setOnClickListener {
                        holder.itemView.videoView.setVideoURI(Uri.parse(referenceList[position].second.url), HashMap<String, String>())

                        MediaController(activity).apply {
                            setAnchorView(holder.itemView.videoView)
                            holder.itemView.videoView.setMediaController(this)
                        }
                        if(holder.itemView.videoView.isPlaying){
                            holder.itemView.videoView.pause()
                            holder.itemView.play_image.visibility = View.VISIBLE
                        }else{
                            holder.itemView.videoView.start()
                            holder.itemView.play_image.visibility = View.INVISIBLE
                        }
                    }

                    mShowReferencePage.reference_title_text.text = referenceList[position].second.title ?: ""
                    mShowReferencePage.reference_notes_text.text = referenceList[position].second.notes ?: ""
                }

                else ->{
                    "Unsupported".debugLog()
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (referenceList[position].second.type) {
                in arrayOf(Type.URL) -> {
                    URL
                }

                in arrayOf(Type.BMP, Type.JPEG, Type.GIF, Type.JPG, Type.PNG, Type.WBMP, Type.WEBP) -> {
                    IMAGE
                }

                in arrayOf(Type.AAC, Type.MP3, Type.MWA) -> {
                    AUDIO
                }

                in arrayOf(Type.MKV, Type.MP4, Type.YOUTUBE) -> {
                    VIDEO
                }

                else -> -1
            }
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    interface OnFragmentInitializedListener {
        fun onReferenceFragmentInit(fragment: ReferencesFragment)
    }

    /**
     * Called when the pdfView is swiped to a new page
     * */
    override fun onCurrentPageChangeReference(pageId: Int) {
        mShowReferencePage.reference_recycler.adapter = ReferenceRecycler(mViewModel.getPageReferences(pageId))
        GravitySnapHelper(Gravity.START, true, GravitySnapHelper.SnapListener {
            val currentReference = (mShowReferencePage.reference_recycler.adapter as ReferenceRecycler).referenceList[it].second
            mShowReferencePage.reference_title_text.text = currentReference.title ?: ""
            mShowReferencePage.reference_notes_text.text = currentReference.notes ?: ""
        }).attachToRecyclerView(mShowReferencePage.reference_recycler)
    }

    override fun onSaveNewReference(reference: Reference) {
        mReferenceLoadListener?.onSaveNewReference(reference)
    }

    /**
     * Called when the reference list of the current page is changed
     * */
    override fun onPageReferencesChanged() {
        mReferenceUiHelper.showView(mShowReferencePage)
        mShowReferencePage.reference_recycler.adapter = ReferenceRecycler(mViewModel.getPageReferences(mViewModel.mCurrentPageNumber))
    }

    companion object {
        const val URL = 1001
        const val VIDEO = 1101
        const val AUDIO = 1011
        const val IMAGE = 1111
    }
}
