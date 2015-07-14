package com.connorlinfoot.backupmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Date;

public class BackupManager extends JavaPlugin implements Listener {
    private static BackupManager instance;
    public static boolean advancedLogs = false;
    public static String location = "backups/";
    public static String pluginPrefix = ChatColor.GRAY + "[" + ChatColor.AQUA + "BackupManager" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public void onEnable() {
        instance = this;
        final ConsoleCommandSender console = getServer().getConsoleSender();
        getConfig().options().copyDefaults(true);
        saveConfig();

        advancedLogs = getConfig().getBoolean("Advanced Logs");
        location = getConfig().getString("Backup Location") + "/";

        console.sendMessage("");
        console.sendMessage(ChatColor.BLUE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        console.sendMessage("");
        console.sendMessage(ChatColor.AQUA + getDescription().getName());
        console.sendMessage(ChatColor.AQUA + "Version " + getDescription().getVersion());
        console.sendMessage("");
        console.sendMessage(ChatColor.BLUE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        console.sendMessage("");

        if (getConfig().getBoolean("Backup On.Server Start")) {
            backupWorlds(console);
        }

        if (getConfig().getBoolean("Backup On.Every 15 Minutes")) {
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    backupWorlds(console);
                }
            }, (20 * 60) * 15l, (20 * 60) * 15l);
        }
    }

    private void backupWorlds(ConsoleCommandSender console) {
        console.sendMessage(ChatColor.GREEN + "Starting world backups...");
        for (World world : Bukkit.getWorlds()) {
            world.save();
            boolean as = false;
            if (world.isAutoSave()) {
                as = true;
                world.setAutoSave(false);
            }
            Date date = new Date(System.currentTimeMillis());
            new File(location).mkdirs();
            ZIPManager zipManager = new ZIPManager(location + world.getName() + "_" + date.toString().replaceAll(" ", "_") + "_" + date.getTime() + ".zip", "logs");
            zipManager.doZip();
            if (as) {
                world.setAutoSave(true);
            }
            console.sendMessage(ChatColor.GREEN + "Backed up " + world.getName());
        }
        console.sendMessage(ChatColor.GREEN + "World backups completed");
    }

    public static BackupManager getPlugin() {
        return instance;
    }

}
