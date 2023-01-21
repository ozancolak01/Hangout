package com.example.hangout

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hangout.databinding.ActivityMainBinding
import com.example.recylerviewkotlin.MyAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var edtcreatebutton : Button
    private lateinit var logoutbutton: Button

    private lateinit var events: ArrayList<Events>
    private lateinit var tempArrayList : ArrayList<Events>
    private lateinit var newRecylerview : RecyclerView

    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userID = intent.getStringExtra("ID")

        val storageReference = FirebaseStorage.getInstance().getReference()
        profileImage = findViewById<ImageView>(R.id.profile)
        val profileRef = storageReference.child("users/"+userID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(profileImage)
        }

        profileImage.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        newRecylerview =findViewById(R.id.rcView)
        newRecylerview.layoutManager = LinearLayoutManager(this)
        newRecylerview.setHasFixedSize(true)
        events = arrayListOf<Events>()
        tempArrayList = arrayListOf<Events>()


        findParticipated(userID!!)

        logoutbutton=findViewById<Button>(R.id.button)
        logoutbutton.setOnClickListener()
        {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        edtcreatebutton=findViewById<Button>(R.id.button4)
        edtcreatebutton.setOnClickListener()
        {
            val intent = Intent(this, createEvent::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }
    }

    private fun fillArray(userID: String, eventDetails: ArrayList<String>){
        FirebaseFirestore.getInstance().collection("events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val creatorID = document.data["hostID"]
                    val eventID = document.data["eventID"]
                    val title = document.data["title"]
                    val category = document.data["category"]
                    val location = document.data["place"]
                    val description = document.data["description"]
                    val currentParticipants = document.data["current"]
                    val participantNumber = document.data["parnumber"]
                    val private = document.data["private"]
                    val date = document.data["date"]
                    val time = document.data["time"]
                    val temp: Events = Events(date.toString(), time.toString(), creatorID.toString(), eventID.toString(), title.toString(), category.toString(), location.toString(), description.toString(), currentParticipants.toString(), participantNumber.toString(), private.toString(), userID)

                    if (!eventDetails.contains(eventID.toString()) && currentParticipants.toString().toInt() < participantNumber.toString().toInt() && !creatorID.toString().equals(userID)){
                        events.add(temp)
                    }

                }
                tempArrayList = events
                val adapter = MyAdapter(tempArrayList, context, userID)

                newRecylerview.adapter = adapter
                adapter.setOnItemClickListener(object : MyAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }


    }

    private fun findParticipated(userID: String){
        val arr = ArrayList<String>()
        FirebaseFirestore.getInstance().collection("eventDetails")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val participantID = document.data["participantID"]
                    val eventID = document.data["eventID"]
                    if (participantID.toString().equals(userID)){
                        arr.add(eventID.toString())
                    }
                }
                fillArray(userID, arr)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}
