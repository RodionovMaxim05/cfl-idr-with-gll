package org.cfl_idr_with_gll.terminal

/**
 * Defines a protocol for parsing and generating parentheses and bracket labeled terminals used in Dyck language processing.
 *
 * This interface provides methods for working with parentheses and bracket labels that represent
 * opening and closing parentheses/brackets in context-free languages. Implementations define
 * the specific format for encoding parentheses/bracket type, identifier, and direction (open/close).
 */
interface ITerminalFormat {
	/**
	 * Represents the type of bracket used in terminal labels.
	 */
	enum class BracketType {
		/** Parentheses: ( ) */
		Parentheses,

		/** Brackets: [ ] */
		Brackets,
	}

	/**
	 * Generates a terminal label for a given bracket configuration.
	 *
	 * @param type the type of bracket (parentheses or brackets)
	 * @param id a unique identifier for the bracket pair
	 * @param isOpen `true` for opening bracket, `false` for closing bracket
	 * @return a formatted string representing the terminal label
	 */
	fun generateLabel(type: BracketType, id: String, isOpen: Boolean): String

	/**
	 * Extracts the bracket type from a terminal label.
	 *
	 * @param label the terminal label to parse
	 * @return the [BracketType] if the label format is recognized, `null` otherwise
	 */
	fun getType(label: String): BracketType?

	/**
	 * Determines if a label represents an opening bracket.
	 *
	 * @param label the terminal label to check
	 * @return `true` if the label represents an opening bracket,
	 *         `false` if it represents a closing bracket,
	 *         `null` if the label format is invalid
	 */
	fun isOpen(label: String): Boolean?

	/**
	 * Extracts the identifier from a terminal label.
	 *
	 * @param label the terminal label to parse
	 * @return the identifier string
	 */
	fun extractId(label: String): String

	/**
	 * Generates the matching bracket label for a given label.
	 *
	 * This method converts an opening bracket to its corresponding closing bracket,
	 * or vice versa, while preserving the bracket type and identifier.
	 *
	 * @param label the terminal label (either opening or closing)
	 * @return the matching label with opposite direction
	 */
	fun matchingLabel(label: String): String
}
