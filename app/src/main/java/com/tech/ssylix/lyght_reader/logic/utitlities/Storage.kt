package com.tech.ssylix.lyght_reader.logic.utitlities

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Button
import androidx.core.net.toUri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.UploadTask
import com.tech.ssylix.lyght_reader.data.models.*
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.NewUpload
import kotlinx.android.synthetic.main.fragment_new_upload.view.*

class Storage(val uid : String) {

    val USERINFO = "$APP_TYPE/Users/$uid"

    val CONTENT_INFO = "$APP_TYPE/ContentInfo"

    val USER_DATA = "$APP_TYPE/ContentInfo/UserData"

    val FILES_DATA = "$APP_TYPE/ContentInfo/Files"

    val FILES = "$APP_TYPE/Content/Files"

    val THUMBS = "$APP_TYPE/Content/Files/Thumbnails"

    //Storage/ContentInfo/viewedList
    val VIEWED_LIST = "$USER_DATA/ViewedList/$uid"

    val VIEWED_DOCS = "$VIEWED_LIST/Documents"
    val VIEWED_VIDEOS = "$VIEWED_LIST/Media/Video"
    val VIEWED_AUDIO = "$VIEWED_LIST/Media/Audio"
    val VIEWED_PHOTOS = "$VIEWED_LIST/Media/Photos"

    //Storage/ContentInfo/uploadsList
    val UPLOAD_LIST = "$USER_DATA/UploadsList/$uid"

    val UPLOAD_DOCS = "$UPLOAD_LIST/Documents"
    val UPLOAD_VIDEOS = "$UPLOAD_LIST/Media/Video"
    val UPLOAD_AUDIO = "$UPLOAD_LIST/Media/Audio"
    val UPLOAD_PHOTOS = "$UPLOAD_LIST/Media/Photos"

    //Storage/ContentInfo/Files
    val REG_DOCS = "$FILES_DATA/Documents"
    val REG_VIDEOS = "$FILES_DATA/Media/Video"
    val REG_AUDIO = "$FILES_DATA/Media/Audio"
    val REG_PHOTOS = "$FILES_DATA/Media/Photos"

    //Storage/Content/Files
    val DOCS = "$FILES/Documents"
    val VIDEOS = "$FILES/Media/Video"
    val AUDIOS = "$FILES/Media/Audio"
    val PHOTOS = "$FILES/Media/Photos"

    val DOCS_THUMBS = "$THUMBS/Documents"
    val VIDEOS_THUMBS = "$THUMBS/Media/Video"
    val AUDIOS_THUMBS = "$THUMBS/Media/Audio"
    val PHOTOS_THUMBS = "$THUMBS/Media/Photos"

    val INTERPRETN : (String, Int) -> String = { docId, pageId ->
        "$FILES_DATA/Interpretations/$docId/$pageId"
    }

    val SUMMARY_OUTLINE : (String, Int) -> String = { docId, pageId ->
        "$FILES_DATA/Summaries/$docId/$pageId"
    }

    val REFERENCE : (String, Int) -> String = { docId, pageId ->
        "$FILES_DATA/References/$docId/$pageId"
    }

    fun updateUserInfo(databaseReference: DatabaseReference, doc: Document, action : (() -> Unit)? = null) {
        databaseReference.child(this.UPLOAD_DOCS).push().apply {
            doc.stringId = key
            setValue(doc).addOnSuccessListener {
                action?.invoke()
            }
            databaseReference.child(this@Storage.REG_DOCS).child(doc.stringId!!).apply {
                setValue(doc)
            }
        }
    }

    fun uploadThumb(
        newUpload: NewUpload,
        bitmap: Bitmap?,
        meta: Metadata,
        snapshot: UploadTask.TaskSnapshot,
        button: Button,
        uri: Uri,
        action : (() -> Unit)? = null
    ) {
        if(bitmap != null) {
            newUpload.mViewModel.mStorageReference.child(newUpload.mViewModel.mStoreUtils.DOCS_THUMBS).child(newUpload.view?.reference_title_text?.text.toString())
                .putFile(bitmap.toFile("Temp").toUri()).addOnSuccessListener {
                    newUpload.mViewModel.mStorageReference.child(newUpload.mViewModel.mStoreUtils.DOCS_THUMBS)
                        .child(newUpload.view?.reference_title_text?.text.toString())
                        .downloadUrl.addOnSuccessListener { thumbUri ->
                        meta.thumbnailUrl = thumbUri.toString()
                        val doc = Document(
                            newUpload.view?.reference_title_text?.text.toString(),
                            snapshot.metadata?.path!!,
                            null,
                            meta,
                            null,
                            Type.getMimeType(button.context!!, uri)
                        )
                        newUpload.mViewModel.mStoreUtils.updateUserInfo(newUpload.mViewModel.mDatabaseReference, doc, action)
                    }
                }
        }else{
            val doc = Document(
                newUpload.view?.reference_title_text?.text.toString(),
                snapshot.metadata?.path!!,
                null,
                meta,
                null,
                Type.getMimeType(button.context!!, uri)
            )
            newUpload.mViewModel.mStoreUtils.updateUserInfo(newUpload.mViewModel.mDatabaseReference, doc, action)
        }
    }

    fun uploadFile(
        newUpload: NewUpload,
        uri: Uri,
        bitmap: Bitmap?,
        button: Button,
        action: (() -> Unit)? = null
    ) {
        val upload = newUpload.view?.upload_progress
        upload?.isIndeterminate = false
        newUpload.mViewModel.mStorageReference.child(newUpload.mViewModel.mStoreUtils.DOCS).child(newUpload.view?.reference_title_text?.text.toString())
            .putFile(uri).addOnSuccessListener { snapshot ->
                val meta = Metadata()
                meta.type = snapshot.metadata?.contentType
                newUpload.mViewModel.mStorageReference.child(newUpload.mViewModel.mStoreUtils.DOCS).child(newUpload.view?.reference_title_text?.text.toString())
                    .downloadUrl.addOnSuccessListener { Uri ->
                    meta.downloadUrl = Uri.toString()
                    newUpload.mViewModel.mStoreUtils.uploadThumb(newUpload, bitmap, meta, snapshot, button, uri, action)
                }
            }.addOnProgressListener {
                upload?.progress = ((upload?.max ?: 0) * (it.bytesTransferred/it.totalByteCount)).toInt()
            }
    }

    companion object {
        const val APP_TYPE = "Beta-v1"
    }
}