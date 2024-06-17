package ru.example.company.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexSettings {
    private boolean displayAllNews = true;
    private boolean displayBreakingNews = true;
    private boolean displayNotifications = true;
}
