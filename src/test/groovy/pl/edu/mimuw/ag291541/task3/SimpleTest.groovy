package pl.edu.mimuw.ag291541.task3

import org.junit.Test
import static org.junit.Assert.assertEquals

class SimpleTest {
    private final static String JUNIT_PATH = '/tmp/junit'
    private CodeMetrics codeMetrics = new CodeMetrics()

    @Test
    public void testNumberOfConstructors() {
        final String ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS = 'numberOfConstructorsMetric'
        final long NUMBER_OF_ALREADY_BADLY_MEASURED = 4
        codeMetrics.numberOfConstructors(JUNIT_PATH)
        long difference = codeMetrics.differentOn(JUNIT_PATH, CodeMetrics.CLASS_TYPE, ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS, CodeMetrics.MY_NUMBER_OF_CONSTRUCTORS)
        assertEquals(NUMBER_OF_ALREADY_BADLY_MEASURED, difference)
    }

    @Test
    public void testAverageNumberOfSubtypes() {
        final double EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES = 1.1156
        assertEquals(EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES, codeMetrics.averageNumberOfSubtypes(JUNIT_PATH), 0.001)
    }
    
    @Test
    public void testEfferentCouplings() {
        final int EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT = 154;
        assertEquals(EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT, codeMetrics.efferentCouplingsForSpecified(JUNIT_PATH, CodeMetrics.PROJECT_VERTEX_ID))
    }

    @Test
    public void testAverageDepthOfInheritance() {
        final double EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE = 1.731707317
        final double ASSERT_EQUALITY_TOLERANCE = 0.001
        assertEquals(EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE, codeMetrics.averageDepthOfInheritance(JUNIT_PATH), ASSERT_EQUALITY_TOLERANCE)
    }
}
