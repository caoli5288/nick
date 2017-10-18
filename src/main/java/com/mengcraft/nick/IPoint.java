package com.mengcraft.nick;

import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

/**
 * Created on 17-4-13.
 */
public interface IPoint {

    boolean take(OfflinePlayer who, int value);

    void give(OfflinePlayer who, int value);

    class PP implements IPoint {

        private final PlayerPointsAPI api;

        public PP(PlayerPointsAPI api) {
            this.api = api;
        }

        @Override
        public boolean take(OfflinePlayer who, int value) {
            return api.take(who.getUniqueId(), value);
        }

        @Override
        public void give(OfflinePlayer who, int value) {
            api.give(who.getUniqueId(), value);
        }
    }
}
