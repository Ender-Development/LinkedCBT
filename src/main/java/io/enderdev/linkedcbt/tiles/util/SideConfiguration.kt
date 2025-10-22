package io.enderdev.linkedcbt.tiles.util

import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting

enum class SideConfiguration(val named: String, val colour: TextFormatting) {
	/**
	 * The default state of a side, i.e. can input and output
	 */
	DEFAULT("default", TextFormatting.WHITE),
	/**
	 * No inputting or outputting may occur
	 */
	NONE("none", TextFormatting.DARK_GRAY),
	/**
	 * Can only input
	 */
	INPUT("input", TextFormatting.BLUE),
	/**
	 * Will try to pull from the adjacened tile
	 */
	PULL("pull", TextFormatting.AQUA),
	/**
	 * Can only output
	 */
	OUTPUT("output", TextFormatting.YELLOW),
	/**
	 * Will try to push to the adjacened tile
	 */
	PUSH("push", TextFormatting.GOLD);

	fun previous() =
		when(this) {
			DEFAULT -> PUSH
			NONE -> DEFAULT
			INPUT -> NONE
			PULL -> INPUT
			OUTPUT -> PULL
			PUSH -> OUTPUT
		}

	fun next() =
		when(this) {
			DEFAULT -> NONE
			NONE -> INPUT
			INPUT -> PULL
			PULL -> OUTPUT
			OUTPUT -> PUSH
			PUSH -> DEFAULT
		}

	val u: Int
		get() = when(this) {
			DEFAULT, PULL -> 193
			NONE, OUTPUT -> 203
			INPUT, PUSH -> 213
		}

	val v: Int
		get() = when(this) {
			DEFAULT, NONE, INPUT -> 52
			PULL, OUTPUT, PUSH -> 62
		}

	// should this be translatable? maybe.
	fun describe(facing: EnumFacing) =
		"${facing.name.lowercase().replaceFirstChar(Char::uppercaseChar)} - $named"
}
