package com.josecarlos.maletavirtual.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.josecarlos.maletavirtual.Utils.Companion.goToActivity
import com.josecarlos.maletavirtual.Utils.Companion.isValidConfirmPassword
import com.josecarlos.maletavirtual.Utils.Companion.isValidEmail
import com.josecarlos.maletavirtual.Utils.Companion.isValidPassword
import com.josecarlos.maletavirtual.Utils.Companion.toast
import com.josecarlos.maletavirtual.Utils.Companion.validate
import com.josecarlos.maletavirtual.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_up)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonGoLogIn.setOnClickListener {
            goToActivity<AutenticacionActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.buttonSignUp.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            //if (isValidEmail(email) && isValidPassword(password) && isValidConfirmPassword(password, confirmPassword)) {
            if (isValidEmail(email) && isValidConfirmPassword(password, confirmPassword)) {
                signUpByEmail(email, password)
            } else {
                toast("Please make sure all the data is correct.")
            }
        }

        binding.editTextEmail.validate {
            binding.editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        binding.editTextPassword.validate {
            binding.editTextPassword.error = if (isValidPassword(it)) null else "Password should contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 4 characters length at least."
        }

        binding.editTextConfirmPassword.validate {
            binding.editTextConfirmPassword.error = if (isValidConfirmPassword(binding.editTextPassword.text.toString(), it)) null else "Confirm Password does not match with Password"
        }

    }

    private fun signUpByEmail(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this) {
                    toast("An email has been sent to you. Please, confirm before sign in.")

                    goToActivity<AutenticacionActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                toast("An unexpected error occurred, please try again.")
            }
        }
    }
}
