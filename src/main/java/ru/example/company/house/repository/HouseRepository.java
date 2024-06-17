package ru.example.company.house.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.company.house.model.House;

import java.util.UUID;

@Repository
public interface HouseRepository extends JpaRepository<House, UUID> {
    House findIdByAddressIgnoreCase(String address);
}