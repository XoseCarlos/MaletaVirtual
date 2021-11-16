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

import android.app.Application
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Utils : Application() {
    companion   object{
        fun getAuth(): FirebaseAuth{
            return FirebaseAuth.getInstance()
        }

        fun getFirestore () : FirebaseFirestore{
            return FirebaseFirestore.getInstance()
        }

        fun getStorageUsuario() : StorageReference {
            return FirebaseStorage.getInstance().getReference().child(getUsuarioLogeado())
        }

        fun getUsuarioLogeado() : String {
            return getAuth().currentUser!!.uid.toString()
        }

        fun getRaizFBUsuarioLogeado() : DocumentReference{
            return FirebaseFirestore.getInstance().collection("usuarios").document(getUsuarioLogeado())
        }

        fun getCarpetaFBUsuarioLogueado(maleta : String, articulo : String){
            if (articulo.equals("")){
                getRaizFBUsuarioLogeado().collection("maletas").document(maleta)
            }else{
                getRaizFBUsuarioLogeado().collection("maletas").document(maleta).collection("articulos").document(articulo)
            }
        }

    }
}