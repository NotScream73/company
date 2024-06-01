package ru.example.company.house.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.company.house.model.House;

import java.util.UUID;

public interface HouseRepository extends JpaRepository<House, UUID> {
}