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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.utils.Utils.Companion.ocultarTeclado
import com.josecarlos.maletavirtual.databinding.ActivityCuentaPersonalBinding
import com.josecarlos.maletavirtual.fragments.CambioContrasenaFragment
import com.josecarlos.maletavirtual.models.Usuario
import com.josecarlos.maletavirtual.utils.Utils
import com.josecarlos.maletavirtual.utils.Utils.Companion.abrirActivity
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

class CuentaPersonalActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var binding : ActivityCuentaPersonalBinding
    private var photoSelectedUri : Uri? = null
    private var rutaImagen : String ?=null

    private var imagenUsuarioCambiada = false

    //private lateinit var firestoreListener : ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_cuenta_personal)
        binding = ActivityCuentaPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitle(getString(R.string.maleta_virtual))
        toolbar.setSubtitle(getString(R.string.configuracion_personal))
        //toolbar.setLogo(R.drawable.maletas3_little)

        binding.btnUpdate.setOnClickListener{
            configFirestoreRealTime()
        }

        binding.btnCambioContrasena.setOnClickListener{
            cambioContrasena()
        }

        binding.etFullName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) ocultarTeclado(v)
        }
        binding.telefono.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) ocultarTeclado(v)
        }
        binding.btnUpdate.setOnFocusChangeListener{v, hasFocus->
            if (hasFocus) ocultarTeclado(v)
        }
        binding.btnCambioContrasena.setOnFocusChangeListener{v, hasFocus->
            if (hasFocus) ocultarTeclado(v)
        }

        getUser()
        configurarBotones()
        configFirestoreRealTime()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_configuracion_personal, menu)
        return super.onCreateOptionsMenu(menu)
    }
    //Funcionalidad del boton del toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title){
            getString(R.string.ventana_principal) -> {
                abrirActivity<MainActivity>()
                finish()
            }
            getString(R.string.maletas_activas) -> {
                abrirActivity<MaletasActivity>("activa")
                finish()
            }
            getString(R.string.maletas_cerradas) -> {
                abrirActivity<MaletasActivity>("cerrada")
                finish()
            }
            getString(R.string.maletas_compartidas) -> {
                abrirActivity<MaletasActivity>("compartida")
                finish()
            }
            //getString(R.string.cerrar_sesion) -> { AuthUI.getInstance().signOut(this) ; finish() }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cambioContrasena(){
        CambioContrasenaFragment().show(supportFragmentManager, CambioContrasenaFragment::class.java.simpleName)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        //val intent = Intent(this,MainActivity::class.java)
        //startActivity(intent)
        //finish()
        return false
    }

    private fun configFirestoreRealTime(){
        val db = FirebaseFirestore.getInstance()
        val usuarioAct = db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
        usuarioAct.get().addOnSuccessListener {
            val us = it.toObject(Usuario::class.java)
            //Toast.makeText(this, us!!.nombre.toString(), Toast.LENGTH_SHORT).show()
            binding.telefono.setText(us!!.telefono.toString())
        }
    }

    private fun getUser(){
        FirebaseAuth.getInstance().currentUser.let {user->
            binding.etFullName.setText(user?.displayName)
            Glide.with(this)
                .load(user?.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.ic_time_access)
                .error(R.drawable.maletas3_little)
                .into(binding.ibProfile)
        }
        //val user = Utils.getFireStore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)

        encontrarUsuario(FirebaseAuth.getInstance().currentUser!!.uid)
        //Toast.makeText(this, user.toString(), Toast.LENGTH_SHORT).show()

        //val userID = FirebaseAuth.getInstance().currentUser!!.uid
        //db.document(Utils.getAuth().uid.toString(), usuario.)
        //    .set((Usuario(Utils.getAuth().uid.toString(),)))
    }

    private fun configurarBotones(){
        binding.let {binding->
            binding.ibProfile.setOnClickListener{
                abrirGaleria()
            }

            binding.btnCamara.setOnClickListener{
                abrirCamara()
            }

            binding.btnUpdate.setOnClickListener{
                binding.etFullName.clearFocus()
                FirebaseAuth.getInstance().currentUser?.let {user->
                    if (photoSelectedUri==null){

                        FirebaseStorage.getInstance().getReference().child("maletas3_little.png").downloadUrl.addOnSuccessListener{ uriFotoDefecto->
                            if (user.photoUrl==null) {   //toString().contains("maletas3", true)){
                                updateDatosUsuario(user,uriFotoDefecto)
                            }else {
                                updateDatosUsuario(user, user.photoUrl!!)
                            }
                        }

                    }else{
                        //Toast.makeText(this, photoSelectedUri.toString(), Toast.LENGTH_SHORT).show()
                        subirImagenComprimida(user)
                    }
                }
            }
        }
    }

    private fun abrirGaleria(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== Activity.RESULT_OK){
            photoSelectedUri = it.data?.data
            imagenUsuarioCambiada=true
            //binding?.imageProductPreview?.setImageURI(photoSelectedUri)
            binding.let {
                Glide.with(this)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.ibProfile)
            }
        }
    }

    private fun updateDatosUsuario(user : FirebaseUser, uri : Uri){

        val db = FirebaseFirestore.getInstance()
        val telefono = if(binding.telefono.text.isNullOrEmpty()) 0 else Integer.parseInt(binding.telefono.text.toString())

        FirebaseAuth.getInstance().currentUser?.let {

            val updateDatos = UserProfileChangeRequest.Builder()
                .setDisplayName(binding.etFullName.text.toString().trim())
                .setPhotoUri(uri)
                .build()

            user.updateProfile(updateDatos)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.usuario_actualizado), Toast.LENGTH_SHORT).show()

                }.addOnFailureListener{
                    Toast.makeText(this, getString(R.string.usuario_actualizacion_fallo), Toast.LENGTH_SHORT).show()
                }

            Utils.getFirestore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)

        }
        val usuarioAct = db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
        usuarioAct.get().addOnSuccessListener {
            val us = it.toObject(Usuario::class.java)

            val user = Usuario(
                nombre = us!!.nombre,
                telefono = telefono,
                emailUsuario = Utils.getAuth().currentUser!!.email.toString(),
                imgURL = uri.toString()
            )
            usuarioAct.set(user)

            //binding.telefono.setText(us!!.telefono.toString()
        }
    }

    fun encontrarUsuario (uid : String) : LiveData<Usuario>{
        val data = MutableLiveData<Usuario>()
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().addOnSuccessListener{
            val usuario = it.toObject<Usuario>()
            data.value = usuario!!
        }
        return data
    }

    private fun subirImagenComprimida(user : FirebaseUser) {

        val profileRef = FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid).child(
            Utils.getAuth().currentUser!!.uid)

        photoSelectedUri?.let { uri->
            binding.let {binding->
                getBitmapFromUri(uri)?.let{bitmap ->

                    binding.progressBar.visibility= View.VISIBLE

                    val baos = ByteArrayOutputStream()

                    bitmap.compress(Bitmap.CompressFormat.JPEG,75,baos)

                    profileRef.putBytes(baos.toByteArray()    )
                        .addOnProgressListener {
                            val progress = (100*it.bytesTransferred/it.totalByteCount).toInt()
                            it.run {
                                binding.progressBar.progress=progress
                                binding.tvProgress.text= String.format("%s%%", progress)
                            }
                        }
                        .addOnCompleteListener{
                            binding.progressBar.visibility=View.INVISIBLE
                            binding.tvProgress.text=""
                        }
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {downloadUrl->
                                updateDatosUsuario(user,downloadUrl)

                            }
                        }.addOnFailureListener{
                            //Nada
                        }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri : Uri) : Bitmap? {
        this.let{
            val bitmap = if( Build.VERSION.SDK_INT>= Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(it.contentResolver,uri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(it.contentResolver,uri)
            }
            return getImagenRedimensionada(bitmap,320)
        }
        return null
    }


    private fun getImagenRedimensionada(image : Bitmap, maxSize: Int) : Bitmap  {
        var ancho = image.width
        var alto = image.height
        if (ancho<=maxSize && alto <= maxSize) return image
        val bitmapRatio = ancho.toFloat() / alto.toFloat()
        if (bitmapRatio>1){
            ancho = maxSize
            alto = (ancho/bitmapRatio).toInt()
        }else{
            alto = maxSize
            ancho = (alto/bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, ancho, alto, true)
    }


    private fun abrirCamara() {
        //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //if (intent.resolveActivity(packageManager) != null) {
        //    startActivityForResult(intent, 1)
        // }
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var myUri : String ?=null
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //val extras = data!!.extras
            //val imgBitmap = extras!!["data"] as Bitmap?
            data?.extras?.let {bundle->
                val imageBitmap = bundle.get("data") as Bitmap?

                binding.ibProfile.setImageBitmap(imageBitmap)

                val storageRef = FirebaseStorage.getInstance().reference

                val imagenRef = storageRef.child(Utils.getAuth().uid + "/" + Utils.getAuth().uid)

                imagenRef.metadata.addOnSuccessListener {
                    it.reference!!.downloadUrl.addOnSuccessListener {
                        myUri = it.toString() //binding.textView.text=myUri

                        Utils.getFirestore().collection("usuarios").document(Utils.getAuth().uid!!).get().addOnSuccessListener { user->
                            val usuario = user.toObject(Usuario::class.java)
                            usuario?.imgURL= myUri
                            updateDatosUsuario(FirebaseAuth.getInstance().currentUser!!,it)

                            Utils.getFirestore().collection("usuarios").document(Utils.getAuth().uid!!).set(usuario!!)
                        }
                    }
                }

                binding.ibProfile.isDrawingCacheEnabled = true
                binding.ibProfile.buildDrawingCache()
                val bitmap = (binding.ibProfile.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = imagenRef.putBytes(data)

                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                    photoSelectedUri = Uri.parse(Utils.getAuth().uid + "/" + Utils.getAuth().uid)
                }.addOnSuccessListener { taskSnapshot ->
                    //Nada
                }
                try{
                    val fos : FileOutputStream = openFileOutput(Utils.crearImagenJpg(), Context.MODE_PRIVATE)
                    imageBitmap!!.compress(Bitmap.CompressFormat.JPEG,90, fos)
                    fos.close()
                }catch (ex : IOException){
                    //Nada
                }
            }
        }
    }
}