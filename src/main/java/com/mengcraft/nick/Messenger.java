package com.mengcraft.nick;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Created on 16-4-13.
 */
public class Messenger {

    private final static String PREFIX = "message.";
    private final Plugin main;

    public Messenger(Plugin main) {
        this.main = main;
    }

    public void send(CommandSender p, String path) {
        send(p, path, null);
    }

    public void send(CommandSender p, String path, String def) {
        sendMessage(p, find(path, def));
    }

    public void sendList(CommandSender p, String path, List<String> def) {
        for (String line : findList(path, def)) {
            sendMessage(p, line);
        }
    }

    public void sendList(CommandSender p, String path) {
        sendList(p, path, null);
    }

    public String find(String path) {
        return find(path, null);
    }

    public List<String> findList(String path) {
        return findList(path, null);
    }

    public List<String> findList(String path, List<String> def) {
        List<String> found = main.getConfig().getStringList(with(path));
        if (found.isEmpty()) {
            if (def != null && !def.isEmpty()) {
                main.getConfig().set(with(path), found = def);
                main.saveConfig();
            }
        }
        return found;
    }

    public String find(String path, String def) {
        String found = main.getConfig().getString(with(path));
        if (found == null) {
            if (def == null) {
                found = with(path);
            } else {
                main.getConfig().set(with(path), found = def);
                main.saveConfig();
            }
        }
        return found;
    }

    private void sendMessage(CommandSender p, String text) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    private String with(String str) {
        return PREFIX + str;
    }

}
