package com.capg.delivery.enums;

public enum DeliveryStatus {

    // NORMAL FLOW
    DRAFT,
    BOOKED,
    PACKED,
    DISPATCHED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,

    // EXCEPTION STATES
    DELAYED,
    FAILED,
    RETURNED
}