/* *******************************************
Alumno: José Carlos Vázquez Míguez
Mail: xosecarlos@mundo-r.com
Centro: ILERNA ONLINE
Ciclo: DAM
Curso: 2021-2022 (1º semestre)
Proyecto: Maleta Virtual
Tutor: Mario Gago
Fecha última revisión: 15/11/2021
Revisión: 1.0
**********************************************
*/

package com.josecarlos.maletavirtual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.josecarlos.maletavirtual.databinding.ActivityMainBinding
import com.josecarlos.maletavirtual.models.Usuario
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //private lateinit var fireStoreListener: ListenerRegistration

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitle("Maleta Virutal")
        toolbar.setSubtitle(Utils.getAuth().currentUser?.displayName)


        binding.mainBotonActivas.setOnClickListener{
            //val intent = Intent(this,MaletasActivasActivity::class.java)
            val intent = Intent(this,MaletasActivity::class.java)
            intent.putExtra("Activas", true)
            startActivity(intent)
            //finish()
        }

        binding.mainBotonCerradas.setOnClickListener{
            //val intent = Intent(this,MaletasActivasActivity::class.java)
            //val intent = Intent(this,ArticulosActivity::class.java)
            val intent = Intent(this,MaletasActivity::class.java)
            intent.putExtra("Activas", false)
            startActivity(intent)
            //finish()
        }

        binding.actionSignOut.setOnClickListener{
            AuthUI.getInstance().signOut(this)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sesión terminada.", Toast.LENGTH_SHORT).show()
                    //finish()
                    //System.exit(0)
                }
        }
        binding.mainBotonCuenta.setOnClickListener{
            val intent = Intent(this,CuentaPersonalActivity::class.java)
            startActivity(intent)
            //finish()
        }
    }

    private fun usuarioRegistrado(uid: String): Boolean {
        var registrado : Boolean = false
        val db = FirebaseFirestore.getInstance().collection("usuarios")

        db.get().addOnSuccessListener { result ->
            for (document in result) {
                if ((document.id).equals(uid)) {
                    registrado = true
                }
            }
        }
            .addOnFailureListener{
                val usuario = Usuario(
                    id = firebaseAuth.currentUser!!.uid,
                    nombre = firebaseAuth.currentUser?.displayName,
                    emailUsuario = firebaseAuth.currentUser?.email
                )
                save(usuario)
                Toast.makeText(this, getString(R.string.usuario_guardado), Toast.LENGTH_SHORT).show()
            }
        return registrado
    }


    override fun onStart() {
        super.onStart()
        binding.textViewInfoName.text=firebaseAuth.currentUser?.displayName.toString()
        binding.textViewInfoEmail.text=firebaseAuth.currentUser?.email.toString()
        //firebaseAuth.signOut()
        val usuarioAct = Utils.getFirestore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
        usuarioAct.get().addOnSuccessListener {
            val us = it.toObject(Usuario::class.java)
            //Picasso.get().load(us?.imgURL).error(R.drawable.maletas3_little).into(binding.imageViewInfoAvatar)
            Glide.with(this)
                .load(us?.imgURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.maletas3_little)
                .into(binding.imageViewInfoAvatar)
        }
    }

    private fun configAuth(){
        firebaseAuth= FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null){
                supportActionBar?.subtitle = auth.currentUser?.displayName
            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder().build())
                    //AuthUI.IdpConfig.FacebookBuilder().build())

                val loginView = AuthMethodPickerLayout
                    .Builder(R.layout.vista_pantalla_login)
                    .setEmailButtonId(R.id.botonEmail)
                    .setGoogleButtonId(R.id.botonGoogle)
                    .setPhoneButtonId(R.id.phone_button)
                    //.setFacebookButtonId(R.id.FacebookBoton)
                    .setTosAndPrivacyPolicyId(R.id.condiciones)
                    .build()

                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.maletas3_little)
                        //.setAuthMethodPickerLayout(loginView)
                        .build())
            }
        }
    }


    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        //configFireStoreRealTime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        //fireStoreListener.remove()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.sesion_terminada), Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            //Nada
                        } else {
                            Toast.makeText(this, getString(R.string.sesion_terminar_fallo), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun save(usuario : Usuario){
        val userID = firebaseAuth.currentUser!!.uid
        var encontrado : Boolean = false

        //Primero, la instancia de la base de datos
        val db = FirebaseFirestore.getInstance().collection("usuarios")

        db.get().addOnSuccessListener { result ->
            for (document in result) {
                if ((document.id).equals(userID)) {
                    encontrado = true
                }
            }
        }
            .addOnFailureListener{
                //Hacer nada
            }

        if (!encontrado) {
            db
                .document(userID)
                .set(usuario)
                .addOnSuccessListener {
                    //Toast.makeText(this, "Usurious grabado con éxito", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    //Toast.makeText(this, "Fallo en la grabación del usuario", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {

                }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                Toast.makeText(this, getString(R.string.bienvenido), Toast.LENGTH_SHORT).show()

            }
        } else {
            if (response == null){
                Toast.makeText(this, getString(R.string.despedida), Toast.LENGTH_SHORT).show()
                //finish()
            } else {
                response.error?.let {
                    if (it.errorCode == ErrorCodes.NO_NETWORK){
                        Toast.makeText(this, getString(R.string.sin_red), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.codigo_error) + "${it.errorCode}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}