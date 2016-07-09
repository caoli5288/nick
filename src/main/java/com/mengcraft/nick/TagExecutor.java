package com.mengcraft.nick;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

/**
 * Created on 16-7-9.
 */
public class TagExecutor implements Listener {
    @EventHandler
    public void handle(AsyncPlayerReceiveNameTagEvent event) {
        Player target = event.getNamedPlayer();
        if (target.getCustomName() != null) {
            event.setTag(target.getCustomName());
        }
    }
}
