package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedChest
import io.enderdev.linkedcbt.client.gui.GuiLinkedChest
import io.enderdev.linkedcbt.tiles.TileLinkedChest
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedChestBlock : BaseLinkedBlock("chest", LinkedCBT.guiHandler.registerId(TileLinkedChest::class.java, ContainerLinkedChest::class.java) { GuiLinkedChest::class.java })
