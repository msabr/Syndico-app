package com.syndico.syndicoapp.repositories;


import com.syndico.syndicoapp.models.Reservation;
import com.syndico.syndicoapp.models.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByResidentId(Long residentId);
    List<Reservation> findBySpaceType(SpaceType spaceType);
}
