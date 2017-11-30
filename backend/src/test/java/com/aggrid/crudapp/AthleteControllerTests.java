package com.aggrid.crudapp;

import com.aggrid.crudapp.model.Athlete;
import com.aggrid.crudapp.model.Country;
import com.aggrid.crudapp.model.Result;
import com.aggrid.crudapp.model.Sport;
import com.aggrid.crudapp.repositories.AthleteRepository;
import com.aggrid.crudapp.repositories.CountryRepository;
import com.aggrid.crudapp.repositories.SportRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CrudAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AthleteControllerTests {

    @Autowired
    private EntityManager entityManager;

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private SportRepository sportRepository;

    @Test
    public void testThatWeRetrieveTheExpectedNumberOfAthleteResults() {
        ResponseEntity<Athlete[]> response = restTemplate.getForEntity(createURLWithPort("/athletes"), Athlete[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        Athlete[] athletes = response.getBody();

        // not ideal using greaterThan, but as the controller isn't transactional and these tests modify the db its necessary
        // a real world app would prob do tear downs etc between tests - for our purposes this is fine
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
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), newAthlete, Athlete.class);

        // expect
        Athlete createdAthlete = response.getBody();
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

        // when
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), existingAthlete, Athlete.class);

        // expect
        Athlete updatedAthlete = response.getBody();
        assertEquals("Mick Phelps", updatedAthlete.getName());
        assertEquals(australia, updatedAthlete.getCountry());
    }

    @Test
    public void testWeCanAddResultToAthlete() {
        // given
        Athlete existingAthlete = athleteRepository.findByName("Aleksey Nemov");

        // update an existing result, and add a new one
        List<Result> results = existingAthlete.getResults();
        Result existingResult = results.get(0);
        existingResult.setAge(100);
        existingResult.setGold(200);

        Sport cycling = sportRepository.findByName("Gymnastics");
        Result newResult = new Result(cycling, 101, 2017, "01/01/2017", 1, 2, 3);
        results.add(newResult);

        // when
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), existingAthlete, Athlete.class);

        // expect
        Athlete updatedAthlete = response.getBody();
        List<Result> updatedAthleteResults = updatedAthlete.getResults();

        assertEquals(updatedAthleteResults.get(0), existingResult);

        Result newlyCreatedResult = updatedAthleteResults.get(1);
        assertEquals(newlyCreatedResult.getAge(), 101);
        assertEquals(newlyCreatedResult.getYear(), 2017);
        assertEquals(newlyCreatedResult.getDate(), "01/01/2017");
        assertEquals(newlyCreatedResult.getGold(), 1);
        assertEquals(newlyCreatedResult.getSilver(), 2);
        assertEquals(newlyCreatedResult.getBronze(), 3);
        assertEquals(newlyCreatedResult.getSport().getName(), "Gymnastics");
    }

    @Test
    public void testWeCanDeleteAnExistingAthlete() {
        // given
        Athlete existingAthlete = athleteRepository.findByName("Jenny Thompson");

        // when
        restTemplate.postForEntity(createURLWithPort("/deleteAthlete"), existingAthlete.getId(), Void.class);

        // expect
        existingAthlete = athleteRepository.findByName("Jenny Thompson");
        assertNull(existingAthlete);
    }

    @Test
    public void testWeCanUpdateDetachedAthlete() {
        // given

        // get the existing persisted athlete & result - we need these for the IDs
        Athlete persistedAthlete = athleteRepository.findByName("Petter Northug Jr.");
        Result persistedResult = persistedAthlete.getResults().get(0);

        // now create copies of the country, sport, athlete and result
        // these are DETACHED copies of what will exist in hibernate, and simulate
        // a request from a rest service
        Country country = new Country("Norway");
        country.setId(persistedAthlete.getCountry().getId());

        Sport sport = new Sport("Cross Country Skiing");
        sport.setId(persistedResult.getSport().getId());

        List<Result> results = new ArrayList<>();

        // update the athletes name
        Athlete existingAthlete = new Athlete("Petter Northug Jrxxx.", country, results);
        existingAthlete.setId(persistedAthlete.getId());

        // keep the existing result the same
        Result existingResult = new Result(sport, 24, 2010, "28/02/2010", 2, 1, 1);
        existingResult.setAthlete(existingAthlete);
        existingResult.setId(persistedResult.getId());
        results.add(existingResult);

        // add a new result
        Result newResult = new Result(sport, 25, 2011, "22/02/2010", 1, 2, 0);
        newResult.setAthlete(existingAthlete);
        results.add(newResult);

        System.out.println("------------------------------------------------------------------------------------------");

        // when
        ResponseEntity<Athlete> response = restTemplate.postForEntity(createURLWithPort("/saveAthlete"), existingAthlete, Athlete.class);

        // clear associated cache to ensure clean read from db
        entityManager.clear();

        // expect
        HttpStatus statusCode = response.getStatusCode();
        assertTrue(statusCode.is2xxSuccessful());

        Athlete updatedAthlete = athleteRepository.findById(persistedAthlete.getId()).get();
        assertEquals("Petter Northug Jrxxx.", updatedAthlete.getName());

        List<Result> updatedResults = updatedAthlete.getResults();
        assertEquals(2, updatedResults.size());
        Result firstResult = updatedResults.get(0);
        Result secondResult = updatedResults.get(1);
        if(Objects.equals(firstResult.getId(), persistedResult.getId())) {
            assertResultsAreEqual(existingResult, firstResult);
            assertResultsAreEqual(newResult, secondResult);
        } else {
            assertResultsAreEqual(existingResult, secondResult);
            assertResultsAreEqual(newResult, firstResult);
        }
    }

    private void assertResultsAreEqual(Result first, Result second) {
        assertEquals(first.getSport(), second.getSport());
        assertEquals(first.getDate(), second.getDate());
        assertEquals(first.getYear(), second.getYear());
        assertEquals(first.getAge(), second.getAge());
        assertEquals(first.getGold(), second.getGold());
        assertEquals(first.getSilver(), second.getSilver());
        assertEquals(first.getBronze(), second.getBronze());
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private String createURLWithPortAndAthleteId(String uri, Long athleteId) {
        return createURLWithPort(uri) + "?athleteId=" + athleteId;
    }
}
