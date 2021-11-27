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
import com.josecarlos.maletavirtual.utils.Utils.Companion.esCorreoValido
import com.josecarlos.maletavirtual.utils.Utils.Companion.toast
import com.josecarlos.maletavirtual.utils.Utils.Companion.validar
import com.josecarlos.maletavirtual.databinding.ActivityForgotPasswordBinding

class OlvidoContrasenaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_forgot_password)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.editTextEmail.validar {
            binding.editTextEmail.error = if (esCorreoValido(it)) null else "Correo electrónico no válido"
        }

        binding.buttonGoLogIn.setOnClickListener {
            abrirActivity<AutenticacionActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.buttonForgot.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (esCorreoValido(email)) {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this) {
                    toast("Enviado correo electrónco para reestablecer contraseña.")
                    abrirActivity<AutenticacionActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                toast("Asegúrate de que el correo electrónco es correcto !")
            }
        }
    }
}

