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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.MaletasActivity
import com.josecarlos.maletavirtual.utils.EvenPost
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.utils.Utils
import com.josecarlos.maletavirtual.utils.Utils.Companion.ocultarTeclado
import com.josecarlos.maletavirtual.databinding.FragmentDialogAddCompartidaBinding
import com.josecarlos.maletavirtual.interfaces.MaletasAux
import com.josecarlos.maletavirtual.models.Maletas
import com.josecarlos.maletavirtual.utils.Utils.Companion.esContrasenaValida
import com.josecarlos.maletavirtual.utils.Utils.Companion.getIdUsuarioLogeado
import java.io.ByteArrayOutputStream

/**
 * Clase que gestiona el diálogo de añadir artículo en la pantalla de maletas compartidas
 */

class AddDialogCompartidaFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding : FragmentDialogAddCompartidaBinding? = null

    private var positiveButton : Button ? = null
    private var negativeButton : Button ? = null

    private var maleta : Maletas?= null
    private var fotoMaletaActualizada : Boolean = false
    private var maletaCargada : Boolean = false

    private var photoSelectedUri : Uri? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode==Activity.RESULT_OK){
            fotoMaletaActualizada=true
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

        binding?.etClave?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.etNombre?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.tilNombre?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)  ocultarTeclado()
        }
        binding?.ibMaleta?.setOnFocusChangeListener{v, hashFocus ->
            if (hashFocus) ocultarTeclado()
        }
        binding?.etFechaViaje?.setOnFocusChangeListener{v, hashFocus ->
            if (hashFocus) ocultarTeclado()
        }

        //var String = if (maletaCargada) getString(R.string.agregar) else getString(R.string.actualizar)
        activity.let {activity->
            binding = FragmentDialogAddCompartidaBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Agregar Maleta Compartida")
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

    /**
     * Método que muestra el dataPicker cuando se introduce la fecha del viaje
     */

    private fun mostrarDataPicker(){
        val datepicker = DatePickerFragment { dia, mes, ano -> fechaSeleccionada(dia, mes, ano) }
        datepicker.show(childFragmentManager,"datepicker")
    }

    /**
     * Método auxiliar del anterior para corregir el mes seleccionado y poner la fecha seleccionada en el editText
     */

    fun fechaSeleccionada(dia:Int, mes: Int, ano: Int){
        val mesCorregido = mes+1
        binding!!.etFechaViaje.setText("$dia/$mesCorregido/$ano")
    }

    /**
     * Método que gestiona la carga del fragmento y gestiona el proceso de crear una maleta compartida
     */

    override fun onShow(dialogInterface: DialogInterface?) {
        binding?.etFechaViaje?.isEnabled = false
        binding?.etFechaViaje2?.isEnabled = false
        binding?.etNombre?.isEnabled=false
        binding?.ibMaleta?.isEnabled=false

        ponerMaletaSeleccionada()
        configButtons()

        val dialog = dialog as? AlertDialog

        dialog?.let {dialogo->

            var encontrada = false
            var maletaCerrada = false
            var maletaCompartida : Maletas ?= null
            
            binding?.etClave?.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {

                    //binding?.etNombre?.setText("Clave : "+s.length)

                    if(!esContrasenaValida(s.toString())) {
                        binding?.tilClave?.error = getString(R.string.contenido_clave)
                    }

                    else if (s.length<9)  {

                        binding?.tilClave?.error =""
                        encontrada=false

                        if (s.length==8){
                           Utils.getFirestore().collection("compartidas").get().addOnSuccessListener { snapshots ->

                                for (snapshot in snapshots){
                                    //if (snapshot.id.equals(s.toString()))
                                    maletaCompartida = snapshot.toObject(Maletas::class.java)

                                    if (maletaCompartida?.id.equals(s.toString())){
                                        encontrada = true
                                        var maletaYaCompartida = false

                                        for (idUsuario in maletaCompartida!!.usuariosCompartida){
                                            if (idUsuario.equals(getIdUsuarioLogeado())){
                                                maletaYaCompartida=true
                                                binding?.tilClave?.error = getString(R.string.maleta_compartida_ya)
                                            }
                                        }

                                        if (!maletaCompartida?.activa!!){
                                            binding?.tilClave?.error = getString(R.string.maleta_compartida_cerrada)
                                        }

                                        if (!maletaYaCompartida && maletaCompartida?.activa!!) {
                                            binding?.etClave?.isEnabled = false
                                            binding?.etNombre?.setText(maletaCompartida!!.nombre.toString())
                                            binding?.etFechaViaje?.setText(maletaCompartida!!.fechaViaje.toString())
                                            Glide.with(this@AddDialogCompartidaFragment)
                                                .load(maletaCompartida!!.imgURL)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .centerCrop()
                                                .into(binding!!.imageProductPreview)
                                            break
                                        }
                                    }
                                }
                            }.addOnCompleteListener() {
                               if (!encontrada) {
                                   binding?.etNombre?.text?.clear()
                                   binding?.etNombre?.requestFocus()
                                   binding?.etClave?.isEnabled = false
                                   binding?.etFechaViaje?.isEnabled = true
                                   binding?.etFechaViaje2?.isEnabled = true
                                   binding?.etNombre?.isEnabled = true
                                   binding?.ibMaleta?.isEnabled = true
                               }
                           }
                        }
                    }
                }
            })

            positiveButton=dialogo.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=dialogo.getButton(Dialog.BUTTON_NEGATIVE)

            binding!!.etFechaViaje.setOnClickListener{mostrarDataPicker()}

            val usuario = Utils.getUsuarioLogueado()

            if (maletaCargada) dialogo.setTitle(getString(R.string.maleta_actualizar))
            else dialogo.setTitle("Agregar Maleta Compartida")

            positiveButton?.setOnClickListener{

                // Si la maleta ya está en compartidas, actualizo los datos y le añado un nuevo compartido
                if (encontrada){
                    var yaCompartida = false
                    for (s in maletaCompartida!!.usuariosCompartida){
                        if (s.equals(getIdUsuarioLogeado())){
                            yaCompartida = true
                            Toast.makeText(activity, getString(R.string.adevertencia_maleta_compartida), Toast.LENGTH_SHORT).show()
                        }
                    }

                    //Pero antes de aceptar, compruebo que no la tiene compartida este usuario ya
                    if (!yaCompartida) {
                        maletaCompartida.apply {
                            maletaCompartida?.emailUsuario =
                                Utils.getUsuarioLogueado().email.toString()
                            maletaCompartida?.compartida = true
                            maletaCompartida?.usuariosCompartida?.add(Utils.getIdUsuarioLogeado())
                            Toast.makeText(
                                activity,
                                getString(R.string.maleta_compartida_correcto),
                                Toast.LENGTH_LONG
                            ).show()
                            update(maletaCompartida!!)

                            var intent = Intent(activity,MaletasActivity::class.java)
                            intent.putExtra("TIPO", "compartida")
                            startActivity(intent)
                        }
                    }

                // Compruebo que debe tener una clave de maleta para poder continuar
                }else if(binding?.etClave?.text.isNullOrEmpty()){
                    Toast.makeText(this.requireContext(),getString(R.string.clave_maleta_introducir_obligatorio), Toast.LENGTH_LONG).show()
                }
                //Compruebo que se hayan metido datos en el campo nombre y una fecha de viaje
                else if (binding!!.etFechaViaje.text.isNullOrEmpty() || binding!!.etNombre.text.isNullOrEmpty()){ // || photoSelectedUri.toString().equals("null", true)) {
                    Toast.makeText(this.requireContext(), getString(R.string.advertencia_faltan_datos_maleta), Toast.LENGTH_SHORT).show()
                }else {

                    if (maletaCargada){
                        //Toast.makeText(this.requireContext(), "Maleta Cargada", Toast.LENGTH_SHORT).show()
                    }else if (photoSelectedUri.toString().equals("null", true)){
                        //Toast.makeText(this.requireContext(), "Maleta Null", Toast.LENGTH_SHORT).show()
                    }else{
                        //Toast.makeText(this.requireContext(), "Maleta Con cambio de foto", Toast.LENGTH_SHORT).show()
                    }

                    binding?.let {
                        habilitarIntefaz(false)

                        if (fotoMaletaActualizada){

                            subirImagenComprimida(maleta?.id) { eventPost ->
                                if (eventPost.isSuccess) {
                                    if (maleta == null) {

                                        val maleta = Maletas(
                                            id =  it.etClave.text.toString().trim(),
                                            nombre = it.etNombre.text.toString().trim(),
                                            fechaViaje = it.etFechaViaje.text.toString().trim(),
                                            emailUsuario = usuario?.email.toString(),
                                            emailCreador = usuario?.email.toString(),
                                            compartida = true,
                                            imgURL = eventPost.photoURL
                                        )
                                        maleta.usuariosCompartida.add(usuario.uid)
                                        //save(maleta, Utils.getAuth().currentUser!!.uid)
                                        save(maleta, binding?.etClave?.text.toString())

                                    } else {
                                        //Toast.makeText(this.requireContext(), "Entra aquí", Toast.LENGTH_SHORT).show()
                                        maleta?.apply {
                                            nombre = it.etNombre.text.toString().trim()
                                            fechaViaje = it.etFechaViaje.text.toString().trim()
                                            imgURL = eventPost.photoURL
                                            usuariosCompartida.add(usuario.toString())
                                            update(this)
                                        }
                                    }
                                }
                            }
                        }else{

                            if (photoSelectedUri.toString().equals("null", true) && !maletaCargada){
                                Toast.makeText(this.requireContext(), getString(R.string.advertencia_faltan_datos_maleta), Toast.LENGTH_SHORT).show()
                                habilitarIntefaz(true)
                            }else{
                                maleta?.apply {
                                    nombre = it.etNombre.text.toString().trim()
                                    fechaViaje = it.etFechaViaje.text.toString().trim()
                                    //imgURL = eventPost.photoURL
                                    usuariosCompartida.add(usuario.toString())
                                    update(this)
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

    /**
     * Método que sube al Storage la imagen una vez comprimida
     */

    private fun subirImagenComprimida(maletaID : String?, callback : (EvenPost)->Unit) {

        val eventPost = EvenPost()
        eventPost.documentId = maletaID ?: FirebaseFirestore.getInstance().collection("maletas").document().id

        val storageRef = FirebaseStorage.getInstance().reference

        photoSelectedUri?.let { uri->
            binding?.let {binding->


                cogerImagenDesdeURI(uri)?.let{ bitmap ->

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
                            habilitarIntefaz(true)
                            Toast.makeText(activity, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                            callback(eventPost)
                        }
                }

            }
        }
    }

    /**
     * Método auxiliar del anterior, que captura la imagen desde un URI y retorna el bitmap
     */

    private fun cogerImagenDesdeURI(uri : Uri) : Bitmap? {
        activity?.let{
            val bitmap = if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(it.contentResolver,uri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(it.contentResolver,uri)
            }
            return getImagenRedimensioada(bitmap,320)
        }
        return null
    }

    /**
     * Método auxiliar del anterior, que redimensiona la imagen que se el pasa por parámetro
     */

    private fun getImagenRedimensioada(image : Bitmap, maxSize: Int) : Bitmap  {
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
     * Método que pone los datos de la maleta seleccionada en los campos del fragmento
     */

    private fun ponerMaletaSeleccionada() {
        maleta = (activity as MaletasAux)?.getMaletaSeleccionada()
        maleta?.let { maleta->
            binding?.let {
                maletaCargada = true
                it.etNombre.setText(maleta.nombre.toString())
                it.etFechaViaje.setText(maleta.fechaViaje.toString())
                Glide.with(this)
                    .load(maleta.imgURL)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imageProductPreview)
            }
        }
    }

    /**
     * Método para guardar los datos de la nueva maleta en FireBase
     */

    private fun save(maleta : Maletas, documentId : String){
        val db = FirebaseFirestore.getInstance()
        db.collection("compartidas")
            .document(documentId)
            .set(maleta)
            .addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.maleta_anadida_ok), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(activity, getString(R.string.maleta_anadir_error), Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener{
                habilitarIntefaz(true)
                binding?.progressBar?.visibility=View.INVISIBLE
                dismiss()

            }
       /* db.collection("usuarios").document(Utils.getIdUsuarioLogeado()).get().addOnSuccessListener {usuario ->
            val us = usuario.toObject(Usuario::class.java)
            us?.compartidas?.add(binding?.etClave?.text?.trim().toString())
            updateUsuario(us!!)
            //db.collection("usuarios").document(Utils.getIdUsuarioLogeado()).set(us)
        }*/
    }

    /**
     * Método para actualizar los datos de una maleta seleccionada y de la que se quieren cambiar los datos en Firebase
     */

    private fun update(maleta : Maletas){
        val db = FirebaseFirestore.getInstance()
        maleta.id?.let {id->
            db.collection("compartidas").document(id).set(maleta).addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.maleta_actualizada_ok), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(activity, getString(R.string.maleta_actualizar_error), Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener{
                habilitarIntefaz(true)
                binding?.progressBar?.visibility=View.INVISIBLE
                dismiss()
            }
        }

    }

    /**
     * Metodo que habilita o inhabilita botones y campos cuando se están ejecutando determinados procesos, para evitar problemas
     */

    private fun habilitarIntefaz(enabled : Boolean){
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