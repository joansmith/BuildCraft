/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.recipes;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * The Integration Table's primary purpose is to modify an input item's NBT
 * data. As such its not a "traditional" type of recipe. Rather than predefined
 * inputs and outputs, it takes an input and transforms it.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IIntegrationRecipeManager {

	public static interface IIntegrationRecipe {

		double getEnergyCost();

		boolean isValidInput(ItemStack stack);

		ItemStack getOutputForInput(ItemStack stack);

		ItemStack[] getExampleInputs();
	}

	/**
	 * Add an Integration Table recipe.
	 *
	 */
	void addRecipe(IIntegrationRecipe recipe);

	List<? extends IIntegrationRecipe> getRecipes();
}
