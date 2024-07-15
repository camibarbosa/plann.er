package com.camila.barbosa.planner.link;

import com.camila.barbosa.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LinkService {
    @Autowired
    private LinkRepository repository;

    public LinkResponse registerLink(LinkRequest request, Trip trip) {
        Link newLink = new Link(request.title(), request.url(), trip);

        repository.save(newLink);

        return new LinkResponse(newLink.getId());
    }

    public List<LinkData> getAllLinksFromTrip(UUID tripId) {
        return repository.findByTripId(tripId).stream().map(link -> new LinkData(link.getId(), link.getTitle(), link.getUrl())).toList();
    }
}
