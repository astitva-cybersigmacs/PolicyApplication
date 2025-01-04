package com.example.policy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users_new")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    @Column(nullable = false)
    private String userName;

    private String firstName;
    private String middleName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNo;
}
