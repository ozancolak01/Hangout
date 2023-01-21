package com.example.hangout

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recylerviewkotlin.MyCreatedAdapter
import com.example.recylerviewkotlin.MyDetailAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EventDetails : AppCompatActivity() {
    private lateinit var profile: ImageView
    private lateinit var title: TextView
    private lateinit var creator: TextView
    private lateinit var time: TextView
    private lateinit var category: TextView
    private lateinit var description: TextView
    private lateinit var backPage: ImageView
    private lateinit var homePage: ImageView

    private lateinit var attendeesIDs: ArrayList<String>
    private lateinit var tempArrayList : ArrayList<String>
    private lateinit var newRecylerview : RecyclerView
    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        val userID = intent.getStringExtra("ID")
        val eventID = intent.getStringExtra("eventID")

        val storageReference = FirebaseStorage.getInstance().getReference()
        val profileRef = storageReference.child("users/"+userID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(profile)
        }

        backPage = findViewById(R.id.backPage)
        backPage.setOnClickListener {
            val intent = Intent(this, EventsAttended::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        homePage = findViewById(R.id.homePage)
        homePage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        profile = findViewById(R.id.profile)
        title = findViewById(R.id.title)
        creator = findViewById(R.id.creator)
        time = findViewById(R.id.time)
        category = findViewById(R.id.category)
        description = findViewById(R.id.description)

        newRecylerview =findViewById(R.id.rcView)
        newRecylerview.layoutManager = LinearLayoutManager(this)
        newRecylerview.setHasFixedSize(true)
        attendeesIDs = arrayListOf<String>()
        tempArrayList = arrayListOf<String>()

        fillDetails(userID!!, eventID!!)

    }

    private fun findCreatedEvents(userID: String, eventID: String){
        FirebaseFirestore.getInstance().collection("eventDetails")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["eventID"].toString().equals(eventID)){
                        attendeesIDs.add(document.data["participantID"].toString())
                    }
                }
                tempArrayList = attendeesIDs
                val adapter = MyDetailAdapter(tempArrayList, context, userID)

                newRecylerview.adapter = adapter
                adapter.setOnItemClickListener(object : MyDetailAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun fillDetails(uID: String, eID: String){
        val docRef = FirebaseFirestore.getInstance().collection("events").document(eID)

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val titleV = document.getString("title")
                    val categoryV = document.getString("category")
                    val timeV = document.getString("time")
                    val descriptionV = document.getString("description")
                    title.text = titleV
                    category.text = categoryV
                    time.text = timeV
                    description.text = descriptionV

                    findCreator(document.getString("hostID")!!)
                    findCreatedEvents(uID, eID)
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }
    }

    private fun findCreator(ID: String) {
        val docRef = FirebaseFirestore.getInstance().collection("users").document(ID)

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val nameV = document.getString("name")
                    creator.text = nameV
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }
    }
}