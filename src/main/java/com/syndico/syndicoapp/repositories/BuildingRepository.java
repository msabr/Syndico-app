package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByNameContainingIgnoreCase(String name);
    List<Building> findByAddressContainingIgnoreCase(String address);
}
