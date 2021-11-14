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

data class Maletas(@get:Exclude var id: String? = null,
                   var nombre:String? = null,
                   var emailUsuario:String? = null,
                   var emailCreador: String? = null,
                   var fechaViaje: String? = null,
                   var imgURL : String? = null,
                   var activa : Boolean? = true,
                   var comprobado: Boolean? = false) {

    var fechaCreacion : Date? = Date()
    /*
        constructor(id:Long, nombre:String, cantidad: Int, comprobado: Boolean, emailUsucario: String, fotografia: File, emailCreador: String, fechaCreacion: Date) : this(nombre, cantidad, emailUsucario, fotografia, emailCreador) {
            this.id = id;
            this.fechaCreacion = fechaCreacion
            this.comprobado = comprobado
    }*/

    override fun toString(): String {
        return "Maleta: id ($id), nombre ($nombre), controlada ($comprobado), usuario: ($emailUsuario), creador: ($emailCreador), fecha fotografía ($fechaCreacion)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Maletas

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
