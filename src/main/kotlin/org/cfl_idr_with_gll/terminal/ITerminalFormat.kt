package org.cfl_idr_with_gll.terminal

interface ITerminalFormat {
	enum class BracketType {
		Parentheses, // ( )
		Brackets,    // [ ]
	}

	fun generateLabel(type: BracketType, id: String, isOpen: Boolean): String
	fun getType(label: String): BracketType?
	fun extractId(label: String): String
	fun matchingLabel(label: String): String
}
