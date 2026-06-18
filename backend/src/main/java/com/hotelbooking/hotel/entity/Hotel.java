package com.hotelbooking.hotel.entity;

import com.hotelbooking.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Hotel extends AuditableEntity {

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private String address;

    @Setter
    @Column(nullable = false)
    private String city;

    @Setter
    private String country;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    private Integer starRating;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Setter
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoomType> roomTypes = new ArrayList<>();
}
