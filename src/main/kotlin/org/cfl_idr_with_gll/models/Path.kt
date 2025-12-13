package org.cfl_idr_with_gll.models

/**
 * Represents a directed path between two vertices in a graph.
 *
 * @property source the source vertex of the path
 * @property target the target vertex of the path
 */
data class Path<V>(val source: V, val target: V)
