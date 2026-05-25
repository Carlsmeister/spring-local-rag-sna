package com.example.demo.config;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TikaConfigTest {

    @Autowired
    private Tika tika;

    @Test
    void testTikaBeanIsInjectable() {
        assertNotNull(tika, "Tika bean should be successfully injected from Spring Context");
    }
}
