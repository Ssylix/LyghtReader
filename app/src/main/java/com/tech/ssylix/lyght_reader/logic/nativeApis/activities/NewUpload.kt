package com.tech.ssylix.lyght_reader.logic.nativeApis.activities


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_new_upload.view.*
import androidx.loader.content.CursorLoader
import com.tech.ssylix.lyght_reader.*
import com.tech.ssylix.lyght_reader.data.models.Type
import com.tech.ssylix.lyght_reader.logic.utitlities.*
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel
import com.tech.ssylix.lyght_reader.logic.viewmodels.NewContentViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 *
 */
class NewUpload : Fragment() {

    lateinit var  mViewModel: NewContentViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this)[NewContentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.image_upload.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
//            startActivityForResult(Intent.createChooser(intent, "Get Images"), REQ_IMG)
//        }

        view.document_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Get Documents"),
                REQ_DOC
            )
        }

        /*view.video_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Get Documents"), REQ_VID)
        }

        view.audio_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Get Documents"), REQ_AUD)
        }*/*/*/
    }


    private val DEFAULT_TITLE: String = "Temporary file name (change this)"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun onStartUpload(action: (() -> Unit)? = null) {
            view?.upload_progress?.visibility = View.VISIBLE
            view?.save_upload?.isEnabled = false
            view?.document_upload?.isEnabled = false
            view?.image_upload?.isEnabled = false
            action?.invoke()
        }

        fun onFinishedUpload() {
            context!!.toast("Saved")
            view?.save_upload?.visibility = View.GONE
            view?.upload_progress?.visibility = View.GONE
            view?.save_upload?.isEnabled = true
            view?.document_upload?.isEnabled = true
            view?.image_upload?.isEnabled = true
            view?.preview_image?.setImageResource(R.color.fui_buttonShadow)
            view?.reference_title_text?.setText("")
        }

        when(requestCode){
            REQ_DOC -> {
                if (resultCode == RESULT_OK){
                    val uri : Uri = data?.data!!
                    val file = File.createTempFile("Temp", ".temp")
                    file.copyInputStreamToFile(context?.contentResolver?.openInputStream(uri)!!)

                    try {
                        val bitmap = file.getPdfPageBitmap(2f)
                        view?.preview_image?.setImageBitmap(bitmap)
                        view?.reference_title_text?.setText(getFileName(context!!, uri) ?: DEFAULT_TITLE)
                        if(Type.getMimeType(context!!, uri) != Type.OTHER){
                            view?.save_upload?.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    if(view?.reference_title_text?.text?.isNotEmpty() == true &&
                                        view?.reference_title_text?.text?.toString() != DEFAULT_TITLE){
                                        onStartUpload{
                                            mViewModel.mStoreUtils.uploadFile(this@NewUpload, uri, bitmap, this){
                                                onFinishedUpload()
                                            }
                                        }
                                    }else{
                                        view?.reference_title_text?.requestFocus()
                                        view?.reference_title_text?.error = "Invalid name"
                                    }
                                }
                            }
                        }else{
                            context!!.toast("Unsupported file format")
                        }
                    }catch (io : IOException){
                        context?.toast("Only PDF files are supported")
                    }
                }
            }

            /*REQ_VID -> {
                if (resultCode == RESULT_OK){
                    val uri : Uri = data?.data!!
                    val file = File.createTempFile("Temp", ".temp")
                    file.copyInputStreamToFile(context?.contentResolver?.openInputStream(uri)!!)
                }
            }*/

            /*REQ_AUD -> {
                if (resultCode == RESULT_OK){
                    val uri : Uri = data?.data!!
                    uri.getRealPathFromURI(MediaStore.Audio.Media.DATA)
                }
            }*/
        }
    }

    private fun Uri.getRealPathFromURI(data: String): String {
        val proj = arrayOf(data)
        val loader = CursorLoader(context!!, this, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val columnIndex = cursor!!.getColumnIndexOrThrow(data)
        cursor.moveToFirst()
        val result = cursor.getString(columnIndex)
        cursor.close()
        return result
    }

    companion object {
        const val REQ_IMG = 1011
        const val REQ_DOC = 1010
        const val REQ_VID = 1001
        const val REQ_AUD = 1110
        const val ARG_PARAM2 = "param2"
    }
}