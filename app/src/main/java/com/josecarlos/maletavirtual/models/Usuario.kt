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

package com.josecarlos.maletavirtual.models

import com.google.firebase.firestore.Exclude
import java.util.*

data class Usuario(@get:Exclude var id: String? = null,
                   var nombre:String? = null,
                   var telefono : Int? = null,
                   var emailUsuario:String? = null,
                   var imgURL : String? = null,
                   var administrador: Boolean = false) {

    var fechaActualizacion : Date? = Date()

    constructor(): this("","",0,"","",false)

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
