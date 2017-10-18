package com.mengcraft.nick;

import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

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
        val p = event.getPlayer();
        main.run(20, () -> {
            if (p.isOnline()) {
                ((CompletableFuture<Nick>) main.getAsync(p)).thenAccept(nick -> main.run(() -> {
                    if (p.isOnline()) {
                        NickFetchedEvent evt = NickFetchedEvent.call(p, nick);
                        if (!evt.isCancelled() && !nil(nick.getNick())) {
                            main.set(p, nick);
                        }
                    }
                }));
            }
        });
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        main.pool.invalidate(event.getPlayer().getUniqueId());
    }

}
