/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import buildcraft.core.GuiIds;
import buildcraft.factory.gui.*;

public class FactoryGuiHandler implements IGuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        BlockPos pos = new BlockPos(x, y, z);

        if (world.isAirBlock(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);

        switch (id) {

            case GuiIds.AUTO_CRAFTING_TABLE:
                if (!(tile instanceof TileAutoWorkbench)) {
                    return null;
                } else {
                    return new GuiAutoCrafting(player, world, (TileAutoWorkbench) tile);
                }

            case GuiIds.REFINERY:
                if (!(tile instanceof TileRefinery)) {
                    return null;
                } else {
                    return new GuiRefinery(player, (TileRefinery) tile);
                }

            case GuiIds.HOPPER:
                if (!(tile instanceof TileChute)) {
                    return null;
                } else {
                    return new GuiChute(player, (TileChute) tile);
                }

            default:
                return null;
        }
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        BlockPos pos = new BlockPos(x, y, z);

        if (world.isAirBlock(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);

        switch (id) {

            case GuiIds.AUTO_CRAFTING_TABLE:
                if (!(tile instanceof TileAutoWorkbench)) {
                    return null;
                } else {
                    return new ContainerAutoWorkbench(player, (TileAutoWorkbench) tile);
                }

            case GuiIds.REFINERY:
                if (!(tile instanceof TileRefinery)) {
                    return null;
                } else {
                    return new ContainerRefinery(player, (TileRefinery) tile);
                }

            case GuiIds.HOPPER:
                if (!(tile instanceof TileChute)) {
                    return null;
                } else {
                    return new ContainerChute(player, (TileChute) tile);
                }

            default:
                return null;
        }
    }

}
