package com.example.practicum7

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class DatePickerFragment : DialogFragment() {
    private val args: DatePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        // Use the date passed from TicketDetailFragment
        calendar.time = args.ticketDate

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDate = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val resultCalendar = GregorianCalendar(year, month, day)

            // Preserve the hour and minute from the original date
            val originalCalendar = Calendar.getInstance()
            originalCalendar.time = args.ticketDate
            resultCalendar.set(Calendar.HOUR_OF_DAY, originalCalendar.get(Calendar.HOUR_OF_DAY))
            resultCalendar.set(Calendar.MINUTE, originalCalendar.get(Calendar.MINUTE))

            val resultDate = resultCalendar.time

            setFragmentResult(REQUEST_KEY_DATE,
                bundleOf(BUNDLE_KEY_DATE to resultDate))
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDate
        )
    }

    companion object {
        const val REQUEST_KEY_DATE = "REQUEST_KEY_DATE"
        const val BUNDLE_KEY_DATE = "BUNDLE_KEY_DATE"
    }
}