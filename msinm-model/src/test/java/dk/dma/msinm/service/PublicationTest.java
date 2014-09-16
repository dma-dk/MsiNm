package dk.dma.msinm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.msinm.model.Publication;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for the Publication/Publisher entities
 */
public class PublicationTest {

    @Test
    public void test() throws IOException {

        ObjectMapper jsonMapper = new ObjectMapper();

        String json = "{ \"type\": \"mail\", \"publish\": true, \"data\": \"[ 1, 2, 3 ]\" }";

        Publication publication = jsonMapper.readValue(json, Publication.class);

        System.out.println("Publication payload: " + publication.getData());

    }
}
