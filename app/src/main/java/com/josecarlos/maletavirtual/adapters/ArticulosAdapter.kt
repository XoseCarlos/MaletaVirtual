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

package com.josecarlos.maletavirtual.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.databinding.ItemArticulosRecyclerviewBinding
import com.josecarlos.maletavirtual.interfaces.OnArticuloListener
import com.josecarlos.maletavirtual.models.Articulos


class ArticulosAdapter(private val articulosList: MutableList<Articulos>,
                       private val listener: OnArticuloListener)
    : RecyclerView.Adapter<ArticulosAdapter.ViewHolder>() {

     private lateinit var context: Context

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_articulos_recyclerview, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val articulo = articulosList[position]

        holder.setListener(articulo)

        holder.binding.nombre.text = articulo.nombre.toString()
        holder.binding.txtCantidad.text = articulo.cantidad.toString()
        holder.binding.checkBoxComprobado.isChecked = articulo.comprobado!!
        Glide.with(context)
            .load(articulo.imgURL)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.ic_time_access)
            .error(R.drawable.ic_broken_image)
            .into(holder.binding.imageViewInfoAvatar)
    }

    override fun getItemCount(): Int = articulosList.size

    fun add(articulo: Articulos){
        if (!articulosList.contains(articulo)){
            articulosList.add(articulo)
            /*articulosList.sortBy {
                it.fechaCreacion
            }*/
            notifyItemInserted(articulosList.size - 1)
        }else{
            update(articulo)
        }
    }

    fun update(articulo: Articulos){
        val index = articulosList.indexOf(articulo)
        if (index!=-1){
            articulosList.set(index, articulo)
            notifyItemChanged(index)
        }
    }

    fun delete(articulo: Articulos){
        val index = articulosList.indexOf(articulo)
        if (index!=-1){
            articulosList.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemArticulosRecyclerviewBinding.bind(view)

        fun setListener(articulo: Articulos){

            binding.checkBoxComprobado.isEnabled = articulo.cerrado != true
            binding.btnBorrarArticulo.isVisible = articulo.cerrado != true

            binding.btnBorrarArticulo.setOnClickListener {
                if (articulo.cerrado==false) listener.onBorrarClick(articulo)
            }

            binding.imageViewInfoAvatar.setOnClickListener() {
                if (articulo.cerrado==false) listener.onImageClick(articulo)
            }

            binding.checkBoxComprobado.setOnClickListener(){
                if (articulo.cerrado==false) listener.onComprobadoClick(articulo)
            }
        }
    }
}