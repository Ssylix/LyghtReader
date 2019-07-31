package com.tech.ssylix.lyght_reader.logic.utitlities.helpers

import android.view.View
import com.google.firebase.database.DataSnapshot
import com.tech.ssylix.lyght_reader.data.models.Reference
import com.tech.ssylix.lyght_reader.data.models.Type
import com.tech.ssylix.lyght_reader.logic.viewmodels.ReaderViewModel
import kotlinx.android.synthetic.main.page_new_video_reference.view.*
import kotlinx.android.synthetic.main.page_new_web_reference.view.*

class ReferenceOnlineHelper {
    fun getReferenceListFilteredByPage(
        referenceList: ArrayList<Pair<Int, Reference>>,
        pageId: Int
    ): List<Pair<Int, Reference>> {
        return referenceList.filter {
            it.first == pageId
        }
    }

    fun addLoadedReferenceToReferenceList(
        dataSnapshot: DataSnapshot,
        referenceList: ArrayList<Pair<Int, Reference>>,
        referenceLoadListener: ReaderViewModel.OnReferenceLoadListener,
        pageId: Int,
        mBookId: String
    ) {
        val reference = dataSnapshot.getValue(Reference::class.java)
        reference?.mPageId = pageId
        reference?.mDocId = mBookId
        if(reference != null) {
            val curiousPair = Pair(pageId, reference)
            if(!referenceList.contains(curiousPair)){
                referenceList.add(curiousPair)
                referenceLoadListener.onPageReferencesChanged()
            }
        }
    }

    inner class WebReferenceOnlineHelper{
        fun saveNewReference(
            webReferencePage: View,
            helper: ReferenceUiHelper.WebReferenceUiHelper,
            onSaveAction: (Reference) -> Unit
        ) {
            if (helper.isLinkValid(webReferencePage)) {
                Reference(
                    webReferencePage.web_reference_title.text.toString(),
                    webReferencePage.web_reference_description.text.toString(),
                    0f,
                    Type.URL
                ).apply {
                    this.url = webReferencePage.web_reference_link.text.toString()
                    onSaveAction.invoke(this)
                }
            } else {
                helper.showLinkError(webReferencePage, "Enter a valid link here")
            }
        }
    }

    inner class VideoReferenceOnlineHelper {
        fun saveNewReference(
            videoReferencePage: View,
            helper: ReferenceUiHelper.VideoReferenceUiHelper,
            onSaveAction: (Reference) -> Unit
        ) {
            if (helper.isLinkValid(videoReferencePage)) {
                Reference(
                    videoReferencePage.video_reference_title.text.toString(),
                    null,
                    null,
                    Type.MP4
                ).apply {
                    this.url = videoReferencePage.video_reference_link.text.toString()
                    onSaveAction.invoke(this)
                }
            } else {
                helper.showLinkError(videoReferencePage, "Enter a valid link here")
            }
        }
    }
}