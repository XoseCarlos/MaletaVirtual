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

//Clase que define el modelo de UsuarioViewModel, sus atributos
//Sobreescribe los métodos toString, equals y hashcode para evitar duplicados

//No la utilizo, la descarté

import androidx.lifecycle.ViewModel
import com.josecarlos.maletavirtual.utils.Utils

class UsuarioViewModel: ViewModel() {
    val auth = Utils.getAuth()
    fun login(email:String, password: String){

    }
    fun registro(email: String, password: String){

    }
}