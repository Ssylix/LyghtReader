package com.tech.ssylix.lyght_reader.logic.utitlities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.nio.ByteBuffer
import android.provider.OpenableColumns
import android.graphics.Color
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.tech.ssylix.lyght_reader.R
import java.io.*
import java.nio.charset.Charset
import java.util.*


class ExtensionsAndUtils {

    val dummy_usernames =
        arrayListOf(
            "Pst. Lil'Dicky Omarion",
            "Rev. Snoop Khalifa",
            "Mrs. Adele Simisola",
            "Rev. Bieber Timberlake",
            "Hon. Diddy P",
            "Mr. Jason Jonas",
            "Ms. Alicia Cara",
            "Mr. Enrique Idibia",
            "Pst. Avril Avicii",
            "Mr. Poto Poto",
            "Mrs. Brandy Marley",
            "Ms. Celine Cher",
            "Rev. Chris Lovato",
            "Mrs. Diana Dre",
            "Madam Emeli Sheeran"
        )

    val dummy_status = arrayListOf(
        "Unverified",
        "Redeemed",
        "Expired",
        "Expires soon"
    )

    val dummy_amount = arrayListOf(
        "₦ 20,000",
        "₦ 10,000",
        "₦ 15,000",
        "₦ 23,000"
    )

    val dummy_titles = arrayListOf(
        "Easter package widows 2020",
        "Christmas love feast 2019",
        "Easter camping 2019",
        "Reimbursement of evangelist from South-Africa"
    )

    val status_colors = arrayListOf<Int>(
    )

    val dummy_events = arrayListOf(
        "Easter package widows 2020",
        "Christmas love feast 2019",
        "Prison visitation 2019",
        "October 2019 Harvest funding",
        "Disabled people care program",
        "General Gabriel camping and missionary mission",
        "Internally displaced person support funds",
        "Let's save the earth fund raiser",
        "Renovate the old cathedral at Ekpoma",
        "Reimbursement of evangelist from South-Africa"
    )

    val dummy_avatars = arrayListOf<Int>(

    )

    fun inDemo(view: View, msg: String) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    inner class DefaultRecyclerAdapter(
        val context: Context,
        private val itemModel: Int,
        private val itemCnt: Int = 18,
        private val initAction: (() -> Unit)? = null,
        private val bindAction: ((holder: RecyclerView.ViewHolder, position: Int) -> Unit)? = null,
        private val clickAction: ((view: View, holder: RecyclerView.ViewHolder) -> Unit)? = null
    ) : RecyclerView.Adapter<DefaultRecyclerAdapter.MyViewHolder>() {

        var emptyList = false
        var DEFAULT_EMPTY = 1001

        init {
            initAction?.invoke()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                DEFAULT_EMPTY -> MyViewHolder(
                    LayoutInflater
                        .from(context)
                        .inflate(R.layout.model_empty_list_text, parent, false)
                )

                else -> MyViewHolder(
                    LayoutInflater
                        .from(context)
                        .inflate(itemModel, parent, false)
                )
            }
        }

        override fun getItemCount(): Int {
            return if (itemCnt < 1) {
                emptyList = true
                0
            } else {
                itemCnt
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (emptyList) {
                DEFAULT_EMPTY
            } else {
                0
            }
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (!emptyList) {
                bindAction?.invoke(holder, position)
            } else {

            }
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                clickAction?.invoke(itemView, this)
            }
        }
    }
}

fun Context.toast(obj: Any = "Here", duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, obj.toString(), duration).show()
}

fun <T> T.debugLog(logLevel: Int = Log.ERROR): T {
    when (logLevel) {
        Log.VERBOSE -> {
            Log.v("Debugger", this.toString())
        }
        Log.DEBUG -> {
            Log.d("Debugger", this.toString())
        }
        Log.INFO -> {
            Log.i("Debugger", this.toString())
        }
        Log.WARN -> {
            Log.w("Debugger", this.toString())
        }
        Log.ERROR -> {
            Log.e("Debugger", this.toString())
        }
        Log.ASSERT -> {
            Log.wtf("Debugger", this.toString())
        }
    }
    return this
}


fun File.getPdfPageBitmap(scaleDownFactor: Float = 5f, pageNumber: Int = 0): Bitmap {
    val pageOne = PdfRenderer(ParcelFileDescriptor.open(this, ParcelFileDescriptor.MODE_READ_ONLY))
        .openPage(pageNumber)

    val bitmap = Bitmap.createBitmap(
        (pageOne.width / scaleDownFactor).toInt(),
        (pageOne.height / scaleDownFactor).toInt(), Bitmap.Config.ARGB_8888
    )
    pageOne.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    return bitmap.removeTransparentPixels()
}

fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor!!.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }

    return result.substringBeforeLast(".")
}

@Throws(IOException::class)
fun File.copyInputStreamToFile(inputStream: InputStream) {

    FileOutputStream(this).use { outputStream ->
        var read: Int
        val bytes = ByteArray(1024)

        read = inputStream.read(bytes)
        while (read != -1) {
            outputStream.write(bytes, 0, read)
            read = inputStream.read(bytes)
        }
    }
}

