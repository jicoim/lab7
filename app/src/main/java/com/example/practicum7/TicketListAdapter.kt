package com.example.practicum7


import android.app.ProgressDialog.show
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.practicum7.databinding.ListItemTicketBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TicketHolder(
    val binding: ListItemTicketBinding,
    val onTicketClicked: (ticketId: UUID) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    private val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy, HH:mm", Locale.getDefault()) // Format

    fun bind(ticket: Ticket) {
        binding.ticketTitle.text = ticket.title
        binding.ticketDate.text = dateFormat.format(Date(ticket.date))

        binding.root.setOnClickListener {
            onTicketClicked(ticket.id)
        }
        binding.ticketSolved.visibility = if (ticket.isSolved) {
            View.VISIBLE
        } else {
            View.GONE
        }

    }

}


class TicketListAdapter(
    private val tickets: List<Ticket>,
    private val onTicketClicked: (ticketId: UUID) -> Unit
) : RecyclerView.Adapter<TicketHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding = ListItemTicketBinding.inflate(inflator, parent, false)
        return TicketHolder(binding, onTicketClicked)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    override fun onBindViewHolder(holder: TicketHolder, position: Int) {
        val ticket = tickets[position]
        holder.bind(ticket)
    }
}