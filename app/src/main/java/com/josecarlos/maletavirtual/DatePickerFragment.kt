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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*

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