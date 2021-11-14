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
import com.josecarlos.maletavirtual.databinding.FragmentDialogAddBinding
import com.josecarlos.maletavirtual.interfaces.MaletasAux
import com.josecarlos.maletavirtual.models.Maletas
import java.io.ByteArrayOutputStream
import kotlin.concurrent.fixedRateTimer

class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding : FragmentDialogAddBinding? = null

    private var positiveButton : Button ? = null
    private var negativeButton : Button ? = null

    private var maleta : Maletas?= null

    private var photoSelectedUri : Uri? = null

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

        activity.let {activity->
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.maleta_agregar))
                    .setPositiveButton(getString(R.string.agregar), null)
                    .setNegativeButton(getString(R.string.cancelar),null)
                    .setView(it.root)
                val dialog = builder.create()
                dialog.setOnShowListener(this)
                return dialog
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    private fun showDatePickerDialog(){
        val datepicker = DatePickerFragment { dia, mes, ano -> onDateSelected(dia, mes, ano) }
        datepicker.show(childFragmentManager,"datepicker")
    }

    fun onDateSelected(dia:Int, mes: Int, ano: Int){
        val mesCorregido = mes+1
        binding!!.etFechaViaje.setText("$dia/$mesCorregido/$ano")
    }

    override fun onShow(dialogInterface: DialogInterface?) {

        initMaleta()
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {dialogo->
            positiveButton=dialogo.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=dialogo.getButton(Dialog.BUTTON_NEGATIVE)
            binding!!.etFechaViaje.setOnClickListener{showDatePickerDialog()}
            val firebase = FirebaseAuth.getInstance()
            val usuario = firebase.currentUser
            positiveButton?.setOnClickListener{
                binding?.let{
                    enableUI(false)

                    //Carga imagen
                    //uploadImage (maleta?.id){eventPost->
                    uploadRecucedImage (maleta?.id) {eventPost->
                        if (eventPost.isSuccess){
                            if (maleta==null) {
                                dialogo.setTitle(getString(R.string.maleta_agregar))
                                val maleta = Maletas(
                                    nombre = it.etNombre.text.toString().trim(),
                                    fechaViaje = it.etFechaViaje.text.toString().trim(),
                                    emailUsuario = usuario?.email.toString(),
                                    emailCreador = usuario?.email.toString(),
                                    imgURL = eventPost.photoURL
                                )
                                //save(maleta, Utils.getAuth().currentUser!!.uid)
                                save(maleta,eventPost.documentId!!)

                            }else {
                                dialogo.setTitle(getString(R.string.maleta_actualizar))
                                maleta?.apply {
                                    nombre = it.etNombre.text.toString().trim()
                                    fechaViaje = it.etFechaViaje.text.toString().trim()
                                    imgURL = eventPost.photoURL
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

    private fun uploadImage(maletaID : String?, callback : (EvenPost)->Unit) {

        val eventPost = EvenPost()
        eventPost.documentId = maletaID ?: FirebaseFirestore.getInstance().collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas").document().id

        Utils.getAuth().currentUser.let {user ->
            val imagenRef = FirebaseStorage.getInstance().reference.child(user!!.uid + "-imagenes")

            val photoRef = imagenRef.child(eventPost.documentId!!)

            photoSelectedUri?.let { uri->
                binding?.let {binding->
                    binding.progressBar.visibility= View.VISIBLE

                    photoRef.putFile(uri)
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
                            enableUI(true)
                            Toast.makeText(activity, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                            callback(eventPost)
                        }
                }
            }
        }
    }


    private fun uploadRecucedImage(maletaID : String?, callback : (EvenPost)->Unit) {

        val eventPost = EvenPost()
        eventPost.documentId = maletaID ?: FirebaseFirestore.getInstance().collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas").document().id

        val storageRef = FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid)

        photoSelectedUri?.let { uri->
            binding?.let {binding->
                getBitmapFromUri(uri)?.let{bitmap ->

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
                            enableUI(true)
                            Toast.makeText(activity, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                            callback(eventPost)
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
            it.ibMaleta.setOnClickListener{
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun initMaleta() {
        maleta = (activity as MaletasAux)?.getMaletaSelect()
        maleta?.let { maleta->
            binding?.let {
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

    private fun save(maleta : Maletas, documentId : String){
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
            //.add(maleta)
            .document(documentId)
            .set(maleta)
            .addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.maleta_anadida_ok), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(activity, getString(R.string.maleta_anadir_error), Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener{
                enableUI(true)
                binding?.progressBar?.visibility=View.INVISIBLE
                dismiss()

            }
    }

    private fun update(maleta : Maletas){
        val db = FirebaseFirestore.getInstance()
        maleta.id?.let {id->
            db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas").document(id).set(maleta).addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.maleta_actualizada_ok), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(activity, getString(R.string.maleta_actualizar_error), Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener{
                enableUI(true)
                binding?.progressBar?.visibility=View.INVISIBLE
                dismiss()
            }
        }

    }

    private fun enableUI(enabled : Boolean){
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