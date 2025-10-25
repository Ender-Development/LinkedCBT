package io.enderdev.linkedcbt

import net.minecraftforge.common.config.Config

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
object LCBTConfig {
	@JvmStatic
	@Config.Name("Chests")
	val chests = Chests()

	class Chests() {
		@Config.Name("Push/pull only every [ticks]")
		@Config.Comment("Process push/pull operations only every [ticks] (i.e. 1 => every tick, 2 => every other tick, …)")
		@Config.RangeInt(min = 1, max = Int.MAX_VALUE)
		var pushPullEvery = 1

		// TODO: didn't feel like implementing right meow (implement this maybe? or maybe not ;p)
		//@Config.Name("Push/pull slot limit")
		//@Config.Comment("When processing push/pull operations, only the given amount of possible slots may be transferred at a time")
		//@Config.RangeInt(min = 1, max = Constants.LINKED_CHEST_INVENTORY_SIZE)
		//var pushPullMaxSlots = 2
	}

	@JvmStatic
	@Config.Name("Batteries")
	val batteries = Batteries()

	class Batteries {
		@Config.Name("Battery capacity")
		@Config.Comment("Max energy amount [FE] stored in a channel")
		@Config.RangeInt(min = 1, max = Int.MAX_VALUE)
		var capacity = 20000

		@Config.Name("Battery capacity changes with battery count")
		@Config.Comment(
			"Should battery capacity be multiplied by the total amount of batteries in a channel",
			"ex. if you have a battery capacity of 20 kFE, and the channel has 3 batteriies connected to it, the total channel capacity will be 60 kFE"
		)
		var capacityChangesWithBatteryCount = false

		@Config.Name("Max battery push/pull throughput per side")
		@Config.Comment(
			"Max energy amount [FE] that a battery will try to push/pull per side per tick",
			"0 to disable any limits"
		)
		@Config.RangeInt(min = 0, max = Int.MAX_VALUE)
		var maxPushPullThroughput = 1500
	}

	@JvmStatic
	@Config.Name("Tanks")
	val tanks = Tanks()

	class Tanks {
		@Config.Name("Tank capacity")
		@Config.Comment("Max fluid amount [mB] stored in a channel")
		@Config.RangeInt(min = 1, max = Int.MAX_VALUE)
		var capacity = 8000

		@Config.Name("Tank capacity changes with tank count")
		@Config.Comment(
			"Should tank capacity be multiplied by the total amount of tanks in a channel",
			"ex. if you have a tank capacity of 8,000 mB, and the channel has 3 tanks connected to it, the total channel capacity will be 24,000 mB"
		)
		var capacityChangesWithTankCount = false

		@Config.Name("Max tank push/pull throughput per side")
		@Config.Comment(
			"Max fluid amount [mB] that a tank will try to push/pull per side per tick",
			"0 to disable any limits"
		)
		@Config.RangeInt(min = 0, max = Int.MAX_VALUE)
		var maxPushPullThroughput = 250
	}

	fun jvmLoadClass() {}
}
