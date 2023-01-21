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

class MyRequestAdapter(private val newsList : ArrayList<String>, private val context: Context, private val par : ArrayList<String>, private val userID: String) : RecyclerView.Adapter<MyRequestAdapter.MyViewHolder>(),Filterable {

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
            R.layout.card_request,
            parent,false)

        return MyViewHolder(itemView,mListener)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentEvent = newsList[position]
        val currentPar = par[position]

        holder.mail.setOnClickListener {
            val intent = Intent(context, User::class.java)
            intent.putExtra("ID", userID)
            intent.putExtra("profileID", currentPar)
            context.startActivity(intent)
        }

        FirebaseFirestore.getInstance().collection("events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.data["title"]
                    val eventID = document.data["eventID"]
                    if (eventID.toString().equals(currentEvent)){
                        holder.title.text = title.toString()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val email = document.data["email"]
                    val userIDV = document.data["userID"]
                    if (userIDV.toString().equals(currentPar)){
                        holder.mail.text = email.toString()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }

        var approvement = false
        holder.deny.setOnClickListener {
            //request listesinden currentPar'a göre participantID eşitse sil
            if (approvement){
                Toast.makeText(context, "Decision has already been made.", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseFirestore.getInstance().collection("requests")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            val participantID = document.data["participantID"]
                            if (eventIDV.toString().equals(currentEvent) && participantID == currentPar){
                                FirebaseFirestore.getInstance().collection("requests").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener { Toast.makeText(context, "ERROR.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }
                Toast.makeText(context, "User has been denied.", Toast.LENGTH_SHORT).show()
            }
            approvement = true
        }

        holder.approve.setOnClickListener {
            //request listesinden currentPar'a göre participantID eşitse sil
            //eventsin currentini 1 artır
            //eventdetailsa ekle
            if (approvement){
                Toast.makeText(context, "Decision has already been made.", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseFirestore.getInstance().collection("requests")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            val participantID = document.data["participantID"]
                            if (eventIDV.toString().equals(currentEvent) && participantID == currentPar){
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

                val documentID: String = UUID.randomUUID().toString()
                val documentReference: DocumentReference = FirebaseFirestore.getInstance().collection("eventDetails").document(documentID)
                val attendee: HashMap<String, String> = HashMap<String, String>()
                attendee.put("eventID", currentEvent)
                attendee.put("participantID", currentPar)
                documentReference.set(attendee).addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

                FirebaseFirestore.getInstance().collection("events")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val eventIDV = document.data["eventID"]
                            val current = document.data["current"]
                            if (eventIDV == currentEvent){
                                val x = current.toString().toInt() + 1
                                FirebaseFirestore.getInstance().collection("events").document(currentEvent).update("current", x.toString())
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                    }

                Toast.makeText(context, "User has been approved.", Toast.LENGTH_SHORT).show()
            }
            approvement = true
        }
    }


    private fun deleteFrom(holder: MyViewHolder, position: Int, colName: String) {

    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val mail : TextView = itemView.findViewById(R.id.mail)
        val title : TextView = itemView.findViewById(R.id.title)
        val deny: ImageView = itemView.findViewById(R.id.deny)
        val approve: ImageView = itemView.findViewById(R.id.approve)

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
