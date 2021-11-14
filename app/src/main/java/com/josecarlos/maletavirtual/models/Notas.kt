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

data class Notas(@get:Exclude var id: String? = null,
                 var nota:String? = null,
                 var emailUsuario:String? = null){

    var fechaCreacion : Date? = Date()

    override fun toString(): String {
        return "Maleta: id ($id), nota ($nota), fecha ($fechaCreacion)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notas

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}