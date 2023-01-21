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

class MyCreatorDetailAdapter(private val newsList : ArrayList<String>, private val context: Context, private val eventID: String, private val userID: String, private val current: Int) : RecyclerView.Adapter<MyCreatorDetailAdapter.MyViewHolder>(),Filterable {

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
            R.layout.events_participant_item,
            parent,false)

        return MyViewHolder(itemView,mListener)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]

        changeName(holder, currentItem, false)

        var flag = false
        holder.delete.setOnClickListener {
            if (flag){
                Toast.makeText(context, "Attendee has already been removed.", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseFirestore.getInstance().collection("eventDetails")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val participantID = document.data["participantID"]
                            val eventIDV = document.data["eventID"]
                            if (eventIDV.toString().equals(eventID) && participantID == newsList[position]){

                                FirebaseFirestore.getInstance().collection("eventDetails").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Attendee has been removed successfully!", Toast.LENGTH_SHORT).show()
                                        changeName(holder, currentItem, true)
                                        val x = current - 1
                                        FirebaseFirestore.getInstance().collection("events").document(eventID).update("current", x.toString())
                                    }
                                    .addOnFailureListener { Toast.makeText(context, "Attendee has already been removed.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }
            }
            flag = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeName(holder: MyCreatorDetailAdapter.MyViewHolder, currentItem: String, flag: Boolean) {
        val docRef = FirebaseFirestore.getInstance().collection("users").document(currentItem)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val name = document.getString("name")
                    val ID = document.getString("userID")
                    if (flag){
                        holder.attendee.text = "$name (REMOVED)"
                    }
                    else{
                        holder.attendee.text = name
                    }
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
        val delete: ImageView = itemView.findViewById(R.id.delete)

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
