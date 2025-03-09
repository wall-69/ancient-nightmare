package io.github.wall69.ancientnightmare.managers;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.utils.FileType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager {

	private final Main main;

	private final List<File> files;
	private final List<FileConfiguration> fileConfigurations;

	public FileManager(Main main) {
		this.main = main;
		this.files = new ArrayList<>();
		this.fileConfigurations = new ArrayList<>();

		setup();
	}

	/*
	 * SETUP METHOD
	 */

	private void setup() {
		if (!main.getDataFolder().exists()) {
			main.getDataFolder().mkdir();
		}

		createFile(FileType.CONFIG);
		createFile(FileType.ARENAS);
		createFile(FileType.LANGUAGE);
		createFile(FileType.PLAYER_STATS);
	}

	/*
	 * BASE METHODS
	 */

	private void createFile(FileType type) {
		File file = new File(main.getDataFolder(), type.getName() + ".yml");

		if (!file.exists()) {
			main.saveResource(type.getName() + ".yml", false);
		}

		FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

		files.add(file);
		fileConfigurations.add(fileConfiguration);

		save(type);
	}

	public File getFile(FileType type) {
		if (files.get(type.getID()) != null) {
			return files.get(type.getID());
		}

		Bukkit.getLogger().log(Level.SEVERE,
				main.consolePrefix + "Invalid file name in getFile method: " + type.getName());
		return null;
	}

	public FileConfiguration getConfig(FileType type) {
		int index = files.indexOf(getFile(type));

		if (fileConfigurations.get(index) != null) {
			return fileConfigurations.get(index);
		}

		Bukkit.getLogger().log(Level.SEVERE,
				main.consolePrefix + "Invalid file config name in getConfig method: " + type.getName());
		return null;
	}

	public void save(FileType type) {
		try {
			getConfig(type).save(getFile(type));
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE,
					main.consolePrefix + "Invalid file config name in save method: " + type.getName());
		}
	}

	public void reload(FileType type) {
		int index = files.indexOf(getFile(type));

		fileConfigurations.set(index, YamlConfiguration.loadConfiguration(getFile(type)));
	}

}
