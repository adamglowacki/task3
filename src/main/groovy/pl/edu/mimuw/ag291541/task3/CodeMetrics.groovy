package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.pipes.Pipe

class CodeMetrics {
    public static final String NUMBER_OF_CONSTRUCTORS_PROPERTY = 'ag291541NumberOfConstructors'

    private static final String CLASS_TYPE = 'class'
    private static final String METHOD_TYPE = 'method'

    private static final String CONTAINS_EDGE = 'CONTAINS'

    static {
        Gremlin.load()


        Gremlin.defineStep('filterType', [Vertex, Pipe], {final String typeName -> _().filter {it.TYPE_PROPERTY == typeName}})
        Gremlin.defineStep('filterClass', [Vertex, Pipe], {null._().filter {it.TYPE_PROPERTY == CLASS_TYPE}})
        Gremlin.defineStep('filterMethod', [Vertex, Pipe], {_().filterType(METHOD_TYPE)})
        Gremlin.defineStep('filterName', [Vertex, Pipe], {final String desiredName -> _().filter {it.name == desiredName}})
        Gremlin.defineStep('filterNotStub', [Vertex, Pipe], {_().filter {!it.stubNode}})
        Gremlin.defineStep('outContains', [Vertex, Pipe], {_().out(CONTAINS_EDGE)})
        Gremlin.defineStep('inContains', [Vertex, Pipe], {_().in(CONTAINS_EDGE)})
        Gremlin.defineStep('incProperty', [Vertex, Pipe], {final String name, final String value -> _().sideEffect {it[name] += value}})

    }


    /**
     * For each vertex of type <code>typeName</code> change its property <code>propertyName</code> to
     * <code>value</code>.
     * @param g .sideEffect{ print("==>" + it) }* @param propertyName
     * @param value
     * @param typeName
     */
    private static void resetProperty(Graph g, String propertyName, Object value, String typeName) {
        g.vertices._().filterType(typeName.toString()).sideEffect {it[propertyName] = value}
    }

    /**
     * Adds to every class a propertvertices._()y <code>NUMBER_OF_CONSTRUCTORS_PROPERTY</code> with a number of all constructors
     * of the class and every class contained by it.
     * @param g The graph t$__clinit__closure2.o be measured and modified.      ().filter {it.TYPE_PROPERTY == CLASS_TYPE}}
     */
    public static void numberOfConstructors(Graph g) {
        int count = 0


        g._().V.filterClass.each {println(it); count++}
//        g._().V.filter {it.TYPE_PROPERTY == CLASS_TYPE}.each {println(it); count++}
        println(count)

//        println(g.v(1).outE.collect {it})

//        g.vertices._().filter {it['TYPE_PROPERTY'] == 'class'}.each { println(it['TYPE_PROPERTY']) }

//        g.vertices._().filter {it['TYPE_PROPERTY'] == 'class'}.sideEffect {it['ag291541NumberOfConstructors'] = 0}.

//        resetProperty(g, NUMBER_OF_CONSTRUCTORS_PROPERTY, 0, CLASS_TYPE)

//        g.vertices._().filterClass().filterNotStub.sideEffect {x = it.name}.outContains.filterMethod.filterName(x).inContains
//                .filterClass.setProperty(NUMBER_OF_CONSTRUCTORS_PROPERTY, 1).loop(3)
    }

    public static void nothing(Graph g) {

    }


    public static void main(String[] args) {



        Neo4jGraph g = new Neo4jGraph('/tmp/junit')
        CodeMetrics.numberOfConstructors(g)
        g.shutdown()
    }

}
