package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname", length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 10, nullable = false)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "password", length = 200, nullable = false)
    private String password;

    private boolean active;

    @Column(name = "day_of_birth", length = 200)
    private Date dateOfBirth;

    @Column(name = "facebook_account_id", length = 200)
    private int facebookAccountId;

    @Column(name = "google_account_id", length = 200)
    private int googleAccountId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role roleId;

}
