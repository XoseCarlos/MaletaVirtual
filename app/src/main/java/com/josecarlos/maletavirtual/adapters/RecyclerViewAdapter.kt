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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.josecarlos.maletavirtual.databinding.ItemArticulosRecyclerviewBinding
import com.josecarlos.maletavirtual.models.Maletas

class RecyclerViewAdapter(val maletas : List<Maletas>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    //var maletas = mutableListOf<Maletas>()

   /* fun recyclerViewAdapter(maletas: List<Maletas>){
        this.maletas = maletas as MutableList<Maletas>
    }
*/
    fun deteleMaleta (maleta: Maletas){
        //Eliminamos
    }

    class ViewHolder private constructor(val binding: ItemArticulosRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root){

        fun rellenarDatos(maletas : Maletas){

            binding.nombre.text = maletas.nombre.toString()
            binding.txtCantidad.text = maletas.fechaViaje.toString()
        }

        companion object{
            fun crearViewHolder(parent:ViewGroup):ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemArticulosRecyclerviewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = ViewHolder.crearViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.rellenarDatos(maletas[position])

    override fun getItemCount() = maletas.size
}