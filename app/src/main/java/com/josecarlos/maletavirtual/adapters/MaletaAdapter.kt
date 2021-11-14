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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.josecarlos.maletavirtual.models.Maletas
import com.josecarlos.maletavirtual.interfaces.OnMaletaListener
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.databinding.ItemMaletaBinding
import java.time.LocalDate


open class MaletaAdapter(private val maletaList: MutableList<Maletas>,
                    private val listener: OnMaletaListener)
    : RecyclerView.Adapter<MaletaAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_maleta, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val maleta = maletaList[position]

        holder.setListener(maleta)

        holder.binding.tvName.text = maleta.nombre
        holder.binding.tvFechaViaje.text = maleta.fechaViaje.toString()
        Glide.with(context)
            .load(maleta.imgURL)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.ic_time_access)
            .error(R.drawable.ic_broken_image)
            .into(holder.binding.imgProduct)
    }

    override fun getItemCount(): Int = maletaList.size

    fun add(maleta: Maletas){
        if (!maletaList.contains(maleta)){
            maletaList.add(maleta)
            maletaList.sortBy {
                it.nombre
            }
            notifyItemInserted(maletaList.size - 1)
        }else{
            update(maleta)
        }
    }

    fun update(maleta: Maletas){
        val index = maletaList.indexOf(maleta)
        if (index!=-1){
            maletaList.set(index, maleta)
            maletaList.sortBy {
                it.nombre
            }
            notifyItemChanged(index)
        }
    }

    fun delete(maleta: Maletas){
        val index = maletaList.indexOf(maleta)
        if (index!=-1){
            maletaList.removeAt(index)
            maletaList.sortBy {
                it.nombre
            }
            notifyItemRemoved(index)
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemMaletaBinding.bind(view)


        fun setListener(maleta: Maletas){
            /*binding.root.setOnClickListener {
                listener.onClick(maleta)
            }*/

            if (maleta.activa==false){
                binding.btnEditar.visibility=View.INVISIBLE
                binding.btnBorrar.visibility=View.VISIBLE
            }else{
                binding.btnEditar.visibility=View.VISIBLE
                binding.btnBorrar.visibility=View.VISIBLE
            }

            binding.btnBorrar.setOnClickListener{
                listener.onBorrarClick(maleta)
            }

            binding.btnEditar.setOnClickListener{
                listener.onClick(maleta)
            }

            binding.imgProduct.setOnClickListener() {
                listener.onImageClick(maleta)
            }
        }
    }
}