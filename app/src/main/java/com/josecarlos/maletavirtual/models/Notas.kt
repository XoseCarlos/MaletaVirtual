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

//Clase que define el modelo de Notas, sus atributos
//Sobreescribe los métodos toString, equals y hashcode para evitar duplicados

//No se utiliza, ya que está pensada su implementación en una versión posterior de la aplicación

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