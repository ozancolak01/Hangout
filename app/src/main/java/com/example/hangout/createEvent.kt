package com.example.hangout

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class createEvent : AppCompatActivity() {
    private lateinit var edtTitle: EditText
    private lateinit var edtCategory: EditText
    private lateinit var edtPlace: EditText
    private lateinit var edtParNumber: EditText
    private lateinit var edtDescription: EditText
    private lateinit var finishEvents: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        val userID = intent.getStringExtra("ID")

        edtTitle=findViewById<EditText>(R.id.edt_title)
        edtCategory=findViewById<EditText>(R.id.edt_category)
        edtPlace=findViewById<EditText>(R.id.edt_place)
        edtParNumber=findViewById<EditText>(R.id.edt_par_number)
        edtDescription=findViewById<EditText>(R.id.edt_description)
        finishEvents=findViewById<Button>(R.id.btnFinish)

        finishEvents.setOnClickListener {
            val title=edtTitle.text.toString()
            val category=edtCategory.text.toString()
            val place=edtPlace.text.toString()
            val parnumber=edtParNumber.text.toString()
            val description=edtDescription.text.toString()
            createEvent(title,category,place,parnumber,description,userID!!)
        }
    }
    private fun createEvent(title: String, category: String, place: String,parnumber:String,description:String,userID:String) {
        val eventID: String = UUID.randomUUID().toString()
        val documentReference:DocumentReference = FirebaseFirestore.getInstance().collection("events").document(eventID)
        val events: HashMap<String, String> = HashMap<String, String>()
        events.put("eventID", eventID)
        events.put("hostID", userID)
        events.put("title", title)
        events.put("category", category)
        events.put("place", place)
        events.put("current", "0")
        events.put("parnumber", parnumber)
        events.put("description", description)


        documentReference.set(events).addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
         .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ID", userID)
        startActivity(intent)
    }
}