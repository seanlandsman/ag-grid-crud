package com.aggrid.crudapp;

import com.aggrid.crudapp.model.Athlete;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CrudAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AthleteControllerTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testThatWeRetrieveTheExpectedNumberOfAthleteResults() {
        ResponseEntity<Athlete[]> response = restTemplate.getForEntity(createURLWithPort("/olympicData"), Athlete[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);

        Athlete[] athletes = response.getBody();
        assertEquals(athletes.length, 6955);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
