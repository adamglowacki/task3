package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe

class CodeMetrics {
    public static final String MY_NUMBER_OF_CONSTRUCTORS = 'ag291541NumberOfConstructors'
    public static final String MY_DEPTH_OF_INHERITANCE = 'ag291541DepthOfInheritance'
    private static final String NAME = 'name'
    private static final String KEY = 'KEY'
    private static final String TYPE = 'TYPE_PROPERTY'

    private static final String OBJECT_KEY = 'Ljava/lang/Object;'

    public static final String CLASS_TYPE = 'class'
    private static final String METHOD_TYPE = 'method'
    private static final String INTERFACE_TYPE = 'interface'
    private static final String ANNOTATION_TYPE = 'annotation_type'

    private static final String CONTAINS_EDGE = 'CONTAINS'
    private static final String EXTENDS_EDGE = 'EXTENDS'
    private static final int PROJECT_VERTEX_ID = 1

    private void loadGremlin() {
        Gremlin.load()
        def cl = {final String typeName -> _().filter {it[TYPE] == typeName}}
        Gremlin.defineStep('filterType', [Vertex, Pipe], cl)
        Gremlin.defineStep('filterClass', [Vertex, Pipe], {_().filterType(CLASS_TYPE)})
        Gremlin.defineStep('filterMethod', [Vertex, Pipe], {_().filterType(METHOD_TYPE)})
        Gremlin.defineStep('filterInterface', [Vertex, Pipe], {_().filterType(INTERFACE_TYPE)})
        Gremlin.defineStep('filterAnnotation', [Vertex, Pipe], {_().filterType(ANNOTATION_TYPE)})
        Gremlin.defineStep('filterName', [Vertex, Pipe], {final String desiredName -> _().filter {it[NAME] == desiredName}})
        Gremlin.defineStep('filterNotStub', [Vertex, Pipe], {_().filter {!it.stubNode}})
        Gremlin.defineStep('outContains', [Vertex, Pipe], {_().out(CONTAINS_EDGE)})
        Gremlin.defineStep('inContains', [Vertex, Pipe], {_().in(CONTAINS_EDGE)})
        Gremlin.defineStep('inExtends', [Vertex, Pipe], {_().in(EXTENDS_EDGE)})
        Gremlin.defineStep('inc', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] += value}})
        Gremlin.defineStep('set', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('inc', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] += value}})
        Gremlin.defineStep('set', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] = value}})
    }

    /**
     * Counts number of constructors for each class and stores it as a {@code MY_NUMBER_OF_CONSTRUCTORS} property.
     * @param path File path of a Neo4j graph.
     */
    public void numberOfConstructors(String path) {
        loadGremlin()
        Graph g = openNeo4j(path)
        try {
            g.V.filterClass.set(MY_NUMBER_OF_CONSTRUCTORS, 0).iterate()
            def x = ''
            g.V.filterClass.sideEffect {x = it.name}.outContains.filterMethod.filter {it.name == x}
                    .as('marking').inContains.filterClass.inc(MY_NUMBER_OF_CONSTRUCTORS, 1).loop('marking') {true}.iterate()
        } finally {
            g.shutdown()
        }
    }

    /**
     * Stores depth of inheritance for each class and interface as a {@code MY_DEPTH_OF_INHERITANCE} property.
     * @param g
     */
    private void depthOfInheritance(Graph g) {
        g.V.filterInterface.set(MY_DEPTH_OF_INHERITANCE, 1).iterate()
        g.V.filterAnnotation.set(MY_DEPTH_OF_INHERITANCE, 1).iterate()
        def lastDepth = 0
        g.V.filter {it[KEY] == OBJECT_KEY}.sideEffect {it[MY_DEPTH_OF_INHERITANCE] = 1}
                .as('store_depth').sideEffect {lastDepth = it[MY_DEPTH_OF_INHERITANCE]}.inExtends.sideEffect {it[MY_DEPTH_OF_INHERITANCE] = lastDepth + 1}
                .loop('store_depth') {true}.iterate()
    }

    /**
     * Returns an average depth of inheritance hierarchy among all classes and interfaces. {@code java.lang.Object} has depth 1 and every interface has depth 1.
     * @param path
     * @return An average depth of inheritance.
     */
    public double averageDepthOfInheritance(String path) {
        Graph g = openNeo4j(path)
        try {
            depthOfInheritance(g)
            def sumOfDepths = 0
            def countOfTypes = 0
            g.v(PROJECT_VERTEX_ID)
                    .as('come_to_son').outContains.filter {it[TYPE] == CLASS_TYPE || it[TYPE] == INTERFACE_TYPE || it[TYPE] == ANNOTATION_TYPE}
                    .sideEffect {sumOfDepths += it[MY_DEPTH_OF_INHERITANCE]; countOfTypes++}.loop('come_to_son') {true}
            return ((double)sumOfDepths) / countOfTypes
        } finally {
            g.shutdown()
        }
    }

    /**
     * Returns number of vertices of given type that have different values of given numeric properties. It ignores stub nodes.
     * @param path
     * @param type
     * @param property1
     * @param property2
     * @return Number of vertices that have non-equal properties.
     */
    public long differentOn(String path, String type, String property1, String property2) {
        Graph g = openNeo4j(path)
        try {
            def number = 0
            g.V.filterType(type).filterNotStub.filter {it[property1] >= 0 && it[property2] >= 0 && it[property1] != it[property2]}.sideEffect {number++}.iterate()
            return number
        } finally {
            g.shutdown()
        }
    }

    private Graph openNeo4j(String path) {
        return new Neo4jGraph(path)
    }
}