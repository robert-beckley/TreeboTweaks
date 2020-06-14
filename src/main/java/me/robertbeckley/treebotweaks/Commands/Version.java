package me.robertbeckley.treebotweaks.Commands;

import me.robertbeckley.treebotweaks.TreeboTweaks;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class Version {

    private TreeboTweaks pl;

    public Version(TreeboTweaks main) {
        this.pl = main;
    }

    public boolean register(String command) {
        if (!pl.getConfig().getBoolean("disabledCommands." + command)) {
            BukkitCommand item2 = new BukkitCommand(command.toLowerCase()) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    this.setDescription("Reads the Treebo Tweaks version from file");
                    this.setUsage("/treebotweaksreload - requires treebotweaks.admin");
                    this.setPermission("treebotweaks.admin");
                    if (sender.hasPermission(this.getPermission())) {

                        sender.sendMessage(pl.badge + "Version - " + pl.getDescription().getVersion());
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have access to this command. You require permission node " + ChatColor.GOLD + this.getPermission());
                    }
                    return true;
                }
            };
            pl.registerNewCommand(pl.getDescription().getName(), item2);
        }
        return true;
    }
}