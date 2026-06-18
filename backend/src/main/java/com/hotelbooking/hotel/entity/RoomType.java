package com.hotelbooking.hotel.entity;

import com.hotelbooking.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@SuperBuilder
@NoArgsConstructor
public class RoomType extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    private Integer maxCapacity;

    @Setter
    private Double area;

    @Setter
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Setter
    @Column(nullable = false)
    private Integer totalRooms;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_type_amenities", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "amenity")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_type_images", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Setter
    @Builder.Default
    private boolean active = true;
}
