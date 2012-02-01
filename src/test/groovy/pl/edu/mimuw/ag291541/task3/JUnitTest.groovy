package pl.edu.mimuw.ag291541.task3

import org.junit.Test
import static org.junit.Assert.assertEquals

class JUnitTest {
    private final double ASSERT_EQUALITY_TOLERANCE = 0.001
    private final String ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS = 'numberOfConstructorsMetric'
    private final double EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES = 0.36734693877 /* CodePro: 0.38 */
    private final int EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT = 154 /* CodePro: 343 */
    private final double EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE = 1.9268292 /* CodePro: 2.53 */

    private final static String JUNIT_PATH = '/tmp/junit'

    private CodeMetrics codeMetrics = new CodeMetrics()

    @Test
    public void testNumberOfConstructors() {
        final long NUMBER_OF_ALREADY_BADLY_MEASURED = 4
        codeMetrics.numberOfConstructors(JUNIT_PATH)
        long difference = codeMetrics.differentOn(JUNIT_PATH, CodeMetrics.CLASS_TYPE, ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS, CodeMetrics.MY_NUMBER_OF_CONSTRUCTORS)
        assertEquals(NUMBER_OF_ALREADY_BADLY_MEASURED, difference)
    }

    @Test
    public void testAverageNumberOfSubtypes() {
        assertEquals(EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES, codeMetrics.averageNumberOfSubtypes(JUNIT_PATH), 0.001)
    }
    
    @Test
    public void testEfferentCouplings() {
        assertEquals(EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT, codeMetrics.efferentCouplingsForSpecified(JUNIT_PATH, CodeMetrics.PROJECT_VERTEX_ID))
    }

    @Test
    public void testAverageDepthOfInheritance() {
        assertEquals(EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE, codeMetrics.averageDepthOfInheritance(JUNIT_PATH), ASSERT_EQUALITY_TOLERANCE)
    }
}
