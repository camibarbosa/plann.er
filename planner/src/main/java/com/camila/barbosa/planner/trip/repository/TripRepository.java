package com.camila.barbosa.planner.trip.repository;


import com.camila.barbosa.planner.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
}
