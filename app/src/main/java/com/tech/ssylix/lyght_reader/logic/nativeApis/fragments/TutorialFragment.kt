package com.tech.ssylix.lyght_reader.logic.nativeApis.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.DialogFragment
import com.tech.ssylix.lyght_reader.R
import android.app.Dialog
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.tech.ssylix.lyght_reader.logic.utitlities.ExtensionsAndUtils
import com.tech.ssylix.lyght_reader.logic.utitlities.animateClicks
import kotlinx.android.synthetic.main.fragment_tutorial.view.*
import kotlinx.android.synthetic.main.model_tutorial_recycler.view.*
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TutorialFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */
class TutorialFragment : DialogFragment() {

    fun newInstance(tutorialId : Int): TutorialFragment {
        val frag = this
        val args = Bundle()
        args.putInt("typeOfTutorial", tutorialId)
        frag.arguments = args
        return frag
    }

    var recyclerPosition = 0

    val homeArray = arrayOf(R.drawable.continue_reading, R.drawable.recommended_reading, R.drawable.upload_new)
    val newArray = arrayOf(R.drawable.select_file, R.drawable.preview_file, R.drawable.change_name, R.drawable.begin_upload)
    val readArray = arrayOf(R.drawable.read_periferals, R.drawable.dark_mode)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TutorialDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tutorialId = arguments!!.getInt("typeOfTutorial")

        val view = View.inflate(context, R.layout.fragment_tutorial, FrameLayout(context!!))
        view.tutorial_recycler.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                v?.performClick()
                return true
            }
        })
        view.tutorial_recycler.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        view.tutorial_recycler.setHasFixedSize(true)
        GravitySnapHelper(Gravity.START, true)
        when(tutorialId) {
            HOME_TUTORIAL -> {
                view.tutorial_recycler.adapter = ExtensionsAndUtils().DefaultRecyclerAdapter(
                    context!!, R.layout.model_tutorial_recycler, homeArray.size, bindAction = { holder, position ->
                        holder.itemView.tutorial_image.setImageResource(homeArray[position])
                    })
            }

            READER_PAGE -> {
                view.tutorial_recycler.adapter = ExtensionsAndUtils().DefaultRecyclerAdapter(
                    context!!, R.layout.model_tutorial_recycler, readArray.size, bindAction = { holder, position ->
                        holder.itemView.tutorial_image.setImageResource(readArray[position])
                    })
            }

            NEW_CONTENT -> {
                view.tutorial_recycler.adapter = ExtensionsAndUtils().DefaultRecyclerAdapter(
                    context!!, R.layout.model_tutorial_recycler, newArray.size, bindAction = { holder, position ->
                        holder.itemView.tutorial_image.setImageResource(newArray[position])
                    })
            }
        }

        view.next_slide.setOnClickListener {
            it.animateClicks {
                recyclerPosition++

                fun validateNextAction(size: Int) {
                    when {
                        recyclerPosition < (size - 1) -> {
                            view.tutorial_recycler.smoothScrollToPosition(recyclerPosition)
                            view.previous_slide.visibility = View.VISIBLE
                        }

                        recyclerPosition == (size - 1) -> {
                            view.tutorial_recycler.smoothScrollToPosition(recyclerPosition)
                            view.previous_slide.visibility = View.VISIBLE
                            view.next_slide.text = "Finish"
                        }

                        recyclerPosition > (size - 1) -> {
                            dismiss()
                        }
                    }
                }

                when(tutorialId) {
                    HOME_TUTORIAL -> {
                        validateNextAction(homeArray.size)
                    }

                    READER_PAGE -> {
                        validateNextAction(readArray.size)
                    }

                    NEW_CONTENT -> {
                        validateNextAction(newArray.size)
                    }
                }
            }
        }

        view.previous_slide.setOnClickListener {
            it.animateClicks {
                recyclerPosition--

                fun validatePreviousAction(size: Int) {
                    when {
                        recyclerPosition == 0 -> {
                            view.tutorial_recycler.smoothScrollToPosition(recyclerPosition)
                            view.previous_slide.visibility = View.INVISIBLE
                        }

                        recyclerPosition < (size - 1) -> {
                            view.tutorial_recycler.smoothScrollToPosition(recyclerPosition)
                            view.next_slide.text = "Next"
                        }
                    }
                }

                when(tutorialId) {
                    HOME_TUTORIAL -> {
                        validatePreviousAction(homeArray.size)
                    }

                    READER_PAGE -> {
                        validatePreviousAction(readArray.size)
                    }

                    NEW_CONTENT -> {
                        validatePreviousAction(newArray.size)
                    }
                }
            }
        }

        return AlertDialog.Builder(context!!)
            .setIcon(R.drawable.ic_launcher_foreground)
            .setView(view)
            .create()
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        const val HOME_TUTORIAL = 1001
        const val READER_PAGE = 1011
        const val NEW_CONTENT = 1010
    }
}
