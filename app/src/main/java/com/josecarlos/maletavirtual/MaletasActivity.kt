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
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.josecarlos.maletavirtual.adapters.MaletaAdapter
import com.josecarlos.maletavirtual.adapters.RecyclerViewAdapter
import com.josecarlos.maletavirtual.databinding.ActivityMaletasBinding
import com.josecarlos.maletavirtual.interfaces.MaletasAux
import com.josecarlos.maletavirtual.interfaces.OnMaletaListener
import com.josecarlos.maletavirtual.models.Maletas

class MaletasActivity : AppCompatActivity() , OnMaletaListener, MaletasAux {

    private lateinit var binding: ActivityMaletasBinding
    private lateinit var adapter: MaletaAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var firestorelistener : ListenerRegistration
    private var maletaSeleccionada : Maletas? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaletasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitle(getString(R.string.maleta_virtual))


        val extras = intent.extras
        if (extras!!.getBoolean("Activas")) {
            toolbar.setSubtitle(getString(R.string.activas))
        }else {
            toolbar.setSubtitle(getString(R.string.cerradas))
            binding.anadirMaletaButton.visibility= View.INVISIBLE
        }

        botonAnadirMaleta()
        //configRecyclerView()
        //Esta funciona correctamente
        //configFireStore()
        //Detecta los cambios en tiempo real y los añade al listado. Se comenta porque se inicia en el onResume
        //configFireStoreRealTime()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    private fun botonAnadirMaleta(){
        binding.anadirMaletaButton.setOnClickListener{
            maletaSeleccionada=null
            AddDialogFragment().show(supportFragmentManager,AddDialogFragment::class.java.simpleName)
        }
    }

    private fun createRecyclerView(){
        val mAdapter = RecyclerViewAdapter(cargarMaletas())
        //adapter.recyclerViewAdapter(cargarMaletas())
        val recyclerView = binding!!.recyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MaletasActivity, RecyclerView.VERTICAL,false)
            adapter = mAdapter
        }
    }

    private fun cargarMaletas():List<Maletas>{

        val maletas = mutableListOf<Maletas>() //Lista inmutable

        maletas.add(Maletas("1", "jose carlos", "josecarlos.com", "josecarlos.com", "01/01/2020", "44", true, false))
        maletas.add(Maletas("1", "jose carlos2", "josecarlos.com", "josecarlos.com", "01/01/2020", "44", true, false))
        maletas.add(Maletas("1", "jose carlos3", "josecarlos.com", "josecarlos.com", "01/01/2020", "44", true, false))
        maletas.add(Maletas("1", "jose carlos4", "josecarlos.com", "josecarlos.com", "01/01/2020", "44", true, false))

        return maletas
    }

    //Para carga de articulos en tiempo real
    private fun configFireStoreRealTime(){
        val extras = intent.extras
        adapter = MaletaAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MaletasActivity, 2,
                GridLayoutManager.VERTICAL, false)
            adapter = this@MaletasActivity.adapter
        }


        val db = Utils.getFirestore()
        val maletaRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
        firestorelistener = maletaRef.addSnapshotListener{snapshots, error->
            if (error!=null){
                Toast.makeText(this, getString(R.string.consulta_datos_error), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (maletas in snapshots!!.documentChanges){
                val maleta = maletas.document.toObject(Maletas::class.java)
                if (extras!!.getBoolean("Activas") && maleta.activa==true){
                    maleta.id = maletas.document.id
                    when(maletas.type){
                        DocumentChange.Type.ADDED -> adapter.add(maleta)
                        DocumentChange.Type.MODIFIED -> adapter.update(maleta)
                        DocumentChange.Type.REMOVED -> adapter.delete(maleta)
                    }
                }
                if (!extras!!.getBoolean("Activas") && maleta.activa==false){
                    maleta.id = maletas.document.id
                    when(maletas.type){
                        DocumentChange.Type.ADDED -> adapter.add(maleta)
                        DocumentChange.Type.MODIFIED -> adapter.update(maleta)
                        DocumentChange.Type.REMOVED -> adapter.delete(maleta)
                    }
                }
            }
        }

    }

    private fun configFireStore() {

        adapter = MaletaAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MaletasActivity, 2,
                GridLayoutManager.VERTICAL, false)
            adapter = this@MaletasActivity.adapter
        }

        Utils.getFirestore().collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas").get()
            .addOnSuccessListener { snapshots ->
                for (maletas in snapshots){
                    val maleta = maletas.toObject(Maletas::class.java)

                    if (maleta.activa==true){
                        //Para agregar toda la lista de maletas poner la siguiente línea
                        maleta.id=maletas.id
                        adapter.add(maleta)
                    }else{
                        //No hace nada
                    }
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, getString(R.string.consulta_datos_error), Toast.LENGTH_SHORT).show()
            }

          /*
        //val db = FirebaseFirestore.getInstance()
        //val userID = FirebaseAuth.getInstance().currentUser!!.uid

        db.collectionGroup("maletas").whereEqualTo("activa", true).get()
            .addOnSuccessListener { snapshots->
                for(document in snapshots){

                }
            }

        db.collection("usuarios").document(userID).collection("maletas")
            .get().addOnSuccessListener { snapshots->
                for(document in snapshots){
                    val maleta = document.toObject(Maletas::class.java)
                    maleta.id = document.id
                    adapter.add(maleta)
                }
            }.addOnFailureListener{
                Toast.makeText(this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show()
            }
        */
    }
