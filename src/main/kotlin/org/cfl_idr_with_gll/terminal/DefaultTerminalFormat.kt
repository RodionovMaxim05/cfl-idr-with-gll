package org.cfl_idr_with_gll.terminal

import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType

/**
 * Default implementation of [ITerminalFormat] using a specific string encoding.
 *
 * This implementation uses the following format: `{direction}{type}--{id}`
 * - `direction`: 'o' for opening, 'c' for closing
 * - `type`: 'p' for parentheses, 'b' for brackets
 * - `id`: arbitrary identifier string
 */
object DefaultTerminalFormat : ITerminalFormat {
	/**
	 * Generates a label in format `{direction}{type}--{id}`.
	 */
	override fun generateLabel(type: BracketType, id: String, isOpen: Boolean): String {
		val prefix = if (isOpen) 'o' else 'c'
		val typeChar = when (type) {
			BracketType.Parentheses -> 'p'
			BracketType.Brackets -> 'b'
		}
		return "$prefix$typeChar--$id"
	}

	/**
	 * Determines bracket type by examining the second character of the label.
	 */
	override fun getType(label: String): BracketType? = when (label[1]) {
		'p' -> BracketType.Parentheses
		'b' -> BracketType.Brackets
		else -> null
	}

	/**
	 * Determines if a label is an opening bracket by analyzing the first character of the label.
	 */
	override fun isOpen(label: String): Boolean? = when (label[0]) {
		'o' -> true
		'c' -> false
		else -> null
	}

	/**
	 * Extracts the identifier from the label (substring after "--").
	 */
	override fun extractId(label: String): String =
		label.substringAfter("--", "")

	/**
	 * Swaps the direction character ('o' <-> 'c') to find the matching bracket.
	 */
	override fun matchingLabel(label: String): String =
		when (label.firstOrNull()) {
			'o' -> "c" + label.drop(1)
			'c' -> "o" + label.drop(1)
			else -> error("Unknown label: $label")
		}
}
