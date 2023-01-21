package com.example.hangout

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.*

class createEvent : AppCompatActivity() {
    private lateinit var edtTitle: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtTime: EditText
    private lateinit var edtParNumber: EditText
    private lateinit var edtDescription: EditText
    private lateinit var switchFlag: Switch
    private lateinit var finishEvents: Button
    private lateinit var spinnerC: Spinner
    private lateinit var spinnerP: Spinner
    private lateinit var backPage: ImageView

    private val categories = arrayOf("Sport", "Board Game", "Talk", "Reading", "Watching")
    private val places = arrayOf("Computer Engineering Building", "Library", "SKS", "Kelebek", "Molecular Biology Building", "Material Engineering Building", "Running Track")
    private var selectedCategory = categories[0]
    private var selectedPlace = places[0]

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        val userID = intent.getStringExtra("ID")

        edtTitle=findViewById<EditText>(R.id.edt_title)
        edtDate=findViewById<EditText>(R.id.edt_date)
        edtTime=findViewById<EditText>(R.id.edt_time)
        edtParNumber=findViewById<EditText>(R.id.edt_par_number)
        edtDescription=findViewById<EditText>(R.id.edt_description)
        finishEvents=findViewById<Button>(R.id.btnFinish)
        backPage=findViewById(R.id.backPage)

        switchFlag=findViewById<Switch>(R.id.SW)
        var message: String = "Public"
        switchFlag.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                message = "Private"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            else{
                message = "Public"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        spinnerC=findViewById<Spinner>(R.id.spinnerC)
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerC.adapter = arrayAdapter
        spinnerC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedCategory = categories[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        spinnerP=findViewById<Spinner>(R.id.spinnerP)
        val arrayAdapter2 = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, places)
        spinnerP.adapter = arrayAdapter2
        spinnerP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedPlace = places[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        finishEvents.setOnClickListener {
            val title=edtTitle.text.toString()
            val date = edtDate.text.toString()
            val time = edtTime.text.toString()
            val parnumber=edtParNumber.text.toString()
            val description=edtDescription.text.toString()

            try {
                var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                var dateF = LocalDate.parse(date, formatter)
                val part = parnumber.toInt()
                if (dateF.year != 2023){
                    Toast.makeText(this, "Wrong date.", Toast.LENGTH_SHORT).show()
                }
                else if (dateF.month == Month.JANUARY && dateF.dayOfMonth < 9){
                    Toast.makeText(this, "Wrong date.", Toast.LENGTH_SHORT).show()
                }
                else if (!checkTime(time)){
                    Toast.makeText(this, "Wrong time.", Toast.LENGTH_SHORT).show()
                }
                else if (part <= 0){
                    Toast.makeText(this, "Wrong participant number.", Toast.LENGTH_SHORT).show()
                }
                else{
                    dateConflict(date, time, userID!!, title, parnumber,description,message, "eventDetails")
                }
            }
            catch (e: Exception){
                Toast.makeText(this, "Wrong number.", Toast.LENGTH_SHORT).show()
            }
        }

        backPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }
    }

    private fun dateConflict(date: String, time: String, userID: String, title: String, parnumber: String, description: String, message: String, colName: String) {
        val arr = ArrayList<String>()
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
                                    Toast.makeText(this, "You have another event at the same time.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.d("LOGGER", "No such document")
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.exception)
                        }


                        if (!flag && arr.indexOf(element) == arr.size - 1 && colName == "eventDetails"){
                            checkCreateEvents(date, time, userID!!, title, parnumber,description,message)
                        }
                        else if (!flag && arr.indexOf(element) == arr.size - 1 && colName == "requests"){
                            createEvent(title,date,time,parnumber,description,message,userID!!)
                        }
                    }

                }
                if (arr.size == 0 && colName == "eventDetails"){
                    checkCreateEvents(date, time, userID!!, title, parnumber,description,message)
                }
                else if (arr.size == 0 && colName == "requests"){
                    createEvent(title,date,time,parnumber,description,message,userID!!)
                }

            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkCreateEvents(date: String, time: String, userID: String, title: String, parnumber: String, description: String, message: String) {
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
                        Toast.makeText(this, "You have created an event at the same time before.", Toast.LENGTH_SHORT).show()
                    }
                    else if (!flag && result.indexOf(document) == result.size() - 1){
                        dateConflict(date, time, userID!!, title, parnumber,description,message, "requests")
                    }
                }
                if (result.size() == 0){
                    dateConflict(date, time, userID!!, title, parnumber,description,message, "requests")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkTime(timeValue: String): Boolean{
        if (timeValue.length != 5 || timeValue[2] != ':'){
            return false
        }
        else{
            val hour = timeValue.take(2)
            val minute = timeValue.takeLast(2)

            if (hour.toInt() > 23 || hour.toInt() < 0 || minute.toInt() < 0 || minute.toInt() > 59){
                return false
            }
        }
        return true
    }

    private fun createEvent(title: String, date: String, time: String,parnumber:String,description:String,message:String,userID:String) {
        if (title.length == 0 || date.length == 0 || time.length == 0 || parnumber.length == 0 || description.length == 0){
            Toast.makeText(this, "Inputs cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (parnumber.toInt() <= 0){
            Toast.makeText(this, "Invalid participant numbers.", Toast.LENGTH_SHORT).show()
            return
        }
        val eventID: String = UUID.randomUUID().toString()
        val documentReference:DocumentReference = FirebaseFirestore.getInstance().collection("events").document(eventID)
        val events: HashMap<String, String> = HashMap<String, String>()
        events.put("eventID", eventID)
        events.put("hostID", userID)
        events.put("title", title)
        events.put("date", date)
        events.put("time", time)
        events.put("category", selectedCategory)
        events.put("place", selectedPlace)
        events.put("current", "0")
        events.put("parnumber", parnumber)
        events.put("description", description)
        events.put("private", message)


        documentReference.set(events).addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
         .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ID", userID)
        startActivity(intent)
    }
}