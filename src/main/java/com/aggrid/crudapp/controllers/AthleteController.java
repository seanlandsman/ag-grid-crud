package com.aggrid.crudapp.controllers;

import com.aggrid.crudapp.model.Athlete;
import com.aggrid.crudapp.model.Result;
import com.aggrid.crudapp.model.Sport;
import com.aggrid.crudapp.repositories.AthleteRepository;
import com.aggrid.crudapp.repositories.SportRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AthleteController {
    private AthleteRepository athleteRepository;
    private SportRepository sportRepository;

    public AthleteController(AthleteRepository athleteRepository,
                             SportRepository sportRepository) {
        this.athleteRepository = athleteRepository;
        this.sportRepository = sportRepository;
    }

    @GetMapping("/athletes")
    public Iterable<Athlete> getAthletes() {
        return athleteRepository.findAll();
    }

    @GetMapping("/test")
    public Athlete test() {
        Athlete existingAthlete = athleteRepository.findByName("Petter Northug Jr.");

        // update an existing result, and add a new one
        List<Result> results = existingAthlete.getResults();
        Result existingResult = results.get(0);
        existingResult.setAge(100);
        existingResult.setGold(200);

        Sport cycling = sportRepository.findByName("Cycling");
        Result newResult = new Result(cycling, 101, 2017, "01/01/2017", 1, 2, 3);
        results.add(newResult);

        return athleteRepository.save(existingAthlete);
    }

    @GetMapping("/athlete")
    public Optional<Athlete> getAthlete(@RequestParam(value = "id") Long athleteId) {
        return athleteRepository.findById(athleteId);
    }

    @PostMapping("/saveAthlete")
    public Athlete saveAthlete(@RequestBody Athlete athlete) {
        return athleteRepository.save(athlete);
    }

    @PostMapping("/deleteAthlete")
    public void deleteAthlete(@RequestBody Long athleteId) {
        athleteRepository.deleteById(athleteId);
    }
}
