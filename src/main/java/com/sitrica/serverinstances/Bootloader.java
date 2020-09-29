package com.sitrica.serverinstances;

import java.io.File;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.sitrica.japson.client.JapsonClient;
import com.sitrica.japson.gson.JsonObject;
import com.sitrica.japson.shared.Packet;

public class Bootloader extends JavaPlugin {

	private FileConfiguration configuration;
	private static Bootloader instance;
	private JapsonClient japson;
	private File DATA_FOLDER;

	@Override
	public void onEnable() {
		instance = this;
		DATA_FOLDER = new File(getDataFolder().getParentFile(), "ServerInstances");
		if (!DATA_FOLDER.exists())
			return;
		configuration = YamlConfiguration.loadConfiguration(new File(DATA_FOLDER, "config.yml"));
		try {
			japson = new JapsonClient(configuration.getString("bootloader.address-bind", "127.0.0.1"), configuration.getInt("bootloader.port", 6110))
					.setPassword(configuration.getString("bootloader.password", "serverinstances"))
					.makeSureConnectionValid();
			if (configuration.getBoolean("bootloader.debug"))
				japson.enableDebug();
			japson.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			japson.sendPacket(new Packet(0x01) {
				@Override
				public JsonObject toJson() {
					JsonObject object = new JsonObject();
					object.addProperty("port", Bukkit.getPort());
					object.addProperty("address", Bukkit.getIp());
					return object;
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	public static Bootloader getInstance() {
		return instance;
	}

}
