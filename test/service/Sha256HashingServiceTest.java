package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Sha256HashingServiceTest {

    private HashingService hashingService;

    @BeforeEach
    public void setUp() {
        hashingService = new Sha256HashingService();
    }


    @Test
    public void testHash() {
        // Test with a normal string
        String input = "hello world";
        String expectedHash = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9";
        assertEquals(expectedHash, hashingService.hash(input), "The hash of the input string should match the expected hash");
    }
}
