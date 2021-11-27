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

package com.josecarlos.maletavirtual.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.utils.EvenPost
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.utils.Utils
import com.josecarlos.maletavirtual.utils.Utils.Companion.ocultarTeclado
import com.josecarlos.maletavirtual.databinding.FragmentDialogAddArticuloBinding
import com.josecarlos.maletavirtual.interfaces.ArticulosAux
import com.josecarlos.maletavirtual.models.Articulos
import java.lang.NullPointerException
import java.io.*


class AddDialogArticuloFragment (maletaID: String, compartida: Boolean, creadorMaleta: String): DialogFragment(), DialogInterface.OnShowListener {

    private var fotoArticuloActualizada : Boolean = false

    private var binding : FragmentDialogAddArticuloBinding? = null

    private var maletaIdentificador = maletaID
    private val maletaCompartida = compartida
    private val duenoMaleta = creadorMaleta

    private var positiveButton : Button ? = null
    private var negativeButton : Button ? = null

    private var articulo : Articulos?= null

    private var mCurrentPhotoPath : String? = null

    private var photoSelectedUri : Uri? = null

    private var articuloCargado : Boolean = false

    private val RC_GALLERY = 101
    private val RC_CAMARA = 100

    private val RP_CAMARA = 121
    private val RP_STORAGE = 122

    private val IMAGE_DIRECTORY = "/misFotosApp"
    private val MY_PHOTO = "my_photo"

    private var PATH_PROFILE = "profile"
    private var PATH_PHOTO_URL = "photoURL"

    private var LOGO_APP = "maletas3_little_light.png"


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding?.etNombre?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.etCantidad?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.ibArticulo?.setOnFocusChangeListener{v, hasFocus ->
            if (hasFocus) ocultarTeclado()
        }

