package com.camila.barbosa.planner.trip.controller;

import com.camila.barbosa.planner.activity.ActivityData;
import com.camila.barbosa.planner.activity.ActivityRequest;
import com.camila.barbosa.planner.activity.ActivityResponse;
import com.camila.barbosa.planner.activity.ActivityService;
import com.camila.barbosa.planner.participant.*;
import com.camila.barbosa.planner.trip.Trip;
import com.camila.barbosa.planner.trip.TripCreateResponse;
import com.camila.barbosa.planner.trip.TripRequest;
import com.camila.barbosa.planner.trip.repository.TripRepository;
import com.camila.barbosa.planner.link.LinkData;
import com.camila.barbosa.planner.link.LinkRequest;
import com.camila.barbosa.planner.link.LinkResponse;
import com.camila.barbosa.planner.link.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ActivityService activityService;

    //trip endpoints
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequest request){
        Trip newTrip = new Trip(request);
        this.tripRepository.save(newTrip);
        this.participantService.registerParticipantsToEvent(request.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip> trip = this.tripRepository.findById(id);
        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequest request){
        Optional<Trip> trip = this.tripRepository.findById(id);
        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(request.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(request.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination((request.destination()));

            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.tripRepository.findById(id);
        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipant(id);

            return ResponseEntity.ok(rawTrip);
        }
        return ResponseEntity.notFound().build();
    }

    //participant endpoints
    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequest participantRequest){
        Optional<Trip> trip = this.tripRepository.findById(id);
        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantCreateResponse = this.participantService.registerParticipantToTrip(participantRequest.email(), rawTrip);
            if(rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(participantRequest.email());
            return ResponseEntity.ok(participantCreateResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromTrip(id);
        return ResponseEntity.ok(participantList);
    }

    //activity endpoints
    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequest request) {
        Optional<Trip> trip = this.tripRepository.findById(id);
        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            ActivityResponse activityResponse = this.activityService.registerActivity(request, rawTrip);
            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id){
        List<ActivityData> activityList = this.activityService.getAllActivitiesFromTrip(id);
        return ResponseEntity.ok(activityList);
    }

    //link endpoints
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequest request) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(request, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id){
        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkDataList);
    }


}
