package com.mengcraft.nick;

import com.mengcraft.nick.db.EbeanHandler;
import com.mengcraft.nick.db.EbeanManager;
import com.mengcraft.nick.entity.Nick;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 16-5-6.
 */
public class Main extends JavaPlugin {

    private String prefix;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (db.isNotInitialized()) {
            db.define(Nick.class);
            try {
                db.initialize();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        db.install();
        db.reflect();

        prefix = getConfig().getString("prefix");

        getServer().getPluginManager().registerEvents(new Executor(this), this);
        getCommand("nick").setExecutor(new Commander(this));
    }

    public Nick fetch(OfflinePlayer p) {
        Nick fetched = getDatabase().find(Nick.class, p.getUniqueId());
        Nick nick;
        if (fetched == null) {
            nick = getDatabase().createEntityBean(Nick.class);
            nick.setId(p.getUniqueId());
            nick.setName(p.getName());
        } else {
            nick = fetched;
        }
        return nick;
    }

    public void set(Player p, String nick) {
        String fin = getPrefix() + "§r" + nick;
        p.setDisplayName(fin);
        p.setPlayerListName(fin);
        p.setCustomName(fin);
    }

    public void execute(Runnable task) {
        getServer().getScheduler().runTaskAsynchronously(this, task);
    }

    public void process(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    public String getPrefix() {
        return prefix == null ? "" : prefix;
    }

    public void save(Object nick) {
        getDatabase().save(nick);
    }

}
