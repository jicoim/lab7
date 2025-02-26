package com.example.practicum7
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG = "TicketListViewModel"

class TicketListViewModel: ViewModel() {
    val tickets = mutableListOf<Ticket>()

    init {
        Log.d(TAG,"init starting")
        viewModelScope.launch {
            Log.d(TAG,"coroutine launched")
            tickets += loadTickets()
            Log.d(TAG,"Loading tickets finished")
        }

    }


suspend fun loadTickets():List<Ticket>{
    val result = mutableListOf<Ticket>()
    delay(5000)

    for (i in 0 until 100){
        val ticket = Ticket(
            id = UUID.randomUUID(),
            title = "ticket #$i",
            date = Date(),
            isSolved = i % 2 == 0
        )
        result += ticket
    }
    return result
}
}