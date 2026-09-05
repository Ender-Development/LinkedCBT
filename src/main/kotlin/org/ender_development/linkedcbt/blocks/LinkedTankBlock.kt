package org.ender_development.linkedcbt.blocks

import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.client.container.ContainerLinkedTank
import org.ender_development.linkedcbt.client.gui.GuiLinkedTank
import org.ender_development.linkedcbt.tiles.TileLinkedTank

class LinkedTankBlock : BaseLinkedBlock("tank", LinkedCBT.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java })
