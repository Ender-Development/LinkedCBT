package org.ender_development.linkedcbt.blocks

import org.ender_development.linkedcbt.LinkedCBT
import org.ender_development.linkedcbt.client.container.ContainerLinkedChest
import org.ender_development.linkedcbt.client.gui.GuiLinkedChest
import org.ender_development.linkedcbt.tiles.TileLinkedChest

class LinkedChestBlock : BaseLinkedBlock("chest", LinkedCBT.guiHandler.registerId(TileLinkedChest::class.java, ContainerLinkedChest::class.java) { GuiLinkedChest::class.java })
