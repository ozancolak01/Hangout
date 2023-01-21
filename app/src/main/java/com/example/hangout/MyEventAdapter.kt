package com.example.recylerviewkotlin

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.hangout.EventDetails
import com.example.hangout.Events
import com.example.hangout.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class MyEventAdapter(private val newsList : ArrayList<Events>, private val context: Context, private val userID: String) : RecyclerView.Adapter<MyEventAdapter.MyViewHolder>(),Filterable {

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

    fun addItem(i : Int, news : Events){

        newsList.add(i, news)
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.cards_events,
            parent,false)

        return MyViewHolder(itemView,mListener)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.date.text = currentItem.date
        holder.title.text = currentItem.title
        holder.description.text = currentItem.description
        holder.time.text = currentItem.time
        holder.location.text = currentItem.place

        holder.linear.setOnClickListener{
            val intent = Intent(context, EventDetails::class.java)
            intent.putExtra("ID", currentItem.userID)
            intent.putExtra("eventID", currentItem.eventID)
            context.startActivity(intent)
        }

        val storageReference = FirebaseStorage.getInstance().getReference()
        val profileRef = storageReference.child("users/"+currentItem.hostID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(holder.hostProfile)
        }

        var flag = false
        holder.cancel.setOnClickListener {
            if (flag){
                Toast.makeText(context, "You have already left.", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseFirestore.getInstance().collection("eventDetails")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val participantID = document.data["participantID"]
                            val eventIDV = document.data["eventID"]
                            if (eventIDV.toString().equals(newsList[position].eventID) && participantID == userID){
                                FirebaseFirestore.getInstance().collection("eventDetails").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "You have left from event successfully!", Toast.LENGTH_SHORT).show()
                                        holder.date.text = currentItem.date + "\n(LEFT)"
                                        val x = newsList[position].current.toInt() - 1
                                        FirebaseFirestore.getInstance().collection("events").document(newsList[position].eventID).update("current", x.toString())
                                    }
                                    .addOnFailureListener { Toast.makeText(context, "You have already left.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }
            }
            flag = true
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val date : TextView = itemView.findViewById(R.id.date)
        val title : TextView = itemView.findViewById(R.id.title)
        val description : TextView = itemView.findViewById(R.id.description)
        val time : TextView = itemView.findViewById(R.id.time)
        val location : TextView = itemView.findViewById(R.id.location)
        val hostProfile: ImageView = itemView.findViewById(R.id.hostProfile)
        val cancel: ImageView = itemView.findViewById(R.id.cancel)

        val linear: LinearLayout = itemView.findViewById(R.id.linear)

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
