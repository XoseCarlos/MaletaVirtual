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


package com.josecarlos.maletavirtual.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.josecarlos.maletavirtual.R
import com.josecarlos.maletavirtual.databinding.FragmentCambioContrasenaBinding

class CambioContrasenaFragment: DialogFragment(), DialogInterface.OnShowListener {

    private var binding : FragmentCambioContrasenaBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var positiveButton : Button? = null
    private var negativeButton : Button? = null

    /**
     * Método que se ejecuta cuando se abre el diágolo
     */

    override fun onShow(dialogInterface: DialogInterface?) {

        val dialog = dialog as? AlertDialog
        dialog?.let {dialogo->
            firebaseAuth = FirebaseAuth.getInstance()
            positiveButton=dialogo.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=dialogo.getButton(Dialog.BUTTON_NEGATIVE)
            val firebase = FirebaseAuth.getInstance()
            positiveButton?.setOnClickListener{
                binding?.let{
                    habilitarInterfaz(false)

                    if (binding!!.etConfirmaContrasena.text.isNullOrEmpty() || binding!!.etNuevaContrasena.text.isNullOrEmpty()){
                        Toast.makeText(this.requireContext(), "Debe introducir una contraseña", Toast.LENGTH_SHORT).show()
                        habilitarInterfaz(true)
                    }else{
                        val contrasenaNueva = binding!!.etNuevaContrasena.text.toString()
                        val contrasenaConfirmada = binding!!.etConfirmaContrasena.text.toString()

                        if (contrasenaNueva.length<6){
                            Toast.makeText(this.context, getString(R.string.largo_contrasena), Toast.LENGTH_SHORT).show()
                            habilitarInterfaz(true)
                            return@setOnClickListener
                        }

                        if (contrasenaConfirmada.equals(contrasenaNueva)) {
                             val usuario = firebaseAuth.currentUser
                            if (usuario != null) {
                                usuario.updatePassword(contrasenaNueva)
                                    .addOnSuccessListener {
                                        Toast.makeText(this.context, getString(R.string.contrasena_actualizada), Toast.LENGTH_SHORT).show()
                                        dismiss()
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(this.context, getString(R.string.contrasena_actualizada_error), Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnCompleteListener{
                                        habilitarInterfaz(true)
                                    }
                            }
                        }else{
                            Toast.makeText(this.context, "Las contraseñas no coinciden... Inténtelo otra vez", Toast.LENGTH_SHORT).show()
                            habilitarInterfaz(true)
                        }
                    }
                }
            }
            negativeButton?.setOnClickListener{
                dismiss()
            }
        }
    }

    /**
     * Sobreescritura del método onCreate, que crea toda la interfaz del diálogo
     */

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity.let { activity ->
            binding = FragmentCambioContrasenaBinding.inflate(LayoutInflater.from(context))
            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.cambio_contrasena))
                    .setPositiveButton(getString(R.string.cambiar), null)
                    .setNegativeButton(getString(R.string.cancelar), null)
                    .setView(it.root)
                val dialog = builder.create()
                dialog.setOnShowListener(this)
                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    /**
     * Metodo que habilita o inhabilita botones y campos cuando se están ejecutando determinados procesos, para evitar problemas
     */

    private fun habilitarInterfaz(enabled : Boolean){
        positiveButton?.isEnabled = enabled
        negativeButton?.isEnabled = enabled
        binding?.let {
            with(it) {
                it.etConfirmaContrasena.isEnabled = enabled
                it.etNuevaContrasena.isEnabled = enabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}