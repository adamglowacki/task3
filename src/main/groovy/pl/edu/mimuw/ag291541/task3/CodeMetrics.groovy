package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe

class CodeMetrics {
    public static final String MY_NUMBER_OF_CONSTRUCTORS = 'ag291541NumberOfConstructors'
    public static final String MY_INHERITANCE_DEPTH = 'ag291541NumberOfConstructors'
    private static final String NAME = 'name'

    private static final String CLASS_TYPE = 'class'
    private static final String METHOD_TYPE = 'method'

    private static final String CONTAINS_EDGE = 'CONTAINS'

    private void loadGremlin() {
        Gremlin.load()
        def cl = {final String typeName -> _().filter {it.TYPE_PROPERTY == typeName}}
        Gremlin.defineStep('filterType', [Vertex, Pipe], cl)
        Gremlin.defineStep('filterClass', [Vertex, Pipe], {_().filterType(CLASS_TYPE)})
        Gremlin.defineStep('filterMethod', [Vertex, Pipe], {_().filterType(METHOD_TYPE)})
        Gremlin.defineStep('filterName', [Vertex, Pipe], {final String desiredName -> _().filter {it[NAME] == desiredName}})
        Gremlin.defineStep('filterNotStub', [Vertex, Pipe], {_().filter {!it.stubNode}})
        Gremlin.defineStep('outContains', [Vertex, Pipe], {_().out(CONTAINS_EDGE)})
        Gremlin.defineStep('inContains', [Vertex, Pipe], {_().in(CONTAINS_EDGE)})
        Gremlin.defineStep('inc', [Vertex, Pipe], {final String name, final float value -> _().sideEffect {it[name] += value}})
        Gremlin.defineStep('zero', [Vertex, Pipe], {final String property -> _().sideEffect {it[property] = 0.0f}})
    }

    public void numberOfConstructors(String path) {
        loadGremlin()
        Graph g = openNeo4j(path)
        try {
            g.V.filterClass.zero(MY_NUMBER_OF_CONSTRUCTORS).iterate()
            def x = ''
            g.V.filterClass.sideEffect {x = it.name}.outContains.filterMethod.filter {it.name == x}.as('marking').inContains.filterClass.inc(MY_NUMBER_OF_CONSTRUCTORS,
                    1.0f).loop('marking') {true}.iterate()
        } finally {
            g.shutdown()
        }
    }

    private static Graph openNeo4j(String path) {
        return new Neo4jGraph(path)
    }
}