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

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.adapters.ArticulosAdapter
import com.josecarlos.maletavirtual.databinding.FragmentDialogDuplicarMaletaBinding
import com.josecarlos.maletavirtual.models.Articulos
import com.josecarlos.maletavirtual.models.Maletas
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import java.io.IOException

import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.storage.FileDownloadTask

import com.google.android.gms.tasks.OnSuccessListener
import com.josecarlos.maletavirtual.utils.EvenPost
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.utils.Utils
import com.josecarlos.maletavirtual.utils.Utils.Companion.ocultarTeclado

/**
 * Clase que gestiona el diálogo de duplicar maleta en la pantalla de maletas cerradas y compartidas
 */


class AddDialogDuplicarMaletaFragment (maletaID: String, maletaCompartida : Boolean) : DialogFragment(), DialogInterface.OnShowListener {

    private var binding : FragmentDialogDuplicarMaletaBinding? = null

    private var positiveButton : Button ? = null
    private var negativeButton : Button ? = null

    private lateinit var adapter : ArticulosAdapter

    private var maleta : Maletas?= null

    private var photoSelectedUri : Uri? = null

    private var maletaOriginalID = maletaID
    private var esMaletaCompartida = maletaCompartida

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode==Activity.RESULT_OK){
            photoSelectedUri = it.data?.data
            //binding?.imageProductPreview?.setImageURI(photoSelectedUri)
            binding?.let {
                Glide.with(this)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imageProductPreview)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding?.etNombre?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.ibMaleta?.setOnFocusChangeListener{v, hashFocus ->
            if (hashFocus) ocultarTeclado()
        }
        binding?.etFechaViaje?.setOnFocusChangeListener{v, hashFocus ->
            if (hashFocus) ocultarTeclado()
        }

        activity.let {activity->
            binding = FragmentDialogDuplicarMaletaBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.duplicar_maleta_cerrada))
                    .setPositiveButton(getString(R.string.duplicar), null)
                    .setNegativeButton(getString(R.string.cancelar),null)
                    .setView(it.root)
                val dialog = builder.create()
                dialog.setOnShowListener(this)
                return dialog
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    /**
     * Método que gestiona la carga del fragmento y gestiona el proceso de duplicar una maleta
     */

    override fun onShow(dialogInterface: DialogInterface?) {

        //initMaleta()
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {dialogo->
            positiveButton=dialogo.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=dialogo.getButton(Dialog.BUTTON_NEGATIVE)
            binding!!.etFechaViaje.setOnClickListener{mostrarDialogoDataPicker()}
            val firebase = FirebaseAuth.getInstance()
            val usuario = firebase.currentUser
            positiveButton?.setOnClickListener{

                if (binding!!.etFechaViaje.text.isNullOrEmpty() || binding!!.etNombre.text.isNullOrEmpty() || photoSelectedUri.toString().equals("null", true)) {
                    Toast.makeText(this.requireContext(), getString(R.string.advertencia_faltan_datos_maleta), Toast.LENGTH_SHORT).show()
                }else {

                    binding?.let {
                        habilitarInterfaz(false)
                        //Carga imagen
                        //uploadImage (maleta?.id){eventPost->
                        subirImagenComprimida(maleta?.id) { eventPost ->
                            if (eventPost.isSuccess) {
                                dialogo.setTitle(getString(R.string.duplicar_maleta))
                                val maleta = Maletas(
                                    id = maleta?.id,
                                    nombre = it.etNombre.text.toString().trim(),
                                    fechaViaje = it.etFechaViaje.text.toString().trim(),
                                    emailUsuario = usuario?.email.toString(),
                                    emailCreador = usuario?.email.toString(),
                                    imgURL = eventPost.photoURL
                                )
                                //save(maleta, Utils.getAuth().currentUser!!.uid)
                                save(maletaOriginalID, maleta, eventPost.documentId!!, esMaletaCompartida)
                                this.requireActivity().finish()
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

    /**
     * Método que sube al Storage la imagen una vez comprimida
     */

    private fun subirImagenComprimida(maletaID : String?, callback : (EvenPost)->Unit) {

        val eventPost = EvenPost()
        eventPost.documentId = maletaID ?: FirebaseFirestore.getInstance().collection("usuarios").document(
            Utils.getAuth().currentUser!!.uid).collection("maletas").document().id

        val storageRef = FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid )

        photoSelectedUri?.let { uri->
            binding?.let {binding->
                getImagenDeURI(uri)?.let{ bitmap ->

                    binding.progressBar.visibility= View.VISIBLE
                    val baos = ByteArrayOutputStream()

                    bitmap.compress(Bitmap.CompressFormat.JPEG,75,baos)

                    val photoRef = storageRef.child(eventPost.documentId!!)

                    photoRef.putBytes(baos.toByteArray()    )
                        .addOnProgressListener {
                            val progress = (100*it.bytesTransferred/it.totalByteCount).toInt()
                            it.run {
                                binding.progressBar.progress=progress
                                binding.tvProgress.text= String.format("%s%%", progress)
                            }
                        }
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener {downloadUrl->
                                eventPost.isSuccess=true
                                eventPost.photoURL=downloadUrl.toString()
                                callback(eventPost)
                            }
                        }.addOnFailureListener{
                            eventPost.isSuccess=false
                            habilitarInterfaz(true)
                            Toast.makeText(activity, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                            callback(eventPost)
                        }
                }

            }
        }
    }

    /**
     * Método que devuelve el bitmap de una imagen a partir de una URI de Firebase FireSore
     */

    private fun getImagenDeURI(uri : Uri) : Bitmap? {
        activity?.let{
            val bitmap = if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(it.contentResolver,uri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(it.contentResolver,uri)
            }
            return getImagenRedimensionada(bitmap,320)
        }
        return null
    }

    /**
     * Método auxiliar del anterior, que captura la imagen desde un URI y retorna el bitmap
     */

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

    private fun configButtons() {
        binding?.let {
            it.ibMaleta.setOnClickListener{
                abrirGaleria()
            }
        }
    }

    /**
     * Método que abre la galería de imágnes del dispositivo
     */

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    /**
     * Método que muestra el DataPicker para seleccionar la fecha del viaje
     */

    private fun mostrarDialogoDataPicker(){
        val datepicker = DatePickerFragment { dia, mes, ano -> onFechaSeleccionada(dia, mes, ano) }
        datepicker.show(childFragmentManager,"datepicker")
    }

    /**
     * Método auxiliar del anterior, para corregir el problema del mes, y poner la fecha seleccionada en el editText
     */

    fun onFechaSeleccionada(dia:Int, mes: Int, ano: Int){
        val mesCorregido = mes+1
        binding!!.etFechaViaje.setText("$dia/$mesCorregido/$ano")
    }

    /**
     * Método para guardar los datos de la nueva maleta duplicada en FireBase
     */

    private fun save(maletaOriginalID: String, maleta : Maletas, documentId : String, maletaCompartida: Boolean){
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
            //.add(maleta)
            .document(documentId)
            .set(maleta)
            .addOnSuccessListener {

                if (!maletaCompartida) {

                    db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid)
                        .collection("maletas")
                        .document(maletaOriginalID).collection("articulos").get()
                        .addOnSuccessListener { snapshots ->
                            for (articulos in snapshots) {

                                val articulo = articulos.toObject(Articulos::class.java)

                                //Creo la referencia al archivo de FireStore a través de su URL guardada
                                val storage = FirebaseStorage.getInstance()
                                val httpsReference =
                                    storage.getReferenceFromUrl(articulo.imgURL.toString())

                                //Creo el archivo temporal local y le doy un nombre
                                var archivoLocal: File? = null
                                try {
                                    archivoLocal = File.createTempFile(documentId, "")
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                                //Lo descargo
                                httpsReference.getFile(archivoLocal!!)
                                    .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot?> {
                                        val rutaLocalTemporal = archivoLocal!!.absolutePath

                                        var file = File(rutaLocalTemporal)

                                        val uri = Uri.fromFile(file)

                                        val imagenStorage =
                                            storage.reference.child(Utils.getIdUsuarioLogeado())
                                                .child(documentId).child("${uri.lastPathSegment}")
                                        imagenStorage.putFile(uri)
                                            .addOnSuccessListener {
                                                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->

                                                    val articuloNuevo = Articulos(
                                                        id = articulo?.id,
                                                        comprobado = false,
                                                        nombre = articulo.nombre,
                                                        cerrado = false,
                                                        cantidad = articulo.cantidad,
                                                        emailUsuario = articulo.emailUsuario,
                                                        emailCreador = articulo.emailCreador,
                                                        imgURL = downloadUrl.toString()
                                                    )
                                                    db.collection("usuarios")
                                                        .document(Utils.getAuth().currentUser!!.uid)
                                                        .collection("maletas")
                                                        .document(documentId)
                                                        .collection("articulos")
                                                        .document(articulo.id!!).set(articuloNuevo)
                                                }

                                            }
                                    }).addOnFailureListener(OnFailureListener {

                                    })

                            }
                        }

                    Toast.makeText(
                        activity,
                        getString(R.string.maleta_duplicada_ok),
                        Toast.LENGTH_SHORT
                    ).show()


                //Cuando se trate de duplicar maletas compartidas que se hayan cerrado
                }else{
                    db.collection("compartidas")
                        .document(maletaOriginalID).collection("articulos").get()
                        .addOnSuccessListener { snapshots ->
                            for (articulos in snapshots) {

                                val articulo = articulos.toObject(Articulos::class.java)

                                //Creo la referencia al archivo de FireStore a través de su URL guardada
                                val storage = FirebaseStorage.getInstance()
                                val httpsReference =
                                    storage.getReferenceFromUrl(articulo.imgURL.toString())

                                //Creo el archivo temporal local y le doy un nombre
                                var archivoLocal: File? = null
                                try {
                                    archivoLocal = File.createTempFile(documentId, "")
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                                //Lo descargo
                                httpsReference.getFile(archivoLocal!!)
                                    .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot?> {
                                        val rutaLocalTemporal = archivoLocal!!.absolutePath

                                        var file = File(rutaLocalTemporal)

                                        val uri = Uri.fromFile(file)

                                        val imagenStorage =
                                            storage.reference.child(Utils.getIdUsuarioLogeado())
                                                .child(documentId).child("${uri.lastPathSegment}")
                                        imagenStorage.putFile(uri)
                                            .addOnSuccessListener {
                                                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->

                                                    val articuloNuevo = Articulos(
                                                        id = articulo?.id,
                                                        comprobado = false,
                                                        nombre = articulo.nombre,
                                                        cerrado = false,
                                                        cantidad = articulo.cantidad,
                                                        emailUsuario = articulo.emailUsuario,
                                                        emailCreador = articulo.emailCreador,
                                                        imgURL = downloadUrl.toString()
                                                    )
                                                    db.collection("usuarios")
                                                        .document(Utils.getAuth().currentUser!!.uid)
                                                        .collection("maletas")
                                                        .document(documentId)
                                                        .collection("articulos")
                                                        .document(articulo.id!!).set(articuloNuevo)
                                                }

                                            }
                                    }).addOnFailureListener(OnFailureListener {
                                    })
                            }
                        }

                    Toast.makeText(
                        activity,
                        getString(R.string.maleta_duplicada_ok),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }.addOnFailureListener{
                Toast.makeText(activity, getString(R.string.duplicar_maleta_error), Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener{
                habilitarInterfaz(true)
                binding?.progressBar?.visibility=View.INVISIBLE
                dismiss()
            }
    }

    /**
     * Metodo que habilita o inhabilita botones y campos cuando se están ejecutando determinados procesos, para evitar problemas
     */

    private fun habilitarInterfaz(enabled : Boolean){
        positiveButton?.isEnabled = enabled
        negativeButton?.isEnabled = enabled
        binding?.let {
            with(it) {
                it.etNombre.isEnabled = enabled
                it.etFechaViaje.isEnabled = enabled
                it.ibMaleta.isEnabled =enabled
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}