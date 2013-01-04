package wiebbe.farmtricity.common;

import wiebbe.farmtricity.common.container.ContainerLandtiller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy implements IGuiHandler
{
        public static String ITEMS_PNG = "/tutorial/generic/items.png";
        public static String BLOCK_PNG = "/tutorial/generic/block.png";
        
        // Client stuff
        public void registerRenderers() 
        {
                // Nothing here as the server doesn't render graphics!
        }
        
    	public void preInit()
    	{
    	}

    	public void init()
    	{
    		/**
    		 * Registering Tile Entities
    		 */
    		GameRegistry.registerTileEntity(TileEntityLandtiller.class, "FALantiller");
    	}
    	
    	@Override
    	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    	{
    		return null;
    	}

    	@Override
    	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    	{
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

    		if (tileEntity != null)
    		{
    			switch (ID)
    			{
    				case 0:
    					return new ContainerLandtiller(player.inventory, ((TileEntityLandtiller) tileEntity));
    			}
    		}

    		return null;
    	}
}