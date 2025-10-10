package utils.sppfForExtractEdges

import TestExtractEdgesFromSppfResult.MockTerminal
import org.ucfs.sppf.node.InputRange
import org.ucfs.sppf.node.RangeSppfNode
import org.ucfs.sppf.node.TerminalType


fun buildSimpleSppf(): Set<RangeSppfNode<Int>> {
	val terminalNode = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = null,
		type = TerminalType(MockTerminal("a"))
	)

	return setOf(terminalNode)
}
