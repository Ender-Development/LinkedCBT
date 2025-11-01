package io.enderdev.linkedcbt.blocks

import io.enderdev.linkedcbt.LinkedCBT
import io.enderdev.linkedcbt.client.container.ContainerLinkedTank
import io.enderdev.linkedcbt.client.gui.GuiLinkedTank
import io.enderdev.linkedcbt.tiles.TileLinkedTank
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.ender_development.catalyx.blocks.BaseRotatableMachineBlock

class LinkedTankBlock : BaseLinkedBlock("tank", LinkedCBT.guiHandler.registerId(TileLinkedTank::class.java, ContainerLinkedTank::class.java) { GuiLinkedTank::class.java })
