package com.example.hangout

import android.content.ContentValues.TAG
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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var edtcreatebutton : Button

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

        edtcreatebutton=findViewById<Button>(R.id.button4)
        edtcreatebutton.setOnClickListener()
        {
            val intent = Intent(this, createEvent::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }
    }
}