/*
    private fun configButtons() {
        binding.anadirMaletaButton.setOnClickListener{
            maletaSeleccionada = null
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }
*/
    private fun configRecyclerView(){
        adapter = MaletaAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MaletasActivity, 2,
                GridLayoutManager.VERTICAL, false)
            adapter = this@MaletasActivity.adapter
        }

        //(1..20).forEach{
        //    val maleta = Maletas(it.toString(), "Maleta $it", "xosecarlos", "xosecarlos","01/01/2020",null,true,false)
        //    adapter.add(maleta)
        //}
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onClick(maleta: Maletas) {
        maletaSeleccionada = maleta
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    override fun onLongClick(maleta: Maletas) {
        val db = FirebaseFirestore.getInstance()
        val maletaRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
        maleta.id?.let {id ->
            maletaRef.document(id)
                .delete()
                .addOnFailureListener{
                    Toast.makeText(this, getString(R.string.eliminar_error), Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBorrarClick(maleta: Maletas) {

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.maleta_eliminar_pregunta))
            .setMessage(getString(R.string.maleta_eliminar_advertencia))
            .setPositiveButton(getString(R.string.confirmar)){_,_->

                val db = FirebaseFirestore.getInstance()
                val maletaRef = db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
                maleta.id?.let {id ->
                    maleta.imgURL?.let {url->
                        val fotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

                        //Firebase no borra las subcolecciones de un documento. Hay que recorrerlas todas para poder borrar
                        //https://firebase.google.com/docs/firestore/manage-data/delete-data?hl=es-419#collections

                        db.collection("usuarios").document(Utils.getAuth().currentUser!!.uid).collection("maletas")
                            .document(maleta.id.toString()).collection("articulos").get().addOnSuccessListener { snapshots ->
                                for (articulos in snapshots) {
                                    maletaRef.document(maleta.id.toString()).collection("articulos").document(articulos.id).delete()
                                }
                            }



                        //Borro todos los archivos del Storage para no ocupar espacio. El borrado es permanente
                        //https://firebase.google.com/docs/storage/android/list-files?hl=es
                        Utils.getStorageUsuario().child(maleta.id.toString()).listAll().addOnSuccessListener {
                            it.items.forEach{
                                it.delete()
                            }
                            Toast.makeText(this, "Borrado en el Storage", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al borrar en Storage", Toast.LENGTH_SHORT).show()
                        }



                        //FirebaseStorage.getInstance().reference.child(Utils.getAuth().currentUser!!.uid + "-imagenes").child(id)
                        fotoRef
                            .delete().addOnSuccessListener {

                                maletaRef.document(id)
                                    .delete()
                                    .addOnFailureListener{
                                        Toast.makeText(this, getString(R.string.eliminar_error), Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener{
                                Toast.makeText(this, getString(R.string.eliminar_registro_error), Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .setNegativeButton("Cancelar",null)
            .show()
}

    override fun onImageClick(maleta: Maletas) {
        //Toast.makeText(this, maleta.id, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ArticulosActivity::class.java)
        intent.putExtra("MaletaID", maleta.id)
        intent.putExtra("MaletaActiva", maleta.activa)
        startActivity(intent)
    }

    //Para liberar los listener
    override fun onResume() {
        super.onResume()
        configFireStoreRealTime()
    }

    override fun onPause() {
        super.onPause()
        firestorelistener.remove()
    }

    override fun getMaletaSelect(): Maletas? = maletaSeleccionada

}