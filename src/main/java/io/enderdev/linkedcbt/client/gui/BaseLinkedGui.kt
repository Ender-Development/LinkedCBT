package io.enderdev.linkedcbt.client.gui

import io.enderdev.linkedcbt.Tags
import io.enderdev.linkedcbt.data.Constants
import io.enderdev.linkedcbt.data.base.BaseChannelData
import io.enderdev.linkedcbt.data.base.client.BaseClientChannelListManager
import io.enderdev.linkedcbt.data.base.client.ClientBaseChannelData
import io.enderdev.linkedcbt.tiles.BaseLinkedTile
import io.enderdev.linkedcbt.tiles.buttons.DeleteButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.LinkButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.RenameButtonWrapper
import io.enderdev.linkedcbt.tiles.buttons.SideConfigurationButtonWrapper
import io.enderdev.linkedcbt.tiles.util.SideConfiguration
import io.enderdev.linkedcbt.util.extensions.guiTranslate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n
import net.minecraft.util.EnumFacing
import org.ender_development.catalyx.client.button.AbstractButtonWrapper
import org.ender_development.catalyx.client.container.BaseContainer
import org.ender_development.catalyx.client.gui.BaseGuiTyped
import org.ender_development.catalyx.client.gui.wrappers.CapabilityDisplayWrapper
import org.ender_development.catalyx.client.gui.wrappers.CapabilityEnergyDisplayWrapper
import org.ender_development.catalyx.client.gui.wrappers.CapabilityFluidDisplayWrapper
import org.ender_development.catalyx.utils.DevUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

abstract class BaseLinkedGui<CH_DATA : BaseChannelData<CH_DATA, CLIENT_CH_DATA>, CLIENT_CH_DATA : ClientBaseChannelData<CLIENT_CH_DATA, CH_DATA>, TE : BaseLinkedTile<TE, CH_DATA, *, *>>(container: BaseContainer, val tile: TE, val channelListManager: BaseClientChannelListManager<CH_DATA, CLIENT_CH_DATA, *>) : BaseGuiTyped<TE>(container, tile) {
	override val textureLocation = Constants.LINKED_CBT_GUI
	override val displayName = ""

	abstract val displayWrapper: CapabilityDisplayWrapper?

	var currentDisplay = CurrentDisplay.NONE
	val unlinkButton = LinkButtonWrapper(0, 0, UNLINK_BTN_W, UNLINK_BTN_H, channelListManager) // MAIN_OVERVIEW
	val deleteButton = DeleteButtonWrapper(0, 0) // MAIN_OVERVIEW
	val renameButton = RenameButtonWrapper(0, 0) // CHANNEL_RENAME; not rendered
	val sideConfigurationButtons = EnumFacing.entries.map { // MAIN_OVERVIEW
		SideConfigurationButtonWrapper(0, 0).apply {
			facing = it
		}
	}
	val linkChannelButtons = Array(CHANNEL_LINK_MAX_DRAWN_CHANNELS) { // CHANNEL_LINK
		LinkButtonWrapper(0, 0, LINK_BTN_W, LINK_BTN_H, channelListManager)
	}

	var mouseClick: MouseClickData? = null
	val canEditChannelData: Boolean
		get() = tile.channelData?.canBeEditedBy(Minecraft.getMinecraft().player.uniqueID) == true

	var channelListSkipChannels = 0
	var channelListMatchingChannels = 0
	val channelListSearchBar = GuiTextField(1, FONT_RENDERER, CHANNEL_SEARCH_X, CHANNEL_SEARCH_Y, CHANNEL_SEARCH_W, CHANNEL_SEARCH_H).apply {
		maxStringLength = CHANNEL_SEARCH_MAX_LENGTH
		enableBackgroundDrawing = false
		setTextColor(HIGHLIGHTED_TEXT_COLOUR)
	}

	var mainOverviewDeleteClicked = false

