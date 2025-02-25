package com.example.practicum7
import androidx.lifecycle.ViewModel
import java.util.Date
import java.util.UUID

class TicketListViewModel: ViewModel() {
    val tickets = mutableListOf<Ticket>()

    init{
        for (i in 0 until 100){
            val ticket = Ticket(
                id = UUID.randomUUID(),
                title = "ticket #$i",
                date = Date(),
                isSolved = i%2 == 0,
                // Make every third ticket require manager intervention
                requiresManager = i%3 == 0
            )
            tickets+=ticket
        }
    }
}