package com.example.mobileproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: Button
    private lateinit var forgetBtn: Button

    companion object {
        const val preferenceNameText: String = "CREDENTIALS"
        const val emailText: String = "EMAIL"
        const val passwordText: String = "PASSWORD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI components
        emailET = findViewById(R.id.emailET)
        passwordET = findViewById(R.id.passwordET)
        loginBtn = findViewById(R.id.LoginBtn)
        signupBtn = findViewById(R.id.SignupBtn)
        forgetBtn = findViewById(R.id.forgetPasswordBtn)

        retrieveCredentialsAndSetData()

        loginBtn.setOnClickListener {
            val emailText = emailET.text.toString()
            val passwordText = passwordET.text.toString()
            val rememberCheckBox: CheckBox = findViewById(R.id.checkbox)

            if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                if (rememberCheckBox.isChecked) {
                    storeCredentials(emailText, passwordText)
                }
                authenticateUser(emailText, passwordText)
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        signupBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        forgetBtn.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun storeCredentials(email: String, password: String) {
        val sharedPreference = getSharedPreferences(preferenceNameText, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString(emailText, email)
        editor.putString(passwordText, password)
        editor.apply()
        Log.d("LoginActivity", "Stored credentials: Email - $email")
    }

    private fun retrieveCredentialsAndSetData() {
        val sharedPreference = getSharedPreferences(preferenceNameText, Context.MODE_PRIVATE)
        val email = sharedPreference.getString(emailText, "")
        val password = sharedPreference.getString(passwordText, "")

        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            emailET.setText(email)
            passwordET.setText(password)
        }
    }

    private fun authenticateUser(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isEmailVerified = user?.isEmailVerified == true

                    if (isEmailVerified) {
                        // Fetch and print ID token
                        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                Log.d("Login", "ID Token: $idToken") // Print ID token to Logcat
                            } else {
                                Log.e("Login", "Error fetching ID token", tokenTask.exception)
                            }
                        }

                        Log.d("Login", "${user?.email} successfully logged in.")
                        Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, StartActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please verify your email.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Login", "Authentication failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}