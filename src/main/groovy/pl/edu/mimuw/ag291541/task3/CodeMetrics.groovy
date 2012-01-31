package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe

class CodeMetrics {
    public static final String MY_NUMBER_OF_CONSTRUCTORS = 'ag291541NumberOfConstructors'
    public static final String MY_NUMBER_OF_SUBTYPES = 'ag291541NumberOfSubtypes'
    public static final String MY_NUMBER_OF_DIRECT_SUBTYPES = 'ag291541NumberOfDirectSubtypes'
    public static final String MY_AVERAGE_NUMBER_OF_SUBTYPES = 'ag291541AverageNumberOfSubtypes'
    public static final String VISITED = 'ag291541Visited'
    public static final String NUMBER_OF_OUT = 'ag291541NumberOfOut'
    private static final String NAME = 'name'
    private static final String KEY = 'KEY'
    private static final String TYPE = 'TYPE_PROPERTY'

    private static final String OBJECT_KEY = 'Ljava/lang/Object;'

    public static final String CLASS_TYPE = 'class'
    private static final String METHOD_TYPE = 'method'
    private static final String INTERFACE_TYPE = 'interface'
    private static final String ANNOTATION_TYPE = 'annotation_type'
    private static final String PARAMETERIZED_TYPE = 'parametrized_type'
    private static final String PACKAGE_TYPE = 'package'
    private static final String PROJECT_TYPE = 'project'

    private static final String CONTAINS_EDGE = 'CONTAINS'
    private static final String EXTENDS_EDGE = 'EXTENDS'
    private static final int PROJECT_VERTEX_ID = 1

