package pl.edu.mimuw.ag291541.task3

import org.junit.Test
import static org.junit.Assert.assertEquals

class SimpleTest {
    private final static String JUNIT_PATH = '/tmp/junit'
    private CodeMetrics codeMetrics = new CodeMetrics()

    @Test
    public void testNumberOfConstructors() {
        final String THEIR_NUMBER_OF_CONSTRUCTORS = 'numberOfConstructorsMetric'
        codeMetrics.numberOfConstructors(JUNIT_PATH)
        assertEquals(4, codeMetrics.differentOn(JUNIT_PATH, CodeMetrics.CLASS_TYPE, THEIR_NUMBER_OF_CONSTRUCTORS, CodeMetrics.MY_NUMBER_OF_CONSTRUCTORS))
    }

    @Test
    public void testAverageDepthOfInheritance() {
        final double EXPECTED_VALUE_OF_METRIC = 4.0
        assertEquals(EXPECTED_VALUE_OF_METRIC, codeMetrics.averageDepthOfInheritance(JUNIT_PATH), 0.001)
    }
}
