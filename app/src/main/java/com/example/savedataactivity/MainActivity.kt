package com.example.savedataactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSave: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.add_name)
        etAddress = findViewById(R.id.add_adress)
        etEmail = findViewById(R.id.add_email)
        etPassword = findViewById(R.id.add_password)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            saveDataToFirestore()
        }
    }

    private fun saveDataToFirestore() {
        val name = etName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = hashMapOf(
            "name" to name,
            "address" to address,
            "email" to email,
            "password" to password
        )

        // kullanıcının UID'sini alma
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password) //firebase kütüphanesinin fonksiyonu
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val newUser = FirebaseAuth.getInstance().currentUser
                        val userId = newUser?.uid

                        userId?.let {
                            db.collection("users").document(it)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Veri başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                                    etName.text.clear()
                                    etAddress.text.clear()
                                    etEmail.text.clear()
                                    etPassword.text.clear()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Veri ekleme hatası", e)
                                    Toast.makeText(this, "Veri ekleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Kullanıcı oluşturma hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else {
            val userId = user.uid

            db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Veri başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                    etName.text.clear()
                    etAddress.text.clear()
                    etEmail.text.clear()
                    etPassword.text.clear()
                }
                .addOnFailureListener { e -> // burayı tekrar arastır!!
                    Log.e("Firestore", "Veri ekleme hatası", e)
                    Toast.makeText(this, "Veri ekleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
