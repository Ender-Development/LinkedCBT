package io.enderdev.linkedtanks.client.gui

import io.enderdev.linkedtanks.Tags
import io.enderdev.linkedtanks.client.ClientChannelListManager
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.data.Constants
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import io.enderdev.linkedtanks.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.RenameButtonWrapper
import io.enderdev.linkedtanks.tiles.buttons.SideConfigurationButtonWrapper
import io.enderdev.linkedtanks.tiles.util.FluidSideConfiguration
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.inventory.IInventory
import net.minecraft.util.EnumFacing
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

	val fluidDisplayWrapper = CapabilityFluidDisplayWrapper(FLUID_TANK_X, FLUID_TANK_Y, FLUID_TANK_W, FLUID_TANK_H, tile::fluidHandler)

	var currentDisplay = CurrentDisplay.NONE
	val unlinkButton = LinkButtonWrapper(0, 0, UNLINK_BTN_W, UNLINK_BTN_H) // MAIN_OVERVIEW
	val deleteButton = DeleteButtonWrapper(0, 0) // MAIN_OVERVIEW
	val renameButton = RenameButtonWrapper(0, 0) // CHANNEL_RENAME; not rendered
	val sideConfigurationButtons = EnumFacing.entries.map { // MAIN_OVERVIEW
		SideConfigurationButtonWrapper(0, 0).apply {
			facing = it
		}
	}
	val linkChannelButtons = List(CHANNEL_LINK_MAX_DRAWN_CHANNELS) { // CHANNEL_LINK
		LinkButtonWrapper(0, 0, LINK_BTN_W, LINK_BTN_H)
	}

	var mouseClick: MouseClickData? = null

	var channelListSkipChannels = 0

	var mainOverviewDeleteClicked = false

	val canEditChannelData: Boolean
		get() = tile.channelData?.canBeEditedBy(Minecraft.getMinecraft().player.uniqueID) == true

	// yes, Minecraft.getMinecraft() is needed cause this runs before `fontRenderer`/`mc` get initialised
	val channelRenameTypedString = GuiTextField(0, Minecraft.getMinecraft().fontRenderer, NAME_TEXT_X, NAME_TEXT_Y, Minecraft.getMinecraft().fontRenderer.getCharWidth('W') * Constants.CHANNEL_NAME_LENGTH_LIMIT, FONT_HEIGHT).apply {
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

		deleteButton.x = guiLeft + DELETE_BTN_X
		deleteButton.y = guiTop + DELETE_BTN_Y
		unlinkButton.x = guiLeft + UNLINK_BTN_X
		unlinkButton.y = guiTop + UNLINK_BTN_Y
		repositionAndUpdateSideConfigurationButtons()
		repositionLinkChannelButtons()

		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW) {
			if(deleteButton.button!!.visible)
				buttonList.add(deleteButton.button)

			if(unlinkButton.button!!.visible)
				buttonList.add(unlinkButton.button)

			sideConfigurationButtons.mapTo(buttonList, SideConfigurationButtonWrapper::button)
		} else if(currentDisplay == CurrentDisplay.CHANNEL_LINK)
			linkChannelButtons.mapTo(buttonList, LinkButtonWrapper::button)
	}

	@Suppress("NOTHING_TO_INLINE") // this is only here to segment it off from initGui
	private inline fun repositionAndUpdateSideConfigurationButtons() {
		// .NU
		// W.E
		// .SD
		val gridArrangement = arrayOf(
			null, EnumFacing.NORTH, EnumFacing.UP,
			EnumFacing.WEST, null, EnumFacing.EAST,
			null, EnumFacing.SOUTH, EnumFacing.DOWN
		)

		sideConfigurationButtons.forEach { btn ->
			val gridIdx = gridArrangement.indexOf(btn.facing)
			val gridCol = gridIdx % 3
			val gridRow = gridIdx / 3
			btn.x = guiLeft + SIDE_CONFIG_BTN_X + SIDE_CONFIG_BTN_W * gridCol
			btn.y = guiTop + SIDE_CONFIG_BTN_Y + SIDE_CONFIG_BTN_H * gridRow
			btn.side = tile.fluidSideConfiguration[btn.facing]
		}
	}

	@Suppress("NOTHING_TO_INLINE") // this is only here to segment it off from initGui
	private inline fun repositionLinkChannelButtons() {
		linkChannelButtons.forEachIndexed { idx, btn ->
			btn.x = guiLeft + LINK_BTN_X
			btn.y = guiTop + LINK_BTN_Y + LINK_BTN_OFF_Y * idx
		}
	}

	override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
		displayData.remove(fluidDisplayWrapper)
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW)
			displayData.add(fluidDisplayWrapper)

		when(currentDisplay) {
			CurrentDisplay.MAIN_OVERVIEW -> {
				// draw fluid tank bg
				drawTexturedModalRect(guiLeft + FLUID_TANK_BACKGROUND_X, guiTop + FLUID_TANK_BACKGROUND_Y, FLUID_TANK_BACKGROUND_U, FLUID_TANK_BACKGROUND_V, FLUID_TANK_BACKGROUND_W, FLUID_TANK_BACKGROUND_H)
				// draw fluid tank
				drawFluidTank(fluidDisplayWrapper, guiLeft + fluidDisplayWrapper.x, guiTop + fluidDisplayWrapper.y)
			}
			CurrentDisplay.CHANNEL_LINK -> {
				linkChannelButtons.forEach {
					it.button!!.visible = false
				}

				for(idx in (channelListSkipChannels..<(channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS).coerceAtMost(ClientChannelListManager.channels.size))) {
					val btnIdx = idx - channelListSkipChannels
					val btn = linkChannelButtons[btnIdx]
					btn.button!!.visible = true
					btn.channelId = ClientChannelListManager.channels[idx].id
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
				if(canEditChannelData && isHovered(guiLeft + NAME_TEXT_X, guiTop + NAME_TEXT_Y, fontRenderer.getStringWidth(nameText), FONT_HEIGHT, mouseX, mouseY)) {
					nameColour = HIGHLIGHTED_TEXT_COLOUR
					if(mouseClick?.btn == 0)
						switchDisplay(CurrentDisplay.CHANNEL_RENAME)
				}

				fontRenderer.drawString(nameText, NAME_TEXT_X, NAME_TEXT_Y, nameColour)
				fontRenderer.drawString("Owner: ${tile.channelData?.ownerUsername}", OWNER_TEXT_X, OWNER_TEXT_Y, TEXT_COLOUR)
				fontRenderer.drawString(fluidDisplayWrapper.textLines[0], CONTENTS_TEXT_X, CONTENTS_TEXT_Y, TEXT_COLOUR)

				if(mainOverviewDeleteClicked) {
					drawCenteredString(fontRenderer, "Are you sure?", DELETE_BTN_CONFIRMATION_TEXT_X, DELETE_BTN_CONFIRMATION_TEXT_Y, RED_TEXT_COLOUR)
					mainOverviewDeleteClicked = deleteButton.button!!.hovered
				}

				// this could be theoretically moved to [SideConfigurationButtonWrapper], but then I'd need to pass in the gridOffsetX/Y to it to properly position the text
				sideConfigurationButtons.forEach { btn ->
					if(btn.button!!.hovered)
						drawCenteredString(fontRenderer, "§${btn.side.colour.formattingCode}${btn.side.describe(btn.facing)}", SIDE_CONFIG_BTN_X + SIDE_CONFIG_TOOLTIP_OFF_X, SIDE_CONFIG_BTN_Y + SIDE_CONFIG_TOOLTIP_OFF_Y, 0)
				}
			}
			CurrentDisplay.CHANNEL_LINK -> {
				fontRenderer.drawString("Channels:", CHANNELS_TEXT_X, CHANNELS_TEXT_Y, TEXT_COLOUR)

				if(channelListSkipChannels > 0)
					fontRenderer.drawString("^", CHANNELS_SCROLL_HINT_UP_X, CHANNELS_SCROLL_HINT_UP_Y, HIGHLIGHTED_TEXT_COLOUR)

				if(channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS < ClientChannelListManager.channels.size)
					fontRenderer.drawString("v", CHANNELS_SCROLL_HINT_DOWN_X, CHANNELS_SCROLL_HINT_DOWN_Y, HIGHLIGHTED_TEXT_COLOUR)
			}
			CurrentDisplay.CHANNEL_RENAME -> {
				channelRenameTypedString.drawTextBox()
			}
			else -> TODO(currentDisplay.debugName)
		}

		if(DevUtils.isDeobfuscated) {
			fontRenderer.drawString(currentDisplay.debugName, 50, -10, RED_TEXT_COLOUR)
			fontRenderer.drawString(tile.channelId.toString(), -300, (ySize shr 1) - FONT_HEIGHT, RED_TEXT_COLOUR)
			fontRenderer.drawSplitString(tile.channelData.toString(), -300, ySize shr 1, 300, RED_TEXT_COLOUR)
		}

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

		AbstractButtonWrapper.getWrapper<SideConfigurationButtonWrapper>(button)?.let {
			it.side = it.side.let {
				if(isShiftKeyDown())
					FluidSideConfiguration.Side.NONE
				else if(mouseClick!!.btn == 1)
					it.previous()
				else
					it.next()
			}
			it.affectsAll = isCtrlKeyDown()
			if(it.affectsAll)
				sideConfigurationButtons.forEach { allBtns ->
					allBtns.side = it.side
				}
		}

		super.actionPerformed(button)

		AbstractButtonWrapper.getWrapper<LinkButtonWrapper>(button)?.let {
			if(it.channelId == Constants.NO_CHANNEL)
				switchDisplay(CurrentDisplay.CHANNEL_LINK)
			else {
				// update client-side tile to hopefully display the correct data faster (also needed for unlinkButton visiblity)
				tile.channelId = it.channelId
				tile.channelData = ClientChannelListManager.channels.find { ch -> ch.id == it.channelId }?.toFakeChannelData()
				switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
			}
		}
	}

	override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
		mouseClick = MouseClickData(mouseX, mouseY, mouseButton)
		super.mouseClicked(mouseX, mouseY, mouseButton)
		// copied and edited from from super.super.mouseClicked(mouseX, mouseY, mouseButton) but changed `mouseButton == 1`
		if(mouseButton == 1)
			for(btn in sideConfigurationButtons) {
				val btn = btn.button!!
				if(!btn.mousePressed(mc, mouseX, mouseY))
					continue

				selectedButton = btn
				btn.playPressSound(mc.soundHandler)
				actionPerformed(btn)
			}
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
			buttonList.remove(unlinkButton.button)
			buttonList.remove(deleteButton.button)
			buttonList.removeAll(sideConfigurationButtons.map(SideConfigurationButtonWrapper::button))
		}

		if(new == CurrentDisplay.MAIN_OVERVIEW) {
			displayData.add(fluidDisplayWrapper)
			buttonList.add(unlinkButton.button)
			buttonList.add(deleteButton.button)
			sideConfigurationButtons.mapTo(buttonList, SideConfigurationButtonWrapper::button)
		}

		unlinkButton.button!!.visible = new == CurrentDisplay.MAIN_OVERVIEW && canEditChannelData
		deleteButton.button!!.visible = new == CurrentDisplay.MAIN_OVERVIEW && canEditChannelData
		sideConfigurationButtons.forEach {
			it.button!!.visible = new == CurrentDisplay.MAIN_OVERVIEW
			it.button!!.enabled = canEditChannelData
		}

		if(new == CurrentDisplay.CHANNEL_LINK)
			linkChannelButtons.mapTo(buttonList, LinkButtonWrapper::button)

		if(currentDisplay == CurrentDisplay.CHANNEL_LINK) {
			channelListSkipChannels = 0
			buttonList.removeAll(linkChannelButtons.map(LinkButtonWrapper::button))
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
		// shared
		const val TEXT_COLOUR = 0x505090
		const val HIGHLIGHTED_TEXT_COLOUR = 0x7080BA
		const val RED_TEXT_COLOUR = 0xFF0000 or TEXT_COLOUR
		const val BASE_TEXT_X = 8
		const val BASE_TEXT_Y = 8

		val FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT

		// --- MAIN_OVERVIEW ---
		const val NAME_TEXT_X = BASE_TEXT_X + 20
		const val NAME_TEXT_Y = BASE_TEXT_Y

		const val OWNER_TEXT_X = NAME_TEXT_X
		val OWNER_TEXT_Y = NAME_TEXT_Y + FONT_HEIGHT + 1

		const val CONTENTS_TEXT_X = NAME_TEXT_X
		val CONTENTS_TEXT_Y = OWNER_TEXT_Y + FONT_HEIGHT + 1

		const val FLUID_TANK_X = 8
		const val FLUID_TANK_Y = 8
		const val FLUID_TANK_W = 16
		const val FLUID_TANK_H = 70

		const val FLUID_TANK_BACKGROUND_X = FLUID_TANK_X - 1
		const val FLUID_TANK_BACKGROUND_Y = FLUID_TANK_Y - 1
		const val FLUID_TANK_BACKGROUND_W = FLUID_TANK_W + 2
		const val FLUID_TANK_BACKGROUND_H = FLUID_TANK_H + 2
		const val FLUID_TANK_BACKGROUND_U = 175
		const val FLUID_TANK_BACKGROUND_V = 0

		const val DELETE_BTN_X = 98
		const val DELETE_BTN_Y = 79
		const val DELETE_BTN_W = 36
		const val DELETE_BTN_H = 13
		const val DELETE_BTN_U = 193
		const val DELETE_BTN_V = 26
		const val DELETE_BTN_V_HOVERED = DELETE_BTN_V + DELETE_BTN_H
		const val DELETE_BTN_TEXT_OFF_X = 3
		const val DELETE_BTN_TEXT_OFF_Y = 3
		const val DELETE_BTN_CONFIRMATION_TEXT_X = DELETE_BTN_X + DELETE_BTN_TEXT_OFF_X + (DELETE_BTN_W shr 1)
		val DELETE_BTN_CONFIRMATION_TEXT_Y = DELETE_BTN_Y + DELETE_BTN_TEXT_OFF_Y - FONT_HEIGHT - 4

		const val UNLINK_BTN_X = 136
		const val UNLINK_BTN_Y = DELETE_BTN_Y
		const val UNLINK_BTN_W = 33
		const val UNLINK_BTN_H = 13
		const val UNLINK_BTN_U = 193
		const val UNLINK_BTN_V = 0
		const val UNLINK_BTN_V_HOVERED = UNLINK_BTN_V + UNLINK_BTN_H
		const val UNLINK_BTN_TEXT_OFF_X = DELETE_BTN_TEXT_OFF_X
		const val UNLINK_BTN_TEXT_OFF_Y = DELETE_BTN_TEXT_OFF_Y

		const val SIDE_CONFIG_BTN_X = 40
		const val SIDE_CONFIG_BTN_Y = 49
		const val SIDE_CONFIG_BTN_W = 10
		const val SIDE_CONFIG_BTN_H = 10
		const val SIDE_CONFIG_TOOLTIP_OFF_X = SIDE_CONFIG_BTN_W * 3 shr 1
		const val SIDE_CONFIG_TOOLTIP_OFF_Y = SIDE_CONFIG_BTN_H * 3 + 3

		// --- CHANNEL_LINK ---
		const val CHANNEL_LINK_MAX_DRAWN_CHANNELS = 4

		const val CHANNELS_TEXT_X = BASE_TEXT_X
		const val CHANNELS_TEXT_Y = BASE_TEXT_Y

		const val LINK_BTN_X = 7
		const val LINK_BTN_Y = 20
		const val LINK_BTN_W = 150
		const val LINK_BTN_H = 13
		const val LINK_BTN_U = 0
		const val LINK_BTN_V = 179
		const val LINK_BTN_TEXT_OFF_X = 3
		const val LINK_BTN_TEXT_OFF_Y = 3
		const val LINK_BTN_OFF_Y = LINK_BTN_H + LINK_BTN_TEXT_OFF_Y
		const val LINK_BTN_V_HOVERED = LINK_BTN_V + LINK_BTN_H

		const val CHANNELS_SCROLL_HINT_UP_X = 161
		const val CHANNELS_SCROLL_HINT_UP_Y = 23
		const val CHANNELS_SCROLL_HINT_DOWN_X = CHANNELS_SCROLL_HINT_UP_X
		const val CHANNELS_SCROLL_HINT_DOWN_Y = CHANNELS_SCROLL_HINT_UP_Y + LINK_BTN_OFF_Y * (CHANNEL_LINK_MAX_DRAWN_CHANNELS - 1)
	}

	// TODO
	// - translation
	// - fluid whitelist selector?
	// - channel selector search?
}
