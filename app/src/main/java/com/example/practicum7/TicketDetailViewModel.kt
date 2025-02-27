package com.example.practicum7

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class TicketDetailViewModel(ticketId: UUID?) : ViewModel() {
    private val ticketId = ticketId
    private val ticketRepository = TicketRepository.get()
    private val _ticket: MutableStateFlow<Ticket?> = MutableStateFlow(value = null)
    val ticket: StateFlow<Ticket?> = _ticket.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if(ticketId!=null){
                    ticketRepository.getTicket(ticketId).collect{
                        _ticket.value = it
                    }
                }else{
                    _ticket.value = Ticket(UUID.randomUUID(),"", Date().time,false)
                }
            } catch (e: Throwable) {
                println(e.message)
            }
        }
    }

    fun updateTicket(onUpdate: (Ticket) -> Ticket) {
        _ticket.update { oldTicket ->
            oldTicket?.let { onUpdate(it) }
        }
    }

    fun deleteTicket() {
        _ticket.value?.let { ticket ->
            ticketRepository.deleteTicket(ticket)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _ticket.value?.let{
            if(ticketId!=null) {
                ticketRepository.updateTicket(it)
            }else{
                ticketRepository.addTicket(it)
            }
        }
    }
}

class TicketDetailViewModelFactory(
    private val ticketId: UUID?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TicketDetailViewModel(ticketId) as T
    }
}