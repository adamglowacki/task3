package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import junit.framework.TestCase

class SimpleTest extends TestCase {
    private Graph g;

    @Override
    protected void setUp() {
        g = new Neo4jGraph('/tmp/junit')
        println('test set up')
    }

    @Override
    protected void tearDown() {
        g.shutdown()
        println('test torn down')
    }

    public void testTick() {
        MethodTicker.tick()
    }

    public void testNumberOfConstructors() {
        CodeMetrics.nothing(g)
        CodeMetrics.numberOfConstructors(g)
    }
}
