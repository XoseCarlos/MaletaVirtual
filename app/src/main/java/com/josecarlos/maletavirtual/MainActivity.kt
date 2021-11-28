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

package com.josecarlos.maletavirtual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.databinding.ActivityMainBinding
import com.josecarlos.maletavirtual.models.Usuario
import com.josecarlos.maletavirtual.utils.Utils

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

        ocultarPantallaPrincipal()
        configAuth()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitle(getString(R.string.app_name))
        toolbar.setSubtitle(Utils.getAuth().currentUser?.displayName)


        binding.mainBotonActivas.setOnClickListener{
            //val intent = Intent(this,MaletasActivasActivity::class.java)
            val intent = Intent(this,MaletasActivity::class.java)
            intent.putExtra("TIPO", "activa")
            startActivity(intent)
            //finish()
        }

        binding.mainBotonCerradas.setOnClickListener{
            //val intent = Intent(this,MaletasActivasActivity::class.java)
            //val intent = Intent(this,ArticulosActivity::class.java)
            val intent = Intent(this,MaletasActivity::class.java)
            intent.putExtra("TIPO", "cerrada")
            startActivity(intent)
            //finish()
        }

        binding.mainBotonCompartidas.setOnClickListener {
            val intent = Intent(this, MaletasActivity::class.java)
            intent.putExtra("TIPO", "compartida")
            startActivity(intent)
            //finish()
        }

        binding.actionSignOut.setOnClickListener{
            AuthUI.getInstance().signOut(this)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.sesion_terminada), Toast.LENGTH_SHORT).show()
                }
        }
        binding.mainBotonCuenta.setOnClickListener{
            val intent = Intent(this,CuentaPersonalActivity::class.java)
            startActivity(intent)
            //finish()
        }

    }


    /**
     * Método que registra al usuario en la base de datos de Usuarios en Firebase
     */

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
            .addOnCompleteListener(){
                if (!registrado){
                    val telefono = if(firebaseAuth.currentUser?.phoneNumber.isNullOrEmpty()) "0" else firebaseAuth.currentUser?.phoneNumber.toString()

                    FirebaseStorage.getInstance().getReference()
                        .child("maletas3_little.png").downloadUrl.addOnSuccessListener {uriFotoDefecto->
                            var imagenDefectoMaleta = uriFotoDefecto
                            val usuario = Usuario(
                                id = firebaseAuth.currentUser!!.uid,
                                nombre = firebaseAuth.currentUser?.displayName,
                                emailUsuario = firebaseAuth.currentUser?.email,
                                telefono = Integer.parseInt(telefono),
                                imgURL = imagenDefectoMaleta.toString()
                            )
                            save(usuario)
                            registrado = true
                            //Toast.makeText(this, getString(R.string.usuario_guardado), Toast.LENGTH_SHORT).show()
                        }
                }
            }

        return registrado
    }


    /**
     * Método que establece la lógica del inicio de la aplicación. Muestra el nombre, correo e imagen del usuario autenticado en la pantalla principal
     */

    override fun onStart() {
        super.onStart()
        binding.textViewInfoName.text=firebaseAuth.currentUser?.displayName.toString()
        binding.textViewInfoEmail.text=firebaseAuth.currentUser?.email.toString()
        //firebaseAuth.signOut()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val usuarioAct = Utils.getFirestore().collection("usuarios")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
            usuarioAct.get().addOnSuccessListener {
                val us = it.toObject(Usuario::class.java)
                if (Utils.getUsuarioLogueado().photoUrl!=null) {
                    //Picasso.get().load(us?.imgURL).error(R.drawable.maletas3_little).into(binding.imageViewInfoAvatar)
                    Glide.with(this)
                        .load(us?.imgURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .error(Utils.getUsuarioLogueado().photoUrl)
                        .into(binding.imageViewInfoAvatar)
                }else{
                    Glide.with(this)
                        .load(us?.imgURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .error(R.drawable.maletas3_little)
                        .into(binding.imageViewInfoAvatar)
                }
            }
        }
    }


    /**
     * Este método lo que hace es ocultar los botones e imágenes de la pantalla principal para mostrar el linear layout
     * de progreso antes de cargar la pantalla principal
     */

    private fun ocultarPantallaPrincipal(){
        binding.llProgress.visibility = View.VISIBLE
        binding.imageViewInfoAvatar.visibility=View.GONE
        binding.textViewInfoEmail.visibility=View.GONE
        binding.textViewInfoName.visibility=View.GONE
        binding.mainBotonActivas.visibility = View.GONE
        binding.mainBotonCerradas.visibility = View.GONE
        binding.mainBotonCuenta.visibility = View.GONE
        binding.actionSignOut.visibility = View.GONE
        binding.mainBotonCompartidas.visibility=View.GONE
    }


    /**
     * Método para cargar la pantalla de logueo y autenticación, en el caso de que el usuario
     * no se haya autenticado antes. Si no, salta a la pantalla principal sin más
     */

    private fun configAuth(){
        firebaseAuth= FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null){
                supportActionBar?.subtitle = auth.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.imageViewInfoAvatar.visibility=View.VISIBLE
                binding.textViewInfoEmail.visibility=View.VISIBLE
                binding.textViewInfoName.visibility=View.VISIBLE
                binding.mainBotonActivas.visibility = View.VISIBLE
                binding.mainBotonCerradas.visibility = View.VISIBLE
                binding.mainBotonCuenta.visibility = View.VISIBLE
                binding.actionSignOut.visibility = View.VISIBLE
                binding.mainBotonCompartidas.visibility=View.VISIBLE
                //binding.anadirMaletaButton.show()
                usuarioRegistrado(auth.currentUser!!.uid)
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
                        .setTosAndPrivacyPolicyUrls("https://policies.google.com/terms?hl=es","https://firebase.google.com/support/privacy?hl=es-419" )
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
                        //Toast.makeText(this, getString(R.string.sesion_terminada), Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.llProgress.visibility = View.GONE
                            binding.imageViewInfoAvatar.visibility=View.GONE
                            binding.textViewInfoEmail.visibility=View.GONE
                            binding.textViewInfoName.visibility=View.GONE
                            //binding.llProgress.visibility = View.GONE
                            binding.mainBotonActivas.visibility = View.GONE
                            binding.mainBotonCerradas.visibility = View.GONE
                            binding.mainBotonCuenta.visibility = View.GONE
                            binding.actionSignOut.visibility = View.GONE
                            binding.mainBotonCompartidas.visibility=View.GONE
                            //binding.anadirMaletaButton.hide()
                        } else {
                            Toast.makeText(this, getString(R.string.sesion_terminar_fallo), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Ugarda al usuario autenticado, el nuevo usuario en la base de datos Usuarios de FireBase FireStpore
     */

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
                finish()
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