package wiebbe.farmtricity.client.gui;

import wiebbe.farmtricity.common.Farmtricity;
import wiebbe.farmtricity.common.TileEntityLandtiller;
import wiebbe.farmtricity.common.container.ContainerLandtiller;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLandtiller extends GuiContainer
{
	private TileEntityLandtiller tileEntity;

	private int containerWidth;
	private int containerHeight;

	public GuiLandtiller(InventoryPlayer par1InventoryPlayer, TileEntityLandtiller tileEntity)
	{
		super(new ContainerLandtiller(par1InventoryPlayer, tileEntity));
		this.tileEntity = tileEntity;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), 60, 6, 4210752);
		this.fontRenderer.drawString("Tilling:", 10, 28, 4210752);
		this.fontRenderer.drawString("Battery:", 10, 53, 4210752);
		String displayText = "";

		if (this.tileEntity.isDisabled())
		{
			displayText = "Disabled!";
		}
		else if (this.tileEntity.tillingTimeTicks > 0)
		{
			displayText = "Tilling";
		}
		else
		{
			displayText = "Idle";
		}

		this.fontRenderer.drawString("Status: " + displayText, 82, 45, 4210752);
		this.fontRenderer.drawString("Voltage: " + ElectricInfo.getDisplayShort(this.tileEntity.getVoltage(), ElectricUnit.VOLTAGE), 82, 56, 4210752);
		this.fontRenderer.drawString("Require: " + ElectricInfo.getDisplayShort(this.tileEntity.WATTS_PER_TILLING * 20, ElectricUnit.WATT), 82, 68, 4210752);
		this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		int var4 = this.mc.renderEngine.getTexture(Farmtricity.FILE_PATH + "Landtiller.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(var4);
		containerWidth = (this.width - this.xSize) / 2;
		containerHeight = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);

		if (this.tileEntity.tillingTimeTicks > 0)
		{
			int scale = (int) (((double) this.tileEntity.tillingTimeTicks / (double) this.tileEntity.TILLING_TIME_REQUIRED) * 23);
			this.drawTexturedModalRect(containerWidth + 77, containerHeight + 24, 176, 0, 23 - scale, 20);
		}
	}
}