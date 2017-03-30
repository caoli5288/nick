package com.mengcraft.nick;

import org.bukkit.entity.Player;

/**
 * Created on 16-10-20.
 */
public interface NickManager {

    Nick get(Player p);

    void persist(Nick nick);

    void set(Player p, Nick nick);

}
