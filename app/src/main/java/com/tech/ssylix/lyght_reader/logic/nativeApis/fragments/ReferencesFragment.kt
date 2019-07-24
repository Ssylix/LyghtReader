package com.tech.ssylix.lyght_reader.logic.nativeApis.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isNotEmpty
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.data.models.Reference
import com.tech.ssylix.lyght_reader.data.models.Type
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.NewUpload.Companion.REQ_IMG
import com.tech.ssylix.lyght_reader.logic.utitlities.animateClicks
import com.tech.ssylix.lyght_reader.logic.utitlities.animateClicksRotation
import com.tech.ssylix.lyght_reader.logic.utitlities.debugLog
import com.tech.ssylix.lyght_reader.logic.utitlities.generateRandomKey
import com.tech.ssylix.lyght_reader.logic.viewmodels.ReaderViewModel
import kotlinx.android.synthetic.main.fragment_references.view.*
import kotlinx.android.synthetic.main.page_new_audio_reference.view.*
import kotlinx.android.synthetic.main.page_new_image_reference.view.*
import kotlinx.android.synthetic.main.page_new_video_reference.*
import kotlinx.android.synthetic.main.page_new_web_reference.view.*
import kotlinx.android.synthetic.main.page_new_video_reference.view.*
import kotlinx.android.synthetic.main.page_show_reference.view.*

class ReferencesFragment : Fragment(), ReaderViewModel.OnReferenceLoadListener {

    private var listener: OnFragmentInitializedListener? = null
    private var mReferenceLoadListener: ReaderViewModel.OnReferenceLoadListener? = null
    private lateinit var mViewModel: ReaderViewModel

    lateinit var mShowReferencePage: View
    private lateinit var mNewWebReferencePage: View
    private lateinit var mNewVideoReferencePage: View
    private lateinit var mNewAudioReferencePage: View
    private lateinit var mNewImageReferencePage: View

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

        mShowReferencePage = View.inflate(context!!, R.layout.page_show_reference, FrameLayout(context!!))
        mNewWebReferencePage = View.inflate(context!!, R.layout.page_new_web_reference, FrameLayout(context!!))
        mNewImageReferencePage = View.inflate(context!!, R.layout.page_new_image_reference, FrameLayout(context!!))
        mNewVideoReferencePage = View.inflate(context!!, R.layout.page_new_video_reference, FrameLayout(context!!))
        mNewAudioReferencePage = View.inflate(context!!, R.layout.page_new_audio_reference, FrameLayout(context!!))

        rootView.page_layout.addView(mShowReferencePage)
        rootView.page_layout.addView(mNewWebReferencePage)
        rootView.page_layout.addView(mNewImageReferencePage)
        rootView.page_layout.addView(mNewVideoReferencePage)
        rootView.page_layout.addView(mNewAudioReferencePage)

        showView(mShowReferencePage)

        setupShowReferencePage()

        mShowReferencePage.expand_btn.setOnClickListener {
            if (mShowReferencePage.reference_container.visibility == View.VISIBLE) {
                mShowReferencePage.reference_container.visibility = View.INVISIBLE
            } else {
                mShowReferencePage.reference_container.visibility = View.VISIBLE
            }
        }
        mShowReferencePage.reference_title_text.setOnClickListener {
            if (mShowReferencePage.reference_container.visibility == View.VISIBLE) {
                mShowReferencePage.reference_container.visibility = View.INVISIBLE
            } else {
                mShowReferencePage.reference_container.visibility = View.VISIBLE
            }
        }

        rootView.add_new_reference.setOnClickListener {
            it.animateClicks(50) {
                if (rootView.new_text_reference.visibility == View.VISIBLE) {
                    rootView.new_text_reference.visibility = View.INVISIBLE
                    rootView.new_image_reference.visibility = View.INVISIBLE
                    rootView.new_video_reference.visibility = View.INVISIBLE
                    rootView.new_audio_reference.visibility = View.INVISIBLE
                    showView(mShowReferencePage)
                    rootView.add_new_reference.animateClicksRotation(true, 50)
                } else {
                    rootView.new_text_reference.visibility = View.VISIBLE
                    rootView.new_image_reference.visibility = View.VISIBLE
                    rootView.new_video_reference.visibility = View.VISIBLE
                    rootView.new_audio_reference.visibility = View.VISIBLE
                    showView(mNewWebReferencePage)
                    rootView.add_new_reference.animateClicksRotation(false, 50)
                }
            }
        }

