package org.cfl_idr_with_gll.graph

import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph

/**
 * Represents a complete edge with explicit source, target, and label.
 */
data class FullEdge<V, L>(val from: V, val to: V, val label: L)

/**
 * Given a Dyck-style bracket label, returns its matching counterpart:
 * - "op--N" <-> "cp--N"
 * - "ob--N" <-> "cb--N"
 *
 * @param label a non-empty string starting with 'o' (open) or 'c' (close)
 * @return the corresponding opposite label
 * @throws IllegalArgumentException if the label does not start with 'o' or 'c'
 */
fun otherLabel(label: String): String = when (label[0]) {
	'o' -> "c" + label.substring(1)
	'c' -> "o" + label.substring(1)
	else -> error("Unknown Dyck label: $label")
}

/**
 * Parses a Dyck-labeled graph to extract balanced bracket components.
 *
 * This function:
 * 1. Identifies complete Dyck pairs.
 * 2. Collects numeric IDs of balanced parentheses and brackets.
 * 3. Constructs a new graph containing only edges labeled "normal",
 * and edges whose labels participate in complete Dyck pairs.
 *
 * The algorithm assumes Dyck labels follow the format:
 * - `[o|c]>[p|b]--<id>`, e.g., "op--42", "cb--7"
 *
 * @param graph input graph with Dyck-style edge labels
 * @return a triple of:
 *   - list of balanced parentheses IDs,
 *   - list of balanced brackets IDs,
 *   - filtered graph containing only edges from complete Dyck pairs or labeled "normal"
 */
fun <V, L : ILabel> parseDyckComponent(graph: InputGraph<V, L>): Triple<List<Int>, List<Int>, InputGraph<V, L>> {
	val seen = mutableSetOf<String>()
	val parId = mutableListOf<Int>()
	val braId = mutableListOf<Int>()

	val allEdges = graph.vertices.flatMap { from ->
		graph.getEdges(from).map { edge ->
			FullEdge(from, edge.targetVertex, edge.label)
		}
	}

	for (e in allEdges) {
		val label = e.label.toString()
		if (label.isEmpty() || label == "normal") continue

		if (label !in seen && otherLabel(label) in seen) {
			val idString = label.substring(4)
			val currId = idString.toIntOrNull() ?: continue

			when (label[1]) {
				'p' -> parId.add(currId)
				'b' -> braId.add(currId)
			}
		}
		seen.add(label)
	}

	val parsedDyck = InputGraph<V, L>()
	for (e in allEdges) {
		val label = e.label.toString()
		if (label == "normal" || (label.isNotEmpty() && label in seen && otherLabel(label) in seen)) {
			parsedDyck.addVertex(e.from)
			parsedDyck.addVertex(e.to)
			parsedDyck.addEdge(e.from, e.label, e.to)
		}
	}

	return Triple(parId, braId, parsedDyck)
}

/**
 * Naive Dyck component parser that collects all bracket IDs without checking for matching pairs.
 *
 * This function:
 * - Ignores edges labeled "normal" or with empty labels.
 * - Extracts numeric IDs from Dyck-style labels.
 * - Assumes label format: [o|c][p|b]--<id> (e.g., "op--42", "cb--7").
 * - Does NOT verify that both open and close labels exist.
 *
 * @param graph input graph with Dyck-style edge labels
 * @return a triple of:
 *   - list of parentheses IDs,
 *   - list of brackets IDs,
 *   - a copy of the input graph containing all non-empty labeled edges
 */
fun <V, L : ILabel> parseDyckComponentNaive(graph: InputGraph<V, L>): Triple<List<Int>, List<Int>, InputGraph<V, L>> {
	val seen = mutableSetOf<String>()
	val parId = mutableListOf<Int>()
	val braId = mutableListOf<Int>()

	val allEdges = graph.vertices.flatMap { from ->
		graph.getEdges(from).map { edge ->
			FullEdge(from, edge.targetVertex, edge.label)
		}
	}

	for (e in allEdges) {
		val label = e.label.toString()
		if (label == "normal" || label.isEmpty()) continue

		val idKey = label.substring(1)
		if (idKey !in seen) {
			val idString = label.substring(4)
			val id = idString.toIntOrNull() ?: continue

			when (label[1]) {
				'p' -> parId.add(id)
				'b' -> braId.add(id)
			}
		}
		seen.add(idKey)
	}

	val parsedDyck = InputGraph<V, L>()
	for (e in allEdges) {
		val label = e.label.toString()
		if (label.isNotEmpty()) {
			parsedDyck.addVertex(e.from)
			parsedDyck.addVertex(e.to)
			parsedDyck.addEdge(e.from, e.label, e.to)
		}
	}

	return Triple(parId, braId, parsedDyck)
}