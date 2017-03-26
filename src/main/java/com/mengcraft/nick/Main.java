package com.mengcraft.nick;

import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.val;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created on 16-5-6.
 */
public class Main extends JavaPlugin implements NickManager {

    private final Map<UUID, Nick> set = new HashMap<>();
    private boolean coloured;
    private String prefix;
    private Pattern pattern;
    private List<String> blockList;

    private ThreadPoolExecutor pool;

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

        pool = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        coloured = getConfig().getBoolean("nick.coloured");
        prefix = getConfig().getString("prefix", "#");
        pattern = Pattern.compile(getConfig().getString("nick.allow", "[\\u4E00-\\u9FA5]+"));
        blockList = getConfig().getStringList("nick.block");

        Plugin p1 = getServer().getPluginManager().getPlugin("Vault");
        if (p1 != null) {
            VaultP.bind(getServer().getServicesManager().getRegistration(Chat.class).getProvider());
        }

        Plugin p2 = getServer().getPluginManager().getPlugin("TagAPI");
        if (p2 != null && getConfig().getBoolean("modify.tag")) {
            getServer().getPluginManager().registerEvents(new TagExecutor(), this);
        }

        getServer().getPluginManager().registerEvents(new Executor(this), this);
        getCommand("nick").setExecutor(new Commander(this));

        getServer().getConsoleSender().sendMessage(new String[]{
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        });

        getServer().getServicesManager().register(NickManager.class,
                this,
                this,
                ServicePriority.Normal);

        new MetricsLite(this).start();
    }

    @Override
    public void onDisable() {
        try {
            pool.shutdown();
        } catch (Exception e) {
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        prefix = getConfig().getString("prefix");
        pattern = Pattern.compile(getConfig().getString("nick.allow"));
        blockList = getConfig().getStringList("nick.block");
    }

    public Nick fetch(OfflinePlayer p) {
        val fetched = getDatabase().find(Nick.class, p.getUniqueId());
        if (fetched == null) {
            val ank = getDatabase().createEntityBean(Nick.class);
            ank.setId(p.getUniqueId());
            ank.setName(p.getName());
            return ank;
        }
        return fetched;
    }

    public boolean check(String nick) {
        if (pattern.matcher(nick).matches()) {
            return !cntBlockedStr(nick);
        }
        return false;
    }

    private boolean cntBlockedStr(String nick) {
        for (String block : blockList) {
            if (nick.contains(block)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Nick get(Player p) {
        return set.get(p.getUniqueId());
    }

    public void set(Player p, Nick nick) {
        set(p, nick, false);
    }

    public void set(Player p, Nick nick, boolean color) {
        if (nick == null) {
            if (p.isOnline()) {
                if (getConfig().getBoolean("modify.tab")) {
                    p.setPlayerListName(null);
                }
                p.setCustomName(null);
                p.setDisplayName(null);
            }
            set.remove(p.getUniqueId());
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(prefix);
            buf.append("§r");
            val fmt = nick.getFmt();
            if (!nil(fmt)) buf.append(fmt);

            val col = nick.getColor();
            if (!nil(col) && (coloured || color)) {
                buf.append(nick.getColor());
            }

            buf.append(nick.getNick());
            buf.append("§r");

            val fin = buf.toString();
            p.setDisplayName(fin);
            if (getConfig().getBoolean("modify.tab")) {
                p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', VaultP.get(p)) + fin);
            }

            p.setCustomName(fin);
            set.put(p.getUniqueId(), nick);
        }
        TagExecutor.f5(p);
    }

    protected void quit(Player p) {
        set.remove(p.getUniqueId());
    }

    public void execute(Runnable task) {
        pool.execute(task);
    }

    public void process(Runnable task, int i) {
        getServer().getScheduler().runTaskLater(this, task, i);
    }

    public void process(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    public void save(Object nick) {
        getDatabase().save(nick);
    }

    public Collection<? extends Player> getOnline() {
        return getServer().getOnlinePlayers();
    }

    public static boolean nil(Object any) {
        return any == null;
    }

    public static void valid(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

}
