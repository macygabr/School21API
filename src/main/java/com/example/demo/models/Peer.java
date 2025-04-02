package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
@Table(name = "peers")
public class Peer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "class_name")
    private String className;

    @Column(name = "parallel_name")
    private String parallelName;

    @Column(name = "exp_value")
    private Integer expValue;

    @Column(name = "level")
    private Integer level;

    @Column(name = "exp_to_next_level")
    private Integer expToNextLevel;

    @ManyToOne
    private Campus campus;

    @Column(name = "status")
    private String status;

    public Peer(String login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(login, peer.login) && Objects.equals(className, peer.className) && Objects.equals(parallelName, peer.parallelName) && Objects.equals(expValue, peer.expValue) && Objects.equals(level, peer.level) && Objects.equals(expToNextLevel, peer.expToNextLevel) && Objects.equals(campus, peer.campus) && Objects.equals(status, peer.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, className, parallelName, expValue, level, expToNextLevel, campus, status);
    }
}
