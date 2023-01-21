package com.example.hangout

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recylerviewkotlin.MyCreatedAdapter
import com.example.recylerviewkotlin.MyEventAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EventsCreated : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var name: TextView

    private lateinit var events: ArrayList<Events>
    private lateinit var tempArrayList : ArrayList<Events>
    private lateinit var newRecylerview : RecyclerView

    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_created)
        val userID = intent.getStringExtra("ID")

        backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        val storageReference = FirebaseStorage.getInstance().getReference()
        profileImage = findViewById<ImageView>(R.id.profile)
        val profileRef = storageReference.child("users/"+userID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(profileImage)
        }

        name = findViewById(R.id.name)
        val docRef = FirebaseFirestore.getInstance().collection("users").document(userID!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val nameV = document.getString("name")
                    name.text = nameV
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }

        events = arrayListOf<Events>()
        tempArrayList = arrayListOf<Events>()
        newRecylerview = findViewById(R.id.rcView2)
        newRecylerview.layoutManager = LinearLayoutManager(this)
        newRecylerview.setHasFixedSize(true)
        findCreatedEvents(userID!!)
    }

    private fun findCreatedEvents(userID: String){
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

                    if (temp.hostID.equals(userID)){
                        events.add(temp)
                    }

                }
                tempArrayList = events
                val adapter = MyCreatedAdapter(tempArrayList, context)

                newRecylerview.adapter = adapter
                adapter.setOnItemClickListener(object : MyCreatedAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }


    }
}