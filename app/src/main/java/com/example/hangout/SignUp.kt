package com.example.hangout

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.CallSuper
import android.support.annotation.Nullable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class SignUp : AppCompatActivity() {
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtAbout: EditText
    private lateinit var btnSignUp: Button

    private lateinit var mAuth: FirebaseAuth


    private lateinit var fStore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ImageView
    private lateinit var btnUpload: Button

    private var imageUri: Uri? = null

    private var uploadFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference()

        //TO SHOW WHATS STORED FORMERLY AT THE BEGINNING
        //val profileRef = storageReference.child("users/"+id+"/profile.jpg")
        //profileRef.downloadUrl.addOnSuccessListener { uri ->
        //    Picasso.get().load(uri).into(profileImage)
        //}

        profileImage = findViewById<ImageView>(R.id.app_logo)
        btnUpload = findViewById<Button>(R.id.btnUpload)

        edtAbout = findViewById<EditText>(R.id.edt_about)
        edtName = findViewById<EditText>(R.id.edt_name)
        edtEmail = findViewById<EditText>(R.id.edt_email)
        edtPassword = findViewById<EditText>(R.id.edt_password)
        btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val name = edtName.text.toString()
            val about = edtAbout.text.toString()

            signUp(email, password, name, about)
        }

        btnUpload.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(openGalleryIntent)
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            imageUri = data!!.getData()
            profileImage.setImageURI(imageUri)
            uploadFlag = true
        }
    }

    private fun uploadImageToFirebase(id:String) {
        val fileRef = storageReference.child("users/"+id+"/profile.jpg")
        fileRef.putFile(imageUri!!).addOnSuccessListener {
            Toast.makeText(this@SignUp, "Image Uploaded", Toast.LENGTH_SHORT).show()
            fileRef.getDownloadUrl().addOnSuccessListener{ uri ->
                Picasso.get().load(uri).into(profileImage)
            }
        }.addOnFailureListener {
            Toast.makeText(this@SignUp, "Some error occured", Toast.LENGTH_SHORT).show()
        }

    }


    private fun signUp(email: String, password: String, name: String, about: String){
        if (email.contains("@gtu.edu.tr") == false){
            Toast.makeText(this@SignUp, "You have to use gtu mail.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!uploadFlag){
            Toast.makeText(this@SignUp, "Please upload an image.", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.length == 0){
            Toast.makeText(this@SignUp, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6){
            Toast.makeText(this@SignUp, "Please enter stronger password.", Toast.LENGTH_SHORT).show()
            return
        }
        if (about.length < 1){
            Toast.makeText(this@SignUp, "Please write something about yourself.", Toast.LENGTH_SHORT).show()
            return
        }
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //jump to home
                    val userID:String = mAuth.getCurrentUser()!!.getUid()
                    uploadImageToFirebase(userID)
                    val documentReference:DocumentReference = FirebaseFirestore.getInstance().collection("users").document(userID)
                    val user: HashMap<String, String> = HashMap<String, String>()
                    user.put("userID", userID)
                    user.put("name", name)
                    user.put("email", email)
                    user.put("about", about)
                    documentReference.set(user).addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }



                    val intent = Intent(this@SignUp, LogIn::class.java)
                    intent.putExtra("ID", userID)
                    startActivity(intent)

                }
                else {
                    Toast.makeText(this@SignUp, "Some error occured", Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun uploadImage(){

    }
}

