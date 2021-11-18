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

package com.josecarlos.maletavirtual.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class Utils : Application() {

    companion   object{
        fun getAuth(): FirebaseAuth{
            return FirebaseAuth.getInstance()
        }

        fun getFirestore () : FirebaseFirestore{
            return FirebaseFirestore.getInstance()
        }

        fun getStorageUsuario() : StorageReference {
            return FirebaseStorage.getInstance().getReference().child(getIdUsuarioLogeado())
        }

        fun getUsuarioLogueado() : FirebaseUser {
            return getAuth().currentUser!!
        }

        fun getIdUsuarioLogeado() : String {
            return getAuth().currentUser!!.uid.toString()
        }

        fun getRaizFBUsuarioLogeado() : DocumentReference{
            return FirebaseFirestore.getInstance().collection("usuarios").document(
                getIdUsuarioLogeado()
            )
        }

        fun getCarpetaFBUsuarioLogueado(maleta : String, articulo : String){
            if (articulo.equals("")){
                getRaizFBUsuarioLogeado().collection("maletas").document(maleta)
            }else{
                getRaizFBUsuarioLogeado().collection("maletas").document(maleta).collection("articulos").document(articulo)
            }
        }

        //Extensións:

        fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, duration).show()

        fun Activity.toast(resourceId: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, resourceId, duration).show()

        fun ViewGroup.inflate(layoutId: Int) = LayoutInflater.from(context).inflate(layoutId, this, false)!!

        inline fun <reified T : Activity> Activity.goToActivity(noinline init: Intent.() -> Unit = {}) {
            val intent = Intent(this, T::class.java)
            intent.init()
            startActivity(intent)
        }

        inline fun <reified T : Activity> Activity.goToActivity(activas : Boolean, noinline init: Intent.() -> Unit = {}) {
            val intent = Intent(this, T::class.java)
            intent.putExtra("Activas", activas)
            intent.init()
            startActivity(intent)
        }

        fun EditText.validate(validation: (String) -> Unit) {
            this.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) {
                    validation(editable.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }

        fun Activity.isValidEmail(email: String): Boolean {
            val pattern = Patterns.EMAIL_ADDRESS
            return pattern.matcher(email).matches()
        }

        fun Activity.isValidPassword(password: String): Boolean {
            // Necesita Contener -->    1 Num / 1 Minuscula / 1 Mayuscula / 1 Special / Min Caracteres 4
            //val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
            //val pattern = Pattern.compile(passwordPattern)
            return password.length>5
            //return pattern.matcher(password).matches()
        }

        fun Activity.isValidConfirmPassword(password: String, confirmPassword: String): Boolean {
            return password == confirmPassword
        }

        fun Fragment.hideKeyboard() {
            view?.let { activity?.hideKeyboard(it) }
        }

        fun Activity.hideKeyboard() {
            hideKeyboard(currentFocus ?: View(this))
        }

        fun Context.hideKeyboard(view: View) {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }


        //FUNCIÓNES QUE SE INVOCAN MÁS DE UNA VEZ

        //Función que comprueba si el texto introducido por parámetro
        //es un número, simplemente utilizando un try - catch
        fun esNumero(texto : String) : Boolean{
            try{
                var valor = Integer.parseInt(texto)
                return true
            }catch (e: Exception){
                return false
            }
        }

        //Creo un nombre de imagen que se corresponde con la fecha actual en milisegundos (irrepetible, espero)
        //En este caso porque solo un usuario puede añadir artículos, pero si fueran varios los que trabajaran
        //contra la misma base de datos esto no valdría. Tendría que añadir antes el id del usiario y ya lo haría único
        fun crearImagenJpg() : String {
            val fecha = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg"
            val archivo = getAuth().currentUser!!.uid +"-"+ System.currentTimeMillis().toString()
            return archivo
        }

        /*fun crarImagenJpg() : String {
            val fecha = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg"
            val nombre = System.currentTimeMillis().toString()
            return nombre
        }*/

        /*fun crearImagen () : File {
            val nombreImagen = "foto_" + Utils.getAuth().currentUser!!.uid.toString()
            val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            //try {
            val imagen = File.createTempFile(nombreImagen, ".jpg", directorio)
            rutaImagen = imagen.absolutePath
            //return imagen
            //}catch (ex : Exception){
            //Toast.makeText(this, rutaImagen.toString(), Toast.LENGTH_SHORT).show()
            //}
            return imagen
        }*/
    }
}