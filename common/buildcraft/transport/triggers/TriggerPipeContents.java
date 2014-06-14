/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.PipePowerWood;


public class TriggerPipeContents extends BCTrigger {

	public enum PipeContents {

		empty,
		containsItems,
		containsFluids,
		containsEnergy,
		requestsEnergy,
		tooMuchEnergy;
		public BCTrigger trigger;
	};
	private PipeContents kind;
	private IIcon icon;

	public TriggerPipeContents(PipeContents kind) {
		super("buildcraft:pipe.contents." + kind.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.contents." + kind.name());
		this.kind = kind;
		kind.trigger = this;
	}

	@Override
	public int maxParameters() {
		switch (kind) {
		case containsItems:
		case containsFluids:
			return 1;
		default:
			return 0;
		}
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.pipe." + kind.name());
	}

	@Override
	public boolean isTriggerActive(IPipe pipe, ITriggerParameter[] parameters) {
		ITriggerParameter parameter = parameters[0];

		if (((Pipe) pipe).transport instanceof PipeTransportItems) {
			PipeTransportItems transportItems = (PipeTransportItems) ((Pipe) pipe).transport;

			if (kind == PipeContents.empty) {
				return transportItems.items.isEmpty();
			} else if (kind == PipeContents.containsItems) {
				if (parameter != null && parameter.getItemStackToDraw() != null) {
					for (TravelingItem item : transportItems.items) {
						if (item.getItemStack().getItem() == parameter.getItemStackToDraw().getItem()
								&& item.getItemStack().getItemDamage() == parameter.getItemStackToDraw()
										.getItemDamage()) {
							return true;
						}
					}
				} else {
					return !transportItems.items.isEmpty();
				}
			}
		} else if (((Pipe) pipe).transport instanceof PipeTransportFluids) {
			PipeTransportFluids transportFluids = (PipeTransportFluids) ((Pipe) pipe).transport;

			FluidStack searchedFluid = null;

			if (parameter != null && parameter.getItemStackToDraw() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItemStackToDraw());
			}

			if (kind == PipeContents.empty) {
				for (FluidTankInfo b : transportFluids.getTankInfo(ForgeDirection.UNKNOWN)) {
					if (b.fluid != null && b.fluid.amount != 0) {
						return false;
					}
				}

				return true;
			} else {
				for (FluidTankInfo b : transportFluids.getTankInfo(ForgeDirection.UNKNOWN)) {
					if (b.fluid != null && b.fluid.amount != 0) {
						if (searchedFluid == null || searchedFluid.isFluidEqual(b.fluid)) {
							return true;
						}
					}
				}

				return false;
			}
		} else if (((Pipe) pipe).transport instanceof PipeTransportPower) {
			PipeTransportPower transportPower = (PipeTransportPower) ((Pipe) pipe).transport;

			switch (kind) {
				case empty:
					for (double s : transportPower.displayPower) {
						if (s > 1e-4) {
							return false;
						}
					}

					return true;
				case containsEnergy:
					for (double s : transportPower.displayPower) {
						if (s > 1e-4) {
							return true;
						}
					}

					return false;
				case requestsEnergy:
					PipePowerWood wood = (PipePowerWood) pipe;
					return wood.requestsPower();
				default:
				case tooMuchEnergy:
					return transportPower.isOverloaded();
			}
		}

		return false;
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_" + kind.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
