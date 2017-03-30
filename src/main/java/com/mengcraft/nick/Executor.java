package com.mengcraft.nick;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.mengcraft.nick.$.nil;

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
        main.process(20, () -> {
            // sync
            val p = event.getPlayer();
            if (p.isOnline()) main.exec(() -> fetch(p));
        });
    }

    private void fetch(Player p) {
        Nick nick = main.fetch(p);
        main.process(() -> {
            if (p.isOnline()) {
                NickFetchedEvent event = NickFetchedEvent.call(p, nick);
                if (!event.isCancelled() && !nil(nick.getNick())) {
                    main.set(p, nick);
                }
            }
        });
    }

}
