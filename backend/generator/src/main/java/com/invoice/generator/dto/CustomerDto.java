package com.invoice.generator.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
}