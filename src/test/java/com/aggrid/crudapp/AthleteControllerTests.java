package com.aggrid.crudapp;

import com.aggrid.crudapp.model.Athlete;
import com.aggrid.crudapp.model.Country;
import com.aggrid.crudapp.repositories.AthleteRepository;
import com.aggrid.crudapp.repositories.CountryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.ArrayList;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CrudAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AthleteControllerTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Test
    public void testThatWeRetrieveTheExpectedNumberOfAthleteResults() {
        ResponseEntity<Athlete[]> response = restTemplate.getForEntity(createURLWithPort("/athletes"), Athlete[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        Athlete[] athletes = response.getBody();

        // not ideal using greaterThan, but as the controller isnt transactional and these tests modify the db its necessary
        // a real worl app would prob do tear downs etc between tests - for our purposes this is fine
        assertThat("number of athletes", athletes.length, greaterThanOrEqualTo(6955));
    }

    @Test
    public void testWeCanSaveNewAthleteToDatabase() {
        // given
        Country unitedStates = countryRepository.findByName("United States");

        Athlete newAthlete = new Athlete("Test Athlete",
                unitedStates,
                new ArrayList<>());

        // when
        // we'll ignore the response here
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), newAthlete, Athlete.class);

        // expect
        Athlete createdAthlete = athleteRepository.findByName("Test Athlete");
        assertNotNull(createdAthlete.getId());
        assertEquals(newAthlete.getName(), createdAthlete.getName());
        assertEquals(newAthlete.getCountry(), createdAthlete.getCountry());
        assertEquals(newAthlete.getResults(), createdAthlete.getResults());
    }

    @Test
    public void testWeUpdateAnExistingAthlete() {
        // given
        Country australia = countryRepository.findByName("Australia");

        Athlete existingAthlete = athleteRepository.findByName("Michael Phelps");
        existingAthlete.setName("Mick Phelps");
        existingAthlete.setCountry(australia);

        Long existingId = existingAthlete.getId();

        // when
        // we'll ignore the response here
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), existingAthlete, Athlete.class);

        // expect
        Athlete updatedAthlete = athleteRepository.findById(existingId).get();
        assertEquals("Mick Phelps", updatedAthlete.getName());
        assertEquals(australia, updatedAthlete.getCountry());
    }

    @Test
    public void testWeCanDeleteAnExistingAthlete() {

        long before = athleteRepository.count();

        // given
        Athlete existingAthlete = athleteRepository.findByName("Jenny Thompson");

        // when
        restTemplate.postForEntity(createURLWithPort("/deleteAthlete"), existingAthlete.getId(), Void.class);

        long after = athleteRepository.count();

        // expect
        existingAthlete = athleteRepository.findByName("Jenny Thompson");
        assertNull(existingAthlete);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private String createURLWithPortAndAthleteId(String uri, Long athleteId) {
        return createURLWithPort(uri) + "?athleteId=" + athleteId;
    }
}
