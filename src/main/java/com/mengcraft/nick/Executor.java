package com.mengcraft.nick;

import com.mengcraft.nick.entity.Nick;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 16-5-6.
 */
public class Executor implements Listener {

    private final Main main;

    public Executor(Main main) {
        this.main = main;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        main.execute(() -> {
            Nick nick = main.fetch(p);
            main.process(() -> {
                process(p, nick);
            });
        });
    }

    @EventHandler
    public void handle(AsyncPlayerReceiveNameTagEvent event) {
        Player target = event.getNamedPlayer();
        if (target.getCustomName() != null) {
            event.setTag(target.getCustomName());
        }
    }

    private void process(Player p, Nick nick) {
        if (p.isOnline() && nick.hasNick()) {
            main.set(p, nick.getNick());
        }
    }

}
