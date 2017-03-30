package com.mengcraft.nick;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

/**
 * Created on 16-7-9.
 */
public class TagExecutor implements Listener {

    private static TagExecutor instance;

    public TagExecutor() {
        instance = this;
    }

    @EventHandler
    public void handle(AsyncPlayerReceiveNameTagEvent event) {
        val target = event.getNamedPlayer();
        val name = target.getCustomName();
        if (!$.nil(name)) {
            event.setTag(target.getCustomName());
        }
    }

    public static void f5(Player p) {
        if (!$.nil(instance)) {
            TagAPI.refreshPlayer(p);
        }
    }

}
