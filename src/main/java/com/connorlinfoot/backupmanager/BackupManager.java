package com.connorlinfoot.backupmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BackupManager extends JavaPlugin implements Listener {
    private static BackupManager instance;
    public static boolean updateAvailable = false;
    public static String updateMessage = "";
    public static boolean advancedLogs = false;
    public static String pluginPrefix = ChatColor.GRAY + "[" + ChatColor.AQUA + "BackupManager" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public void onEnable() {
        instance = this;
        Server server = getServer();
        final ConsoleCommandSender console = server.getConsoleSender();
        getConfig().options().copyDefaults(true);
        saveConfig();

        advancedLogs = getConfig().getBoolean("Advanced Logs");

        // Backup all worlds
        for (World world : Bukkit.getWorlds()) {
            ZIPManager zipManager = new ZIPManager("/backups/test.zip", world.getName());
            zipManager.doZip();
            console.sendMessage(ChatColor.GREEN + "Backed up " + world.getName());
        }

//        if (!getConfig().getBoolean("Update Checks")) {
//            getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
//                public void run() {
//                    checkUpdate(console,
//                            getConfig().getString("Update Branch"),
//                            getConfig().getBoolean("Auto Update"));
//                }
//            }, 10L);
//        }

        console.sendMessage("");
        console.sendMessage(ChatColor.BLUE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        console.sendMessage("");
        console.sendMessage(ChatColor.AQUA + getDescription().getName());
        console.sendMessage(ChatColor.AQUA + "Version " + getDescription().getVersion());
        console.sendMessage("");
        console.sendMessage(ChatColor.BLUE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        console.sendMessage("");
    }

    public static BackupManager getPlugin() {
        return instance;
    }

    private void checkUpdate(final ConsoleCommandSender console, final String branch, final boolean install) {
        if (branch.equalsIgnoreCase("nightly")) {
            console.sendMessage(ChatColor.GREEN + "Oh, nightly builds? You're brave...");

        } else if (branch.equalsIgnoreCase("spigot")) {
            console.sendMessage(ChatColor.GREEN + "Checking for updates via Spigot...");
            final Updater updater = new Updater(this, 5018, false);
            final Updater.UpdateResult result = updater.getResult();
            switch (result) {
                default:
                    break;
                case BAD_RESOURCEID:
                case FAIL_NOVERSION:
                case FAIL_SPIGOT:
                    updateAvailable = false;
                    updateMessage = pluginPrefix + "Failed to check for updates. Will try again later.";
                    getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                        public void run() {
                            checkUpdate(console,
                                    branch,
                                    install);
                        }
                    }, 60 * (60 * 20L)); // Checks again an hour later
                    break;
                case NO_UPDATE:
                    updateAvailable = false;
                    updateMessage = pluginPrefix + "No update was found, you are running the latest version. Will check again later.";
                    getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                        public void run() {
                            checkUpdate(console,
                                    branch,
                                    install);
                        }
                    }, 60 * (60 * 20L)); // Checks again an hour later
                    break;
                case DISABLED:
                    updateAvailable = false;
                    updateMessage = pluginPrefix + "You currently have update checks disabled";
                    break;
                case UPDATE_AVAILABLE:
                    updateAvailable = true;
                    updateMessage = pluginPrefix + "An update for CratesPlus is available, new version is " + updater.getVersion() + ". Your installed version is " + getDescription().getVersion() + ".\nPlease update to the latest version :)";
                    break;
                case MAJOR_UPDATE_AVALIABLE:
                    updateAvailable = true;
                    updateMessage = pluginPrefix + "A major update for CratesPlus is available, new version is " + updater.getVersion() + ". Your installed version is " + getDescription().getVersion() + ".\nPlease update to the latest version :)";
                    break;
            }
            console.sendMessage(updateMessage);
        }
    }
}