        activity.let {activity->
            binding = FragmentDialogAddArticuloBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.agregar_articulo)) // a $maletaIdentificador")
                    .setPositiveButton(getString(R.string.aceptar), null)
                    .setNegativeButton(getString(R.string.cancelar),null)
                    .setView(it.root)
                val dialog = builder.create()
                dialog.setOnShowListener(this)
                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun configPhotoProfile(){
        try{
            articulo!!.id
            if (!articulo!!.imgURL.equals("null", true)){
                FirebaseStorage.getInstance().getReference().child(Utils.getAuth().currentUser!!.uid).child(maletaIdentificador).child(articulo!!.id.toString()).downloadUrl.addOnSuccessListener {

                    Glide.with(this)
                        .load(it)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding!!.imageProductPreview)
                }.addOnFailureListener{
                    //Toast.makeText(this.requireContext(), getString(R.string.fallo_subir_foto), Toast.LENGTH_SHORT).show()
                }
            }else{
                FirebaseStorage.getInstance().getReference().child("maletas3_little_light.png").downloadUrl.addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding!!.imageProductPreview)
                }.addOnFailureListener{
                    Toast.makeText(this.requireContext(), getString(R.string.fallo_subir_foto), Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e : NullPointerException){

            //FirebaseStorage.getInstance().getReference().child(Utils.getAuth().currentUser!!.uid).child(maletaIdentificador).downloadUrl.addOnSuccessListener {
            FirebaseStorage.getInstance().getReference().child("maletas3_little_light.png").downloadUrl.addOnSuccessListener {

                photoSelectedUri = it

                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding!!.imageProductPreview)
            }.addOnFailureListener{
                Toast.makeText(this.requireContext(), getString(R.string.fallo_subir_foto), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onShow(dialogInterface: DialogInterface?) {

        initArticulo()
        configButtons()
        configPhotoProfile()

        val dialog = dialog as? AlertDialog
        dialog?.let {dialogo->
            positiveButton=dialogo.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=dialogo.getButton(Dialog.BUTTON_NEGATIVE)

            if (articuloCargado) dialog.setTitle(getString(R.string.articulo_actualizar))
            else dialog.setTitle(R.string.agregar_articulo)

            //Un usuario no puede cambiar el nombre del artículo ni su cantidad si no es su propietario
            if(maletaCompartida && articulo!=null && !(Utils.getUsuarioLogueado().email.toString().equals(articulo?.emailCreador))){
                binding?.etNombre?.isEnabled=false
                binding?.etCantidad?.isEnabled=false
                if (Utils.getUsuarioLogueado().email.toString().equals(duenoMaleta)){
                    binding?.etNombre?.isEnabled=true
                    binding?.etCantidad?.isEnabled=true
                }
            }

            val firebase = FirebaseAuth.getInstance()
            val usuario = firebase.currentUser
            positiveButton?.setOnClickListener{

                var esNumeroCantidad : Boolean = Utils.esNumero(binding!!.etCantidad.text.toString())

                if (binding!!.etNombre.text.isNullOrEmpty() || binding!!.etCantidad.text.isNullOrEmpty()){
                    Toast.makeText(this.requireContext(), getString(R.string.introducir_todos_datos), Toast.LENGTH_SHORT).show()
                }else if (!esNumeroCantidad) {
                    Toast.makeText(this.requireContext(), getString(R.string.valor_no_numero), Toast.LENGTH_SHORT).show()
                }else{

                    binding?.let {
                        enableUI(false)

                        if (fotoArticuloActualizada) {

                            uploadRecucedImage(articulo?.id) { eventPost ->
                                if (eventPost.isSuccess) {

                                    if (articulo == null) {

                                        val cant = it.etCantidad.text.toString()
                                        val articulo = Articulos(
                                            id = articulo?.id,
                                            nombre = it.etNombre.text.toString().trim(),
                                            cantidad = Integer.parseInt(cant),
                                            emailUsuario = usuario?.email.toString(),
                                            emailCreador = usuario?.email.toString(),
                                            imgURL = eventPost.photoURL
                                        )
                                        //save(articulo, Utils.getAuth().currentUser!!.uid)
                                        save(articulo, eventPost.documentId!!)

                                    } else {

                                        articulo?.apply {
                                            nombre = it.etNombre.text.toString().trim()
                                            emailUsuario = usuario?.email.toString()
                                            cantidad = Integer.parseInt(
                                                it.etCantidad.text.toString().trim()
                                            )
                                            imgURL = eventPost.photoURL
                                            update(this)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (articulo == null) {

                                val cant = it.etCantidad.text.toString()
                                val articulo = Articulos(
                                    id = articulo?.id,
                                    nombre = it.etNombre.text.toString().trim(),
                                    cantidad = Integer.parseInt(cant),
                                    emailUsuario = usuario?.email.toString(),
                                    emailCreador = usuario?.email.toString(),
                                    imgURL = photoSelectedUri.toString()
                                )
                                //save(articulo, Utils.getAuth().currentUser!!.uid)
                                save(articulo, Utils.crearImagenJpg())

                            } else {

                                articulo?.apply {
                                    if (!articulo!!.imgURL!!.contains("maletas3", true)) {
                                        nombre = it.etNombre.text.toString().trim()
                                        emailUsuario = usuario?.email.toString()
                                        cantidad =
                                            Integer.parseInt(it.etCantidad.text.toString().trim())
                                        update(this)
                                    } else {
                                        FirebaseStorage.getInstance().getReference()
                                            .child("maletas3_little_light.png").downloadUrl.addOnSuccessListener { uriFotoDefecto ->
                                                nombre = it.etNombre.text.toString().trim()
                                                emailUsuario = usuario?.email.toString()
                                                cantidad = Integer.parseInt(
                                                    it.etCantidad.text.toString().trim()
                                                )
                                                imgURL = uriFotoDefecto.toString()
                                                update(this)
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            negativeButton?.setOnClickListener{
                dismiss()
            }
        }
    }

    private fun uploadRecucedImage(articuloID : String?, callback : (EvenPost)->Unit) {

        val eventPost = EvenPost()
        eventPost.documentId = articuloID ?: FirebaseFirestore.getInstance().collection("usuarios").document(
            Utils.getAuth().currentUser!!.uid)
            .collection("maletas").document(maletaIdentificador)
            .collection("articulos").document().id

        val storageRef = FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid ).child(maletaIdentificador)

        if (fotoArticuloActualizada) {

            photoSelectedUri?.let { uri ->
                binding?.let { binding ->

                    getBitmapFromUri(uri)?.let { bitmap ->

                        binding.progressBar.visibility = View.VISIBLE
                        val baos = ByteArrayOutputStream()

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)

                        val photoRef = storageRef.child(eventPost.documentId!!)

                        photoRef.putBytes(baos.toByteArray())
                            .addOnProgressListener {
                                val progress =
                                    (100 * it.bytesTransferred / it.totalByteCount).toInt()
                                it.run {
                                    binding.progressBar.progress = progress
                                    binding.tvProgress.text = String.format("%s%%", progress)
                                }
                            }
                            .addOnSuccessListener {
                                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                    eventPost.isSuccess = true
                                    eventPost.photoURL = downloadUrl.toString()
                                    callback(eventPost)
                                }
                            }.addOnFailureListener {
                                eventPost.isSuccess = false
                                enableUI(true)
                                Toast.makeText(activity, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                                callback(eventPost)
                            }
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri : Uri) : Bitmap? {
        activity?.let{
            val bitmap = if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(it.contentResolver,uri)
                ImageDecoder.decodeBitmap(source)
            }else{

                MediaStore.Images.Media.getBitmap(it.contentResolver,uri)
            }
            return getResizedImage(bitmap,320)
        }
        return null
    }

    private fun getResizedImage(image : Bitmap, maxSize: Int) : Bitmap  {
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

    private fun configButtons() {
        binding?.let {
            it.ibArticulo.setOnClickListener{
                openGallery()
            }
            it.btnCamara.setOnClickListener{
                if (binding?.etCantidad==null || binding?.etNombre==null ||  binding?.etCantidad!!.equals("") || binding?.etNombre!!.equals("")){
                    Toast.makeText(this.requireContext(), getString(R.string.necesario_nombre_cantidad),  Toast.LENGTH_SHORT).show()
                }else {
                    tomarFoto()
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,101)
        //resultLauncher.launch(intent)
    }

    private fun initArticulo() {
        articulo = (activity as ArticulosAux)?.getArticuloSelect()
        articulo?.let { articulo->
            binding?.let {
                articuloCargado = true
                if (!articulo.imgURL.equals("null", true)) {
                    FirebaseStorage.getInstance().getReference(Utils.getAuth().currentUser!!.uid)
                        .child(maletaIdentificador)
                        .child(articulo.id.toString()).downloadUrl.addOnSuccessListener { ur ->
                            photoSelectedUri = ur
                        }

                    it.etNombre.setText(articulo.nombre.toString())
                    it.etCantidad.setText(articulo.cantidad.toString())
                    Glide.with(this)
                        .load(articulo.imgURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(it.imageProductPreview)
                } else {
                    FirebaseStorage.getInstance().getReference().child("maletas3_little_light.png").downloadUrl.addOnSuccessListener {defecto->
                        it.etNombre.setText(articulo.nombre.toString())
                        it.etCantidad.setText(articulo.cantidad.toString())
                        Glide.with(this)
                            .load(defecto)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(it.imageProductPreview)
                    }
                }
            }
        }
    }

    private fun save(articulo : Articulos, documentId : String){
        val db = FirebaseFirestore.getInstance()
        articulo.id = documentId

        if (!maletaCompartida) {
            db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid)
                .collection("maletas").document(maletaIdentificador)
                .collection("articulos")
                //.add(articulo)
                .document(documentId)
                .set(articulo)
                .addOnSuccessListener {
                    Toast.makeText(
                        activity,
                        getString(R.string.articulo_anadido_correcto),
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        activity,
                        getString(R.string.error_anadir_articulo),
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnCompleteListener {
                    enableUI(true)
                    binding?.progressBar?.visibility = View.INVISIBLE
                    dismiss()

                }
        }else{
            db.collection("compartidas").document(maletaIdentificador)
                .collection("articulos")
                //.add(articulo)
                .document(documentId)
                .set(articulo)
                .addOnSuccessListener {
                    Toast.makeText(
                        activity,
                        getString(R.string.articulo_anadido_correcto),
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        activity,
                        getString(R.string.error_anadir_articulo),
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnCompleteListener {
                    enableUI(true)
                    binding?.progressBar?.visibility = View.INVISIBLE
                    dismiss()

                }
        }
    }

    private fun update(articulo: Articulos){
        val db = FirebaseFirestore.getInstance()
        articulo.id?.let { id ->

            if (!maletaCompartida) {

                db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid)
                    .collection("maletas").document(maletaIdentificador)
                    .collection("articulos").document(id).set(articulo).addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            getString(R.string.articulo_actualizado),
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            activity,
                            getString(R.string.error_actualizar_articulo),
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnCompleteListener {
                        enableUI(true)
                        binding?.progressBar?.visibility = View.INVISIBLE
                        dismiss()
                    }
            }else{

                db.collection("compartidas").document(maletaIdentificador)
                    .collection("articulos").document(id).set(articulo).addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            getString(R.string.articulo_actualizado),
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            activity,
                            getString(R.string.error_actualizar_articulo),
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnCompleteListener {
                        enableUI(true)
                        binding?.progressBar?.visibility = View.INVISIBLE
                        dismiss()
                    }
            }
        }
    }

    private fun enableUI(enabled : Boolean){
        positiveButton?.isEnabled = enabled
        negativeButton?.isEnabled = enabled
        binding?.let {
            with(it) {
                it.etNombre.isEnabled = enabled
                it.etCantidad.isEnabled = enabled
                it.ibArticulo.isEnabled =enabled
                it.btnCamara.isEnabled=enabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun tomarFoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this.requireContext(),
                        "com.josecarlos.maletavirtual.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 100)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = System.currentTimeMillis().toString()
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(mCurrentPhotoPath!!)
            mediaScanIntent.data = Uri.fromFile(f)
            this.requireContext().sendBroadcast(mediaScanIntent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Cuando el resultado es el esperado, recibo l a imagen, la almanceno en la variable imageBitmap y se la paso al fragment invocador

        when (requestCode) {
            100 -> if (resultCode == RESULT_OK) {

                val file = File(mCurrentPhotoPath!!)

                fotoArticuloActualizada = true

                val uri = Uri.fromFile(file)
                val bitmap2 : Bitmap   = MediaStore.Images.Media.getBitmap(this.requireContext().contentResolver, uri)

                Glide.with(this)
                    .load(bitmap2)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding!!.imageProductPreview)

                //binding!!.ibArticulo.setImageBitmap(bitmap2)
                photoSelectedUri= uri

                data?.extras?.let { bundle ->

                    val imageBitmap = bundle.get("data") as Bitmap?
                    binding!!.ibArticulo.setImageBitmap(imageBitmap)
                    photoSelectedUri = galleryAddPic() as Uri
                }
            }else{
                //No hace nada
            }

            101 -> if (resultCode == RESULT_OK) {
                if (data!=null){
                    photoSelectedUri = data.data
                    fotoArticuloActualizada = true
                    val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(this.requireContext().contentResolver, photoSelectedUri)
                    binding?.imageProductPreview?.setImageBitmap(bitmap)
                }
            }
        }
    }
}