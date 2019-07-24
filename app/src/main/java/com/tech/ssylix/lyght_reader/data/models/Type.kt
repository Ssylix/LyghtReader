package com.tech.ssylix.lyght_reader.data.models

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.Keep
import java.io.File

@Keep
enum class Type{
    PDF, DOCX, XLSX, PPTX,
    MP4, MKV, YOUTUBE,
    MP3, MWA, AAC,
    JPEG, JPG, PNG, SVG, GIF, WBMP, BMP, WEBP,
    URL,
    OTHER;

    companion object {

        fun getMimeType(context : Context, uri : Uri) : Type {
            val extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))!!
            } else {
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }
            //context.toast(extension)

            return when(extension.toUpperCase()){
                "JPG" -> {
                    JPG
                }
                "JPEG" -> {
                    JPEG
                }
                "GIF" -> {
                    GIF
                }
                "PNG" -> {
                    PNG
                }
                "BMP" -> {
                    BMP
                }
                "SVG" -> {
                    SVG
                }
                "WBMP" -> {
                    WBMP
                }
                "WEBP" -> {
                    WEBP
                }
                "PDF" -> {
                    PDF
                }
                "MP3" -> {
                    MP3
                }
                "MWA" -> {
                    MWA
                }
                "AAC" -> {
                    AAC
                }
                else -> {
                    OTHER
                }
            }
        }

        /*fun getMimeType(type : String) : Type =
            when(type.toUpperCase()){
                "JPG" -> { JPG }
                "JPEG" -> { JPEG }
                "GIF" -> { GIF }
                "PNG" -> { PNG }
                "BMP" -> { BMP }
                "SVG" -> { SVG }
                "WBMP" -> { WBMP }
                "WEBP" -> { WEBP }
                "PDF" -> { PDF }
                else -> { OTHER }
            }*/
    }
}