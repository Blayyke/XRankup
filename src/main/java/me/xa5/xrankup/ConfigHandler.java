package me.xa5.xrankup;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class ConfigHandler {
    private final File file;
    private final XRankup plugin;

    public ConfigHandler(XRankup plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = plugin.getConfig();

        try {
            plugin.saveDefaultConfig();
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().warning("Failed to load config, disabling. Please report this to the developer.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        plugin.setFinalRank(config.getString("final-rank"));
        plugin.setRankupActions(config.getStringList("rankup-actions"));

        ConfigurationSection messages = config.getConfigurationSection("messages");
        Texts.NO_MONEY = messages.getString("cannot-afford");
        Texts.NOT_PLAYER = messages.getString("not-a-player");
        Texts.FINAL_RANK = messages.getString("final-rank");
        Texts.NO_RANKUP_FOUND = messages.getString("no-rankup-found");
    }

    public ArrayList<Rankup> getRankups() {
        ArrayList<Rankup> rankups = new ArrayList<>();

        ConfigurationSection rankupSection = plugin.getConfig().getConfigurationSection("rankups");
        Set<String> rankupKeys = rankupSection.getKeys(false);
        rankupKeys.forEach(key -> {
            ConfigurationSection rankup = rankupSection.getConfigurationSection(key);
            rankups.add(new Rankup(rankup.getLong("cost"), rankup.getName(), rankup.getString("next"), rankup.getStringList("actions")));
        });
        return rankups;
    }
}