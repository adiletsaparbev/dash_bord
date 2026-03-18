package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String originalFileName;

    private String contentType;

    private Long size;
    @Basic(fetch = FetchType.EAGER)
    private byte[] bytes;

    @OneToOne(mappedBy = "avatar")
    private User user;
}
