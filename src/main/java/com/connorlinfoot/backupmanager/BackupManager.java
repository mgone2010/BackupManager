package com.connorlinfoot.backupmanager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BackupManager extends JavaPlugin implements Listener {
    private static BackupManager instance;
    public static boolean advancedLogs = false;
    public static String location = "backups/";
    public static boolean ftpBackupEnabled = false;
    public static String pluginPrefix = ChatColor.GRAY + "[" + ChatColor.AQUA + "BackupManager" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public void onEnable() {
        instance = this;
        final ConsoleCommandSender console = getServer().getConsoleSender();
        getConfig().options().copyDefaults(true);
        saveConfig();

        advancedLogs = getConfig().getBoolean("Advanced Logs");
        location = getConfig().getString("Backup Location") + "/";
        ftpBackupEnabled = getConfig().getBoolean("FTP Backup.Enabled");

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

        if (getConfig().getBoolean("Backup On.Every Day")) {
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    backupWorlds(console);
                }
            }, (24 * (60 * 60)) * 20L, (24 * (60 * 60)) * 20L);
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
            new File(location).mkdirs();

            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String currentTime = sdf.format(dt);
            String name = location + world.getName() + "_" + currentTime + ".zip";
            ZIPManager zipManager = new ZIPManager(name, "logs");
            zipManager.doZip();
            if (as) {
                world.setAutoSave(true);
            }
            console.sendMessage(ChatColor.GREEN + "Backed up " + world.getName());
            if (ftpBackupEnabled) {
                uploadFileToFTP(new File(name), console);
            }
        }
        console.sendMessage(ChatColor.GREEN + "World backups completed");
    }

    private void uploadFileToFTP(File file, ConsoleCommandSender console) {
        console.sendMessage(ChatColor.GREEN + "Starting FTP upload of " + file.getName() + "... ");
        String host = getConfig().getString("FTP Backup.FTP Host");
        int port = getConfig().getInt("FTP Backup.FTP Port");
        String user = getConfig().getString("FTP Backup.FTP User");
        String pass = getConfig().getString("FTP Backup.FTP Pass");
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String firstRemoteFile = file.getName();
            InputStream inputStream = new FileInputStream(file);

            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                console.sendMessage(ChatColor.GREEN + file.getName() + " uploaded successfully.");
            } else {
                console.sendMessage(ChatColor.RED + file.getName() + " uploaded failed.");
            }

        } catch (IOException ex) {
            console.sendMessage(ChatColor.RED + "Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static BackupManager getPlugin() {
        return instance;
    }

}
