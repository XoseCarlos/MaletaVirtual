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

import androidx.lifecycle.ViewModel
import com.josecarlos.maletavirtual.Utils

class UsuarioViewModel: ViewModel() {
    val auth = Utils.getAuth()
    fun login(email:String, password: String){

    }
    fun registro(email: String, password: String){

    }
}