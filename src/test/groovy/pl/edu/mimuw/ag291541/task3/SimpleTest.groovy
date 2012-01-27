package pl.edu.mimuw.ag291541.task3

import org.junit.Test

class SimpleTest {

    @Test
    public void testNumberOfConstructors() {
        new CodeMetrics().numberOfConstructors('/tmp/junit')
    }
}
