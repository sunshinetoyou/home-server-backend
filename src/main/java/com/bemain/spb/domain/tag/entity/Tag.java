package com.bemain.spb.domain.tag.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tag")
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING) // Enum 문자열로 저장
    @Column(nullable = false)
    private TagCategory category; // STACK, VULNERABILITY

    public Tag(String name, TagCategory category) {
        this.name = name;
        this.category = category;
    }
}