package com.tech.ssylix.lyght_reader.logic.nativeApis.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.data.models.Names
import com.tech.ssylix.lyght_reader.data.models.UserData
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.HomePage
import com.tech.ssylix.lyght_reader.logic.utitlities.generateRandomKey
import com.tech.ssylix.lyght_reader.logic.utitlities.toast
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel
import java.nio.charset.Charset
import java.util.*


// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters

    lateinit var mViewModel : HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(activity!!)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        rootView.save_details.setOnClickListener {
            rootView.save_progress.visibility = View.VISIBLE
            if(validateForm(rootView)){
                val userInfo = UserData(
                    Names(
                        rootView.first_name.text.toString(), rootView.middle_name.text.toString(),
                        rootView.surname.text.toString(), rootView.user_name.text.toString(),
                        generateRandomKey(
                            24
                        )
                    ),
                    rootView.email.text.toString(), rootView.phone.text.toString())

                mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.USERINFO).setValue(userInfo).addOnSuccessListener {
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    rootView.save_progress.visibility = View.GONE
                    activity?.startActivity(Intent(activity, HomePage::class.java))
                    activity?.finish()
                    //findNavController().popBackStack(R.id.homeFragment, false)
                }
            }else{
                findError(rootView)
            }
        }

        try{
            rootView.surname.setText((mViewModel.mFirebaseAuth.currentUser?.displayName ?: "").split(" ".toRegex(), 2)[1])
        } catch (a : ArrayIndexOutOfBoundsException){
            rootView.surname.setText("")
        }
        rootView.first_name.setText((mViewModel.mFirebaseAuth.currentUser?.displayName ?: "").split(" ".toRegex(), 2)[0])
        rootView.phone.setText(mViewModel.mFirebaseAuth.currentUser?.phoneNumber)
        rootView.email.setText(mViewModel.mFirebaseAuth.currentUser?.email)
    }



    private fun findError(rootView: View) {
        when{
            rootView.surname.text.isEmpty() -> {
                rootView.surname.error = "Fill this in"
                rootView.surname.requestFocus()
            }

            rootView.first_name.text.isEmpty() -> {
                rootView.first_name.error = "Fill this in"
                rootView.first_name.requestFocus()
            }

            rootView.middle_name.text.isEmpty() -> {
                rootView.middle_name.error = "Fill this in"
                rootView.middle_name.requestFocus()
            }

            rootView.email.text.isEmpty() -> {
                rootView.email.error = "Fill this in"
                rootView.email.requestFocus()
            }

            !rootView.email.text.contains("@") -> {
                rootView.email.error = "Invalid email"
                rootView.email.requestFocus()
            }

            rootView.phone.text.isEmpty() -> {
                rootView.phone.error = "Fill this in"
                rootView.phone.requestFocus()
            }

            rootView.user_name.text.isEmpty() -> {
                rootView.user_name.error = "Fill this in"
                rootView.user_name.requestFocus()
            }
        }
    }

    private fun validateForm(rootView: View) : Boolean =
        rootView.surname.text.isNotEmpty()
                && rootView.first_name.text.isNotEmpty()
                && rootView.middle_name.text.isNotEmpty()
                && rootView.email.text.isNotEmpty()
                && rootView.email.text.contains("@")
                && rootView.phone.text.isNotEmpty()
                && rootView.user_name.text.isNotEmpty()
}
