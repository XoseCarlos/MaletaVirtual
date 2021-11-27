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


package com.josecarlos.maletavirtual.adapters

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.josecarlos.maletavirtual.databinding.ItemArticulosListViewBinding
import com.josecarlos.maletavirtual.models.Maletas

/**
 * Clase sin uso. Se mantiene por si hiciera falta
 */

class ListViewAdapter(context: Context, val vista: Int, val maletas: List<Maletas>) : ArrayAdapter<Maletas>(context, vista ,maletas) {

    //Tantas veces como elementos en la lista
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null){
            view = LayoutInflater.from(context).inflate(vista, null)
        }

        val binding = ItemArticulosListViewBinding.bind(view!!)

        binding.nombre.text = maletas[position].nombre.toString()
        binding.txtCantidad.text = maletas[position].fechaViaje.toString()

        return view

        //return super.getView(position, convertView, parent)
    }


}