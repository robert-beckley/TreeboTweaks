package me.robertbeckley.treebotweaks;

import me.robertbeckley.treebotweaks.Commands.NameIt;
import me.robertbeckley.treebotweaks.Commands.Reload;
import me.robertbeckley.treebotweaks.Commands.Version;
import me.robertbeckley.treebotweaks.UpdateChecker.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;


public final class TreeboTweaks extends JavaPlugin {

    private NameIt nameIt = new NameIt(this);
    private Reload reload = new Reload(this);
    private Version version = new Version(this);


    @Override
    public void onEnable() {

        // Plugin startup logic
        getConfig().options().copyDefaults(true);
        getConfig().set("version", this.getDescription().getVersion());
        saveConfig();


        /*Set Command Executors*/

        /*
            this.getCommand("nameit").setExecutor(new NameIt(this));
            this.getCommand("treebotweaksreload").setExecutor(new Reload(this));
            this.getCommand("treebotweaksversion").setExecutor(new Version(this));
         */

        nameIt.register("nameit");
        reload.register("treebotweaksreload");
        version.register("treebotweaksversion");

        UpdateChecker uc = new UpdateChecker(this);
        uc.getCheckDownloadURL();

    }

    @Override
    public void onDisable() {

    }

    public String badge = ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.messages.badge") + " ");
    public String err = badge + ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.messages.error") + " ");

    public void makeLog(Exception tr) {
        System.out.println("Creating new log folder - " + new File(this.getDataFolder() + File.separator + "logs").mkdir());
        String dateTimeString = LocalDateTime.now().toString().replace(":", "_").replace("T", "__");
        File file = new File(this.getDataFolder() + File.separator + "logs" + File.separator + dateTimeString + "-" + tr.getCause() + ".log");
        try {
            PrintStream ps = new PrintStream(file);
            tr.printStackTrace(ps);
            System.out.println(this.getDescription().getName() + " - " + this.getDescription().getVersion() + "Encountered Error of type: " + tr.getCause());
            System.out.println("A log file has been generated at " + this.getDataFolder() + File.separator + "logs" + File.separator + dateTimeString + "-" + tr.getCause() + ".log");
            ps.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error creating new log file for " + getDescription().getName() + " - " + getDescription().getVersion());
            System.out.println("Error was as follows");
            System.out.println(e.getMessage());
        }
    }

    public void registerNewCommand(String fallback, BukkitCommand command) {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(fallback, command);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public void createDefaultFile(String path, String file, boolean isFolder) {

        if (isFolder) {
            File folderToRegister = new File(path, file);
            if (!folderToRegister.exists()) {
                folderToRegister.mkdir();
            }
        } else {
            File fileToRegister = new File(path, file);
            //System.out.println("Registering file " + path + File.separator + file);
            if (!fileToRegister.exists()) {
                //System.out.println("File does not exist. Creating new file.");
                saveResource(path + file, false);
            }
        }
    }

    public YamlConfiguration getYaml(String path, String file) {
        file = File.separator + file;
        File theYml = new File(path, file);

        if (!theYml.exists()) {
            try {
                theYml.createNewFile();
            } catch (IOException e) {
                makeLog(e);
            }
        }
        return YamlConfiguration.loadConfiguration(theYml);
    }


    public void saveFile(File file, FileConfiguration conf, CommandSender s) {
        try {
            conf.save(file);
        } catch (IOException e) {
            makeLog(e);
            s.sendMessage("Saving " + file + " failed.");
        }
    }

    private static Object getPrivateField(Object object, String field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public static void unRegisterBukkitCommand(PluginCommand cmd) {
        try {
            Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(cmd.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
