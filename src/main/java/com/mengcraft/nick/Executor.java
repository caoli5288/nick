package com.mengcraft.nick;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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

    private void fetch(Player p) {
        Nick nick = main.fetch(p);
        main.process(() -> {
            NickFetchedEvent event1 = new NickFetchedEvent(p, nick);
            main.getServer().getPluginManager().callEvent(event1);
            if (!event1.isCancelled()) {
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
