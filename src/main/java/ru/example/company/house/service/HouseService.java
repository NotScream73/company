package ru.example.company.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.company.house.model.House;
import ru.example.company.house.repository.HouseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;

    public List<House> findAll() {
        return houseRepository.findAll();
    }

    public House findById(UUID id) {
        return houseRepository.findById(id).get();
    }
}
