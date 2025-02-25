package com.example.practicum7

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.practicum7.databinding.ListItemTicketBinding

class TicketHolder (

    val binding: ListItemTicketBinding ): RecyclerView.ViewHolder(binding.root){
        fun bind(ticket: Ticket){
            binding.ticketTitle.text = ticket.title
            binding.ticketDate.text = ticket.date.toString()

            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context,
                    "${ticket.title} clicked!",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

class TicketListAdapter(
    private val tickets: List<Ticket>
) : RecyclerView.Adapter<TicketHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHolder {
        val infaltor = LayoutInflater.from(parent.context)
        val binding = ListItemTicketBinding.inflate(infaltor,parent,false)
        return TicketHolder(binding)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    override fun onBindViewHolder(holder: TicketHolder, position: Int) {
        val ticket = tickets[position]
//        holder.apply {
//            binding.ticketTitle.text = ticket.title
//            binding.ticketDate.text = ticket.date.toString()
//        }
        holder.bind(ticket)
    }

}