    private void loadGremlin() {
        Gremlin.load()
        Gremlin.defineStep('filterType', [Vertex, Pipe], {final String typeName -> _().filter {it[TYPE] == typeName}})
        Gremlin.defineStep('filterTypes', [Vertex, Pipe], {final Set<String> allowed -> _().filter {allowed.contains(it[TYPE])}})
        Gremlin.defineStep('filterC', [Vertex, Pipe], {_().filterType(CLASS_TYPE)})
        Gremlin.defineStep('filterM', [Vertex, Pipe], {_().filterType(METHOD_TYPE)})
        Gremlin.defineStep('filterI', [Vertex, Pipe], {_().filterType(INTERFACE_TYPE)})
        Gremlin.defineStep('filterA', [Vertex, Pipe], {_().filterType(ANNOTATION_TYPE)})
        def ciap = new HashSet<String>([CLASS_TYPE, INTERFACE_TYPE, ANNOTATION_TYPE, PARAMETERIZED_TYPE])
        Gremlin.defineStep('filterCIAP', [Vertex, Pipe], {_().filterTypes(ciap)})
        def typeContainer = new HashSet<String>([CLASS_TYPE, INTERFACE_TYPE, ANNOTATION_TYPE, PARAMETERIZED_TYPE, PACKAGE_TYPE, PROJECT_TYPE])
        Gremlin.defineStep('filterTypeContainer', [Vertex, Pipe], {_().filterTypes(typeContainer)})
        Gremlin.defineStep('filterName', [Vertex, Pipe], {final String desiredName -> _().filter {it[NAME] == desiredName}})
        Gremlin.defineStep('filterNotStub', [Vertex, Pipe], {_().filter {!it.stubNode}})
        Gremlin.defineStep('outContains', [Vertex, Pipe], {_().out(CONTAINS_EDGE)})
        Gremlin.defineStep('inContains', [Vertex, Pipe], {_().in(CONTAINS_EDGE)})
        Gremlin.defineStep('inExtends', [Vertex, Pipe], {_().in(EXTENDS_EDGE)})
        Gremlin.defineStep('setFloat', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('setInt', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('setBoolean', [Vertex, Pipe], {final String property, final boolean value -> _().sideEffect {it[property] = (it[property] != null ? it[property] : 0.0) + value}})
        Gremlin.defineStep('incFloat', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] = (it[property] != null ? it[property] : 0.0) + value}})
        Gremlin.defineStep('incInt', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] = (it[property] != null ? it[property] : 0) + value}})
    }

    private void sumUp(Graph g, Pipe leaves, String propertyToBeSummed, Set<String> types) {
        g.V.filterTypes(types).setInt(VISITED, 0).iterate()
        g.V.filterTypes(types).setInt(NUMBER_OF_OUT, 0).iterate()
        g.V.filterTypes(types).outContains.filterTypes(types).inContains.filterTypes(types).incInt(NUMBER_OF_OUT, 1).iterate()
        def sonValue = 0
        leaves.as('climb_up').sideEffect {sonValue = it[propertyToBeSummed]}.inContains.filterTypes(types)
                .incInt(VISITED, 1).incInt(propertyToBeSummed, sonValue).filter {it[VISITED] == it[NUMBER_OF_OUT]}
                .loop('climb_up') {true}.iterate()
    }

    /**
     * Counts number of constructors for each class and stores it as a {@code MY_NUMBER_OF_CONSTRUCTORS} property.
     * @param path File path of a Neo4j graph
     */
    public void numberOfConstructors(String path) {
        loadGremlin()
        Graph g = openNeo4j(path)
        try {
            g.V.filterTypeContainer.setInt(MY_NUMBER_OF_CONSTRUCTORS, 0).iterate()
            def x = ''
//            g.V.filterC.sideEffect {x = it.name}.outContains.filterM.filter {it.name == x}
//                    .as('marking').inContains.filterC.inc(MY_NUMBER_OF_CONSTRUCTORS, 1).loop('marking') {true}.iterate()
            def constructors = g.V.filterC.sideEffect {x = it[NAME]}.outContains.filterM.filter {it[NAME] == x}
            constructors.inContains.filterC.incInt(MY_NUMBER_OF_CONSTRUCTORS, 1).iterate()
            def leaves = g.V.filterTypeContainer.filter {it._().outContains.filterTypeContainer.iterate() == null}
            sumUp(g, leaves, MY_NUMBER_OF_CONSTRUCTORS, new HashSet<String>([CLASS_TYPE, INTERFACE_TYPE, ANNOTATION_TYPE, PACKAGE_TYPE, PARAMETERIZED_TYPE, PROJECT_TYPE]))
        } finally {
            g.shutdown()
        }
    }

    /**
     * Stores number of subtypes for each class and interface as a {@code MY_NUMBER_OF_SUBTYPES}.
     * @param g A graph
     */
    private void numberOfSubtypes(Graph g) {
        g.V.filterTypeContainer.setInt(MY_NUMBER_OF_SUBTYPES, 0).iterate()
        /* Store also the value in project vertex, package vertices. */
        g.V.filterTypeContainer.as('to_containing').inContains.filterTypeContainer.incInt(MY_NUMBER_OF_SUBTYPES, 1).loop('to_containing') {true}.iterate()
    }

    /**
     * Stores number of subtypes defined directly in each class and interface as their property named {@code MY_NUMBER_OF_DIRECT_SUBTYPES}.
     * @param g A graph
     */
    private void numberOfDirectSubtypes(Graph g) {
        g.V.filterTypeContainer.setInt(MY_NUMBER_OF_DIRECT_SUBTYPES, 0).iterate()
        /* Store also the value in project vertex, package vertices. */
        g.V.filterTypeContainer.inContains.incInt(MY_NUMBER_OF_DIRECT_SUBTYPES, 1).iterate()
    }

    /**
     * Stores an average number of subtypes defined in a type container as a {@code MY_AVERAGE_NUMBER_OF_SUBTYPES}.
     * @param path File path of the Neo4j db.
     * @return A metric value on the project node
     */
    public double averageNumberOfSubtypes(String path) {
        Graph g = openNeo4j(path)
        try {
            numberOfDirectSubtypes(g)
            numberOfSubtypes(g)
            g.V.filterTypeContainer.sideEffect {it[MY_AVERAGE_NUMBER_OF_SUBTYPES] = ((double) it[MY_NUMBER_OF_SUBTYPES]) / it[MY_NUMBER_OF_DIRECT_SUBTYPES]}.iterate()
            return g.v(PROJECT_VERTEX_ID)[MY_AVERAGE_NUMBER_OF_SUBTYPES]
        } finally {
            g.shutdown()
        }
    }

    /**
     * Returns number of vertices of given type that have different values of given numeric properties. It ignores stub nodes.
     * @param path A Neo4j graph.
     * @param type Name of type to filter
     * @param property1 First property name
     * @param property2 Second property name
     * @return Number of vertices that have non-equal properties.
     */
    public long differentOn(String path, String type, String property1, String property2) {
        Graph g = openNeo4j(path)
        try {
            def number = 0L
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