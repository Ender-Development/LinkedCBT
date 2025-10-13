package io.enderdev.linkedtanks.client.gui

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.ClientChannelData
import io.enderdev.linkedtanks.client.ClientChannelListManager
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.RenameButtonWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.inventory.IInventory
import net.minecraft.util.ResourceLocation
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.client.gui.wrappers.CapabilityFluidDisplayWrapper
import org.ender_development.catalyx.utils.DevUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class GuiLinkedTank(playerInv: IInventory, val tile: TileLinkedTank) : BaseGuiTyped<TileLinkedTank>(ContainerLinkedTank(playerInv, tile), tile) {
	override val textureLocation = ResourceLocation(Tags.MOD_ID, "textures/gui/container/linked_tank_gui.png")
	override val displayName = ""

	val fluidDisplayWrapper = CapabilityFluidDisplayWrapper(8, 8, 16, 70, tile::fluidHandler)

	var currentDisplay = CurrentDisplay.NONE
	val linkButton = LinkButtonWrapper(0, 0) // MAIN_OVERVIEW
	val deleteButton = DeleteButtonWrapper(0, 0) // MAIN_OVERVIEW
	val renameButton = RenameButtonWrapper(0, 0) // not rendered

	var mouseClick: MouseClickData? = null

	var channelListDrawnChannels = emptyList<ClientChannelData>()
	var channelListSkipChannels = 0

	var mainOverviewDeleteClicked = false

	val canEditChannelData: Boolean
		get() = tile.channelData?.canBeEditedBy(Minecraft.getMinecraft().player.uniqueID) == true

	// yes, Minecraft.getMinecraft() is needed cause this runs before `fontRenderer`/`mc` get initialised
	val channelRenameTypedString = GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 28, 8, Minecraft.getMinecraft().fontRenderer.getCharWidth('W') * Constants.CHANNEL_NAME_LENGTH_LIMIT, Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT).apply {
		maxStringLength = Constants.CHANNEL_NAME_LENGTH_LIMIT
		enableBackgroundDrawing = false
		setTextColor(HIGHLIGHTED_TEXT_COLOUR)
	}

	init {
		displayData.add(fluidDisplayWrapper)
		if(tile.channelId == Constants.NO_CHANNEL)
			switchDisplay(CurrentDisplay.CHANNEL_LINK)
		else
			switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
	}

	override fun initGui() {
		super.initGui()
		// remove the default buttons cause meh
		buttonList.remove(redstoneButton.button)
		buttonList.remove(pauseButton.button)

		linkButton.x = guiLeft + 136
		linkButton.y = guiTop + 79
		deleteButton.x = guiLeft + 98
		deleteButton.y = linkButton.y

		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW) {
			if(linkButton.button!!.visible)
				buttonList.add(linkButton.button)

			if(deleteButton.button!!.visible)
				buttonList.add(deleteButton.button)
		}
	}

	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
		displayData.remove(fluidDisplayWrapper)
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW)
			displayData.add(fluidDisplayWrapper)

		when(currentDisplay) {
			CurrentDisplay.MAIN_OVERVIEW -> {
				drawTexturedModalRect(guiLeft + 7, guiTop + 7, 175, 0, 18, 72)
				drawFluidTank(fluidDisplayWrapper, guiLeft + fluidDisplayWrapper.x, guiTop + fluidDisplayWrapper.y)
			}
			CurrentDisplay.CHANNEL_LINK -> {
				channelListDrawnChannels = ClientChannelListManager.channels.subList(channelListSkipChannels, (channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS).coerceAtMost(ClientChannelListManager.channels.size))
				for(idx in channelListDrawnChannels.indices) {
					val x = guiLeft + 7
					val y = guiTop + 20 + 16 * idx
					var v = 179
					if(isHovered(guiLeft + 7, guiTop + 20 + 16 * idx, 150, 13, mouseX, mouseY)) {
						v += 13
						if(mouseClick?.btn == 0) { // should be fine to not even check the x,y here
							val channel = channelListDrawnChannels[idx]
							// update client-side tile to hopefully display the correct data faster (also needed for (un)linkButton visiblity)
							tile.channelId = channel.id
							tile.channelData = channel.toFakeChannelData()

							linkButton.channelId = channel.id
							actionPerformed(linkButton.button!!)
							switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
						}
					}
					drawTexturedModalRect(x, y, 0, v, 150, 13)
				}
			}
			CurrentDisplay.CHANNEL_RENAME -> {}
			else -> TODO(currentDisplay.debugName)
		}
	}

	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY)

		when(currentDisplay) {
			CurrentDisplay.MAIN_OVERVIEW -> {
				val nameText = "#${tile.channelId} ${tile.channelData?.name}"
				var nameColour = TEXT_COLOUR
				if(canEditChannelData && isHovered(guiLeft + 28, guiTop + 8, fontRenderer.getStringWidth(nameText), fontRenderer.FONT_HEIGHT, mouseX, mouseY)) {
					nameColour = HIGHLIGHTED_TEXT_COLOUR
					if(mouseClick?.btn == 0)
						switchDisplay(CurrentDisplay.CHANNEL_RENAME)
				}

				fontRenderer.drawString(nameText, 28, 8, nameColour)
				fontRenderer.drawString("Owner: ${tile.channelData?.ownerUsername}", 28, 18, TEXT_COLOUR)
				fontRenderer.drawString(fluidDisplayWrapper.textLines[0], 28, 28, TEXT_COLOUR)

				if(linkButton.button!!.visible)
					fontRenderer.drawString("Unlink", linkButton.x + 3 - guiLeft, linkButton.y + 3 - guiTop, TEXT_COLOUR)

				if(deleteButton.button!!.visible) {
					val textX = deleteButton.x + 3 - guiLeft
					val textY = deleteButton.y + 3 - guiTop
					fontRenderer.drawString("Delete", textX, textY, RED_TEXT_COLOUR)

					if(mainOverviewDeleteClicked) {
						drawCenteredString(fontRenderer, "Are you sure?", textX + (deleteButton.width shr 1), textY - fontRenderer.FONT_HEIGHT - 4, RED_TEXT_COLOUR)
						mainOverviewDeleteClicked = deleteButton.button!!.hovered
					}
				}
			}
			CurrentDisplay.CHANNEL_LINK -> {
				fontRenderer.drawString("Channels:", 8, 8, TEXT_COLOUR)

				for(idx in channelListDrawnChannels.indices) {
					val channel = channelListDrawnChannels[idx]
					fontRenderer.drawString("${if(channel.id == Constants.CREATE_NEW_CHANNEL) "+" else "#${channel.id}"} ${channel.name}", 10, 23 + 16 * idx, TEXT_COLOUR)
				}

				if(channelListSkipChannels > 0)
					fontRenderer.drawString("^", 161, 23, HIGHLIGHTED_TEXT_COLOUR)

				if(channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS < ClientChannelListManager.channels.size)
					fontRenderer.drawString("v", 161, 23 + 16 * (CHANNEL_LINK_MAX_DRAWN_CHANNELS - 1), HIGHLIGHTED_TEXT_COLOUR)
			}
			CurrentDisplay.CHANNEL_RENAME -> {
				channelRenameTypedString.drawTextBox()
			}
			else -> TODO(currentDisplay.debugName)
		}
		if(DevUtils.isDeobfuscated)
			fontRenderer.drawString(currentDisplay.debugName, 50, -10, RED_TEXT_COLOUR)

		mouseClick = null
	}

	override fun renderTooltips(mouseX: Int, mouseY: Int) {} // no-op

	override fun actionPerformed(button: GuiButton) {
		AbstractButtonWrapper.getWrapper<DeleteButtonWrapper>(button)?.let {
			if(!mainOverviewDeleteClicked) {
				mainOverviewDeleteClicked = true
				return
			}
			switchDisplay(CurrentDisplay.CHANNEL_LINK)
		}

		super.actionPerformed(button)

		AbstractButtonWrapper.getWrapper<LinkButtonWrapper>(button)?.let {
			if(it.channelId == Constants.NO_CHANNEL)
				switchDisplay(CurrentDisplay.CHANNEL_LINK)
		}
	}

	override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
		super.mouseClicked(mouseX, mouseY, mouseButton)
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

	override fun keyTyped(typedChar: Char, keyCode: Int) {
		if(currentDisplay == CurrentDisplay.CHANNEL_RENAME) {
			if(keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
				val text = channelRenameTypedString.text.trim()
				if(text.isEmpty())
					return

				tile.channelData?.let { it.name = text } // not needed but whatever
				renameButton.newName = text
				actionPerformed(renameButton.button!!)
				switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
			} else
				channelRenameTypedString.textboxKeyTyped(typedChar, keyCode)
		} else
			super.keyTyped(typedChar, keyCode)
	}

	fun switchDisplay(new: CurrentDisplay) {
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW) {
			displayData.remove(fluidDisplayWrapper)
			buttonList.remove(linkButton.button)
			buttonList.remove(deleteButton.button)
		}

		if(new == CurrentDisplay.MAIN_OVERVIEW) {
			displayData.add(fluidDisplayWrapper)
			buttonList.add(linkButton.button)
			buttonList.add(deleteButton.button)
		}

		linkButton.button!!.visible = new == CurrentDisplay.MAIN_OVERVIEW && canEditChannelData
		deleteButton.button!!.visible = new == CurrentDisplay.MAIN_OVERVIEW && canEditChannelData

		if(currentDisplay == CurrentDisplay.CHANNEL_LINK) {
			channelListSkipChannels = 0
			linkButton.channelId = Constants.NO_CHANNEL
		}

		if(new == CurrentDisplay.CHANNEL_RENAME) {
			channelRenameTypedString.isFocused = true
			channelRenameTypedString.visible = true
			channelRenameTypedString.text = tile.channelData?.name ?: "???"
			Keyboard.enableRepeatEvents(true)
		}

		if(currentDisplay == CurrentDisplay.CHANNEL_RENAME) {
			channelRenameTypedString.isFocused = false
			channelRenameTypedString.visible = false
			Keyboard.enableRepeatEvents(false)
		}

		currentDisplay = new
	}

	override fun onGuiClosed() {
		super.onGuiClosed()
		Keyboard.enableRepeatEvents(false)
	}

	enum class CurrentDisplay(val debugName: String) {
		// no screen
		NONE("none"),

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
		const val RED_TEXT_COLOUR = 0xFF0000 or TEXT_COLOUR
		const val CHANNEL_LINK_MAX_DRAWN_CHANNELS = 4
	}

	// TODO
	// - current link state (fluid, amount)
	// - translation
	// - fluid whitelist selector?
	// - channel selector search?
}
