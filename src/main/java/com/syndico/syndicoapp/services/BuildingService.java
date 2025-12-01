package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.repositories.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {

    private BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> findAll() {
        return buildingRepository.findAll();
    }
}