        rootView.new_text_reference.setOnClickListener {
            it.animateClicks(50) {
                showView(mNewWebReferencePage)
                mNewWebReferencePage.save_reference_webpage.setOnClickListener { save_button ->
                    save_button.animateClicks {
                        if (mNewWebReferencePage.web_reference_link_layout.isNotEmpty()) {
                            Reference(
                                mNewWebReferencePage.web_reference_title.text.toString(),
                                mNewWebReferencePage.web_reference_description.text.toString(),
                                0f,
                                Type.URL
                            ).apply {
                                onSaveNewReference(this)
                            }
                        } else {
                            mNewWebReferencePage.web_reference_link_layout.requestFocus()
                            mNewWebReferencePage.web_reference_link_layout.error = "This field is required"
                        }
                    }
                }
            }
        }

        rootView.new_image_reference.setOnClickListener {
            it.animateClicks(50) {
                showView(mNewImageReferencePage)
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
                showView(mNewVideoReferencePage)
                mNewVideoReferencePage.save_reference_video.setOnClickListener { save_button ->
                    save_button.animateClicks {

                    }
                }
            }
        }

        rootView.new_audio_reference.setOnClickListener {
            it.animateClicks(50) {
                showView(mNewAudioReferencePage)
                mNewAudioReferencePage.save_reference_audio.setOnClickListener { save_button ->
                    save_button.animateClicks {
                        showView(mShowReferencePage)
                    }
                }
            }
        }
    }

    private fun setupShowReferencePage() {
        mShowReferencePage.reference_recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mShowReferencePage.reference_recycler.setHasFixedSize(true)
        mShowReferencePage.reference_recycler.adapter = ReferenceRecycler()
        GravitySnapHelper(Gravity.START, true, GravitySnapHelper.SnapListener {

        }).attachToRecyclerView(mShowReferencePage.reference_recycler)
    }

    private fun showView(view: View?) {
        mShowReferencePage.visibility = View.INVISIBLE
        mNewWebReferencePage.visibility = View.INVISIBLE
        mNewImageReferencePage.visibility = View.INVISIBLE
        mNewVideoReferencePage.visibility = View.INVISIBLE
        mNewAudioReferencePage.visibility = View.INVISIBLE

        mShowReferencePage.elevation = 0f
        mNewWebReferencePage.elevation = 0f
        mNewImageReferencePage.elevation = 0f
        mNewVideoReferencePage.elevation = 0f
        mNewAudioReferencePage.elevation = 0f

        view?.visibility = View.VISIBLE
        view?.elevation = 3f
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

    var mMinVal = 0.toLong()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_IMG -> {
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
                                        this.downloadUrl.addOnSuccessListener {
                                            epiPhantomReference.url = it.toString()
                                            this.putFile(uri).addOnSuccessListener {
                                                mNewImageReferencePage.image_reference_upload_progress.visibility =
                                                    View.INVISIBLE
                                                epiPhantomReference.type = Type.getMimeType(context!!, uri)
                                                onSaveNewReference(epiPhantomReference)
                                            }.addOnProgressListener { task ->
                                                if (mNewImageReferencePage.image_reference_upload_progress.visibility == View.INVISIBLE) {
                                                    mNewImageReferencePage.image_reference_upload_progress.visibility =
                                                        View.VISIBLE
                                                }
                                                if (mMinVal > task.bytesTransferred && task.bytesTransferred != 0.toLong()) {
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
                                            }
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

    override fun onCurrentPageChangeReference(pageId: Int) {
        mShowReferencePage.reference_recycler.adapter = ReferenceRecycler(mViewModel.getPageReferences(pageId))
        GravitySnapHelper(Gravity.START, true, GravitySnapHelper.SnapListener {

        }).attachToRecyclerView(mShowReferencePage.reference_recycler)
    }

    override fun onSaveNewReference(reference: Reference) {
        mReferenceLoadListener?.onSaveNewReference(reference)
    }

    override fun onPageReferencesChanged() {
        showView(mShowReferencePage)
        mShowReferencePage.reference_recycler.adapter = ReferenceRecycler()
    }

    companion object {
        const val URL = 1001
        const val VIDEO = 1101
        const val AUDIO = 1011
        const val IMAGE = 1111
    }
}
