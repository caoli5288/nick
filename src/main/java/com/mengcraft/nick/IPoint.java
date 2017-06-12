package com.mengcraft.nick;

import org.bukkit.OfflinePlayer;

/**
 * Created on 17-4-13.
 */
public interface IPoint {

    boolean take(OfflinePlayer who, int value);

    void give(OfflinePlayer who, int value);
}
