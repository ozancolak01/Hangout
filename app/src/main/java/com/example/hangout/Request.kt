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
import com.example.recylerviewkotlin.MyEventAdapter
import com.example.recylerviewkotlin.MyRequestAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class Request : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var name: TextView

    private lateinit var newRecylerview : RecyclerView

    private var context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)
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

        newRecylerview = findViewById(R.id.rcView2)
        newRecylerview.layoutManager = LinearLayoutManager(this)
        newRecylerview.setHasFixedSize(true)
        findRequestedEvents(userID!!)
    }

    private fun findRequestedEvents(userID: String) {
        val arr = ArrayList<String>()
        val par = ArrayList<String>()
        FirebaseFirestore.getInstance().collection("requests")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val hostID = document.data["hostID"]
                    val eventID = document.data["eventID"]
                    val participantID = document.data["participantID"]
                    if (hostID.toString().equals(userID)){
                        arr.add(eventID.toString())
                        par.add(participantID.toString())
                    }
                }

                val adapter = MyRequestAdapter(arr, context, par, userID)

                newRecylerview.adapter = adapter
                adapter.setOnItemClickListener(object : MyRequestAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

}