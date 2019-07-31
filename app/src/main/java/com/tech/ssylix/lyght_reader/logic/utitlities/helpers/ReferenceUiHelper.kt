package com.tech.ssylix.lyght_reader.logic.utitlities.helpers

import android.app.Activity
import android.net.Uri
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.webkit.URLUtil
import android.widget.FrameLayout
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.logic.nativeApis.fragments.ReferencesFragment
import com.tech.ssylix.lyght_reader.logic.utitlities.GlideApp
import com.tech.ssylix.lyght_reader.logic.utitlities.animateClicksRotation
import kotlinx.android.synthetic.main.fragment_references.view.*
import kotlinx.android.synthetic.main.model_image_reference.view.*
import kotlinx.android.synthetic.main.page_new_video_reference.view.*
import kotlinx.android.synthetic.main.page_new_web_reference.view.*
import kotlinx.android.synthetic.main.page_show_reference.view.*
import java.net.URL


class ReferenceUiHelper(val referencesFragment: ReferencesFragment) {

    fun getPages() {
        referencesFragment.mShowReferencePage =
            View.inflate(
                referencesFragment.context!!,
                R.layout.page_show_reference,
                FrameLayout(referencesFragment.context!!)
            )
        referencesFragment.mNewWebReferencePage =
            View.inflate(
                referencesFragment.context!!,
                R.layout.page_new_web_reference,
                FrameLayout(referencesFragment.context!!)
            )
        referencesFragment.mNewImageReferencePage =
            View.inflate(
                referencesFragment.context!!,
                R.layout.page_new_image_reference,
                FrameLayout(referencesFragment.context!!)
            )
        referencesFragment.mNewVideoReferencePage =
            View.inflate(
                referencesFragment.context!!,
                R.layout.page_new_video_reference,
                FrameLayout(referencesFragment.context!!)
            )
        referencesFragment.mNewAudioReferencePage =
            View.inflate(
                referencesFragment.context!!,
                R.layout.page_new_audio_reference,
                FrameLayout(referencesFragment.context!!)
            )
    }

    fun addPagesToLayout(rootView: View) {
        rootView.page_layout.addView(referencesFragment.mShowReferencePage)
        rootView.page_layout.addView(referencesFragment.mNewWebReferencePage)
        rootView.page_layout.addView(referencesFragment.mNewImageReferencePage)
        rootView.page_layout.addView(referencesFragment.mNewVideoReferencePage)
        rootView.page_layout.addView(referencesFragment.mNewAudioReferencePage)
    }

    fun showView(shownView: View) {
        referencesFragment.mShowReferencePage.visibility = View.INVISIBLE
        referencesFragment.mNewWebReferencePage.visibility = View.INVISIBLE
        referencesFragment.mNewImageReferencePage.visibility = View.INVISIBLE
        referencesFragment.mNewVideoReferencePage.visibility = View.INVISIBLE
        referencesFragment.mNewAudioReferencePage.visibility = View.INVISIBLE

        referencesFragment.mShowReferencePage.elevation = 0f
        referencesFragment.mNewWebReferencePage.elevation = 0f
        referencesFragment.mNewImageReferencePage.elevation = 0f
        referencesFragment.mNewVideoReferencePage.elevation = 0f
        referencesFragment.mNewAudioReferencePage.elevation = 0f

        shownView.visibility = View.VISIBLE
        shownView.elevation = 3f
    }

    fun setupShowReferencePage() {
        referencesFragment.mShowReferencePage.reference_recycler.layoutManager =
            LinearLayoutManager(referencesFragment.context, LinearLayoutManager.HORIZONTAL, false)
        referencesFragment.mShowReferencePage.reference_recycler.setHasFixedSize(true)
        referencesFragment.mShowReferencePage.reference_recycler.adapter = referencesFragment.ReferenceRecycler()
        GravitySnapHelper(Gravity.START, true, GravitySnapHelper.SnapListener {
            val currentReference = (referencesFragment.mShowReferencePage.reference_recycler.adapter
                    as ReferencesFragment.ReferenceRecycler).referenceList[it].second
            referencesFragment.mShowReferencePage.reference_title_text.text = currentReference.title ?: ""
            referencesFragment.mShowReferencePage.reference_notes_text.text = currentReference.notes ?: ""
        }).attachToRecyclerView(referencesFragment.mShowReferencePage.reference_recycler)
    }

