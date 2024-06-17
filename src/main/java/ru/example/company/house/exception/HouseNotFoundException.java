package ru.example.company.house.exception;

import java.util.UUID;

public class HouseNotFoundException extends RuntimeException {
    public HouseNotFoundException(UUID id) {
        super(String.format("House with id [%s] is not found", id));
    }
}