package com.example.practicum7


import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.practicum7.databinding.FragmentTicketDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.Manifest

private const val TAG = "TicketDetailFragment"
private const val DATE_FORMAT = "EEE, MMM, dd"
class TicketDetailFragment : Fragment(R.layout.fragment_ticket_detail) {
    private val args: TicketDetailFragmentArgs by navArgs()
    private val ticketDetailViewModel: TicketDetailViewModel by viewModels {
        TicketDetailViewModelFactory(args.ticketId)
    }
    private var _binding: FragmentTicketDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access the view because it is null."
        }

    private val selectAssignee = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it)}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTicketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchAssigneePhoneNumber()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            binding.apply {
                ticketTitle.doOnTextChanged { text, _, _, _ ->
                    ticketDetailViewModel.updateTicket { oldTicket ->
                        oldTicket.copy(title = text.toString())
                    }
                }

                ticketSolved.setOnCheckedChangeListener { _, isChecked ->
                    ticketDetailViewModel.updateTicket { oldTicket ->
                        oldTicket.copy(isSolved = isChecked)
                    }
                }

                ticketAssignee.setOnClickListener{
                    selectAssignee.launch(null)
                }

                val selectAssigneeIntent = selectAssignee.contract.createIntent(
                    requireContext(),
                    input = null
                )

                ticketAssignee.isEnabled = canResolveIntent(selectAssigneeIntent)

                binding.ticketCall.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        fetchAssigneePhoneNumber()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ticketDetailViewModel.ticket.collect {
                        ticket -> ticket?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            ticketDetailViewModel.updateTicket { oldTicket ->
                oldTicket.copy(date = newDate.time)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(ticket: Ticket) {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm", Locale.getDefault()) // Example: 26 Feb 2025, 14:30

        binding.apply {
            if (ticketTitle.text.toString() != ticket.title) {
                ticketTitle.setText(ticket.title)
            }

            ticketDate.text = dateFormat.format(Date(ticket.date))
            ticketDate.setOnClickListener{
                val currentDate = Date(ticket.date)

                findNavController().navigate((TicketDetailFragmentDirections.selectDate(currentDate)))
            }
            ticketSolved.isChecked = ticket.isSolved

            ticketReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getTicketReport(ticket))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.ticket_report_subject)
                    )
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )

                startActivity(chooserIntent)
            }

            ticketAssignee.text = ticket.assignee.ifEmpty {
                getString(R.string.ticket_assignee_text)
            }
        }
    }

    private fun getTicketReport(ticket: Ticket): String {
        val solvedString = if (ticket.isSolved) {
            getString(R.string.ticket_report_solved)
        } else {
            getString(R.string.ticket_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, ticket.date).toString()

        val assigneeText = if (ticket.assignee.isBlank()) {
            getString(R.string.ticket_report_no_assignee)
        } else {
            getString(R.string.ticket_report_assignee, ticket.assignee)
        }

        return getString(
            R.string.ticket_report,
            ticket.title,
            dateString,
            solvedString,
            assigneeText
        )
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val assignee = cursor.getString(0)
                ticketDetailViewModel.updateTicket { oldTicket ->
                    oldTicket.copy(assignee = assignee)
                }
            }
        }
    }

    private fun fetchAssigneePhoneNumber() {
        val assigneeName = binding.ticketAssignee.text.toString()
        if (assigneeName.isBlank() || assigneeName == getString(R.string.ticket_assignee_text)) {
            Toast.makeText(requireContext(), "No assignee selected", Toast.LENGTH_SHORT).show()
            return
        }

        val cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?",
            arrayOf(assigneeName),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                callAssignee(phoneNumber)
            } else {
                Toast.makeText(requireContext(), "No phone number found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun callAssignee(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(dialIntent)
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        return resolvedActivity != null
    }

}