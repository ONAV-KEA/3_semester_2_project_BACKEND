package dk.kea.onav2ndproject_rest.service;

import dk.kea.onav2ndproject_rest.dto.EventConverter;
import dk.kea.onav2ndproject_rest.dto.EventDTO;
import dk.kea.onav2ndproject_rest.entity.Event;
import dk.kea.onav2ndproject_rest.exception.EventNotFoundException;
import dk.kea.onav2ndproject_rest.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventService {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EventConverter eventConverter;

    public Page<EventDTO> getAllEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        return events.map(eventConverter::toDTO);
    }

    public EventDTO getEventById(int id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            return eventConverter.toDTO(event.get());
        } else {
            throw new EventNotFoundException("Event does not exist" + id);
        }
    }

    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = eventConverter.toEntity(eventDTO);
        event.setId(0);

        Event savedEvent = eventRepository.save(event);
        return eventConverter.toDTO(savedEvent);
    }

    public EventDTO updateEvent(int id, EventDTO eventDTO){
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()){
            Event eventToUpdate = eventConverter.toEntity(eventDTO);
            eventToUpdate.setId(id);
            Event updatedEvent = eventRepository.save(eventToUpdate);
            return eventConverter.toDTO(updatedEvent);
        } else {
            throw new EventNotFoundException("Event does not exist" + id);
        }
    }

    public void deleteEventById(int id){
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()){
            eventRepository.deleteById(id);
        } else {
            throw new EventNotFoundException("Event does not exist" + id);
        }
    }
}
