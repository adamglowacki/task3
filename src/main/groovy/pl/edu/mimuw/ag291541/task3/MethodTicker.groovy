package pl.edu.mimuw.ag291541.task3

import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.gremlin.groovy.Gremlin

class MethodTicker {
    static {
        Gremlin.load()
    }

    public static void tick() {
        println('tik, tik')
    }

    public static List<Vertex> findClasses(Graph g) {
//        Graph h = TinkerGraphFactory.createTinkerGraph()
        def results = []
//        h.v(1).out('knows').fill(results)
        g.vertices._().filter {it.TYPE_PROPERTY == 'class'}.fill(results)
        return results
    }
}
