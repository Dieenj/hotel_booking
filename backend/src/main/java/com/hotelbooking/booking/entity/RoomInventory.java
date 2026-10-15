package com.hotelbooking.booking.entity;

import com.hotelbooking.common.entity.BaseEntity;
import com.hotelbooking.hotel.entity.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "room_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "date"}))
@Getter
@SuperBuilder
@NoArgsConstructor
public class RoomInventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate date;

    @Setter
    @Column(nullable = false)
    private Integer totalRooms;

    @Setter
    @Column(nullable = false)
    private Integer availableRooms;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private Integer bookedRooms = 0;
}
