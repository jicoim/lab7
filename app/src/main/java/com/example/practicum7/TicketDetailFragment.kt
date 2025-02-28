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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "TicketDetailFragment"
private const val DATE_FORMAT = "EEE, MMM, dd"
class TicketDetailFragment : Fragment(R.layout.fragment_ticket_detail) {
    private var photoName: String? = null
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

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            ticketDetailViewModel.updateTicket { oldTicket ->
                oldTicket.copy(photoFileName = photoName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTicketDetailBinding.inflate(inflater, container, false)
        return binding.root
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

                ticketCamera.setOnClickListener {
                    photoName = "IMG_${Date()}.JPG"
                    val photoFile = File(requireContext().applicationContext.filesDir, photoName)
                    val photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.iub.lab7.fileprovider",
                        photoFile
                    )
                    takePhoto.launch(photoUri)
                }

                // Add click listener for the photo to show it in a dialog
                ticketPhoto.setOnClickListener {
                    ticketDetailViewModel.ticket.value?.photoFileName?.let { photoFileName ->
                        val photoFile = File(requireContext().applicationContext.filesDir, photoFileName)
                        if (photoFile.exists()) {
                            val dialog = PhotoViewerFragment.newInstance(photoFile.path)
                            dialog.show(parentFragmentManager, "photo_dialog")
                        }
                    }
                }

                val captureImageIntent = takePhoto.contract.createIntent(
                    requireContext(),
                    Uri.parse("")
                )

                ticketCamera.isEnabled = canResolveIntent(captureImageIntent)
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

            updatePhoto(ticket.photoFileName)
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

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        return resolvedActivity != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.ticketPhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                binding.ticketPhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.ticketPhoto.setImageBitmap(scaledBitmap)
                    binding.ticketPhoto.tag = photoFileName
                }
            } else {
                binding.ticketPhoto.setImageBitmap(null)
                binding.ticketPhoto.tag = null
            }
        }
    }
}