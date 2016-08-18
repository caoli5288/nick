package com.mengcraft.nick;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Created on 16-8-19.
 */
public class NickFetchedEvent extends PlayerEvent implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final Nick nick;
    private boolean b;

    public NickFetchedEvent(Player p, Nick nick) {
        super(p);
        this.nick = nick;
    }

    public Nick getNick() {
        return nick;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return b;
    }

    @Override
    public void setCancelled(boolean b) {
        this.b = b;
    }
}
