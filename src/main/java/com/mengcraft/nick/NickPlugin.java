package com.mengcraft.nick;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Update;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import net.milkbowl.vault.chat.Chat;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.mengcraft.nick.$.nil;

/**
 * Created on 16-5-6.
 */
public class NickPlugin extends JavaPlugin implements NickManager {

    private static NickPlugin plugin;

    private List<String> blockList;
    private boolean coloured;
    private String prefix;
    private Pattern pattern;

    final Cache<UUID, Future<Nick>> pool = CacheBuilder.newBuilder()
            .initialCapacity(Bukkit.getMaxPlayers())
            .build();

    @Setter
    IPoint point;
    private EbeanServer database;

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults(true);
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
        database = db.getServer();

        coloured = getConfig().getBoolean("nick.coloured");
        prefix = getConfig().getString("prefix", "#");
        pattern = Pattern.compile(getConfig().getString("nick.allow", "[\\u4E00-\\u9FA5]+"));
        blockList = getConfig().getStringList("nick.block");

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (!nil(vault)) {
            val provider = getServer().getServicesManager().getRegistration(Chat.class);
            if (!nil(provider)) VaultP.bind(provider.getProvider());
        }

        Plugin tag = getServer().getPluginManager().getPlugin("TagAPI");
        if (!nil(tag) && getConfig().getBoolean("modify.tag")) {
            getServer().getPluginManager().registerEvents(TagExecutor.inst(), this);
        }

        if (getConfig().getBoolean("set.buy")) {
            Plugin p = getServer().getPluginManager().getPlugin("PlayerPoints");
            if (!nil(p)) {
                PlayerPointsAPI api = ((PlayerPoints) p).getAPI();
                point = new IPoint.PP(api);
                getLogger().log(Level.INFO, "关联到点券插件");
            }
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

        new MLite(this).start();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        prefix = getConfig().getString("prefix");
        pattern = Pattern.compile(getConfig().getString("nick.allow"));
        blockList = getConfig().getStringList("nick.block");
    }

    @Override
    @SneakyThrows
    public Nick get(OfflinePlayer p) {
        return getAsync(p).get();
    }

    @Override
    @SneakyThrows
    public Future<Nick> getAsync(OfflinePlayer p) {
        return pool.get(p.getUniqueId(), () -> CompletableFuture.supplyAsync(() -> {
            var out = database.find(Nick.class, p.getUniqueId());
            if (nil(out)) {
                out = database.createEntityBean(Nick.class);
                out.setId(p.getUniqueId());
                out.setName(p.getName());
                out.setFmt("");
                out.setColor("");
            }
            return out;
        }));
    }

    @Override
    @SneakyThrows
    public Nick get(OfflinePlayer p, boolean fetch) {
        if (fetch) {
            return get(p);
        }
        val fut = pool.getIfPresent(p.getUniqueId());
        if (nil(fut)) return null;
        return fut.get();
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

    public void set(Player p, Nick nick, boolean fc) {
        $.valid(!Bukkit.isPrimaryThread(), "PRIMARY ONLY");
        if (nil(nick) || nil(nick.getNick()) || nick.isHide()) {
            if (getConfig().getBoolean("modify.tab")) {
                p.setPlayerListName(null);
            }
            p.setCustomName(null);
            p.setDisplayName(null);
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(prefix);
            buf.append("§r");

            if ((fc || coloured) && !nil(nick.getColor())) {
                buf.append(nick.getColor());
            }

            val fmt = $.fmt2Col(nick.getFmt());
            buf.append(fmt);
            buf.append(nick.getNick());

            buf.append("§r");

            val fin = buf.toString();
            p.setDisplayName(fin);
            if (getConfig().getBoolean("modify.tab")) {
                p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', VaultP.get(p)) + fin);
            }

            p.setCustomName(fin);
        }
        TagExecutor.f5(p);
    }

    public void set(Player p, Nick nick) {
        set(p, nick, false);
    }

    public void exec(Runnable task) {
        CompletableFuture.runAsync(task);
    }

    public void run(int i, Runnable task) {
        getServer().getScheduler().runTaskLater(this, task, i);
    }

    public void run(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    public void persist(Nick nick) {
        Update<Nick> sql = database.createUpdate(Nick.class, "update nick set name = :name, nick = :nick, fmt = :fmt, color = :color, hide = :hide where id = :id")
                .set("name", nick.getName())
                .set("nick", nick.getNick())
                .set("fmt", nick.getFmt())
                .set("color", nick.getColor())
                .set("hide", nick.isHide())
                .set("id", nick.getId());
        if (!(sql.execute() == 1)) {
            database.insert(nick);
        }
    }

    public static NickManager getNickManager() {
        return plugin;
    }

    public Collection<? extends Player> getAll() {
        return getServer().getOnlinePlayers();
    }

    public static void log(Exception e) {
        plugin.getLogger().log(Level.SEVERE, e.toString(), e);
    }

}
