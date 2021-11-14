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

import java.util.*

data class Articulos(var id: String? = null,  //@get:Exclude le quito esto para poder duplicar la maleta y su contenido
                     var nombre:String? = null,
                     var emailUsuario:String? = null,
                     var emailCreador: String? = null,
                     var cantidad: Int? = -1,
                     var imgURL : String? = null,
                     var cerrado : Boolean? = false,
                     var comprobado: Boolean? = false) {

    var fechaCreacion : Date? = Date()

    override fun toString(): String {
        return "Artículo: id ($id), nombre ($nombre), controlada ($comprobado), usuario: ($emailUsuario), creador: ($emailCreador), fecha fotografía ($fechaCreacion)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Articulos

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}