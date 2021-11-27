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

//Clase que define el modelo de Maletas Compartidas, sus atributos
//Sobreescribe los métodos toString, equals y hashcode para evitar duplicados

//No se utiliza ya que al final creé un atributo en la clase Maletas, un booleano que indica si está o compartida una maleta

data class Compartida(var clave: String? = null,
                   var nombre:String? = null,
                   var emailCreador:String? = null,
                   var fechaCreacion : Date? = Date() ) {

    /*
        constructor(id:Long, nombre:String, cantidad: Int, comprobado: Boolean, emailUsucario: String, fotografia: File, emailCreador: String, fechaCreacion: Date) : this(nombre, cantidad, emailUsucario, fotografia, emailCreador) {
            this.id = id;
            this.fechaCreacion = fechaCreacion
            this.comprobado = comprobado
    }*/

    override fun toString(): String {
        return "Maleta: id ($clave), nombre ($nombre), creador: ($emailCreador), fecha fotografía ($fechaCreacion)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Compartida

        if (clave != other.clave) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clave?.hashCode() ?: 0
        result = 31 * result + (nombre?.hashCode() ?: 0)
        return result
    }

}
