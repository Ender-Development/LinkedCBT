package io.enderdev.linkedcbt.util.extensions

import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.ender_development.catalyx.utils.extensions.getColor
import java.awt.Color

// TODO this is copied straight from Catalyx as the function there is `internal`, when Catalyx BIOv2 gets merged into main, remove `internal` from the Catalyx impl and give it a KDoc
private val waterColour = Color.blue.rgb
private val lavaColour = Color(0x81, 0x3d, 0x0e).rgb
internal fun FluidStack.getRealColor() =
	when(fluid) {
		FluidRegistry.WATER -> waterColour
		FluidRegistry.LAVA -> lavaColour
		else -> getColor()
	}
