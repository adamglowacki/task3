package pl.edu.mimuw.ag291541.task3

import org.junit.Test

import static org.junit.Assert.assertEquals

class JHotDrawTest {
    private final double ASSERT_EQUALITY_TOLERANCE = 0.001
    private final String ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS = 'numberOfConstructorsMetric'
    private final double EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES = 0.46732026143 /* ok according CodePro */
    private final int EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT = 709 /* ok according CodePro */
    private final double EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE = 2.0655737704 /* CodePro: 3.2 */

    private final static String JHOTDRAW_PATH = '/tmp/jhotdraw'

    private CodeMetrics codeMetrics = new CodeMetrics()

    @Test
    public void testNumberOfConstructors() {
        final long NUMBER_OF_ALREADY_BADLY_MEASURED = 18
        codeMetrics.numberOfConstructors(JHOTDRAW_PATH)
        long difference = codeMetrics.differentOn(JHOTDRAW_PATH, CodeMetrics.CLASS_TYPE, ALREADY_MEASURED_NUMBER_OF_CONSTRUCTORS, CodeMetrics.MY_NUMBER_OF_CONSTRUCTORS)
        assertEquals(NUMBER_OF_ALREADY_BADLY_MEASURED, difference)
    }

    @Test
    public void testAverageNumberOfSubtypes() {
        assertEquals(EXPECTED_AVERAGE_NUMBER_OF_SUBTYPES, codeMetrics.averageNumberOfSubtypes(JHOTDRAW_PATH), 0.001)
    }

    @Test
    public void testEfferentCouplings() {
        assertEquals(EXPECTED_EFFERENT_COUPLINGS_ON_PROJECT, codeMetrics.efferentCouplingsForSpecified(JHOTDRAW_PATH, CodeMetrics.PROJECT_VERTEX_ID))
    }

    @Test
    public void testAverageDepthOfInheritance() {
        assertEquals(EXPECTED_AVERAGE_DEPTH_OF_INHERITANCE, codeMetrics.averageDepthOfInheritance(JHOTDRAW_PATH), ASSERT_EQUALITY_TOLERANCE)
    }
}
