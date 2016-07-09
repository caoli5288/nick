package com.mengcraft.nick;

import com.mengcraft.nick.entity.Nick;
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
        Player p = event.getPlayer();
        main.execute(() -> {
            Nick nick = main.fetch(p);
            main.process(() -> {
                process(p, nick);
            });
        });
    }

    private void process(Player p, Nick nick) {
        if (p.isOnline() && nick.hasNick()) {
            main.set(p, nick);
        }
    }

}
