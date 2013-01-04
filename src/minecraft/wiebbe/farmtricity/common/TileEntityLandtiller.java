package wiebbe.farmtricity.common;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.electricity.ElectricityNetwork;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityElectricityReceiver;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class TileEntityLandtiller extends TileEntityElectricityReceiver implements IInventory, ISidedInventory, IPacketReceiver
{
	// The amount of watts required by the landtiller per tick
	public static final double WATTS_PER_TILLING = 500;

	// The amount of ticks required to smelt this item
	public static final int TILLING_TIME_REQUIRED = 30;

	// How many ticks has this item been smelting for?
	public int tillingTimeTicks = 0;

	public double joulesReceived = 0;
	
	public Integer xMinLocation = -3;
	public Integer zMinLocation = -3;

	public Integer xMaxLocation = 3;
	public Integer zMaxLocation = 3;

	public Integer xCurLocation = -4;
	public Integer zCurLocation = -3;
	
	private boolean xForward = true;
	private boolean zForward = true;

	
	
	/**
	 * The ItemStacks that hold the items currently being used in the battery box
	 */
	private ItemStack[] containingItems = new ItemStack[3];

	private int playersUsing = 0;

	@Override
	public void initiate()
	{
		ElectricityConnections.registerConnector(this, EnumSet.of(ForgeDirection.getOrientation(this.getBlockMetadata() - BlockBasicMachine.LANDTILLER_METADATA + 2)));
		this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, Farmtricity.blockMachine.blockID);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{

			ForgeDirection inputDirection = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockBasicMachine.LANDTILLER_METADATA + 2);
			TileEntity inputTile = Vector3.getTileEntityFromSide(this.worldObj, new Vector3(this), inputDirection);

			ElectricityNetwork inputNetwork = ElectricityNetwork.getNetworkFromTileEntity(inputTile, inputDirection);

			if (inputNetwork != null)
			{
				if (this.canTill())
				{
					inputNetwork.startRequesting(this, WATTS_PER_TILLING / this.getVoltage(), this.getVoltage());
					ElectricityPack electricityPack = inputNetwork.consumeElectricity(this);
					this.joulesReceived = Math.max(Math.min(this.joulesReceived + electricityPack.getWatts(), WATTS_PER_TILLING), 0);

					if (UniversalElectricity.isVoltageSensitive)
					{
						if (electricityPack.voltage > this.getVoltage())
						{
							this.worldObj.createExplosion(null, this.xCoord, this.yCoord, this.zCoord, 2f, true);
						}
					}
				}
				else
				{
					inputNetwork.stopRequesting(this);
				}
			}
		}

		// The bottom slot is for portable
		// batteries
		if (this.containingItems[0] != null)
		{
			if (this.containingItems[0].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) this.containingItems[0].getItem();

				if (electricItem.canProduceElectricity())
				{
					double receivedWattHours = electricItem.onUse(Math.min(electricItem.getMaxJoules(this.containingItems[0]) * 0.01, ElectricInfo.getWattHours(WATTS_PER_TILLING)), this.containingItems[0]);
					this.joulesReceived += ElectricInfo.getWatts(receivedWattHours);
				}
			}
		}

		
		
		if (this.joulesReceived >= this.WATTS_PER_TILLING - 50 && !this.isDisabled())
		{
			
			if (this.canTill() && this.tillingTimeTicks == 0)
			{
				this.tillingTimeTicks = this.TILLING_TIME_REQUIRED;
			}

			if (this.canTill() && this.tillingTimeTicks > 0)
			{
				this.tillingTimeTicks--;

				if (this.tillingTimeTicks < 1)
				{
					this.joulesReceived -= this.WATTS_PER_TILLING;
					
					Block tilledField = Block.tilledField;
					Block cropsField = Block.crops;
					
					int blockY = yCoord - 1;
					int blockX = xCoord;
					int blockZ = zCoord;
					
					if(xCurLocation < xMaxLocation && xForward)
					{
						xCurLocation++;
					}
					else if(xCurLocation > xMinLocation && !xForward)
					{
						xCurLocation--;
					}
					else
					{
						if(zCurLocation < zMaxLocation && zForward)
						{
							zCurLocation++;
							
							xForward = !xForward;
						}
						else if(zCurLocation > zMinLocation && !zForward)
						{
							zCurLocation--;
							
							xForward = !xForward;
						}						
						else
						{
							zForward = !zForward;
						}
						
					}

					while((zCurLocation <= 1 && zCurLocation >= -1) && (xCurLocation <= 1 && xCurLocation >= -1))
					{
						if(xCurLocation < xMaxLocation && xForward)
						{
							xCurLocation++;
						}
						else if(xCurLocation > xMinLocation && !xForward)
						{
							xCurLocation--;
						}
						else
						{
							zCurLocation++;
							xForward = !xForward;
						}
					}
					
					blockX += zCurLocation;
					blockZ += xCurLocation;
					
					if((worldObj.getBlockId(blockX, blockY, blockZ) == Block.dirt.blockID || worldObj.getBlockId(blockX, blockY, blockZ) == Block.grass.blockID) && worldObj.isAirBlock(blockX, blockY + 1, blockZ))
					{

		                if (!worldObj.isRemote)
		                {
		                	worldObj.setBlock(blockX, blockY, blockZ, tilledField.blockID);
		                	damageHoe();
		                }
					}
					else if(worldObj.getBlockId(blockX, blockY, blockZ) == tilledField.blockID && worldObj.getBlockId(blockX, blockY + 1, blockZ) != cropsField.blockID && worldObj.isAirBlock(blockX, blockY + 1, blockZ))
					{
		                if (!worldObj.isRemote)
		                {
		                	worldObj.setBlock(blockX, blockY + 1, blockZ, cropsField.blockID);
		                }
					}
						
					this.tillingTimeTicks = 0;
				}
			}
			else
			{
				this.tillingTimeTicks = 0;
			}

		}
		 
		if (!this.worldObj.isRemote)
		{
			if (this.ticks % 3 == 0 && this.playersUsing > 0)
			{
				PacketManager.sendPacketToClients(getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(Farmtricity.CHANNEL, this, this.tillingTimeTicks, this.disabledTicks);
	}

	@Override
	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			this.tillingTimeTicks = dataStream.readInt();
			this.disabledTicks = dataStream.readInt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void openChest()
	{
		if (!this.worldObj.isRemote)
		{
			PacketManager.sendPacketToClients(getDescriptionPacket(), this.worldObj, new Vector3(this), 15);
		}
		this.playersUsing++;
	}

	@Override
	public void closeChest()
	{
		this.playersUsing--;
	}

	// Check all conditions and see if we can
	// start smelting
	public boolean canTill()
	{
		if (!(this.containingItems[1] == null)) 
		{ 
			if(this.containingItems[1].itemID == new ItemStack(Item.hoeWood).itemID)
			{
				return true;
			}
			else
			{
			return false;
			}
		}

		return false;
	}
	
	public void damageHoe()
	{
		if (!(this.containingItems[1] == null))
		{

			if(this.containingItems[1].itemID == new ItemStack(Item.hoeWood).itemID)
			{
				if(this.containingItems[1].getItemDamage() >= this.containingItems[1].getMaxDamage())
				{
					this.containingItems[1] = null;
				}
				else
				{
					this.containingItems[1].setItemDamage(this.containingItems[1].getItemDamage() + 1);
				}
			}
		}
	}
	

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		this.tillingTimeTicks = par1NBTTagCompound.getInteger("tillingTimeTicks");
		NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
		this.containingItems = new ItemStack[this.getSizeInventory()];

		for (int var3 = 0; var3 < var2.tagCount(); ++var3)
		{
			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(var3);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.containingItems.length)
			{
				this.containingItems[var5] = ItemStack.loadItemStackFromNBT(var4);
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("tillingTimeTicks", this.tillingTimeTicks);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < this.containingItems.length; ++var3)
		{
			if (this.containingItems[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
				this.containingItems[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}

		par1NBTTagCompound.setTag("Items", var2);
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		if (side == side.DOWN || side == side.UP) { return side.ordinal(); }

		return 2;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	@Override
	public int getSizeInventory()
	{
		return this.containingItems.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1)
	{
		return this.containingItems[par1];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.containingItems[par1] != null)
		{
			ItemStack var3;

			if (this.containingItems[par1].stackSize <= par2)
			{
				var3 = this.containingItems[par1];
				this.containingItems[par1] = null;
				return var3;
			}
			else
			{
				var3 = this.containingItems[par1].splitStack(par2);

				if (this.containingItems[par1].stackSize == 0)
				{
					this.containingItems[par1] = null;
				}

				return var3;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.containingItems[par1] != null)
		{
			ItemStack var2 = this.containingItems[par1];
			this.containingItems[par1] = null;
			return var2;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.containingItems[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public String getInvName()
	{
		return LanguageRegistry.instance().getStringLocalization("tile.bcMachine.2.name");
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public double getVoltage()
	{
		return 120;
	}
}