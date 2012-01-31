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
    public static final String MY_EFFERENT_COUPLINGS = 'ag291541MyEfferentCouplings'
    public static final String VISITED = 'ag291541Visited'
    public static final String NUMBER_OF_OUT = 'ag291541NumberOfOut'
    public static final String FLAG = 'ag291541Flag'
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
    private static final String VARIABLE_TYPE = 'type_variable'

    private static final String CONTAINS_EDGE = 'CONTAINS'
    private static final String IMPLEMENTS_EDGE = 'IMPLEMENTS'
    private static final String EXTENDS_EDGE = 'EXTENDS'
    private static final String CALLS_EDGE = 'CALLS'
    private static final String TAKES_EDGE = 'TAKES'
    private static final String HAS_TYPE_EDGE = 'HAS_TYPE'
    private static final String RETURNS_EDGE = 'RETURNS'

    public static final int PROJECT_VERTEX_ID = 1

    private void loadGremlin() {
        Gremlin.load()
        Gremlin.defineStep('filterType', [Vertex, Pipe], {final String typeName -> _().filter {it[TYPE] == typeName}})
        Gremlin.defineStep('filterTypes', [Vertex, Pipe], {final Set<String> allowed -> _().filter {allowed.contains(it[TYPE])}})
        Gremlin.defineStep('filterNotTypes', [Vertex, Pipe], {final Set<String> allowed -> _().filter {!allowed.contains(it[TYPE])}})
        Gremlin.defineStep('filterC', [Vertex, Pipe], {_().filterType(CLASS_TYPE)})
        Gremlin.defineStep('filterM', [Vertex, Pipe], {_().filterType(METHOD_TYPE)})
        Gremlin.defineStep('filterI', [Vertex, Pipe], {_().filterType(INTERFACE_TYPE)})
        Gremlin.defineStep('filterA', [Vertex, Pipe], {_().filterType(ANNOTATION_TYPE)})
        def ciap = new HashSet<String>([CLASS_TYPE, INTERFACE_TYPE, ANNOTATION_TYPE, PARAMETERIZED_TYPE])
        Gremlin.defineStep('filterCIAP', [Vertex, Pipe], {_().filterTypes(ciap)})
        def typeContainer = new HashSet<String>([CLASS_TYPE, INTERFACE_TYPE, ANNOTATION_TYPE, PARAMETERIZED_TYPE, PACKAGE_TYPE, PROJECT_TYPE])
        Gremlin.defineStep('filterTypeContainer', [Vertex, Pipe], {_().filterTypes(typeContainer)})
        def javaTypes = new HashSet<String>([CLASS_TYPE, ANNOTATION_TYPE, INTERFACE_TYPE, PARAMETERIZED_TYPE])
        Gremlin.defineStep('filterWithoutJavaTypes', [Vertex, Pipe], {_().filterNotTypes(javaTypes)})
        Gremlin.defineStep('filterName', [Vertex, Pipe], {final String desiredName -> _().filter {it[NAME] == desiredName}})
        Gremlin.defineStep('filterNotStub', [Vertex, Pipe], {_().filter {!it.stubNode}})
        Gremlin.defineStep('outContains', [Vertex, Pipe], {_().out(CONTAINS_EDGE)})
        Gremlin.defineStep('inContains', [Vertex, Pipe], {_().in(CONTAINS_EDGE)})
        Gremlin.defineStep('inExtends', [Vertex, Pipe], {_().in(EXTENDS_EDGE)})
        Gremlin.defineStep('setFloat', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('setInt', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('setBoolean', [Vertex, Pipe], {final String property, final boolean value -> _().sideEffect {it[property] = value}})
        Gremlin.defineStep('incFloat', [Vertex, Pipe], {final String property, final float value -> _().sideEffect {it[property] = (it[property] != null ? it[property] : 0.0) + value}})
        Gremlin.defineStep('incInt', [Vertex, Pipe], {final String property, final int value -> _().sideEffect {it[property] = (it[property] != null ? it[property] : 0) + value}})
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
            def types = new HashSet<String>([CLASS_TYPE, ANNOTATION_TYPE, PACKAGE_TYPE, PROJECT_TYPE])
            g.V.filterTypes(types).sideEffect {x = it.name}.outContains.filterM.filter {it.name == x}
                    .as('marking').inContains.filterTypes(types).incInt(MY_NUMBER_OF_CONSTRUCTORS, 1).loop('marking') {true}.iterate()
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
        loadGremlin()
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

    private int efferentCouplings(Graph g, Vertex v) {
        /* all not touched yet */
        g.V.setBoolean(VISITED, false).setBoolean(FLAG, false).iterate()
        /* mark all subtree */
        v._().as('filtering').setBoolean(VISITED, true).outContains.loop('filtering') {true}.iterate()
        /* flag those that have some methods calling unvisited, returning unvisited or variables of unvisited type, or classes extending/implementing
         * unvisited type */
        def x = null
        v._().as('filtering').filterTypeContainer.sideEffect {x = it}.copySplit(
                _().as('coming_out').out.sideEffect {
                    if (!it[VISITED])
                        x.setProperty(FLAG, true)
                }.filterWithoutJavaTypes.filter {it[VISITED]}.loop('coming_out') {true},
                _().outContains.loop('filtering') {true}
        ).fairMerge.iterate()
        int value = 0
        g.V.filter {it[FLAG]}.sideEffect {value++}.iterate()
        return value
    }

    /**
     * Measures efferent couplings for the vertex of the graph and returns it.
     * @param path A path to the Neo4j db
     * @param id Id of the vertex to be measured
     * @return Efferent couplings metric value
     */
    public int efferentCouplingsForSpecified(String path, long id) {
        loadGremlin()
        Graph g = openNeo4j(path)
        try {
            return efferentCouplings(g, g.v(id))
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