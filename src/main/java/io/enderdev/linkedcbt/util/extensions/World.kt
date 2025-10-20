package io.enderdev.linkedcbt.util.extensions

import net.minecraft.world.World

// TODO verify if this is correct; alternatively can always do `DimensionManager.getWorldIDs().find { DimensionManager.getWorld(it) === this }`
val World.dimId: Int
	inline get() = provider.dimensionType.id
