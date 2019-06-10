package org.inventivetalent.elytratarget;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ElytraTarget extends JavaPlugin implements Listener {

	double T_LIME_MIN = -0.3;
	double T_LIME_MAX = 0.3;

	double T_GREEN_MIN = -1;
	double T_GREEN_MAX = 1;

	double T_YELLOW_MIN = -5;
	double T_YELLOW_MAX = 5;

	double T_ORANGE_MIN = -10;
	double T_ORANGE_MAX = 10;

	double D_START = 8;
	double D_TOTAL = 32;

	double BEAM_FREQUENCY = 2;

	boolean TITLE_ENABLED = true;
	boolean BEAM_ENABLED  = true;

	String T_LEVEL = "--     --";
	String T_UP    = "^      ^";
	String T_DOWN  = "v       v";

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		reload();
	}

	void reload() {
		reloadConfig();

		T_LIME_MIN = getConfig().getDouble("threshold.color.lime.min");
		T_LIME_MAX = getConfig().getDouble("threshold.color.lime.max");

		T_GREEN_MIN = getConfig().getDouble("threshold.color.green.min");
		T_GREEN_MAX = getConfig().getDouble("threshold.color.green.max");

		T_YELLOW_MIN = getConfig().getDouble("threshold.color.yellow.min");
		T_YELLOW_MAX = getConfig().getDouble("threshold.color.yellow.max");

		T_ORANGE_MIN = getConfig().getDouble("threshold.color.orange.min");
		T_ORANGE_MAX = getConfig().getDouble("threshold.color.orange.max");

		D_START = getConfig().getDouble("beam.distance.start");
		D_TOTAL = getConfig().getDouble("beam.distance.total");

		TITLE_ENABLED = getConfig().getBoolean("titles.enabled");
		BEAM_ENABLED = getConfig().getBoolean("beam.enabled");

		BEAM_FREQUENCY = getConfig().getDouble("beam.frequency");

		T_LEVEL = getConfig().getString("titles.direction.level");
		T_UP = getConfig().getString("titles.direction.up");
		T_DOWN = getConfig().getString("titles.direction.down");

		if (BEAM_ENABLED) {
			if (!Bukkit.getPluginManager().isPluginEnabled("ParticleLIB") && !classExists("org.inventivetalent.particle.ParticleEffect")) {
				BEAM_ENABLED = false;
				getLogger().warning("*** Please download ParticleLIB to enable the Beam: https://r.spiget.org/2067");
			}
		}
		if (TITLE_ENABLED) {
			if (!Bukkit.getPluginManager().isPluginEnabled("TitleAPI") && !classExists("org.inventivetalent.title.TitleAPI")) {
				TITLE_ENABLED = false;
				getLogger().warning("*** Please download TitleAPI to enable Titles: https://r.spiget.org/1047");
			}
		}
	}

	boolean classExists(String clazz) {
		try {
			Class.forName(clazz);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() != Material.ELYTRA) {
			return;
		}
		boolean elytra = player.isGliding();
		if (elytra && !player.isOnGround() && !player.isFlying()) {
			if (player.getLocation().getBlock().getType() != Material.AIR) {
				return;//In water
			}
			displayTarget(player);
		}
	}

	void displayTarget(Player player) {
		if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() != Material.ELYTRA) { return; }
		Location location = player.getLocation().clone();
		location.add(0, 0.4f, 0);//Eye heigth (from MCP)

		Color color = Color.RED;
		ChatColor chatColor = ChatColor.RED;

		float pitch = location.getPitch();
		if (pitch <= T_ORANGE_MAX && pitch >= T_ORANGE_MIN) {
			color = Color.ORANGE;
			chatColor = ChatColor.GOLD;
		}
		if (pitch <= T_YELLOW_MAX && pitch >= T_YELLOW_MIN) {
			color = Color.YELLOW;
			chatColor = ChatColor.YELLOW;
		}
		if (pitch <= T_GREEN_MAX && pitch >= T_GREEN_MIN) {
			color = Color.GREEN;
			chatColor = ChatColor.DARK_GREEN;
		}
		if (pitch <= T_LIME_MAX && pitch >= T_LIME_MIN) {
			color = Color.LIME;
			chatColor = ChatColor.GREEN;
		}

		if (BEAM_ENABLED && player.hasPermission("elytratarget.beam")) {
			Vector velocity = player.getVelocity();
			for (double d = D_START; d < D_TOTAL; d += BEAM_FREQUENCY) {
				Location location1 = velocity.clone().multiply(d).add(player.getLocation().toVector()).toLocation(player.getLocation().getWorld());
				if (location1.getBlock().getType() != Material.AIR) { continue; }
				player.spawnParticle(Particle.REDSTONE, location1, 0, new Particle.DustOptions(color, 1));
			}
		}
		if (TITLE_ENABLED && player.hasPermission("elytratarget.title")) {
			String direction;
			if (pitch <= T_LIME_MAX && pitch >= T_LIME_MIN) {
				direction = T_LEVEL;
			} else {
				if (pitch > 0) {
					direction = T_UP;
				} else {
					direction = T_DOWN;
				}
			}
			player.sendTitle(chatColor + direction, "", 0, 10, 5);
		}
	}

}