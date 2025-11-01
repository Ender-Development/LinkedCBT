package io.enderdev.linkedcbt.util

// copied from RozUtils cause it's not in Catalyx ;p
// TODO actually, transform into a Kotlin Delegate and throw into Catalyx
/**
 * A cached config reader class
 * @param configValue A supplier of a config value that can change
 * @param parser A supplier of a parser for said config value
 * @property value The parsed latest config value
 * @author rozbrajaczpoziomow; part of RozUtils
 */
class ConfigHelper<CONFIG, RET>(private val configValue: () -> CONFIG, private val parser: (CONFIG) -> RET) {
	private var currValue: CONFIG = configValue()
	private var parsedValue: RET = parser(currValue)

	val value: RET
		get() {
			val curr = configValue()
			if(curr == currValue)
				return parsedValue

			currValue = curr
			parsedValue = parser(curr)
			return parsedValue
		}
}
