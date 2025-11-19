package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "apartments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"building_id", "apartment_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_number", nullable = false, length = 50)
    private String apartmentNumber;

    private Integer floor;

    private Double surface;

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @OneToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;
}
