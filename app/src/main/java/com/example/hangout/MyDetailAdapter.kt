package com.example.recylerviewkotlin

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.hangout.Events
import com.example.hangout.R
import com.example.hangout.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class MyDetailAdapter(private val newsList : ArrayList<String>, private val context: Context, private val userID: String) : RecyclerView.Adapter<MyDetailAdapter.MyViewHolder>(),Filterable {

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{

        fun onItemClick(position : Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener){

        mListener = listener

    }

    fun deleteItem(i : Int){

        newsList.removeAt(i)
        notifyDataSetChanged()

    }

    fun addItem(i : Int, news : String){

        newsList.add(i, news)
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.participants,
            parent,false)

        return MyViewHolder(itemView,mListener)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]

        val docRef = FirebaseFirestore.getInstance().collection("users").document(currentItem)

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val name = document.getString("name")
                    val ID = document.getString("userID")
                    holder.attendee.text = name
                    holder.attendee.setOnClickListener {
                        val intent = Intent(context, User::class.java)
                        intent.putExtra("ID", userID)
                        intent.putExtra("profileID", ID)
                        context.startActivity(intent)
                    }
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val attendee : TextView = itemView.findViewById(R.id.attendee)

        init {

            itemView.setOnClickListener {

                listener.onItemClick(adapterPosition)

            }



        }

    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }

}
