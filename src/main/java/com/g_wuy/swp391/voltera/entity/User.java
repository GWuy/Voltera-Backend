package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    private String firstname;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    private String lastname;

    @Size(max = 200)
    @Column(name = "full_name", length = 200)
    private String fullname;

    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updateAt;

    @Column(name = "avatar", length = Integer.MAX_VALUE)
    private String avatar;

    @Column(name = "email_verified")
    private Boolean emailVerified;

}