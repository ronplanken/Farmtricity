package wiebbe.farmtricity.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.prefab.network.ConnectionHandler;
import universalelectricity.prefab.network.PacketManager;

@Mod(modid=Farmtricity.CHANNEL, name="Farmtricity", version = Farmtricity.VERSION)
@NetworkMod(channels = Farmtricity.CHANNEL, clientSideRequired=true, serverSideRequired=false, connectionHandler = ConnectionHandler.class, packetHandler = PacketManager.class)
public class Farmtricity 
{
		public static final String FILE_PATH = "/resources/farmtricity/textures/";
		public static final String BLOCK_TEXTURE_FILE = FILE_PATH + "blocks.png";
		public static final String ITEM_TEXTURE_FILE = FILE_PATH + "items.png";

		public static FarmtricityCreativeTab tabFarmtricity = new FarmtricityCreativeTab();
		
		public static final String CHANNEL = "Farmtricity";
		
		public static Block blockMachine;
		
		public static ItemStack landtiller;
		
		public static final int MAJOR_VERSION = 0;
		public static final int MINOR_VERSION = 0;
		public static final int REVISION_VERSION = 1;
		public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;
		
        // The instance of your mod that Forge uses.
        @Instance(Farmtricity.CHANNEL)
        public static Farmtricity instance;
        
        // Says where the client and server 'proxy' code is loaded.
        @SidedProxy(clientSide="minecraft.wiebbe.farmtricity.client.ClientProxy", serverSide="minecraft.wiebbe.farmtricity.common.CommonProxy")
        public static CommonProxy proxy;
    	
        
        @PreInit
        public void preInit(FMLPreInitializationEvent event) 
        {
            UniversalElectricity.register(this, 1, 2, 2, true);
            
    		NetworkRegistry.instance().registerGuiHandler(this, this.proxy);
    		
    		proxy.preInit();
        }
        
        @Init
        public void load(FMLInitializationEvent event) 
        {
                proxy.registerRenderers();
                
                addNames();
                
                blockMachine = new BlockBasicMachine(3000,0).setBlockName("BlockBasicMachine");
                landtiller =  ((BlockBasicMachine) Farmtricity.blockMachine).getLandtiller();
      
                GameRegistry.registerBlock(Farmtricity.blockMachine, ItemBlockBasicMachine.class, "Basic Machine");
                
                ModLoader.registerTileEntity(TileEntityLandtiller.class, "Landtiller");
        }
        
        @PostInit
        public void postInit(FMLPostInitializationEvent event) 
        {
                // Stub Method
        }
        
    	public void addNames()
    	{
    		//Localization for Farmtricity creative tab
    		LanguageRegistry.instance().addStringLocalization("itemGroup.tabFarmtricity", "Farmtricity");
    	}

        
}