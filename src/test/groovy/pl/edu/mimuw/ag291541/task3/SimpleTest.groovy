package pl.edu.mimuw.ag291541.task3

import org.junit.Test
import static org.junit.Assert.assertEquals

class SimpleTest {
    private final static String JUNIT_PATH = '/tmp/junit'
    private CodeMetrics codeMetrics = new CodeMetrics()

    @Test
    public void testNumberOfConstructors() {
        final String THEIR_NUMBER_OF_CONSTRUCTORS = 'numberOfConstructorsMetric'
        final long NUMBER_OF_BADLY_MEASURED = 4
        codeMetrics.numberOfConstructors(JUNIT_PATH)
        assertEquals(NUMBER_OF_BADLY_MEASURED, codeMetrics.differentOn(JUNIT_PATH, CodeMetrics.CLASS_TYPE, THEIR_NUMBER_OF_CONSTRUCTORS, CodeMetrics.MY_NUMBER_OF_CONSTRUCTORS))
    }

    @Test
    public void testAverageNumberOfSubtypes() {
        final double EXPECTED_METRIC_ON_PROJECT = 1.1156
        assertEquals(EXPECTED_METRIC_ON_PROJECT, codeMetrics.averageNumberOfSubtypes(JUNIT_PATH), 0.001)
    }
    
    @Test
    public void testEfferentCouplings() {
        final int EXPECTED_METRIC_ON_PROJECT = 343;
        assertEquals(EXPECTED_METRIC_ON_PROJECT, codeMetrics.efferentCouplingsForSpecified(JUNIT_PATH, CodeMetrics.PROJECT_VERTEX_ID))
    }
}
