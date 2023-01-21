package com.example.hangout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class User : AppCompatActivity() {
    private lateinit var ProfilePhoto: ImageView
    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var mail: TextView
    private lateinit var back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val userID = intent.getStringExtra("ID")
        val profileID = intent.getStringExtra("profileID")

        ProfilePhoto = findViewById<ImageView>(R.id.ProfilePhoto)
        name = findViewById<TextView>(R.id.name)
        description = findViewById<TextView>(R.id.description)
        mail = findViewById<TextView>(R.id.mail)
        back = findViewById<Button>(R.id.back)

        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }

        val storageReference = FirebaseStorage.getInstance().getReference()
        val profileRef = storageReference.child("users/"+profileID+"/profile.jpg")
        profileRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(ProfilePhoto)
        }

        val docRef = FirebaseFirestore.getInstance().collection("users").document(profileID!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val nameV = document.getString("name")
                    val about = document.getString("about")
                    val mailV = document.getString("email")
                    name.text = nameV.toString()
                    description.text = about
                    mail.text = mailV
                } else {
                    Log.d("LOGGER", "No such document")
                }
            } else {
                Log.d("LOGGER", "get failed with ", task.exception)
            }
        }
    }
}