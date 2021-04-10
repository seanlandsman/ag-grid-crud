package com.aggrid.crudapp.controllers;

import com.aggrid.crudapp.model.Country;
import com.aggrid.crudapp.model.Sport;
import com.aggrid.crudapp.repositories.CountryRepository;
import com.aggrid.crudapp.repositories.SportRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class StaticDataController {
    private CountryRepository countryRepository;
    private SportRepository sportRepository;

    public StaticDataController(CountryRepository countryRepository,
                                SportRepository sportRepository) {
        this.countryRepository = countryRepository;
        this.sportRepository = sportRepository;
    }

    @GetMapping("/countries")
    public Iterable<Country> getCountries() {
        return countryRepository.findAll();
    }

    @GetMapping("/sports")
    public Iterable<Sport> getSports() {
        return sportRepository.findAll();
    }
}
