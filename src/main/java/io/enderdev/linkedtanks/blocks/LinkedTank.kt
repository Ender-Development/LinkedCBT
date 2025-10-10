package io.enderdev.linkedtanks.blocks

import io.enderdev.linkedtanks.LinkedTanks
import io.enderdev.linkedtanks.client.container.ContainerLinkedTank
import io.enderdev.linkedtanks.client.gui.GuiLinkedTank
import io.enderdev.linkedtanks.tiles.TileLinkedTank
import org.ender_development.catalyx.blocks.BaseMachineBlock

class LinkedTank : BaseMachineBlock(LinkedTanks.modSettings, "linked_tank", TileLinkedTank::class.java, LinkedTanks.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java }) {
}
