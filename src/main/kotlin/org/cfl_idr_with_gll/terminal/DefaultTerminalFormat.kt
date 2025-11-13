package org.cfl_idr_with_gll.terminal

import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType

object DefaultTerminalFormat : ITerminalFormat {
	override fun generateLabel(type: BracketType, id: String, isOpen: Boolean): String {
		val prefix = if (isOpen) 'o' else 'c'
		val typeChar = when (type) {
			BracketType.Parentheses -> 'p'
			BracketType.Brackets -> 'b'
		}
		return "$prefix$typeChar--$id"
	}

	override fun getType(label: String): BracketType? = when (label[1]) {
		'p' -> BracketType.Parentheses
		'b' -> BracketType.Brackets
		else -> null
	}

	override fun extractId(label: String): String =
		label.substringAfter("--", "")

	override fun matchingLabel(label: String): String =
		when (label.firstOrNull()) {
			'o' -> "c" + label.drop(1)
			'c' -> "o" + label.drop(1)
			else -> error("Unknown label: $label")
		}
}

