package wiebbe.farmtricity.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FarmtricityCreativeTab extends CreativeTabs
{
	public FarmtricityCreativeTab()
	{
		super("tabFarmtricity");
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(Item.wheat);
	}
}
