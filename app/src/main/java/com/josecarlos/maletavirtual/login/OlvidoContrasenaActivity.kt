package com.josecarlos.maletavirtual.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.josecarlos.maletavirtual.Utils
import com.josecarlos.maletavirtual.Utils.Companion.goToActivity
import com.josecarlos.maletavirtual.Utils.Companion.isValidEmail
import com.josecarlos.maletavirtual.Utils.Companion.toast
import com.josecarlos.maletavirtual.Utils.Companion.validate
import com.josecarlos.maletavirtual.databinding.ActivityForgotPasswordBinding

class OlvidoContrasenaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_forgot_password)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.editTextEmail.validate {
            binding.editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        binding.buttonGoLogIn.setOnClickListener {
            goToActivity<AutenticacionActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.buttonForgot.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (isValidEmail(email)) {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this) {
                    toast("Email has been sent to reset your password.")
                    goToActivity<AutenticacionActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                toast("Please make sure the email address is correct.")
            }
        }
    }
}

