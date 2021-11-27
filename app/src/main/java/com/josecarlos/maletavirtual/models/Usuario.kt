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

package com.josecarlos.maletavirtual.models

import com.google.firebase.firestore.Exclude
import java.util.*

//Clase que define el modelo de Usuarios, sus atributos
//Sobreescribe los métodos toString, equals y hashcode para evitar duplicados

data class Usuario(//@get:Exclude var id: String? = null,
                   var id: String? = null,
                   var nombre:String? = null,
                   var telefono : Int? = null,
                   var emailUsuario:String? = null,
                   var imgURL : String? = null,
                   //var compartidas : MutableList<String>?,
                   var administrador: Boolean = false) {

    var fechaActualizacion : Date? = Date()
    //var compartidas : MutableList<String> = mutableListOf()

    constructor(): this("","",0,"","")

    override fun toString(): String {
        return "Usuario: id ($id), nombre ($nombre), usuario: ($emailUsuario), fecha alta ($fechaActualizacion)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usuario

        if (id != other.id) return false
        if (emailUsuario != other.emailUsuario) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (emailUsuario?.hashCode() ?: 0)
        return result
    }

}
