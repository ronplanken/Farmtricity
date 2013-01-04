package wiebbe.farmtricity.client;

import wiebbe.farmtricity.client.gui.GuiLandtiller;
import wiebbe.farmtricity.common.CommonProxy;
import wiebbe.farmtricity.common.Farmtricity;
import wiebbe.farmtricity.common.TileEntityLandtiller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		// Preload textures
		MinecraftForgeClient.preloadTexture(Farmtricity.BLOCK_TEXTURE_FILE);
		MinecraftForgeClient.preloadTexture(Farmtricity.ITEM_TEXTURE_FILE);
	}

	@Override
	public void init()
	{
		super.init();
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			switch (ID)
			{
				case 0:
					return new GuiLandtiller(player.inventory, ((TileEntityLandtiller) tileEntity));
			}
		}

		return null;
	}
}