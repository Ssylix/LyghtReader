package com.tech.ssylix.lyght_reader.logic.nativeApis.fragments

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.tech.ssylix.lyght_reader.R
import com.tech.ssylix.lyght_reader.data.models.Document
import com.tech.ssylix.lyght_reader.data.models.UserData
import com.tech.ssylix.lyght_reader.logic.nativeApis.activities.ReaderPage
import com.tech.ssylix.lyght_reader.logic.utitlities.Auth
import com.tech.ssylix.lyght_reader.logic.utitlities.animateClicks
import com.tech.ssylix.lyght_reader.logic.utitlities.debugLog
import com.tech.ssylix.lyght_reader.logic.utitlities.toast
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel
import com.tech.ssylix.lyght_reader.logic.viewmodels.HomeViewModel.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.model_book_recycler.view.*
import org.apache.commons.lang3.tuple.MutablePair


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(), UX, OnContinueItemListener, OnParamsInitialized, OnRecommendedListListener {
    override fun onFirebaseParamsInitialized() {

        if (mViewModel.getUserData(activity!!).contentInfo?.viewed?.docs?.isNotEmpty() == true &&
            (view?.continue_list?.adapter as? DocumentRecyclerAdapter)?.list?.isEmpty() == true
        ) {
            (view?.continue_list?.adapter as? DocumentRecyclerAdapter)?.list =
                mViewModel.getUserData(activity!!).contentInfo?.viewed?.docs as ArrayList<Document>
            (view?.continue_list?.adapter as? DocumentRecyclerAdapter)?.notifyDataSetChanged()
        }

        if (mViewModel.mRecommendedList.isNotEmpty() &&
            (view?.recommended_list?.adapter as? DocumentRecyclerAdapter)?.list?.isEmpty() == true
        ) {
            (view?.recommended_list?.adapter as? DocumentRecyclerAdapter)?.list = mViewModel.mRecommendedList
            (view?.recommended_list?.adapter as? DocumentRecyclerAdapter)?.notifyDataSetChanged()
            onItemLoadedProgress(view?.firebase_load_progress!!.max)
        }
    }

    lateinit var listener: OnInitialize

    private val mLoadProgressTotal = PROGRESS_CONT.left + PROGRESS_REC.left + PROGRESS_USERDATA.left

    override fun onBioDataLoaded(userData: UserData?) {
        if (userData != null) {
            mViewModel.getUserData(activity!!).apply {
                name = userData.name
                email = userData.email
                phone = userData.phone
            }
        } else {
            val args = Bundle()
            args.putString(Auth.UID_ARG, mViewModel.mStoreUtils.uid)
            findNavController().navigate(R.id.signUpFragment, args)
        }
        if (!PROGRESS_USERDATA.right) {
            PROGRESS_USERDATA.right = true
            onItemLoadedProgress(PROGRESS_USERDATA.left)
        }
    }

    override fun onItemAdded(document: Document) {
        (view?.continue_list?.adapter as? HomeFragment.DocumentRecyclerAdapter)?.list.apply {
            fun ArrayList<Document>?.addItem(document: Document) {
                this?.add(document)
                this?.sortByDescending {
                    it.registerTime
                }
            }
            this?.addItem(document)
            mViewModel.mUserData.contentInfo?.viewed?.docs?.addItem(document)
            view?.continue_list?.adapter?.notifyDataSetChanged()
        }
        if (!PROGRESS_CONT.right) {
            PROGRESS_CONT.right = true
            onItemLoadedProgress(PROGRESS_CONT.left)
        }
    }

    override fun onItemRemoved(document: Document) {
        (view?.continue_list?.adapter as? HomeFragment.DocumentRecyclerAdapter)?.list.apply {
            fun ArrayList<Document>?.removeItem(document: Document) {
                this?.remove(find {
                    it.stringId == document.stringId
                })
                this?.sortByDescending {
                    it.registerTime
                }
            }
            this?.removeItem(document)
            mViewModel.mUserData.contentInfo?.viewed?.docs?.removeItem(document)
            view?.continue_list?.adapter?.notifyDataSetChanged()
        }
    }

    override fun onItemChanged(document: Document) {
        (view?.continue_list?.adapter as? HomeFragment.DocumentRecyclerAdapter)?.list.apply {
            fun ArrayList<Document>?.changeItem(document: Document) {
                this?.find {
                    it.stringId == document.stringId
                }.apply {
                    this?.title = document.title
                    this?.metadata = document.metadata
                    this?.rating = document.rating
                    this?.storageLocation = document.storageLocation
                    this?.tagData = document.tagData
                    this?.type = document.type
                    this?.registerTime = document.registerTime
                }
                this?.sortByDescending {
                    it.registerTime
                }
            }
            this?.changeItem(document)
            mViewModel.mUserData.contentInfo?.viewed?.docs?.changeItem(document)
            view?.continue_list?.adapter?.notifyDataSetChanged()
        }
    }

    override fun onItemLoadedProgress(increment: Int) {
        view?.firebase_load_progress?.isIndeterminate = false
        if (view?.firebase_load_progress != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view?.firebase_load_progress!!.setProgress(view?.firebase_load_progress!!.progress + increment, true)
            } else {
                view?.firebase_load_progress!!.progress = view?.firebase_load_progress!!.progress + increment
            }
            if (view?.firebase_load_progress!!.progress == view?.firebase_load_progress!!.max) {
                view?.firebase_load_progress!!.visibility = View.GONE
            }
        }
        try {
            mViewModel.getUserData(activity!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRecommendationAdded(document: Document) {
        (view?.recommended_list?.adapter as? DocumentRecyclerAdapter).apply {
            this?.list?.add(document)
            mViewModel.mRecommendedList.add(document)
            this?.notifyDataSetChanged()
        }
        if (!PROGRESS_REC.right) {
            PROGRESS_REC.right = true
            onItemLoadedProgress(PROGRESS_REC.left)
        }
    }

    override fun onRecommendationChanged(document: Document) {
        var index: Int
        (view?.recommended_list?.adapter as HomeFragment.DocumentRecyclerAdapter).apply {
            list[
                    list.indexOf(
                        list.find {
                            it.stringId == document.stringId
                        }
                    ).apply {
                        index = this
                    }
            ] = document
            notifyItemChanged(index)
        }
    }

    lateinit var mViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnInitialize) {
            listener = context
            listener.referInterfaces(this)
            mViewModel = ViewModelProviders.of(activity!!)[HomeViewModel::class.java]
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.recommended_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.recommended_list.setHasFixedSize(true)
        view.recommended_list.adapter = DocumentRecyclerAdapter()

        view.continue_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.continue_list.setHasFixedSize(true)
        view.continue_list.adapter = DocumentRecyclerAdapter()

        view.firebase_load_progress.max = mLoadProgressTotal
    }

    inner class DocumentRecyclerAdapter(var list: ArrayList<Document> = ArrayList()) :
        RecyclerView.Adapter<DocumentRecyclerAdapter.MyViewHolder>() {
        /*init {
            view?.firebase_load_progress?.visibility = View.GONE
        }*/

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(context!!).inflate(R.layout.model_book_recycler, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        private fun activateGlide(holder: MyViewHolder, thumbLink: String?) {
            //holder.itemView.glide_load_progress.visibility = View.VISIBLE
            Glide
                .with(context!!)
                .load(thumbLink)
                .apply {
                    addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //holder.itemView.glide_load_progress.visibility = View.GONE
                            context?.toast("Failed")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //holder.itemView.glide_load_progress.visibility = View.GONE
                            return false
                        }
                    })
                }
                .apply(RequestOptions().placeholder(R.drawable.ic_open_book))
                .into(holder.itemView.doc_thumb)

            /*if (holder.itemView.glide_imgV.visibility == View.VISIBLE) {
                holder.itemView.glide_imgV.visibility = View.GONE
            } else {
                holder.itemView.glide_imgV.visibility = View.VISIBLE
            }*/
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.itemView.doc_title.text = list[position].title
            if (list[position].metadata?.downloadUrl != null) {
                apply {
                    activateGlide(holder, list[position].metadata?.thumbnailUrl)
                }
            }
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    it.animateClicks {
                        startActivity(
                            Intent(context!!, ReaderPage::class.java)
                                .putExtra(DOC_KEY, list[adapterPosition].stringId)
                                .putExtra(DOC_TITLE, list[adapterPosition].title)
                                .putExtra(DOC_URL, list[adapterPosition].metadata?.downloadUrl)
                        )

                        val readListener = object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val doc = p0.getValue(Document::class.java)
                                if (doc == null) {
                                    list[adapterPosition].registerTime = System.currentTimeMillis()
                                    mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.VIEWED_DOCS)
                                        .child(list[adapterPosition].stringId!!).removeEventListener(this)
                                    mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.VIEWED_DOCS)
                                        .child(list[adapterPosition].stringId!!).setValue(list[adapterPosition])
                                } else {
                                    doc.stringId = p0.key
                                    doc.registerTime = System.currentTimeMillis()
                                    mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.VIEWED_DOCS)
                                        .child(list[adapterPosition].stringId!!).removeEventListener(this)
                                    mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.VIEWED_DOCS)
                                        .child(doc.stringId!!).setValue(doc)
                                }
                            }

                        }
                        mViewModel.mDatabaseReference.child(mViewModel.mStoreUtils.VIEWED_DOCS)
                            .child(list[adapterPosition].stringId!!).addValueEventListener(readListener)
                    }
                }
            }
        }
    }

    interface OnInitialize {
        fun referInterfaces(homeFragment: HomeFragment)
    }

    companion object {
        const val DOC_TITLE = "DocTitle"
        const val DOC_KEY = "DocID"
        const val DOC_URL = "DocUrl"
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        val PROGRESS_CONT = MutablePair(20, false)
        val PROGRESS_REC = MutablePair(20, false)
        val PROGRESS_USERDATA = MutablePair(20, false)
    }
}
