package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe

class CodeMetrics {
    public static final String MY_NUMBER_OF_CONSTRUCTORS = 'ag291541NumberOfConstructors'
    public static final String MY_DEPTH_OF_INHERITANCE = 'ag291541DepthOfInheritance'
    public static final String MY_NUMBER_OF_CONTAINED_TYPES = 'ag291541NumberOfContainedTypes'
    public static final String MY_NUMBER_OF_DIRECT_SUBTYPES = 'ag291541NumberOfDirectSubtypes'
    public static final String MY_AVERAGE_NUMBER_OF_SUBTYPES = 'ag291541AverageNumberOfSubtypes'
    public static final String MY_NUMBER_OF_CONTAINED_TYPES_SUBTYPES = 'ag291541NumberOfContainedTypesSubtypes'
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
        Gremlin.defineStep('filterV', [Vertex, Pipe], {_().filterType(VARIABLE_TYPE)})
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
        Gremlin.defineStep('outExtends', [Vertex, Pipe], {_().out(EXTENDS_EDGE)})
        Gremlin.defineStep('inExtends', [Vertex, Pipe], {_().in(EXTENDS_EDGE)})
        Gremlin.defineStep('outImplements', [Vertex, Pipe], {_().out(IMPLEMENTS_EDGE)})
        Gremlin.defineStep('outCalls', [Vertex, Pipe], {_().out(CALLS_EDGE)})
        Gremlin.defineStep('outReturns', [Vertex, Pipe], {_().out(RETURNS_EDGE)})
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
            def className = ''
            def types = new HashSet<String>([CLASS_TYPE, ANNOTATION_TYPE, PACKAGE_TYPE, PROJECT_TYPE])
            g.V.filterTypes(types).sideEffect {className = it.name}.outContains.filterM.filter {it.name == className}
                    .as('marking').inContains.filterTypes(types).incInt(MY_NUMBER_OF_CONSTRUCTORS, 1).loop('marking') {true}.iterate()
        } finally {
            g.shutdown()
        }
    }

    /**
     * Stores depth of inheritance for each class and interface as a {@code MY_DEPTH_OF_INHERITANCE} property.
     * @param g
     */
    private void depthOfInheritance(Graph g) {
        /* one for all so that those that extend stub nodes also have some value for this property;
         * interfaces (and annotations) have depth 1 by the definition */
        g.V.filterCIAP.setInt(MY_DEPTH_OF_INHERITANCE, 1).iterate()
        /* interfaces (and annotations) have depth 1 by the definition */
//        g.V.filterI.setInt(MY_DEPTH_OF_INHERITANCE, 1).iterate()
//        g.V.filterA.setInt(MY_DEPTH_OF_INHERITANCE, 1).iterate()
        def lastDepth = 0
        /* step down from the Object class */
        g.V.filter {it[KEY] == OBJECT_KEY}.sideEffect {it[MY_DEPTH_OF_INHERITANCE] = 1}
                .as('store_depth').sideEffect {lastDepth = it[MY_DEPTH_OF_INHERITANCE]}.inExtends.sideEffect {it[MY_DEPTH_OF_INHERITANCE] = lastDepth + 1}
                .loop('store_depth') {true}.iterate()
    }

    /**
     * Returns an average depth of inheritance hierarchy among all classes and interfaces. {@code java.lang.Object} has
     * depth 1 and every interface has depth 1.
     * @param path File path of the Neo4j graph
     * @return An average depth of inheritance.
     */
    public double averageDepthOfInheritance(String path) {
        loadGremlin()
        Graph g = openNeo4j(path)
        try {
            depthOfInheritance(g)
            def sumOfDepths = 0
            def countOfTypes = 0
            g.v(PROJECT_VERTEX_ID).as('come_to_son').outContains.filterCIAP.sideEffect {
                sumOfDepths += it[MY_DEPTH_OF_INHERITANCE]
                countOfTypes++
            }.loop('come_to_son') {true}.iterate()
            return ((double) sumOfDepths) / countOfTypes
        } finally {
            g.shutdown()
        }
    }

    /**
     * Stores number of subtypes defined directly in each class, interface, annotation and parameterized class as their
     * property named {@code MY_NUMBER_OF_DIRECT_SUBTYPES}.
     * @param g The graph
     */
    private void numberOfDirectSubtypes(Graph g) {
        g.V.filterCIAP.setInt(MY_NUMBER_OF_DIRECT_SUBTYPES, 0).iterate()
        /* for every class, interface, annotation and interface go to the superclass and increment its counter */
        g.V.filterCIAP.outExtends.incInt(MY_NUMBER_OF_DIRECT_SUBTYPES, 1).iterate()
    }

    /**
     * Stores number of types contained by each type container as their property named
     * {@code MY_NUMBER_OF_CONTAINED_TYPES}.
     * @param g The graph
     */
    private void numberOfContainedTypes(Graph g) {
        g.V.filterTypeContainer.setInt(MY_NUMBER_OF_CONTAINED_TYPES, 0).iterate()
        /* for every class, interface, annotation and interface go to the containing element and increment its counter */
        g.V.filterCIAP.inContains.incInt(MY_NUMBER_OF_CONTAINED_TYPES, 1).iterate()
    }

    /**
     * Stores sum of numbers of subtypes from each contained type as a property named {@MY_NUMBER_OF_CONTAINED_TYPES_SUBTYPES}.
     * @param g
     */
    private void numberOfContainedTypesSubtypes(Graph g) {
        g.V.filterTypeContainer.setInt(MY_NUMBER_OF_CONTAINED_TYPES_SUBTYPES, 0).iterate()
        /* tell the container how many subtypes you have */
        def numberOfDirectSubtypes = 0
        g.V.filterCIAP.sideEffect {numberOfDirectSubtypes = it[MY_NUMBER_OF_DIRECT_SUBTYPES]}.inContains
                .filterTypeContainer.sideEffect {it[MY_NUMBER_OF_CONTAINED_TYPES_SUBTYPES] += numberOfDirectSubtypes}
                .iterate()
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
            numberOfContainedTypes(g)
            numberOfContainedTypesSubtypes(g)
            /* count the ratio in every container */
            g.V.filterTypeContainer.sideEffect {
                it[MY_AVERAGE_NUMBER_OF_SUBTYPES] = ((double) it[MY_NUMBER_OF_CONTAINED_TYPES_SUBTYPES]) / ((double) it[MY_NUMBER_OF_CONTAINED_TYPES])
            }.iterate()
            return g.v(PROJECT_VERTEX_ID)[MY_AVERAGE_NUMBER_OF_SUBTYPES]
        } finally {
            g.shutdown()
        }
    }

    /**
     * Return a value of Efferent Couplings metric on vertex {@code v} from graph {@code g}.
     * @param g The graph
     * @param v The vertex (anything that contains some types)
     * @return A value of Efferent Couplings metric
     */
    private int efferentCouplings(Graph g, Vertex v) {
        /* mark all not touched yet */
        g.V.setBoolean(VISITED, false).setBoolean(FLAG, false).iterate()
        /* visit a whole subtree */
        v._().as('filtering').setBoolean(VISITED, true).outContains.loop('filtering') {true}.iterate()
        def x = null
        /* have methods that call unvisited methods */
        g.V.filter {it[VISITED]}.filter {!it[FLAG]}.filterCIAP.sideEffect {x = it}.outContains.filterM.outCalls.filter {!it[VISITED]}.sideEffect {x[FLAG] = true}.iterate()
        /* have methods that return unvisited types */
        g.V.filter {it[VISITED]}.filter {!it[FLAG]}.filterCIAP.sideEffect {x = it}.outContains.filterM.outReturns.filter {!it[VISITED]}.sideEffect {x[FLAG] = true}.iterate()
        /* extends unvisited types */
        g.V.filter {it[VISITED]}.filter {!it[FLAG]}.filterCIAP.sideEffect {x = it}.outExtends.filter {!it[VISITED]}.sideEffect {x[FLAG] = true}.iterate();
        /* implements unvisited types */
        g.V.filter {it[VISITED]}.filter {!it[FLAG]}.filterCIAP.sideEffect {x = it}.outImplements.filter {!it[VISITED]}.sideEffect {x[FLAG] = true}.iterate();
        /* sum it all */
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
     * @param path A Neo4j graph
     * @param type Name of type to filter
     * @param property1 First property name
     * @param property2 Second property name
     * @return Number of vertices that have non-equal properties (but who do have them defined)
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