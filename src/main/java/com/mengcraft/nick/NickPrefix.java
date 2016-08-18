package com.mengcraft.nick;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created on 16-8-19.
 */
public class NickPrefix {

    public static final NickPrefix INSTANCE = new NickPrefix();

    private Chat chat;

    private NickPrefix() {
    }

    public String getPrefix(Player p) {
        if (chat != null) {
            return getChatPrefix(p);
        }
        return "";
    }

    private String getChatPrefix(Player p) {
        String prefix = chat.getPlayerPrefix(p);
        if (Main.eq(prefix, null) || prefix.isEmpty()) {
            prefix = chat.getPlayerPrefix(null, p);
        }
        return Main.eq(prefix, null) ? "" : prefix;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

}
