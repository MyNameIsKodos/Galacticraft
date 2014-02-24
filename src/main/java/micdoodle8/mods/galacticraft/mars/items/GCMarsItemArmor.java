package micdoodle8.mods.galacticraft.mars.items;

import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxy;
import micdoodle8.mods.galacticraft.mars.GalacticraftMars;
import micdoodle8.mods.galacticraft.mars.items.GCMarsItems.EnumArmorIndexMars;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GCMarsItemArmor.java
 * 
 * This file is part of the Galacticraft project
 * 
 * @author micdoodle8
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
public class GCMarsItemArmor extends ItemArmor
{
	public GCMarsItemArmor(EnumArmorIndexMars type, int par4)
	{
		super(type.getMaterial(), type.getRenderIndex(), par4);
	}

	@Override
	public Item setUnlocalizedName(String par1Str)
	{
		super.setTextureName(par1Str);
		super.setUnlocalizedName(par1Str);
		return this;
	}

	@Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		if (this.getArmorMaterial() == EnumArmorIndexMars.DESH.getMaterial())
		{
			if (stack.getItem() == GCMarsItems.deshHelmet)
			{
				return "textures/model/armor/desh_1.png";
			}
			else if (stack.getItem() == GCMarsItems.deshChestplate || stack.getItem() == GCMarsItems.deshBoots)
			{
				return "textures/model/armor/desh_2.png";
			}
			else if (stack.getItem() == GCMarsItems.deshLeggings)
			{
				return "textures/model/armor/desh_3.png";
			}
		}

		return null;
	}

	@Override
	public CreativeTabs getCreativeTab()
	{
		return GalacticraftMars.galacticraftMarsTab;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return ClientProxy.galacticraftItem;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(this.getUnlocalizedName().replace("item.", "galacticraftmars:"));
	}
}