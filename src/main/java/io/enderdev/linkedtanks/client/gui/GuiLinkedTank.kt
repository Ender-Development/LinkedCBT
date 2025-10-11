package io.enderdev.linkedtanks.client.gui

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.ClientChannelListManager
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.network.ChannelListPacket
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import net.minecraft.inventory.IInventory
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.client.gui.wrappers.CapabilityFluidDisplayWrapper
import org.lwjgl.input.Mouse

class GuiLinkedTank(playerInv: IInventory, val tile: TileLinkedTank) : BaseGuiTyped<TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")
	override val displayName = ""

	val fluidDisplayWrapper = CapabilityFluidDisplayWrapper(8, 8, 16, 70, tile::fluidHandler)
	lateinit var linkButton: TileLinkedTank.LinkButtonWrapper
	var currentDisplay = CurrentDisplay.MAIN_OVERVIEW
	var mouseClick: MouseClickData? = null

	var channelListDrawnChannels: List<ChannelListPacket.ClientChannelData> = emptyList()
	var channelListSkipChannels = 0 // TODO scrolling

	init {
		displayData.add(fluidDisplayWrapper)
		if(tile.channelId == TileLinkedTank.NO_CHANNEL)
			switchDisplay(CurrentDisplay.CHANNEL_LINK)
	}

	override fun initGui() {
		super.initGui()
		// remove the default buttons cause meh
		buttonList.remove(redstoneButton.button)
		buttonList.remove(pauseButton.button)

		linkButton = TileLinkedTank.LinkButtonWrapper(guiLeft + 10, guiTop + 10)
		linkButton.button!!.visible = false
		buttonList.add(linkButton.button)
	}

	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
		displayData.remove(fluidDisplayWrapper)
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW)
			displayData.add(fluidDisplayWrapper)

		val leftX = (width - xSize) shr 1
		val topY = (height - ySize) shr 1

		when(currentDisplay) {
			CurrentDisplay.MAIN_OVERVIEW -> {
				drawTexturedModalRect(leftX + 7, topY + 7, 175, 0, 18, 72)
				drawFluidTank(fluidDisplayWrapper, leftX + fluidDisplayWrapper.x, topY + fluidDisplayWrapper.y)
			}
			CurrentDisplay.CHANNEL_LINK -> {
				channelListDrawnChannels = ClientChannelListManager.channels.subList(channelListSkipChannels, (channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS).coerceAtMost(ClientChannelListManager.channels.size))
				for(idx in channelListDrawnChannels.indices) {
					val x = leftX + 7
					val y = topY + 20 + 16 * idx
					var v = 179
					if(isHovered(leftX + 7, topY + 20 + 16 * idx, 150, 13, mouseX, mouseY)) {
						v += 13
						if(mouseClick?.btn == 0) { // should be fine to not even check the x,y here
							linkButton.channelId = channelListDrawnChannels[idx].id
							actionPerformed(linkButton.button!!)
							switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
						}
					}
					drawTexturedModalRect(x, y, 0, v, 150, 13)
				}
			}
			else -> TODO(currentDisplay.debugName)
		}
	}

	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY)

		val leftX = (width - xSize) shr 1
		val topY = (height - ySize) shr 1

		when(currentDisplay) {
			CurrentDisplay.MAIN_OVERVIEW -> {
				val nameText = "#${tile.channelId} ${tile.channelData?.name}"
				var nameColour = TEXT_COLOUR
				if(isHovered(leftX + 28, topY + 8, fontRenderer.getStringWidth(nameText), fontRenderer.FONT_HEIGHT, mouseX, mouseY)) {
					nameColour = HIGHLIGHTED_TEXT_COLOUR
					if(mouseClick?.btn == 0)
						switchDisplay(CurrentDisplay.CHANNEL_RENAME)
				}
				fontRenderer.drawString(nameText, 28, 8, nameColour)
				fontRenderer.drawString("Owner: ${tile.channelData?.ownerUsername}", 28, 18, TEXT_COLOUR)
				fontRenderer.drawString("debug: ${tile.channelData?.fluid} ${tile.channelData?.fluidAmount}/${tile.channelData?.fluidCapacity} [${tile.channelData?.fluidCapacityOverride}]", 28, 28, TEXT_COLOUR)
			}
			CurrentDisplay.CHANNEL_LINK -> {
				fontRenderer.drawString("Channels:", 8, 8, TEXT_COLOUR)

				for(idx in channelListDrawnChannels.indices) {
					val channel = channelListDrawnChannels[idx]
					fontRenderer.drawString("${if(channel.id == TileLinkedTank.CREATE_NEW_CHANNEL) "+" else "#${channel.id}"} ${channel.name}", 10, 23 + 16 * idx, TEXT_COLOUR)
				}
			}
			else -> TODO(currentDisplay.debugName)
		}
		fontRenderer.drawString(currentDisplay.debugName, xSize shr 1, ySize - 20, 0xff0000 or TEXT_COLOUR)

		mouseClick = null
	}

	override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
		mouseClick = MouseClickData(mouseX, mouseY, mouseButton)
	}

	override fun handleMouseInput() {
		super.handleMouseInput()
		val scroll = Mouse.getEventDWheel()
		if(currentDisplay != CurrentDisplay.CHANNEL_LINK)
			return

		if(scroll != 0) {
			if(scroll > 0) {
				if(channelListSkipChannels != 0)
					--channelListSkipChannels
			} else {
				if(ClientChannelListManager.channels.size - CHANNEL_LINK_MAX_DRAWN_CHANNELS > channelListSkipChannels)
					++channelListSkipChannels
			}
		}
	}

	fun switchDisplay(new: CurrentDisplay) {
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW)
			displayData.remove(fluidDisplayWrapper)

		if(new == CurrentDisplay.MAIN_OVERVIEW)
			displayData.add(fluidDisplayWrapper)

		if(currentDisplay == CurrentDisplay.CHANNEL_LINK)
			channelListSkipChannels = 0

		currentDisplay = new
	}

	enum class CurrentDisplay(val debugName: String) {
		// main screen you go to when you open the GUI
		MAIN_OVERVIEW("main_overview"),

		// rename the current channel
		CHANNEL_RENAME("channel_rename"),

		// link to a new channel
		CHANNEL_LINK("channel_link")
	}

	data class MouseClickData(val x: Int, val y: Int, val btn: Int)

	companion object {
		const val TEXT_COLOUR = 0x505090
		const val HIGHLIGHTED_TEXT_COLOUR = 0x7080BA
		const val CHANNEL_LINK_MAX_DRAWN_CHANNELS = 4
	}

	// TODO: link GUI ;p
	// - current link state (fluid, amount, id, ownername)
	// - menu to link to a new group
	// - renaming current group
}
