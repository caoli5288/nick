package com.mengcraft.nick;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        main.execute(() -> fetch(event.getPlayer()));
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        main.quit(event.getPlayer());
    }

    private void fetch(Player p) {
        Nick nick = main.fetch(p);
        main.process(() -> {
            NickFetchedEvent event = NickFetchedEvent.call(p, nick);
            if (!event.isCancelled()) {
                process(p, nick);
            }
        });
    }

    private void process(Player p, Nick nick) {
        if (p.isOnline() && nick.hasNick()) {
            main.set(p, nick);
        }
    }

}
