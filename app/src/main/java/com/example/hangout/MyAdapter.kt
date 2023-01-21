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
import com.example.hangout.EventDetails
import com.example.hangout.Events
import com.example.hangout.MainActivity
import com.example.hangout.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class MyAdapter(private val newsList : ArrayList<Events>, private val context: Context, private val userID: String) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(),Filterable {

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
            R.layout.item,
            parent,false)

        return MyViewHolder(itemView,mListener)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var flag = false

        val currentItem = newsList[position]
        //holder.image.setImageResource(R.drawable.person)
        holder.title.text = currentItem.title
        holder.category.text = currentItem.category
        holder.participants.text = currentItem.current + "/" + currentItem.parnumber
        holder.date.text = currentItem.date + " - " + currentItem.time

        holder.btnInfo.setOnClickListener{
            val intent = Intent(context, EventDetails::class.java)
            intent.putExtra("ID", currentItem.userID)
            intent.putExtra("eventID", currentItem.eventID)
            context.startActivity(intent)
        }

        var flagReq = true
        FirebaseFirestore.getInstance().collection("requests")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val participantID = document.data["participantID"]
                    val eventID = document.data["eventID"]
                    if (participantID.toString().equals(currentItem.userID) && eventID.toString().equals(currentItem.eventID)){
                        flagReq = false
                        break
                    }
                }
                if (currentItem.private == "Public"){
                    holder.btnAttend.text = "Attend"
                }
                else if (!flagReq){
                    holder.btnAttend.text = "Requested"
                }
                else{
                    holder.btnAttend.text = "Request"
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }

        val storageReference = FirebaseStorage.getInstance().getReference()
        val profileRef = storageReference.child("users/"+currentItem.hostID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(holder.profileImage)
        }

        holder.btnAttend.setOnClickListener {
            if (holder.btnAttend.text == "Requested"){
                Toast.makeText(context, "Request has already been sent.", Toast.LENGTH_SHORT).show()
            }
            else if (currentItem.private.equals("Public")){

                if (!flag) {
                    dateConflict(holder, position, currentItem.date, currentItem.time, userID, "eventDetails", "attend")
                }
                else if (holder.btnAttend.text == "Attended") {
                    Toast.makeText(context, "You are already attended.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context, "There is a conflict.", Toast.LENGTH_SHORT).show()
                }
                flag = true
            }
            else{

                if (!flag){
                    dateConflict(holder, position, currentItem.date, currentItem.time, userID, "eventDetails", "request")
                }
                else{
                    Toast.makeText(context, "There is a conflict.", Toast.LENGTH_SHORT).show()
                }
                flag = true
            }
        }
    }

    private fun requestEvent(holder: MyViewHolder, position: Int) {
        holder.btnAttend.text = "Requested"
        Toast.makeText(context, "Request has been sent!", Toast.LENGTH_SHORT).show()
        val documentID: String = UUID.randomUUID().toString()
        val documentReference: DocumentReference = FirebaseFirestore.getInstance().collection("requests").document(documentID)

        val requester: HashMap<String, String> = HashMap<String, String>()
        requester.put("eventID", newsList[position].eventID)
        requester.put("participantID", newsList[position].userID)
        requester.put("hostID", newsList[position].hostID)
        documentReference.set(requester).addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }
    }

    private fun dateConflict(holder: MyViewHolder, position: Int, date: String, time: String, userID: String, colName: String, type: String) {
        val arr = java.util.ArrayList<String>()
        var flag = false
        FirebaseFirestore.getInstance().collection(colName)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val participantID = document.data["participantID"]
                    val eventID = document.data["eventID"]
                    if (participantID.toString().equals(userID)){
                        arr.add(eventID.toString())
                    }
                }
                for (element in arr){
                    val docRef: DocumentReference = FirebaseFirestore.getInstance().collection("events").document(element)

                    docRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null) {
                                val dateV = document.getString("date")
                                val timeV = document.getString("time")

                                if (dateV == date && timeV == time){
                                    flag = true
                                    Toast.makeText(context, "You have joined another event at the same time.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.d("LOGGER", "No such document")
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.exception)
                        }

                        if (!flag && arr.indexOf(element) == arr.size - 1 && colName == "eventDetails"){
                            checkCreateEvents(holder, position, date, time, userID, type)
                        }
                        else if (!flag && arr.indexOf(element) == arr.size - 1 && colName == "requests" && type == "attend"){
                            attendEvent(holder, position)
                        }
                        else if (!flag && arr.indexOf(element) == arr.size - 1 && colName == "requests" && type == "request"){
                            requestEvent(holder, position)
                        }
                    }
                }
                if (arr.size == 0 && colName == "eventDetails"){
                    checkCreateEvents(holder, position, date, time, userID, type)
                }
                else if (arr.size == 0 && colName == "requests" && type == "attend"){
                    attendEvent(holder, position)
                }
                else if (arr.size == 0 && colName == "requests" && type == "request"){
                    requestEvent(holder, position)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkCreateEvents(holder: MyViewHolder, position: Int, date: String, time: String, userID: String, type: String) {
        var flag = false
        FirebaseFirestore.getInstance().collection("events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val hostIDV = document.data["hostID"]
                    val dateV = document.data["date"]
                    val timeV = document.data["time"]
                    if (hostIDV.toString().equals(userID) && dateV == date && timeV == time){
                        flag = true
                        Toast.makeText(context, "You have created an event at the same time before.", Toast.LENGTH_SHORT).show()
                    }
                    else if (!flag && result.indexOf(document) == result.size() - 1){
                        dateConflict(holder, position, date, time, userID, "requests", type)
                    }
                }
                if (result.size() == 0){
                    dateConflict(holder, position, date, time, userID, "requests", type)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun attendEvent(holder: MyViewHolder, position: Int) {
        holder.btnAttend.text = "Attended"
        val currentItem = newsList[position]
        Toast.makeText(context, "You are attended!", Toast.LENGTH_SHORT).show()

        val documentID: String = UUID.randomUUID().toString()
        val documentReference: DocumentReference = FirebaseFirestore.getInstance().collection("eventDetails").document(documentID)
        val attendee: HashMap<String, String> = HashMap<String, String>()
        attendee.put("eventID", newsList[position].eventID)
        attendee.put("participantID", newsList[position].userID)
        documentReference.set(attendee).addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

        val x = newsList[position].current.toInt() + 1
        FirebaseFirestore.getInstance().collection("events").document(newsList[position].eventID).update("current", x.toString())
        holder.participants.text=x.toString()+"/"+currentItem.parnumber
    }


    override fun getItemCount(): Int {

        return newsList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val image : ImageView = itemView.findViewById(R.id.eventImage)
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val title : TextView = itemView.findViewById(R.id.title)
        val category : TextView = itemView.findViewById(R.id.category)
        val participants : TextView = itemView.findViewById(R.id.goings)
        val date : TextView = itemView.findViewById(R.id.date)
        val btnAttend: Button = itemView.findViewById<Button>(R.id.btnAttend)
        val btnInfo: Button = itemView.findViewById(R.id.btnInfo)

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
