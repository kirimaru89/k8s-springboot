package com.vietinbank.kconsumer.repositories;

import com.vietinbank.kconsumer.models.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRepository extends JpaRepository<Artist, Integer> {
    List<Artist> findByName(String name);
}
