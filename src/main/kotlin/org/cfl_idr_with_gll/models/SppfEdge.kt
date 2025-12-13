package org.cfl_idr_with_gll.models

import org.ucfs.rsm.symbol.ITerminal

/**
 * Represents a directed edge extracted from an SPPF (Shared Packed Parse Forest) structure.
 *
 * @property from the source vertex of the edge
 * @property label the terminal label associated with the edge
 * @property to the target vertex of the edge
 */
data class SppfEdge<VertexType>(val from: VertexType, val label: ITerminal, val to: VertexType)
