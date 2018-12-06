package me.xa5.xrankup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankupExecutor implements CommandExecutor {
    private final XRankup plugin;

    public RankupExecutor(XRankup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, Texts.NOT_PLAYER);
            return true;
        }

        Player player = (Player) sender;
        String[] group = plugin.getPerms().getPlayerGroups(player);

        if (plugin.isFinalRank(group)) {
            plugin.sendMessage(player, Texts.FINAL_RANK);
            return true;
        }

        Rankup rankup = plugin.getRankup(group);
        if (rankup == null) {
            plugin.sendMessage(player, Texts.NO_RANKUP_FOUND);
            return true;
        }

        if (!plugin.getEcon().has(player, rankup.getCost())) {
            plugin.sendMessage(player, plugin.format(Texts.NO_MONEY, player, rankup));
            return true;
        }

        plugin.doRankup(player, rankup);
        return true;
    }
}