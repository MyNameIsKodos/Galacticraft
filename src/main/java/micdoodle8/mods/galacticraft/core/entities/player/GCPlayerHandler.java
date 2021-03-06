package micdoodle8.mods.galacticraft.core.entities.player;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRaceManager;
import micdoodle8.mods.galacticraft.core.entities.EntityParachest;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class GCPlayerHandler
{
	private ConcurrentHashMap<UUID, GCPlayerStats> playerStatsMap = new ConcurrentHashMap<UUID, GCPlayerStats>();
	
	public ConcurrentHashMap<UUID, GCPlayerStats> getServerStatList()
	{
		return playerStatsMap;
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
    	if (event.player instanceof GCEntityPlayerMP)
    	{
    		onPlayerLogin((GCEntityPlayerMP) event.player);
    	}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event)
	{
    	if (event.player instanceof GCEntityPlayerMP)
    	{
    		onPlayerLogout((GCEntityPlayerMP) event.player);
    	}
	}

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
    	if (event.player instanceof GCEntityPlayerMP)
    	{
            onPlayerRespawn((GCEntityPlayerMP) event.player);
    	}
    }

    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof GCEntityPlayerMP && GCPlayerStats.get((GCEntityPlayerMP) event.entity) == null)
        {
            GCPlayerStats.register((GCEntityPlayerMP) event.entity);
        }
    }
	
	private void onPlayerLogin(GCEntityPlayerMP player)
	{
		GCPlayerStats oldData = playerStatsMap.remove(player.getPersistentID());
		if (oldData != null)
		{
			oldData.saveNBTData(player.getEntityData());
		}
		
		GCPlayerStats stats = GCPlayerStats.get(player);
	}
	
	private void onPlayerLogout(GCEntityPlayerMP player)
	{
		
	}
	
	private void onPlayerRespawn(GCEntityPlayerMP player)
	{
		GCPlayerStats oldData = playerStatsMap.remove(player.getPersistentID());
		GCPlayerStats stats = GCPlayerStats.get(player);
		
		if (oldData != null)
		{
			stats.copyFrom(oldData, false);
		}
		
		stats.player = new WeakReference<GCEntityPlayerMP>(player);
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if (event.entityLiving instanceof GCEntityPlayerMP)
		{
			this.onPlayerUpdate((GCEntityPlayerMP) event.entityLiving);
		}
	}
	
	private void onPlayerUpdate(GCEntityPlayerMP player)
	{
		int tick = player.ticksExisted - 1;

		if (tick == 10)
		{
			if (SpaceRaceManager.getSpaceRaceFromPlayer(player.getGameProfile().getName()) == null)
			{
				GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_OPEN_SPACE_RACE_GUI, new Object[] { }), player);
			}
		}

		//This will speed things up a little
		final GCPlayerStats GCPlayer = player.getPlayerStats();
		
		if (GCPlayer.cryogenicChamberCooldown > 0)
		{
			GCPlayer.cryogenicChamberCooldown--;
		}

		if (!player.onGround && GCPlayer.lastOnGround)
		{
			GCPlayer.touchedGround = true;
		}

		if (GCPlayer.teleportCooldown > 0)
		{
			GCPlayer.teleportCooldown--;
		}

		if (GCPlayer.chatCooldown > 0)
		{
			GCPlayer.chatCooldown--;
		}

		if (GCPlayer.openPlanetSelectionGuiCooldown > 0)
		{
			GCPlayer.openPlanetSelectionGuiCooldown--;

			if (GCPlayer.openPlanetSelectionGuiCooldown == 1 && !GCPlayer.hasOpenedPlanetSelectionGui)
			{
				player.sendPlanetList();
				GCPlayer.usingPlanetSelectionGui = true;
				GCPlayer.hasOpenedPlanetSelectionGui = true;
			}
		}

		if (GCPlayer.usingParachute)
		{
			if (GCPlayer.lastParachuteInSlot != null) player.fallDistance = 0.0F;
			if (player.onGround)
			{
				player.setUsingParachute(false);
			}
		}

		player.checkCurrentItem();

		if (GCPlayer.usingPlanetSelectionGui)
		{
			player.sendPlanetList();
		}

/*		if (player.worldObj.provider instanceof IGalacticraftWorldProvider || player.usingPlanetSelectionGui)
		{
			player.playerNetServerHandler.ticksForFloatKick = 0;
		}	
*/		
		if (GCPlayer.damageCounter > 0)
		{
			GCPlayer.damageCounter--;
		}

		if (tick % 30 == 0 && player.worldObj.provider instanceof IGalacticraftWorldProvider)
		{
			player.sendAirRemainingPacket();
			player.sendThermalLevelPacket();
		}

		player.checkGear();

		if (GCPlayer.chestSpawnCooldown > 0)
		{
			GCPlayer.chestSpawnCooldown--;

			if (GCPlayer.chestSpawnCooldown == 180)
			{
				if (GCPlayer.chestSpawnVector != null)
				{
					EntityParachest chest = new EntityParachest(player.worldObj, GCPlayer.rocketStacks, GCPlayer.fuelLevel);

					chest.setPosition(GCPlayer.chestSpawnVector.x, GCPlayer.chestSpawnVector.y, GCPlayer.chestSpawnVector.z);

					if (!player.worldObj.isRemote)
					{
						player.worldObj.spawnEntityInWorld(chest);
					}
				}
			}
		}

		//

		if (GCPlayer.launchAttempts > 0 && player.ridingEntity == null)
		{
			GCPlayer.launchAttempts = 0;
		}

		player.checkThermalStatus();
		player.checkOxygen();

		if (player.worldObj.provider instanceof IGalacticraftWorldProvider && (GCPlayer.oxygenSetupValid != GCPlayer.lastOxygenSetupValid || tick % 100 == 0))
		{
			GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_UPDATE_OXYGEN_VALIDITY, new Object[] { GCPlayer.oxygenSetupValid }), player);
		}

		player.throwMeteors();

		player.updateSchematics();

		if (tick % 250 == 0 && GCPlayer.frequencyModuleInSlot == null && !GCPlayer.receivedSoundWarning && player.worldObj.provider instanceof IGalacticraftWorldProvider && player.onGround && tick > 0)
		{
			player.addChatMessage(new ChatComponentText(EnumColor.YELLOW + "I'll probably need a " + EnumColor.AQUA + GCItems.basicItem.getItemStackDisplayName(new ItemStack(GCItems.basicItem, 1, 19)) + EnumColor.YELLOW + " if I want to hear properly here."));
			GCPlayer.receivedSoundWarning = true;
		}

		GCPlayer.lastOxygenSetupValid = GCPlayer.oxygenSetupValid;
		GCPlayer.lastUnlockedSchematics = GCPlayer.unlockedSchematics;

		GCPlayer.lastOnGround = player.onGround;
	}
}
