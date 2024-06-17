package ru.example.company.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.company.house.exception.HouseNotFoundException;
import ru.example.company.house.model.House;
import ru.example.company.house.repository.HouseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;

    @Transactional(readOnly = true)
    public List<House> findAll() {
        return houseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public House findById(UUID id) {
        if (id == null){
            throw new IllegalArgumentException("House Id can't be null");
        }
        return houseRepository.findById(id).orElseThrow(() -> new HouseNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public House findIdByAddress(String address) {
        if (address.isBlank()){
            throw new IllegalArgumentException("House address can't be blank");
        }
        return houseRepository.findIdByAddressIgnoreCase(address);
    }
}
