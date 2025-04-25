package com.vietinbank.kconsumer.services;

import com.vietinbank.kconsumer.models.Artist;
import com.vietinbank.kconsumer.repositories.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Autowired
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    // Create or update an artist
    public Artist saveArtist(Artist artist) {
        return artistRepository.save(artist);
    }

    // Find an artist by ID
    public Optional<Artist> findArtistById(Integer id) {
        return artistRepository.findById(id);
    }

    // Find artists by name
    public List<Artist> findArtistsByName(String name) {
        return artistRepository.findByName(name);
    }

    // Get all artists
    public List<Artist> findAllArtists() {
        return artistRepository.findAll();
    }

    // Delete an artist by ID
    public void deleteArtistById(Integer id) {
        artistRepository.deleteById(id);
    }
}
