package com.example.mobileproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val loginBtn = findViewById<Button>(R.id.LoginBtn)
        val signupBtn = findViewById<Button>(R.id.SignupBtn)

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupBtn.setOnClickListener(View.OnClickListener {
            val emailEditText: EditText = findViewById(R.id.emailET)
            val passwordEditText: EditText = findViewById(R.id.passwordET)
            val usernameEditText: EditText = findViewById(R.id.nameET)

            val emailText = emailEditText.text.toString().trim()
            val passwordText = passwordEditText.text.toString().trim()
            val usernameText = usernameEditText.text.toString().trim()

            if (emailText.isNotEmpty() && passwordText.isNotEmpty() && usernameText.isNotEmpty()) {
                registerUser(emailText, passwordText, usernameText)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerUser(email: String, password: String, username: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser

                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "Please verify your email", Toast.LENGTH_SHORT).show()
                            Log.d("Registration", "${user.email} successfully Registered.")

                            user?.let {
                                // Create AddUserRequest and call backend API with the Firebase UID
                                val addUserRequest = AddUserRequest(
                                    id = it.uid,
                                    username = username,
                                    email = email,
                                    password = password
                                )

                                RetrofitClient.backendService.createUser(addUserRequest)
                                    .enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                                                finish()
                                            } else {
                                                Toast.makeText(this@SignUpActivity, "Failed to add user.", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Log.e("API Error", "Failed to add user. Response code: ${t.message}")

                                            Toast.makeText(this@SignUpActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Error verifying email", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("Registration", "Registration failure", task.exception)
                    Toast.makeText(this@SignUpActivity, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
