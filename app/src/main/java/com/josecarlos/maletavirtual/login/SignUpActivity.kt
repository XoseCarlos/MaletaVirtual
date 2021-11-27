/* *******************************************
Alumno: José Carlos Vázquez Míguez
Mail: xosecarlos@mundo-r.com
Centro: ILERNA ONLINE
Ciclo: DAM
Curso: 2021-2022 (1º semestre)
Proyecto: Maleta Virtual
Tutor: Mario Gago
Fecha última revisión: 27/11/2021
Revisión: 4.3
**********************************************
*/

package com.josecarlos.maletavirtual.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.josecarlos.maletavirtual.utils.Utils.Companion.abrirActivity
import com.josecarlos.maletavirtual.utils.Utils.Companion.esCorrectaConfirmacionContrasena
import com.josecarlos.maletavirtual.utils.Utils.Companion.esCorreoValido
import com.josecarlos.maletavirtual.utils.Utils.Companion.esContrasenaValida_2
import com.josecarlos.maletavirtual.utils.Utils.Companion.toast
import com.josecarlos.maletavirtual.utils.Utils.Companion.validar
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
            abrirActivity<AutenticacionActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.buttonSignUp.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            //if (isValidEmail(email) && isValidPassword(password) && isValidConfirmPassword(password, confirmPassword)) {
            if (esCorreoValido(email) && esCorrectaConfirmacionContrasena(password, confirmPassword)) {
                signUpByEmail(email, password)
            } else {
                toast("Comprueba que los datos son correctos.")
            }
        }

        binding.editTextEmail.validar {
            binding.editTextEmail.error = if (esCorreoValido(it)) null else "Correo electrónico no válido"
        }

        binding.editTextPassword.validar {
            binding.editTextPassword.error = if (esContrasenaValida_2(it)) null else "La contraseña debe contener 1 minúscula, 1 mayúscula, 1 número, 1 carácter especial y al menos 8 caracteres de longitud"
        }

        binding.editTextConfirmPassword.validar {
            binding.editTextConfirmPassword.error = if (esCorrectaConfirmacionContrasena(binding.editTextPassword.text.toString(), it)) null else "No concuerdan los datos del correo confirmado"
        }

    }

    private fun signUpByEmail(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this) {
                    toast("Se le envió un correo electrónico. Confirme antes de autenticarse.")

                    abrirActivity<AutenticacionActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                toast("Error inesperado. Vuelva a intentarlo!")
            }
        }
    }
}
