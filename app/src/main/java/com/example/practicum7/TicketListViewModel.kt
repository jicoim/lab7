package com.example.practicum7
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val TAG = "TicketListViewModel"

class TicketListViewModel: ViewModel() {
    private val ticketRepository = TicketRepository.get()


    private val _tickets: MutableStateFlow<List<Ticket>> = MutableStateFlow(emptyList())
    val tickets: StateFlow<List<Ticket>>
        get() = _tickets.asStateFlow()

    init {
        viewModelScope.launch {
            ticketRepository.getTickets().collect{
                _tickets.value = it
            }
        }

    }

}