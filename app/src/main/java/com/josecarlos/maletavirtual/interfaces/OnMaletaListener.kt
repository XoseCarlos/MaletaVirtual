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

package com.josecarlos.maletavirtual.interfaces

import com.josecarlos.maletavirtual.models.Maletas

//Interface para implementar los métodos para operar con las maletas en la MaletasActivity

interface OnMaletaListener {

    fun onClick(maleta: Maletas)
    fun onLongClick(maleta: Maletas)
    fun onImageClick(maleta: Maletas)
    fun onBorrarClick(maleta:Maletas)

}