package me.xa5.xrankup;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class XRankup extends JavaPlugin {
    private static XRankup instance;
    private Economy econ = null;
    private Permission perms = null;

    private List<Rankup> rankups;
    private List<String> rankupActions;
    private String finalRank;
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "Qd");
        suffixes.put(1_000_000_000_000_000_000L, "Qn");
    }

    @Override
    public void onEnable() {
        instance = this;
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();

        ConfigHandler configHandler = new ConfigHandler(instance);
        rankups = configHandler.getRankups();
        getCommand("rankup").setExecutor(new RankupExecutor(this));
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static XRankup getInstance() {
        return instance;
    }

    public Economy getEcon() {
        return econ;
    }

    public Permission getPerms() {
        return perms;
    }

    public Rankup getRankup(String[] group) {
        for (int i = 0; i < group.length; i++)
            for (Rankup rankup : rankups)
                if (rankup.getOldRank().equalsIgnoreCase(group[i])) return rankup;
        return null;
    }

    public void setFinalRank(String finalRank) {
        this.finalRank = finalRank;
    }

    public void setRankupActions(List<String> actions) {
        this.rankupActions = actions;
    }

    public List<String> getRankupActions() {
        return rankupActions;
    }

    public String getFinalRank() {
        return finalRank;
    }

    public void doRankup(Player player, Rankup rankup) {
        try {
            econ.withdrawPlayer(player, rankup.getCost());
            rankupActions.forEach(actionStr -> doAction(actionStr, player, rankup));
            rankup.getActions().forEach(actionStr -> doAction(actionStr, player, rankup));
        } catch (Exception e) {
            getLogger().warning("Caught exception when trying to do an action:");
            e.printStackTrace();
        }
    }

    private void doAction(String actionStr, Player player, Rankup rankup) {
        String[] split = actionStr.split(" ", 2);
        String oldActionStr = actionStr;
        actionStr = split[0];
        if (!isAction(actionStr)) {
            String command = format(oldActionStr, player, rankup);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            System.out.println("DISPATCHED COMMAND :" + command);
            return;
        }

        String part2 = split[1];
        Action action = Action.fromString(actionStr.substring(1, actionStr.length() - 1));
        if (action == null) {
            getLogger().warning("Invalid action " + actionStr + "!");
            return;
        }

        switch (action) {
            case ADD_GROUP:
                perms.playerAddGroup("global", player, rankup.getNextRank());
                return;
            case REMOVE_GROUP:
                perms.playerRemoveGroup("global", player, rankup.getOldRank());
                return;
            case BROADCAST:
                Bukkit.broadcastMessage(format(ChatColor.translateAlternateColorCodes('&', part2), player, rankup));
                return;
            case MESSAGE:
                sendMessage(player, format(part2, player, rankup));
                return;
        }
    }

    private boolean isAction(String actionStr) {
        if (actionStr.startsWith("[") && actionStr.endsWith("]")) return true;
        if (count(actionStr, '[') == 1 && count(actionStr, ']') == 1) return true;
        return false;
    }

    private long count(String actionStr, char s) {
        return actionStr.chars().filter(ch -> ch == s).count();
    }

    public void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public String format(String text, Player player, Rankup rankup) {
        return text
                .replace("{player}", player.getName())
                .replace("{cost}", formatCost(rankup.getCost()))
                .replace("{rank}", rankup.getNextRank())
                .replace("{oldrank}", rankup.getOldRank());
    }

    public static String formatCost(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatCost(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatCost(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public boolean isFinalRank(String[] groups) {
        for (int i = 0; i < groups.length; i++)
            if (groups[i].equalsIgnoreCase(finalRank)) return true;
        return false;
    }
}