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
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.josecarlos.maletavirtual.MainActivity
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.utils.Utils.Companion.abrirActivity
import com.josecarlos.maletavirtual.utils.Utils.Companion.esCorreoValido
import com.josecarlos.maletavirtual.utils.Utils.Companion.esContrasenaValida_2
import com.josecarlos.maletavirtual.utils.Utils.Companion.toast
import com.josecarlos.maletavirtual.utils.Utils.Companion.validar
import com.josecarlos.maletavirtual.databinding.ActivityLoginBinding

class AutenticacionActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityLoginBinding
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mGoogleApiClient: GoogleApiClient by lazy { getGoogleApiClient() }
    private val RC_GOOGLE_SIGN_IN = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogIn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (esCorreoValido(email) && esContrasenaValida_2(password)) {
                logInByEmail(email, password)
            } else {
                //toast("Comprueba que los datos son correctos.")
            }
        }

        binding.textViewForgotPassword.setOnClickListener {
            abrirActivity<OlvidoContrasenaActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        binding.buttonCreateAccount.setOnClickListener {
            abrirActivity<SignUpActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        binding.buttonLogInGoogle.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        binding.editTextEmail.validar {
            binding.editTextEmail.error = if (esCorreoValido(it)) null else "Correo Electrónico no válido"
        }

        binding.editTextPassword.validar {
            binding.editTextPassword.error = if (esContrasenaValida_2(it)) null else getString(R.string.contenido_clave)
        }

    }

    private fun getGoogleApiClient(): GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        return GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun loginByGoogleAccountIntoFirebase(googleAccount: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (mGoogleApiClient.isConnected) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            abrirActivity<MainActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }

    private fun logInByEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                if (mAuth.currentUser!!.isEmailVerified) {

                    abrirActivity<MainActivity> {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                } else {
                    toast("Confirmar el correo electrónico e primer lugar")
                }
            } else {
                toast("Error inesperado, intente otra vez!")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                loginByGoogleAccountIntoFirebase(account!!)
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        toast("Fallo de conexión!")
    }
}