    fun setDescriptionVisibilityChangeListener() {
        referencesFragment.mShowReferencePage.expand_btn.setOnClickListener {
            if (referencesFragment.mShowReferencePage.reference_container.visibility == View.VISIBLE) {
                referencesFragment.mShowReferencePage.reference_container.visibility = View.INVISIBLE
            } else {
                referencesFragment.mShowReferencePage.reference_container.visibility = View.VISIBLE
            }
        }
        referencesFragment.mShowReferencePage.reference_title_text.setOnClickListener {
            if (referencesFragment.mShowReferencePage.reference_container.visibility == View.VISIBLE) {
                referencesFragment.mShowReferencePage.reference_container.visibility = View.INVISIBLE
            } else {
                referencesFragment.mShowReferencePage.reference_container.visibility = View.VISIBLE
            }
        }
    }

    fun toggleAddNewReferenceUI(rootView: View) {
        if (rootView.new_web_reference.visibility == View.VISIBLE) {
            rootView.new_web_reference.visibility = View.INVISIBLE
            rootView.new_image_reference.visibility = View.INVISIBLE
            rootView.new_video_reference.visibility = View.INVISIBLE
            rootView.new_audio_reference.visibility = View.INVISIBLE
            showView(referencesFragment.mShowReferencePage)
            rootView.add_new_reference.animateClicksRotation(true, 50)
        } else {
            rootView.new_web_reference.visibility = View.VISIBLE
            rootView.new_image_reference.visibility = View.VISIBLE
            rootView.new_video_reference.visibility = View.VISIBLE
            rootView.new_audio_reference.visibility = View.VISIBLE
            rootView.add_new_reference.animateClicksRotation(false, 50)
        }
    }

    inner class WebReferenceUiHelper {
        fun isLinkValid(webReferencePage: View): Boolean {
            if (webReferencePage.web_reference_link_layout.isNotEmpty()) {
                val urlString = webReferencePage.web_reference_link.text.toString()
                return try {
                    URL(urlString)
                    URLUtil.isValidUrl(urlString) || Patterns.WEB_URL.matcher(urlString).matches()
                } catch (e: Exception) {
                    false
                }
            }
            return false
        }

        fun showLinkError(webReferencePage: View, errorMessage: String) {
            webReferencePage.web_reference_link_layout.requestFocus()
            webReferencePage.web_reference_link_layout.error = errorMessage
        }
    }

    inner class VideoReferenceUiHelper {
        fun setupVideoReferencePage(videoReferencePage: View, context: Activity) {
            videoReferencePage.visibility
            context.packageName
            /*videoReferencePage.video_reference_link.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    Thread{
                        "Started".debugLog()
                        val bitmap = MediaMetadataRetriever().let {
                            it.setDataSource(videoReferencePage.video_reference_link.text.toString(), HashMap<String, String>())
                            "DataSource set".debugLog()
                            it.getFrameAtTime(100)
                            "Time set".debugLog()
                        }

                        "Bitmap set".debugLog()
                        context.runOnUiThread {
                            GlideApp
                                .with(context)
                                .asBitmap()
                                .error(R.drawable.ic_error)
                                .placeholder(R.drawable.banner_blue_flame_black_background_edit)
                                .fitCenter()
                                .load(bitmap).into(videoReferencePage.video_reference_thumbnail)
                        }
                    }.start()

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })*/
        }

        fun isLinkValid(videoReferencePage: View): Boolean {
            videoReferencePage.visibility
            return true
        }

        fun showLinkError(videoReferencePage: View, errorMessage: String) {
            videoReferencePage.video_reference_link.requestFocus()
            videoReferencePage.video_reference_link.error = errorMessage
        }
    }

    inner class ImageReferenceUiHelper{
        fun showReferenceRecyclerBindAction(
            referencesFragment: ReferencesFragment,
            referenceRecycler: ReferencesFragment.ReferenceRecycler,
            position: Int,
            holder: ReferencesFragment.ReferenceRecycler.MyViewHolder
        ) {
            GlideApp
                .with(referencesFragment.context!!)
                .asBitmap()
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.banner_blue_flame_black_background_edit)
                .fitCenter()
                .load(Uri.parse(referenceRecycler.referenceList[position].second.url))
                .into(holder.itemView.reference_image)

            referencesFragment.mShowReferencePage.reference_title_text.text =
                referenceRecycler.referenceList[position].second.title
            referencesFragment.mShowReferencePage.reference_notes_text.text =
                referenceRecycler.referenceList[position].second.notes
        }
    }
}