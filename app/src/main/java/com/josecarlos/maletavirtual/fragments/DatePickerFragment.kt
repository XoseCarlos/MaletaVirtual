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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*

/**
 * Clase que gestiona el DataPicker
 * Impide que pueda seleccionar una fecha a la actual del sistema
 * No permite que se pueda seleccionar una fecha superior en dos años a la actual del sistema
 */

class DatePickerFragment(val listener : (dia : Int, mes: Int, ano: Int) -> Unit) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        val c = Calendar.getInstance()
        val dia = c.get(Calendar.DAY_OF_MONTH)
        val ano = c.get(Calendar.YEAR)
        val mes = c.get(Calendar.MONTH)
        //val picker = DatePickerDialog(activity as Context,R.style.datePickerTheme,this,ano, mes, dia)
        val picker = DatePickerDialog(activity as Context,this,ano, mes, dia)
        picker.datePicker.minDate=c.timeInMillis
        c.add(Calendar.YEAR,+2)
        picker.datePicker.maxDate=c.timeInMillis
        return picker
    }
    override fun onDateSet(view: DatePicker?, ano: Int, mes: Int, dia: Int) {
        listener(dia, mes, ano)
    }
}