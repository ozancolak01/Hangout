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
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.hangout.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class MyCreatedAdapter(private val newsList : ArrayList<Events>, private val context: Context) : RecyclerView.Adapter<MyCreatedAdapter.MyViewHolder>(),Filterable {

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
            R.layout.cards_events_created,
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
            val intent = Intent(context, EventCreatorDetails::class.java)
            intent.putExtra("ID", currentItem.userID)
            intent.putExtra("eventID", currentItem.eventID)
            context.startActivity(intent)
        }

        var flag = false
        holder.cancel.setOnClickListener {
            //TO BE IMPLEMENTED
            //eventdetails'daki eventID tutuyorsa sil
            //requestlerdeki eventID tutuyorsa sil
            //events'ten eventID tutuyorsa sil

            if (flag){
                Toast.makeText(context, "Event has already been cancelled.", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseFirestore.getInstance().collection("eventDetails")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            if (eventIDV.toString().equals(newsList[position].eventID)){
                                FirebaseFirestore.getInstance().collection("eventDetails").document(document.id)
                                    .delete()
                                    .addOnSuccessListener { }
                                    .addOnFailureListener { Toast.makeText(context, "ERROR.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }

                FirebaseFirestore.getInstance().collection("requests")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            if (eventIDV.toString().equals(newsList[position].eventID)){
                                FirebaseFirestore.getInstance().collection("requests").document(document.id)
                                    .delete()
                                    .addOnSuccessListener { }
                                    .addOnFailureListener { Toast.makeText(context, "ERROR.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }

                FirebaseFirestore.getInstance().collection("events")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            if (eventIDV.toString().equals(newsList[position].eventID)){
                                FirebaseFirestore.getInstance().collection("events").document(document.id)
                                    .delete()
                                    .addOnSuccessListener { }
                                    .addOnFailureListener { Toast.makeText(context, "ERROR.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }

                Toast.makeText(context, "Event has been cancelled successfully!", Toast.LENGTH_SHORT).show()
                holder.date.text = currentItem.date + "\n(CANCELLED)"
            }


            flag = true
        }
    }

    private fun deleteFrom(holder: MyViewHolder, position: Int, colName: String) {

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
