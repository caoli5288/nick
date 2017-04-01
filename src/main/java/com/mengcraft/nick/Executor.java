package com.mengcraft.nick;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.mengcraft.nick.$.nil;

/**
 * Created on 16-5-6.
 */
public class Executor implements Listener {

    private final NickPlugin main;

    public Executor(NickPlugin main) {
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

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        main.cached.remove(event.getPlayer().getUniqueId());
    }

    private void fetch(Player p) {
        Nick nick = main.get(p);
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
