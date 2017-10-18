package com.mengcraft.nick;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.Future;

/**
 * Created on 16-10-20.
 */
public interface NickManager {

    /**
     * @param p     the player
     * @param fetch fetch flag
     * @return the nick cached or fetch db only if not cached and fetch flag to true
     */
    Nick get(OfflinePlayer p, boolean fetch);

    /**
     * @param p the player
     * @return the nick always fetch db
     */
    Nick get(OfflinePlayer p);

    Future<Nick> getAsync(OfflinePlayer p);

    void persist(Nick nick);

    /**
     * Please valid nick really had nick set.
     *
     * @param p    the player
     * @param nick the nick
     */
    void set(Player p, Nick nick);

    void set(Player p, Nick nick, boolean fc);

}
