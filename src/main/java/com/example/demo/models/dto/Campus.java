package com.example.demo.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Override
    public String toString() {
        return "Campus{" +
                "id='" + id + '\'' +
                ", shortName='" + shortName + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}