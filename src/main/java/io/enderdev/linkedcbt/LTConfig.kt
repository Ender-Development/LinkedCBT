package io.enderdev.linkedcbt

import net.minecraftforge.common.config.Config

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
object LTConfig {
	@JvmField
	@Config.Name("Tank capacity")
	@Config.Comment("Max fluid amount [mB] stored in a channel")
	@Config.RangeInt(min = 1, max = Int.MAX_VALUE)
	var tankCapacity = 8000

	@JvmField
	@Config.Name("Tank capacity changes with tank count")
	@Config.Comment(
		"Should tank capacity be multiplied by the total amount of tanks in a channel",
		"ex. if you have a tank capacity of 8,000 mB, and the channel has 3 tanks connected to it, the total channel capacity will be 24,000 mB"
	)
	var tankCapacityChangesWithTankCount = false

	@JvmField
	@Config.Name("Max tank push/pull throughput per side")
	@Config.Comment(
		"Max fluid amount [mB] that a tank will try to push/pull per side per tick",
		"0 to disable any limits"
	)
	@Config.RangeInt(min = 0, max = Int.MAX_VALUE)
	var maxPushPullThroughput = 250

	fun jvmLoadClass() {}
}
