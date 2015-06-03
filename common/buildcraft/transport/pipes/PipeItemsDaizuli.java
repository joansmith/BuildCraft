/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.ISerializable;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.statements.ActionPipeColor;
import buildcraft.transport.statements.ActionPipeDirection;

public class PipeItemsDaizuli extends Pipe<PipeTransportItems> implements ISerializable {

	private int standardIconIndex = PipeIconProvider.TYPE.PipeItemsDaizuli_Black.ordinal();
	private int solidIconIndex = PipeIconProvider.TYPE.PipeAllDaizuli_Solid.ordinal();
	private int color = EnumColor.BLACK.ordinal();
	private PipeLogicIron logic = new PipeLogicIron(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof IPipeTile) {
				Pipe<?> otherPipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
				if (otherPipe instanceof PipeItemsWood) {
					return false;
				}
				if (otherPipe.transport instanceof PipeTransportItems) {
					return true;
				}
				return false;
			}
			if (tile instanceof IInventory) {
				return true;
			}
			return false;
		}
	};

	public PipeItemsDaizuli(Item item) {
		super(new PipeTransportItems(), item);

		transport.allowBouncing = true;
	}

	public EnumColor getColor() {
		return EnumColor.fromId(color);
	}

	public void setColor(EnumColor c) {
		if (color != c.ordinal()) {
			this.color = c.ordinal();
			container.scheduleRenderUpdate();
		}
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		if (player.isSneaking()) {
			Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
				setColor(getColor().getNext());
				((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
				return true;
			}
		}

		int newColor = ColorUtils.getColorIDFromDye(player.getCurrentEquippedItem());
		if (newColor >= 0 && newColor < 16) {
			setColor(EnumColor.fromId(15 - newColor));
			return true;
		}

		return logic.blockActivated(player);
	}

	@Override
	public void onBlockPlaced() {
		logic.onBlockPlaced();
		super.onBlockPlaced();
	}

	@Override
	public void initialize() {
		logic.initialize();
		super.initialize();
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		if (direction == EnumFacing.UNKNOWN) {
			return standardIconIndex + color;
		}
		if (container != null && container.getBlockMetadata() == direction.ordinal()) {
			return standardIconIndex + color;
		}
		return solidIconIndex;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public boolean canConnectRedstone() {
		return true;
	}

	public void eventHandler(PipeEventItem.FindDest event) {
		EnumFacing output = EnumFacing.getOrientation(container.getBlockMetadata());
		if (event.item.color == getColor() && event.destinations.contains(output)) {
			event.destinations.clear();
			event.destinations.add(output);
			return;
		}
		event.destinations.remove(output);
	}

	public void eventHandler(PipeEventItem.AdjustSpeed event) {
		event.handled = true;
		TravelingItem item = event.item;

		if (item.getSpeed() > TransportConstants.PIPE_NORMAL_SPEED) {
			item.setSpeed(item.getSpeed() - TransportConstants.PIPE_NORMAL_SPEED / 4.0F);
		}

		if (item.getSpeed() < TransportConstants.PIPE_NORMAL_SPEED) {
			item.setSpeed(TransportConstants.PIPE_NORMAL_SPEED);
		}
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		super.actionsActivated(actions);

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionPipeColor) {
				setColor(((ActionPipeColor) action.statement).color);
				break;
			}
		}

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionPipeDirection) {
				logic.setFacing(((ActionPipeDirection) action.statement).direction);
				break;
			}
		}
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> action = super.getActions();
		action.addAll(Arrays.asList(BuildCraftTransport.actionPipeColor));
		for (EnumFacing direction : EnumFacing.VALID_DIRECTIONS) {
			if (container.isPipeConnected(direction)) {
				action.add(BuildCraftTransport.actionPipeDirection[direction.ordinal()]);
			}
		}
		return action;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setByte("color", (byte) color);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		color = data.getByte("color");
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(color);
	}

	@Override
	public void readData(ByteBuf data) {
		color = data.readByte();
	}
}
