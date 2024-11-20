package com.sunway.app.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Code must not be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be exactly 3 uppercase letters")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "Name must not be blank")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "Name must be 1-10 Chinese characters")
    @Column(nullable = false)
    private String name;

    public Currency() {
    }

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
