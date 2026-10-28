package com.google.common.graph;

import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
abstract class GraphsBridgeMethods {
    GraphsBridgeMethods() {
    }

    public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
        return Graphs.transitiveClosure((Graph) graph);
    }

    public static <N> Set<N> reachableNodes(Graph<N> graph, N node) {
        return Graphs.reachableNodes((Graph) graph, (Object) node);
    }
}
