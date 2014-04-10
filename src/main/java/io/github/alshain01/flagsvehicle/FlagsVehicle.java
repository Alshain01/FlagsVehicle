/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work

 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.

 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */
package io.github.alshain01.flagsvehicle;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Flags Vehicle - Module that adds vehicle flags to the plug-in Flags.
 */
@SuppressWarnings("unused")
public class FlagsVehicle extends JavaPlugin {
	/**
	 * Called when this module is enabled
	 */
	@Override
	public void onEnable() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();

		if (!pm.isPluginEnabled("Flags")) {
			getLogger().severe("Flags was not found. Shutting down.");
			pm.disablePlugin(this);
            return;
		}

		// Connect to the data file and register the flags
        YamlConfiguration flagConfig = YamlConfiguration.loadConfiguration(getResource("flags.yml"));
        Collection<Flag> flags = FlagsAPI.getRegistrar().registerFlag(flagConfig, "Vehicle");
        Map<String, Flag> flagMap = new HashMap<String, Flag>();
        for(Flag f : flags) {
            flagMap.put(f.getName(), f);
        }

		// Load plug-in events and data
		Bukkit.getServer().getPluginManager().registerEvents(new VehicleListener(flagMap), this);
	}
	
	/*
	 * The event handlers for the flags we created earlier
	 */
	private class VehicleListener implements Listener {
        final Map<String, Flag> flags;

        private VehicleListener(Map<String, Flag> flags) {
            this.flags = flags;
        }

		private boolean isDenied(Player player, Flag flag, Area area) {
			if (player.hasPermission(flag.getBypassPermission())) {
				return false;
			}

			if (area.hasTrust(flag, player)) {
				return false;
			}

			if (!area.getState(flag, false)) {
				player.sendMessage(area.getMessage(flag, player.getName()));
				return true;
			}
			return false;
		}

		/*
		 * Handler for Vehicle Creation
		 */
		@EventHandler(ignoreCancelled = true)
		private void onPlayerInteract(PlayerInteractEvent e) {
            if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) {
                return;
            }

            Flag flag;
            switch(e.getItem().getType()) {
                case BOAT:
                    flag = flags.get("PlaceBoat");
                    break;
                case MINECART:
                    flag = flags.get("PlaceMinecart");
                    break;
                default:
                    return;
            }

            if(flag != null) {
                e.setCancelled(isDenied(e.getPlayer(), flag, FlagsAPI.getAreaAt(e.getClickedBlock().getLocation())));
            }
		}

		/*
		 * Handler for Vehicle Damage
		 */
		@EventHandler(ignoreCancelled = true)
		private void onVehicleDamage(VehicleDamageEvent e) {
			if (e.getAttacker() instanceof Player) {
				return;
			}
			
			Location location = e.getVehicle().getLocation();
            Flag flag;
            switch(e.getVehicle().getType()) {
                case BOAT:
                    flag = flags.get("BoatDamage");
                    break;
                case MINECART:
                    flag = flags.get("MinecartDamage");
                    break;
                case PIG:
                    flag = flags.get("SaddledPigDamage");
                    break;
                case HORSE:
                    flag = flags.get("TamedHorseDamage");
                    break;
                default:
                    return;
            }

            if(flag != null) {
                e.setCancelled(!FlagsAPI.getAreaAt(location).getState(flag, false));
            }
        }
	}

	/*
	 * Handler for Saddling animals
	 * 
	 * @EventHandler(ignoreCancelled = true) private void
	 * onPlayerInteractEntity(PlayerInteractEntityEvent e) {
	 * if(e.getPlayer().getItemInHand().getType() != Material.SADDLE) {
	 * return; }
	 * 
	 * if(e.getRightClicked() instanceof Pig) {
	 * 
	 * e.setCancelled(isDenied(e.getPlayer(),
	 * Flags.getRegistrar().getFlag("SaddlePig"),
	 * Director.getAreaAt(e.getRightClicked().getLocation())));
	 * 
	 * } else if (Flags.checkAPI("1.6.2") && e.getRightClicked() instanceof
	 * org.bukkit.entity.Horse) {
	 * 
	 * e.setCancelled(isDenied(e.getPlayer(),
	 * Flags.getRegistrar().getFlag("SaddleHorse"),
	 * Director.getAreaAt(e.getRightClicked().getLocation())));
	 * 
	 * } }
	 */
}
