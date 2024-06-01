package ru.example.company.house.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import ru.example.company.user.model.User;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "houses")
public class House {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "address", nullable = false)
    private String address;

    @OneToMany(mappedBy = "house")
    private List<User> users;

    @OneToMany(mappedBy = "house")
    private List<User> news;
}
