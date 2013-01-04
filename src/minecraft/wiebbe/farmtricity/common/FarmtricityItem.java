package wiebbe.farmtricity.common;

import net.minecraft.item.Item;

public class FarmtricityItem extends Item 
{

	public FarmtricityItem(int par1) 
	{
		super(par1);
		// TODO Auto-generated constructor stub
		setCreativeTab(Farmtricity.tabFarmtricity);
	}

	@Override
	public String getTextureFile() 
	{
		return "/resources/farmtricity/textures/items.png";
	}
	
}

