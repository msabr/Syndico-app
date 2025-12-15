package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.repositories.BuildingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    
    public List<Building> findAll() {
        return buildingRepository.findAll();
    }
    
    public Building findById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + id));
    }
    
    @Transactional
    public Building save(Building building) {
        return buildingRepository.save(building);
    }
    
    @Transactional
    public Building update(Long id, Building building) {
        Building existingBuilding = findById(id);
        existingBuilding.setName(building.getName());
        existingBuilding.setAddress(building.getAddress());
        existingBuilding.setNumberOfFloors(building.getNumberOfFloors());
        existingBuilding.setNumberOfApartments(building.getNumberOfApartments());
        existingBuilding.setYearBuilt(building.getYearBuilt());
        existingBuilding.setDescription(building.getDescription());
        return buildingRepository.save(existingBuilding);
    }
    
    @Transactional
    public void deleteById(Long id) {
        buildingRepository.deleteById(id);
    }
    
    public List<Building> searchByName(String name) {
        return buildingRepository.findByNameContainingIgnoreCase(name);
    }
}
