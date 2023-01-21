package com.example.hangout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class Profile : AppCompatActivity() {

    private lateinit var imageView2: ImageView
    private lateinit var ProfilePhoto: ImageView
    private lateinit var textView2: TextView
    private lateinit var textView: TextView
    private lateinit var mail: TextView
    private lateinit var secondActivityButton: Button
    private lateinit var EventsCreatedBtn: Button
    private lateinit var RequestBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userID = intent.getStringExtra("ID")

        imageView2 = findViewById<ImageView>(R.id.imageView2)
        ProfilePhoto = findViewById<ImageView>(R.id.ProfilePhoto)
        textView2 = findViewById<TextView>(R.id.textView2)
        textView = findViewById<TextView>(R.id.textView)
        mail = findViewById<TextView>(R.id.mail)
        secondActivityButton = findViewById<Button>(R.id.secondActivityButton)
        EventsCreatedBtn = findViewById<Button>(R.id.EventsCreatedBtn)
        RequestBtn = findViewById<Button>(R.id.RequestBtn)

        val storageReference = FirebaseStorage.getInstance().getReference()
        val profileRef = storageReference.child("users/"+userID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(ProfilePhoto)
        }

        imageView2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        val docRef = FirebaseFirestore.getInstance().collection("users").document(userID!!)

        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val name = document.getString("name")
                    val about = document.getString("about")
                    val mailV = document.getString("email")
                    textView2.text = name
                    textView.text = about
                    mail.text = mailV
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }

        secondActivityButton.setOnClickListener{
            val intent = Intent(this, EventsAttended::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        EventsCreatedBtn.setOnClickListener{
            val intent = Intent(this, EventsCreated::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        RequestBtn.setOnClickListener{
            val intent = Intent(this, Request::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }
    }
}