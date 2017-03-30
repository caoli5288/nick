package com.mengcraft.nick;

import lombok.val;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

import static com.mengcraft.nick.$.nil;

/**
 * Created on 16-8-19.
 */
public class VaultP {

    static Chat chat;

    static String get(Player p) {
        if (!nil(chat)) {
            val prefix = chat.getPlayerPrefix(p);
            if (nil(prefix) || prefix.isEmpty()) {
                val glo = chat.getPlayerPrefix(null, p);
                return nil(glo) ? "" : glo;
            }
            return prefix;
        }
        return "";
    }

    static void bind(Chat chat) {
        $.valid($.nil(chat), "null");
        VaultP.chat = chat;
    }

}
