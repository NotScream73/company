package ru.example.company.house.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.example.company.house.model.House;
import ru.example.company.house.service.HouseService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/houses")
public class HouseController {
    private final HouseService houseService;

    @GetMapping
    public String getAllHouses(Model model) {
        model.addAttribute("houses", houseService.findAll());
        return "houses";
    }

    @GetMapping(value = {"/{id}", "/"})
    public String getAllHouses(@PathVariable("id") UUID id,
                               Model model) {
        if (id == null) {
            model.addAttribute("house", new House());
        } else {
            model.addAttribute("house", houseService.findById(id));
        }
        return "edit-house";
    }
}
