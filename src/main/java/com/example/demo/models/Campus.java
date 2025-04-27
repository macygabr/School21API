package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Setter
@Getter
public class Campus {
    @Id
    private String id;

    @Column(name = "short_name", unique = true)
    private String shortName;

    @Column(name = "full_name", unique = true)
    private String fullName;

    @OneToMany(mappedBy = "campus")
    @JsonIgnore
    private List<Peer> peers;
}