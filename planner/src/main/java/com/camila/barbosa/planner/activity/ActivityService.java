package com.camila.barbosa.planner.activity;

import com.camila.barbosa.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {
    @Autowired
    private ActivityRepository activityRepository;

    public ActivityResponse registerActivity(ActivityRequest request, Trip trip) {
        Activity newActivity = new Activity(request.title(), request.occurs_at(), trip);

        activityRepository.save(newActivity);

        return new ActivityResponse(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromTrip(UUID tripId){
        return this.activityRepository.findByTripId(tripId).stream().map(activity -> new ActivityData(activity.getId(), activity.getTitle(), activity.getOccursAt())).toList();
    }
}
