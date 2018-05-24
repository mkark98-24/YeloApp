package com.ddinc.yeloapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ddinc.yeloapp.Model
import com.ddinc.yeloapp.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.list_model_view.view.*

var query = FirebaseDatabase.getInstance()
        .reference
        .child("models")

var options = FirebaseRecyclerOptions.Builder<Model>()
        .setQuery(query, Model::class.java)
        .build()

class FetchAdapter(val context: Context) : FirebaseRecyclerAdapter<Model, FetchAdapter.TaskViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FetchAdapter.TaskViewHolder {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(viewType, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int, model: Model) {
        holder.tvName.text = model.name
        holder.tvMail.text = model.mail
        holder.tvAddress.text = model.address
        Glide.with(context).load(model.picUrl).into(holder.ivPic)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_model_view
    }


    override fun onDataChanged() {
        // Called each time there is a new data snapshot. You may want to use this method
        // to hide a loading spinner or check for the "no documents" state and update your UI.
        // ...
    }

    override fun onError(e: DatabaseError) {
        // Called when there is an error getting data. You may want to update
        // your UI to display an error message to the user.
        // ...
    }


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnDelete: ImageButton = itemView.btn_delete
        val tvName: TextView = itemView.prof_name3
        val tvMail: TextView = itemView.prof_mail3
        val tvAddress: TextView = itemView.prof_address
        val ivPic: ImageView = itemView.prof_pic3

        init {
            btnDelete.visibility = View.GONE
            btnDelete.setOnClickListener {
                val myRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://yeloapp-1526849284615.firebaseio.com/")
                myRef.child("models").child(getItem(this.adapterPosition).id).removeValue()

            }
        }
    }
}