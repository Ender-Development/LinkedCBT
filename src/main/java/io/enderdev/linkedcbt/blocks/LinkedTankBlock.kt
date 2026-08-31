package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedTank
import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.tiles.TileLinkedTank

class LinkedTankBlock : BaseLinkedBlock("tank", LinkedCBT.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java })
