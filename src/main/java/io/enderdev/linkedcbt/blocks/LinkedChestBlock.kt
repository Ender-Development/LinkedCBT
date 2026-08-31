package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedChest
import io.enderdev.linkedcbt.client.gui.GuiLinkedChest
import io.enderdev.linkedcbt.tiles.TileLinkedChest

class LinkedChestBlock : BaseLinkedBlock("chest", LinkedCBT.guiHandler.registerId(TileLinkedChest::class.java, ContainerLinkedChest::class.java) { GuiLinkedChest::class.java })