fun Uri.getByteArray(context: Context): ByteArray {
    val inputStream = context.contentResolver.openInputStream(this)
    val byteBuffer = ByteArrayOutputStream()
    inputStream.use { iS ->
        val bufferSize = 1024
        val byteArray = ByteArray(bufferSize)
        var len = iS!!.read(byteArray)
        while (len != -1) {
            byteBuffer.write(byteArray, 0, len)
            len = iS.read(byteArray)
        }
    }

    return byteBuffer.use {
        it.toByteArray()
    }
}

fun Bitmap.toByteArray(): ByteArray {
    width = this.width
    height = this.height
    val size = this.rowBytes * this.height
    val byteBuffer = ByteBuffer.allocate(size)
    this.copyPixelsToBuffer(byteBuffer)
    return byteBuffer.array()
}

fun Bitmap.toFile(name: String): File {
    val file = File.createTempFile(name, ".jpeg")
    val os = BufferedOutputStream(FileOutputStream(file))
    this.compress(Bitmap.CompressFormat.JPEG, 100, os)
    os.close()
    return file
}

fun Bitmap.invert(): Bitmap {
    val length = this.width * this.height
    val array = IntArray(length)
    this.getPixels(array, 0, this.width, 0, 0, this.width, this.height)
    for (i in 0 until length) {
        val a = 255 - (array[i] shr 24 and 0xFF)
        val r = 255 - (array[i] shr 16 and 0xFF)
        val g = 255 - (array[i] shr 8 and 0xFF)
        val b = 255 - (array[i] shr 0 and 0xFF)

        array[i] = Color.argb(a, r, g, b)
    }
    this.setPixels(array, 0, this.width, 0, 0, this.width, this.height)
    return this
}

fun Bitmap.removeTransparentPixels(): Bitmap {
    val length = this.width * this.height
    val array = IntArray(length)
    this.getPixels(array, 0, this.width, 0, 0, this.width, this.height)
    for (i in 0 until length) {
        if (array[i] == Color.TRANSPARENT) {
            array[i] = Color.WHITE
        }
    }
    this.setPixels(array, 0, this.width, 0, 0, this.width, this.height)
    return this
}

fun View.animateClicks(dur: Long = 20, function: (() -> Unit)? = null) {
    val bouncerXF1 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.7f)
    val bouncerYF1 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.7f)
    val bouncerXR1 = ObjectAnimator.ofFloat(this, "scaleX", 0.7f, 1f)
    val bouncerYR1 = ObjectAnimator.ofFloat(this, "scaleY", 0.7f, 1f)
    val bouncerXF2 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.85f)
    val bouncerYF2 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.85f)
    val bouncerXR2 = ObjectAnimator.ofFloat(this, "scaleX", 0.85f, 1f)
    val bouncerYR2 = ObjectAnimator.ofFloat(this, "scaleY", 0.85f, 1f)

    bouncerXF1.interpolator = DecelerateInterpolator()
    bouncerYF1.interpolator = DecelerateInterpolator()

    bouncerXR1.interpolator = AccelerateInterpolator()
    bouncerYR1.interpolator = AccelerateInterpolator()

    bouncerXF2.interpolator = DecelerateInterpolator()
    bouncerYF2.interpolator = DecelerateInterpolator()

    bouncerXR2.interpolator = AccelerateInterpolator()
    bouncerYR2.interpolator = AccelerateInterpolator()

    /*arrayListOf(bouncerXF1, bouncerXF2, bouncerYF1, bouncerYF2, bouncerXR1, bouncerXR2, bouncerYR1, bouncerYR2).forEach {

    }*/

    AnimatorSet().apply {
        playSequentially(
            AnimatorSet().apply {
                playTogether(bouncerXF1, bouncerYF1)
            },
            AnimatorSet().apply {
                playTogether(bouncerXR1, bouncerYR1)
            },
            AnimatorSet().apply {
                playTogether(bouncerXF2, bouncerYF2)
            },
            AnimatorSet().apply {
                playTogether(bouncerXR2, bouncerYR2)
            }
        )

        duration = dur
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                function?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })

        start()
    }
}

fun View.animateClicksRotation(reverse: Boolean, dur: Long = 20, function: (() -> Unit)? = null) {
    if (!reverse) {
        ObjectAnimator.ofFloat(this, "rotation", 0f, 45f)
    } else {
        ObjectAnimator.ofFloat(this, "rotation", 45f, 0f)
    }.apply {
        duration = dur
        interpolator = DecelerateInterpolator()
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                function?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })

        start()
    }
}

fun generateRandomKey(length : Int) : String {
    var n = length
    // length is bounded by 256 Character
    val array = ByteArray(256)
    Random().nextBytes(array)

    val randomString = String(array, Charset.forName("UTF-8"))

    // Create a StringBuffer to store the result
    val r = StringBuffer()

    // Append first 20 alphanumeric characters
    // from the generated random String into the result
    for (k in 0 until randomString.length) {
        val ch = randomString[k]
        if ((ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9') && n > 0) {
            r.append(ch)
            n--
        }
    }
    // return the resultant string
    return r.toString()
}