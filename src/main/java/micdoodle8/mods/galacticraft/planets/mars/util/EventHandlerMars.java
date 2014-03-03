package micdoodle8.mods.galacticraft.planets.mars.util;

import micdoodle8.mods.galacticraft.api.event.wgen.EventWorldPopulate;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.render.entities.RenderPlayerGC.RotatePlayerEvent;
import micdoodle8.mods.galacticraft.core.entities.player.GCEntityClientPlayerMP;
import micdoodle8.mods.galacticraft.core.entities.player.GCEntityPlayerMP;
import micdoodle8.mods.galacticraft.core.event.EventLandingPadRemoval;
import micdoodle8.mods.galacticraft.core.event.EventWakePlayer;
import micdoodle8.mods.galacticraft.core.event.GCEvents.OrientCameraEvent;
import micdoodle8.mods.galacticraft.planets.mars.blocks.BlockMachineMars;
import micdoodle8.mods.galacticraft.planets.mars.blocks.MarsBlocks;
import micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntitySlimeling;
import micdoodle8.mods.galacticraft.planets.mars.network.PacketSimpleMars;
import micdoodle8.mods.galacticraft.planets.mars.network.PacketSimpleMars.EnumSimplePacketMars;
import micdoodle8.mods.galacticraft.planets.mars.tile.TileEntityCryogenicChamber;
import micdoodle8.mods.galacticraft.planets.mars.tile.TileEntityLaunchController;
import micdoodle8.mods.galacticraft.planets.mars.world.gen.WorldGenSlimelingEggs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GCMarsEvents.java
 * 
 * This file is part of the Galacticraft project
 * 
 * @author micdoodle8
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
public class EventHandlerMars
{
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if (event.source.damageType.equals("slimeling") && event.source instanceof EntityDamageSource)
		{
			EntityDamageSource source = (EntityDamageSource) event.source;

			if (source.getEntity() instanceof EntitySlimeling && !source.getEntity().worldObj.isRemote)
			{
				((EntitySlimeling) source.getEntity()).kills++;
			}
		}
	}

	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if (!event.entity.isEntityInvulnerable() && !event.entity.worldObj.isRemote && event.entityLiving.getHealth() <= 0.0F && !(event.source.isFireDamage() && event.entityLiving.isPotionActive(Potion.fireResistance)))
		{
			Entity entity = event.source.getEntity();

			if (entity instanceof EntitySlimeling)
			{
				EntitySlimeling entitywolf = (EntitySlimeling) entity;

				if (entitywolf.isTamed())
				{
					event.entityLiving.recentlyHit = 100;
					event.entityLiving.attackingPlayer = null;
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerWakeUp(EventWakePlayer event)
	{
		ChunkCoordinates c = event.entityPlayer.playerLocation;
		Block blockID = event.entityPlayer.worldObj.getBlock(c.posX, c.posY, c.posZ);
		int metadata = event.entityPlayer.worldObj.getBlockMetadata(c.posX, c.posY, c.posZ);

		if (blockID == MarsBlocks.machine && metadata >= BlockMachineMars.CRYOGENIC_CHAMBER_METADATA)
		{
			if (event.flag1 == false && event.flag2 == true && event.flag3 == true)
			{
				event.result = EnumStatus.NOT_POSSIBLE_HERE;

				if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && event.bypassed && event.entityPlayer instanceof GCEntityClientPlayerMP)
				{
					GalacticraftCore.packetPipeline.sendToServer(new PacketSimpleMars(EnumSimplePacketMars.S_WAKE_PLAYER, new Object[] {}));
				}
			}
			else if (event.flag1 == false && event.flag2 == false && event.flag3 == true)
			{
				if (!event.entityPlayer.worldObj.isRemote)
				{
					event.entityPlayer.heal(5.0F);
					((GCEntityPlayerMP) event.entityPlayer).setCryogenicChamberCooldown(6000);

					for (WorldServer worldServer : MinecraftServer.getServer().worldServers)
					{
						worldServer.setWorldTime(0);
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerRotate(RotatePlayerEvent event)
	{
		ChunkCoordinates c = event.entityPlayer.playerLocation;
		Block block = event.entityPlayer.worldObj.getBlock(c.posX, c.posY, c.posZ);
		int metadata = event.entityPlayer.worldObj.getBlockMetadata(c.posX, c.posY, c.posZ);

		if (block == MarsBlocks.machine && metadata >= BlockMachineMars.CRYOGENIC_CHAMBER_METADATA)
		{
			event.shouldRotate = false;
		}
	}

	private WorldGenerator eggGenerator;

	@SubscribeEvent
	public void onPlanetDecorated(EventWorldPopulate.Post event)
	{
		if (this.eggGenerator == null)
		{
			this.eggGenerator = new WorldGenSlimelingEggs(MarsBlocks.rock);
		}

		if (event.worldObj.provider instanceof WorldProviderMars)
		{
			int eggsPerChunk = 2;
			int x;
			int y;
			int z;

			for (int eggCount = 0; eggCount < eggsPerChunk; ++eggCount)
			{
				x = event.chunkX + event.rand.nextInt(16) + 8;
				y = event.rand.nextInt(128);
				z = event.chunkZ + event.rand.nextInt(16) + 8;
				this.eggGenerator.generate(event.worldObj, event.rand, x, y, z);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void orientCamera(OrientCameraEvent event)
	{
		EntityPlayer entity = Minecraft.getMinecraft().thePlayer;

		if (entity != null)
		{
			int x = MathHelper.floor_double(entity.posX);
			int y = MathHelper.floor_double(entity.posY);
			int z = MathHelper.floor_double(entity.posZ);
			TileEntity tile = Minecraft.getMinecraft().theWorld.getTileEntity(x, y - 2, z);

			if (tile instanceof TileEntityCryogenicChamber)
			{
//				GL11.glRotatef(-var12 * 90, 0.0F, 1.0F, 0.0F);

				float rotation = 0.0F;
				
				//((TileEntityCryogenicChamber) tile).ticks % 360
				
				FMLLog.info("gg " + entity.getBedOrientationInDegrees());

				switch (tile.getBlockMetadata() - BlockMachineMars.CRYOGENIC_CHAMBER_METADATA)
				{
				case 0:
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(50, 1.0F, 0.0F, 0.0F);
					GL11.glTranslatef(-0.4F, -0.5F, 1.1F);
					GL11.glRotatef(270, 0.0F, 1.0F, 0.0F);
					break;
				case 1:
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(50, 1.0F, 0.0F, 0.0F);
					GL11.glTranslatef(0, -0.5F, 1.1F);
					break;
				case 2:
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(50, 1.0F, 0.0F, 0.0F);
					GL11.glTranslatef(0, -0.5F, 1.1F);
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					break;
				case 3:
					GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(50, 1.0F, 0.0F, 0.0F);
					GL11.glTranslatef(0.0F, -0.5F, 0.9F);
//					GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public void onLandingPadRemoved(EventLandingPadRemoval event)
	{
		TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);

		if (tile instanceof IFuelDock)
		{
			IFuelDock dock = (IFuelDock) tile;

			TileEntityLaunchController launchController = null;

			for (ILandingPadAttachable connectedTile : dock.getConnectedTiles())
			{
				if (connectedTile instanceof TileEntityLaunchController)
				{
					launchController = (TileEntityLaunchController) event.world.getTileEntity(((TileEntityLaunchController) connectedTile).xCoord, ((TileEntityLaunchController) connectedTile).yCoord, ((TileEntityLaunchController) connectedTile).zCoord);
					break;
				}
			}

			if (launchController != null)
			{
				if (!launchController.getDisabled(0) && launchController.getEnergyStored() > 0.0F)
				{
					event.allow = !launchController.launchPadRemovalDisabled;
				}
			}
		}
	}
}
