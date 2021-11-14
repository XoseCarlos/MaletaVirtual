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

package com.josecarlos.maletavirtual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.adapters.ArticulosAdapter
import com.josecarlos.maletavirtual.databinding.ActivityArticulosBinding
import com.josecarlos.maletavirtual.interfaces.ArticulosAux
import com.josecarlos.maletavirtual.interfaces.OnArticuloListener
import com.josecarlos.maletavirtual.models.Articulos
import com.josecarlos.maletavirtual.models.Maletas
import com.squareup.picasso.Picasso

class ArticulosActivity : AppCompatActivity() , OnArticuloListener, ArticulosAux{

    private lateinit var binding : ActivityArticulosBinding
    private lateinit var adapter : ArticulosAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var firestorelistener : ListenerRegistration
    private var articuloSeleccionado : Articulos? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_articulos)

        binding = ActivityArticulosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //listView = binding.recyclerView
        //loadListView()

        val extras = intent.extras

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitle(getString(R.string.maleta_virtual))
        toolbar.setSubtitle(getString(R.string.maleta_articulos) + extras!!.getString("MaletaID"))

        botonAñadirArticulo()
        botonCerrarMaleta()
        btnDuplicarMaleta()

        val maletaActual = Utils.getFirestore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("maletas").document(extras.getString("MaletaID")!!)

        maletaActual.get().addOnSuccessListener {
            val us = it.toObject(Maletas::class.java)
            Picasso.get().load(us?.imgURL).error(R.drawable.maletas3_little).into(binding.imagenMaleta)
            binding.txtNombreMaleta.setText(us!!.nombre)
            binding.txtFechaViaje.setText("Viaje: ${us!!.fechaViaje}")
        }

        if (!extras.getBoolean("MaletaActiva")){
            binding.btnAAdirArticulo.visibility= View.INVISIBLE
            binding.btnCerrarMaleta.visibility=View.INVISIBLE
            binding.btnDuplicarMaleta.visibility=View.VISIBLE
        }else{
            binding.btnAAdirArticulo.visibility= View.VISIBLE
            binding.btnCerrarMaleta.visibility=View.VISIBLE
            binding.btnDuplicarMaleta.visibility=View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        configFireStoreRealTime()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    //Para carga de articulos en tiempo real
    private fun configFireStoreRealTime(){
        val extras = intent.extras
        adapter = ArticulosAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ArticulosActivity, 1,
                GridLayoutManager.VERTICAL, false)
            adapter = this@ArticulosActivity.adapter
        }

        val db = Utils.getFirestore()
        val articuloRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
            .document(extras?.getString("MaletaID")!!).collection("articulos")

        firestorelistener = articuloRef.addSnapshotListener{snapshots, error->
            if (error!=null){
                Toast.makeText(this, getString(R.string.error_consultar_datos), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (articulos in snapshots!!.documentChanges){
                val articulo = articulos.document.toObject(Articulos::class.java)
                            articulo.id = articulos.document.id
                    when(articulos.type){
                        DocumentChange.Type.ADDED -> adapter.add(articulo)
                        DocumentChange.Type.MODIFIED -> adapter.update(articulo)
                        DocumentChange.Type.REMOVED -> adapter.delete(articulo)
                   }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onLongClick(articulo: Articulos) {
        val extras = intent.extras
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.eliminar_articulo_pregunta))
            .setMessage(getString(R.string.advertencia_eliminar_articulo))
            .setPositiveButton(getString(R.string.confirmar)){_,_->
                val db = FirebaseFirestore.getInstance()
                val maletaRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
                    .document(extras?.getString("MaletaID")!!).collection("articulos")
                articulo.id?.let { id ->
                    maletaRef.document(id)
                        .delete()
                        .addOnFailureListener{
                            Toast.makeText(this, getString(R.string.error_eliminar), Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    override fun onBorrarClick(articulo: Articulos) {
        val extras = intent.extras
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.eliminar_articulo_pregunta))
            .setMessage(getString(R.string.advertencia_eliminar_articulo))
            .setPositiveButton(getString(R.string.confirmar)){ _, _->

                val db = FirebaseFirestore.getInstance()
                val articuloRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
                    .document(extras?.getString("MaletaID")!!).collection("articulos")

                articulo.id?.let { id ->

                    articulo.imgURL?.let { url ->

                        if (!url.equals("null")) {

                            val fotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

                            //FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid + "-imagenes").child(id)
                            fotoRef
                                .delete().addOnSuccessListener {
                                    articuloRef.document(id)
                                        .delete()
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                this,getString(R.string.error_eliminar),Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this,getString(R.string.eliminar_registro_error),Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            articuloRef.document(id)
                                .delete()
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this, getString(R.string.error_eliminar),Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancelar),null)
            .show()
    }


    private fun update(articulo: Articulos){
        val extras = intent.extras
        val db = FirebaseFirestore.getInstance()
        articulo.id?.let { id->
            db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas").document(extras?.getString("MaletaID")!!)
                .collection("articulos").document(id).set(articulo).addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.articulo_actualizado), Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{
                    Toast.makeText(this, getString(R.string.error_actualizar_articulo), Toast.LENGTH_SHORT).show()
                }.addOnCompleteListener{
                    //No hacer nada
                }
        }

    }

    override fun onPause() {
        super.onPause()
        firestorelistener.remove()
    }

    override fun getArticuloSelect(): Articulos? = articuloSeleccionado

    override fun onImageClick(articulo: Articulos) {
        val extras = intent.extras
        articuloSeleccionado = articulo
        AddDialogArticuloFragment(extras?.getString("MaletaID")!!).show(supportFragmentManager, AddDialogArticuloFragment::class.java.simpleName)
    }

    override fun onClick(articulo: Articulos) {
        //Para probar el id del artículo que estoy seleccionando
        //Toast.makeText(this, articulo.id, Toast.LENGTH_SHORT).show()
    }

    override fun onComprobadoClick(articulo: Articulos) {
        val extras = intent.extras
        var comprobadoCheck = findViewById<CheckBox>(R.id.checkBoxComprobado)
        var mensaje = comprobadoCheck.isChecked.toString()
        //articulo.comprobado=comprobadoCheck.isChecked
        //Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

        articulo?.apply {
            nombre = this.nombre
            cantidad = this.cantidad
            imgURL = this.imgURL
            id= this.id
            emailCreador = this.emailUsuario
            emailCreador = this.emailCreador
            comprobado = comprobadoCheck.isChecked
            update(this)
        }
    }

    private fun botonAñadirArticulo(){
        val extras = intent.extras
        binding.btnAAdirArticulo.setOnClickListener{
            articuloSeleccionado=null
            var args = Bundle()
            args.putString("maletaID", extras?.getString("MaletaID")!!)
            AddDialogArticuloFragment(extras?.getString("MaletaID")!!).show(supportFragmentManager,AddDialogArticuloFragment::class.java.simpleName, )
        }
    }

    private fun botonCerrarMaleta(){
        binding.btnCerrarMaleta.setOnClickListener {
            val extras = intent.extras
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.cerrar_maleta_pregunta))
                .setMessage(getString(R.string.maleta_cerrar_advertencia))
                .setPositiveButton(getString(R.string.confirmar)){_,_->

                    val maletaActual = Utils.getFirestore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("maletas").document(extras?.getString("MaletaID")!!)

                    maletaActual.get().addOnSuccessListener {
                        val us = it.toObject(Maletas::class.java)
                        us?.apply {
                            id = us.id
                            nombre = us.nombre
                            emailCreador = us.emailCreador
                            emailUsuario = us.emailUsuario
                            fechaViaje = us.fechaViaje
                            imgURL = us.imgURL
                            comprobado= us.comprobado
                            activa = false
                            maletaActual.set(this)
                            Toast.makeText(this@ArticulosActivity, getString(R.string.maleta_cerrar_correcto), Toast.LENGTH_SHORT).show()
                        }

                        val articulosMaleta = Utils.getFirestore().collection("usuarios").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("maletas").document(extras?.getString("MaletaID")!!).collection("articulos")
                        articulosMaleta.get().addOnSuccessListener {documentos->
                                for (documento in documentos){
                                    val articulos = documento.toObject(Articulos::class.java)
                                    Toast.makeText(this, articulos.nombre, Toast.LENGTH_SHORT).show()
                                    articulos?.apply {
                                        id = articulos.id
                                        nombre = articulos.nombre
                                        emailCreador = articulos.emailCreador
                                        emailUsuario = articulos.emailUsuario
                                        cantidad = articulos.cantidad
                                        imgURL = articulos.imgURL
                                        cerrado = true
                                        comprobado = articulos.comprobado
                                        articulosMaleta.document(documento.id).set(articulos)
                                    }
                                }
                            }
                    }
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(getString(R.string.cancelar),null)
                .show()
        }

    }

    private fun btnDuplicarMaleta(){
        val extras = intent.extras
        binding.btnDuplicarMaleta.setOnClickListener{
            var args = Bundle()
            args.putString("maletaID", extras?.getString("MaletaID")!!)
            Toast.makeText(this, getString(R.string.maleta_duplicar_pendiente), Toast.LENGTH_SHORT).show()
            AddDialogDuplicarMaletaFragment(extras?.getString("MaletaID")!!).show(supportFragmentManager,AddDialogDuplicarMaletaFragment::class.java.simpleName, )
        }
    }
}