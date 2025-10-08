package com.invoice.generator.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shopName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(unique = true)
    private String gstin;

    @Column(name = "logo_path")
    private String logoPath;
}