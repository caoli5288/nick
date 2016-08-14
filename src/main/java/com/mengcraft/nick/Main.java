package com.mengcraft.nick;

import com.mengcraft.nick.db.EbeanHandler;
import com.mengcraft.nick.db.EbeanManager;
import com.mengcraft.nick.entity.Nick;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created on 16-5-6.
 */
public class Main extends JavaPlugin {

    private boolean coloured;
    private String prefix;
    private Pattern pattern;
    private List<String> blockList;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

        coloured = getConfig().getBoolean("nick.coloured");
        prefix = getConfig().getString("prefix");
        pattern = Pattern.compile(getConfig().getString("nick.allow"));
        blockList = getConfig().getStringList("nick.block");

        Plugin iTag = getServer().getPluginManager().getPlugin("iTag");
        if (iTag != null && getConfig().getBoolean("modify.tag")) {
            getServer().getPluginManager().registerEvents(new TagExecutor(), this);
        }

        getServer().getPluginManager().registerEvents(new Executor(this), this);
        getCommand("nick").setExecutor(new Commander(this));

        getServer().getConsoleSender().sendMessage(new String[]{
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        });

        new MetricsLite(this).start();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        prefix = getConfig().getString("prefix");
        pattern = Pattern.compile(getConfig().getString("nick.allow"));
        blockList = getConfig().getStringList("nick.block");
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

    public boolean check(String nick) {
        if (pattern.matcher(nick).matches()) {
            return !hasBlocked(nick);
        }
        return false;
    }

    private boolean hasBlocked(String nick) {
        for (String block : blockList) {
            if (nick.contains(block)) {
                return true;
            }
        }
        return false;
    }

    public void set(Player p, Nick nick) {
        StringBuilder b = new StringBuilder();
        b.append(getPrefix());
        if (coloured && nick.hasColor()) {
            b.append(nick.getColor());
        }
        b.append(nick.getNick());
        b.append("§r");

        String fin = b.toString();
        p.setDisplayName(fin);
        if (getConfig().getBoolean("modify.tab")) {
            p.setPlayerListName(fin);
        }
        p.setCustomName(fin);
    }

    public void execute(Runnable task) {
        getServer().getScheduler().runTaskAsynchronously(this, task);
    }

    public void process(Runnable task, int i) {
        getServer().getScheduler().runTaskLater(this, task, i);
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

    public Collection<? extends Player> getOnline() {
        return getServer().getOnlinePlayers();
    }

}
