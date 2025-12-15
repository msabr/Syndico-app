package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Apartment;
import com.syndico.syndicoapp.repositories.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingService buildingService;

    public List<Apartment> findAll() {
        return apartmentRepository.findAll();
    }

    public Apartment findById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with id: " + id));
    }

    public List<Apartment> findByBuildingId(Long buildingId) {
        return apartmentRepository.findByBuildingId(buildingId);
    }

    @Transactional
    public Apartment save(Apartment apartment) {
        return apartmentRepository.save(apartment);
    }

    @Transactional
    public Apartment update(Long id, Apartment apartment) {
        Apartment existingApartment = findById(id);
        existingApartment.setApartmentNumber(apartment.getApartmentNumber());
        existingApartment.setFloor(apartment.getFloor());
        existingApartment.setSurface(apartment.getSurface());
        existingApartment.setNumberOfRooms(apartment.getNumberOfRooms());
        existingApartment.setBuilding(apartment.getBuilding());
        existingApartment.setResident(apartment.getResident());
        return apartmentRepository.save(existingApartment);
    }

    @Transactional
    public void deleteById(Long id) {
        apartmentRepository.deleteById(id);
    }
}
