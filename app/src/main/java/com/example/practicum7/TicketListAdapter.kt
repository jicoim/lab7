package com.example.practicum7

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.practicum7.databinding.ListItemTicketBinding
import com.example.practicum7.databinding.ListItemSeriousTicketBinding

// Normal ticket view holder
class TicketHolder(
    val binding: ListItemTicketBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ticket: Ticket) {
        binding.ticketTitle.text = ticket.title
        binding.ticketDate.text = ticket.date.toString()

        binding.root.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "${ticket.title} clicked!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

// Serious ticket view holder with "contact manager" button
class SeriousTicketHolder(
    val binding: ListItemSeriousTicketBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ticket: Ticket) {
        binding.seriousTicketTitle.text = ticket.title
        binding.seriousTicketDate.text = ticket.date.toString()

        binding.root.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "Serious ${ticket.title} clicked!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.contactManagerButton.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "Contacting manager for ${ticket.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class TicketListAdapter(
    private val tickets: List<Ticket>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Define view types
    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_SERIOUS = 1
    }

    // Implement getItemViewType to determine which view type to use
    override fun getItemViewType(position: Int): Int {
        val ticket = tickets[position]
        return if (ticket.requiresManager) {
            VIEW_TYPE_SERIOUS
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    // Create the appropriate ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_SERIOUS -> {
                val binding = ListItemSeriousTicketBinding.inflate(inflater, parent, false)
                SeriousTicketHolder(binding)
            }
            else -> {
                val binding = ListItemTicketBinding.inflate(inflater, parent, false)
                TicketHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    // Bind the ViewHolder with the appropriate data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ticket = tickets[position]

        when (holder) {
            is SeriousTicketHolder -> holder.bind(ticket)
            is TicketHolder -> holder.bind(ticket)
        }
    }
}