	val channelRenameTextField = GuiTextField(0, FONT_RENDERER, NAME_TEXT_X, NAME_TEXT_Y, FONT_RENDERER.getCharWidth('W') * Constants.CHANNEL_NAME_LENGTH_LIMIT, FONT_HEIGHT).apply {
		maxStringLength = Constants.CHANNEL_NAME_LENGTH_LIMIT
		enableBackgroundDrawing = false
		setTextColor(HIGHLIGHTED_TEXT_COLOUR)
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

		if(currentDisplay == CurrentDisplay.NONE) {
			if(tile.channelId == Constants.NO_CHANNEL)
				switchDisplay(CurrentDisplay.CHANNEL_LINK)
			else
				switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
			return
		}

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
			btn.side = tile.sideConfiguration[btn.facing]
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
		displayData.remove(displayWrapper)
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY)
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW)
			displayWrapper?.let {
				displayData.add(it)
			}

		@Suppress("REDUNDANT_ELSE_IN_WHEN")
		when(currentDisplay) {
			CurrentDisplay.NONE -> {} // shouldn't happen, but don't crash if it does
			CurrentDisplay.MAIN_OVERVIEW -> {
				// draw bar bg
				drawTexturedModalRect(guiLeft + BAR_BACKGROUND_X, guiTop + BAR_BACKGROUND_Y, BAR_BACKGROUND_U, BAR_BACKGROUND_V, BAR_BACKGROUND_W, BAR_BACKGROUND_H)
				// draw bar
				val wrapper = displayWrapper
				when(wrapper) {
					null -> customDrawMainOverview(partialTicks, mouseX, mouseY)
					is CapabilityFluidDisplayWrapper -> drawFluidTank(wrapper, guiLeft + wrapper.x, guiTop + wrapper.y)
					is CapabilityEnergyDisplayWrapper -> drawPowerBar(wrapper, powerBarTexture, powerBarX, powerBarY)
				}
			}
			CurrentDisplay.CHANNEL_LINK -> {
				linkChannelButtons.forEach {
					it.button!!.visible = false
				}

				channelListMatchingChannels = 0
				var skip = channelListSkipChannels
				var btnIdx = 0
				for(channel in channelListManager.channels) {
					if(!channel.displayName.contains(channelListSearchBar.text, true))
						continue

					++channelListMatchingChannels

					if(btnIdx < CHANNEL_LINK_MAX_DRAWN_CHANNELS && skip-- <= 0)
						linkChannelButtons[btnIdx++].apply {
							button!!.visible = true
							channelId = channel.id
						}
				}

				channelListSkipChannels = channelListSkipChannels.coerceIn(0, (channelListMatchingChannels - CHANNEL_LINK_MAX_DRAWN_CHANNELS).coerceAtLeast(0))

				if(btnIdx < CHANNEL_LINK_MAX_DRAWN_CHANNELS && (btnIdx == 0 || linkChannelButtons[btnIdx - 1].channelId != Constants.CREATE_NEW_CHANNEL))
					linkChannelButtons[btnIdx].apply {
						button!!.visible = true
						channelId = Constants.CREATE_NEW_CHANNEL
					}
			}
			CurrentDisplay.CHANNEL_RENAME -> {}
			else -> error(currentDisplay.debugName)
		}
	}

	open fun customDrawMainOverview(partialTicks: Float, mouseX: Int, mouseY: Int) {}

	override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY)

		@Suppress("REDUNDANT_ELSE_IN_WHEN")
		when(currentDisplay) {
			CurrentDisplay.NONE -> {} // shouldn't happen, but don't crash if it does
			CurrentDisplay.MAIN_OVERVIEW -> {
				val nameText = tile.channelData?.displayName(tile.channelId) ?: "???"
				var nameColour = TEXT_COLOUR
				if(canEditChannelData && isHovered(guiLeft + NAME_TEXT_X, guiTop + NAME_TEXT_Y, FONT_RENDERER.getStringWidth(nameText), FONT_HEIGHT, mouseX, mouseY)) {
					nameColour = HIGHLIGHTED_TEXT_COLOUR
					if(mouseClick?.btn == 0)
						switchDisplay(CurrentDisplay.CHANNEL_RENAME)
				}

				FONT_RENDERER.drawString(nameText, NAME_TEXT_X, NAME_TEXT_Y, nameColour)
				// for some reason, this cannot use "owner".guiTranslate(...); in the GUI it shows up as "Owner: [Ljava.lang.Object;@address"
				FONT_RENDERER.drawString(I18n.format("${TRANSLATION_BASE}owner", tile.channelData?.ownerUsername), OWNER_TEXT_X, OWNER_TEXT_Y, TEXT_COLOUR)
				FONT_RENDERER.drawString(displayWrapper?.textLines?.get(0) ?: "", CONTENTS_TEXT_X, CONTENTS_TEXT_Y, TEXT_COLOUR)

				if(mainOverviewDeleteClicked) {
					drawCenteredString(FONT_RENDERER, "confirmation".guiTranslate(), DELETE_BTN_CONFIRMATION_TEXT_X, DELETE_BTN_CONFIRMATION_TEXT_Y, RED_TEXT_COLOUR)
					mainOverviewDeleteClicked = deleteButton.button!!.hovered
				}

				// this could be theoretically moved to [SideConfigurationButtonWrapper], but then I'd need to pass in the gridOffsetX/Y to it to properly position the text
				sideConfigurationButtons.forEach { btn ->
					if(btn.button!!.hovered)
						drawCenteredString(FONT_RENDERER, "§${btn.side.colour.formattingCode}${btn.side.describe(btn.facing)}", SIDE_CONFIG_BTN_X + SIDE_CONFIG_TOOLTIP_OFF_X, SIDE_CONFIG_BTN_Y + SIDE_CONFIG_TOOLTIP_OFF_Y, 0)
				}
			}
			CurrentDisplay.CHANNEL_LINK -> {
				FONT_RENDERER.drawString("channels".guiTranslate(), CHANNELS_TEXT_X, CHANNELS_TEXT_Y, TEXT_COLOUR)

				if(channelListSkipChannels > 0)
					FONT_RENDERER.drawString("^", CHANNELS_SCROLL_HINT_UP_X, CHANNELS_SCROLL_HINT_UP_Y, HIGHLIGHTED_TEXT_COLOUR)

				if(channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS < channelListMatchingChannels)
					FONT_RENDERER.drawString("v", CHANNELS_SCROLL_HINT_DOWN_X, CHANNELS_SCROLL_HINT_DOWN_Y, HIGHLIGHTED_TEXT_COLOUR)

				channelListSearchBar.drawTextBox()
			}
			CurrentDisplay.CHANNEL_RENAME -> {
				channelRenameTextField.drawTextBox()
			}
			else -> error(currentDisplay.debugName)
		}

		@Suppress("KotlinConstantConditions")
		if(Constants.DEBUG) {
			FONT_RENDERER.drawString(currentDisplay.debugName, 50, -10, RED_TEXT_COLOUR)
			FONT_RENDERER.drawString(tile.channelId.toString(), -300, (ySize shr 1) - FONT_HEIGHT, RED_TEXT_COLOUR)
			FONT_RENDERER.drawSplitString(tile.channelData.toString(), -300, ySize shr 1, 300, RED_TEXT_COLOUR)
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
					SideConfiguration.NONE
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

		AbstractButtonWrapper.getWrapper<LinkButtonWrapper>(button)?.let {
			if(currentDisplay == CurrentDisplay.CHANNEL_LINK) {
				val text = channelListSearchBar.text.trim()
				// this will be processed properly on server-side anyways
				if(!text.isEmpty())
					it.newChannelNameOverride = text
			}
		}

		// send packet to server-side
		super.actionPerformed(button)

		AbstractButtonWrapper.getWrapper<LinkButtonWrapper>(button)?.let {
			it.newChannelNameOverride = ""
			if(it.channelId == Constants.NO_CHANNEL)
				switchDisplay(CurrentDisplay.CHANNEL_LINK)
			else {
				// update client-side tile to hopefully display the correct data faster (also needed for unlinkButton visiblity)
				tile.channelId = it.channelId
				tile.channelData = channelListManager.channels.find { ch -> ch.id == it.channelId }?.toFakeChannelData()
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
				if(channelListSkipChannels + CHANNEL_LINK_MAX_DRAWN_CHANNELS < channelListMatchingChannels)
					++channelListSkipChannels
			}
		}
	}

	override fun keyTyped(typedChar: Char, keyCode: Int) {
		when(currentDisplay) {
			CurrentDisplay.CHANNEL_RENAME -> {
				if(keyCode == 1)
					return mc.player.closeScreen()

				if(keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
					channelRenameTextField.textboxKeyTyped(typedChar, keyCode)
					return
				}
				val text = channelRenameTextField.text.trim()
				if(text.isEmpty())
					return

				tile.channelData?.let { it.name = text } // not needed but whatever
				renameButton.newName = text
				actionPerformed(renameButton.button!!)
				switchDisplay(CurrentDisplay.MAIN_OVERVIEW)
			}
			CurrentDisplay.CHANNEL_LINK -> {
				if(keyCode == 1)
					return mc.player.closeScreen()

				channelListSearchBar.textboxKeyTyped(typedChar, keyCode)
			}
			else -> super.keyTyped(typedChar, keyCode)
		}
	}

	fun switchDisplay(new: CurrentDisplay) {
		// MAIN_OVERVIEW
		if(currentDisplay == CurrentDisplay.MAIN_OVERVIEW) {
			displayData.remove(displayWrapper)
			buttonList.remove(unlinkButton.button)
			buttonList.remove(deleteButton.button)
			buttonList.removeAll(sideConfigurationButtons.map(SideConfigurationButtonWrapper::button))
		}

		if(new == CurrentDisplay.MAIN_OVERVIEW) {
			displayWrapper?.let { displayData.add(it) }
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

		// CHANNEL_LINK
		Keyboard.enableRepeatEvents(new == CurrentDisplay.CHANNEL_LINK || new == CurrentDisplay.CHANNEL_RENAME)
		channelListSearchBar.isFocused = new == CurrentDisplay.CHANNEL_LINK
		channelListSearchBar.visible = new == CurrentDisplay.CHANNEL_LINK

		if(new == CurrentDisplay.CHANNEL_LINK) {
			linkChannelButtons.mapTo(buttonList, LinkButtonWrapper::button)
			channelListSearchBar.text = ""
		}

		if(currentDisplay == CurrentDisplay.CHANNEL_LINK) {
			channelListSkipChannels = 0
			buttonList.removeAll(linkChannelButtons.map(LinkButtonWrapper::button))
		}

		// CHANNEL_RENAME
		channelRenameTextField.isFocused = new == CurrentDisplay.CHANNEL_RENAME
		channelRenameTextField.visible = new == CurrentDisplay.CHANNEL_RENAME

		if(new == CurrentDisplay.CHANNEL_RENAME)
			channelRenameTextField.text = tile.channelData?.name ?: "???"

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
		const val TRANSLATION_BASE = "gui.${Tags.MOD_ID}:cbt."

		val FONT_RENDERER: FontRenderer = Minecraft.getMinecraft().fontRenderer
		val FONT_HEIGHT = FONT_RENDERER.FONT_HEIGHT

		// --- MAIN_OVERVIEW ---
		const val NAME_TEXT_X = BASE_TEXT_X + 20
		const val NAME_TEXT_Y = BASE_TEXT_Y

		const val OWNER_TEXT_X = NAME_TEXT_X
		val OWNER_TEXT_Y = NAME_TEXT_Y + FONT_HEIGHT + 1

		const val CONTENTS_TEXT_X = NAME_TEXT_X
		val CONTENTS_TEXT_Y = OWNER_TEXT_Y + FONT_HEIGHT + 1

		const val BAR_X = 8
		const val BAR_Y = 8
		const val BAR_W = 16
		const val BAR_H = 70

		const val BAR_BACKGROUND_X = BAR_X - 1
		const val BAR_BACKGROUND_Y = BAR_Y - 1
		const val BAR_BACKGROUND_W = BAR_W + 2
		const val BAR_BACKGROUND_H = BAR_H + 2
		const val BAR_BACKGROUND_U = 175
		const val BAR_BACKGROUND_V = 0

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

		const val CHANNEL_SEARCH_X = CHANNELS_TEXT_X
		const val CHANNEL_SEARCH_Y = CHANNELS_SCROLL_HINT_DOWN_Y + LINK_BTN_H
		const val CHANNEL_SEARCH_MAX_LENGTH = 25
		val CHANNEL_SEARCH_W = FONT_RENDERER.getStringWidth("W".repeat(CHANNEL_SEARCH_MAX_LENGTH))
		val CHANNEL_SEARCH_H = FONT_HEIGHT
	}
}
