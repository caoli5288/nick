package com.mengcraft.nick;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.mengcraft.nick.Main.nil;
import static java.util.Arrays.asList;
import static org.bukkit.ChatColor.stripColor;

/**
 * Created on 16-5-24.
 */
public class Commander implements CommandExecutor {

    private final List<UUID> allowed = new ArrayList<>();
    private final Main main;
    private final Title title;
    private final Messenger messenger;

    public Commander(Main main) {
        this.main = main;
        title = Title.build(main);
        messenger = new Messenger(main);
    }

    @Override
    public boolean onCommand(CommandSender p, Command cmd, String s, String[] args) {
        return execute(p, asList(args).iterator());
    }

    private boolean execute(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            String next = it.next();
            if (eq(next, "set")) {
                return set(p, it);
            } else if (eq(next, "set-color")) {
                return setColor(p, it);
            } else if (eq(next, "see")) {
                return see(p, it);
            } else if (eq(next, "allow")) {
                return allow(p, it);
            } else if (eq(next, "reload")) {
                return reload(p);
            } else if (eq(next, "set-fmt")) {
                return setFmt(p, it);
            }
        } else {
            sendMessage(p);
        }
        return false;
    }

    private boolean reload(CommandSender p) {
        if (p.hasPermission("nick.admin")) {
            main.reloadConfig();
        }
        return false;
    }

    private boolean allow(CommandSender p, Iterator<String> it) {
        if (it.hasNext() && p.hasPermission("nick.admin")) {
            OfflinePlayer target = main.getServer().getOfflinePlayer(it.next());
            if (target.isOnline()) {
                allowed.add(target.getUniqueId());
                main.process(() -> allowed.remove(target.getUniqueId()), 6000);
                List<String> list = Arrays.asList(
                        "§a你获得了修改昵称的权限",
                        "§a你有五分钟的时间来修改",
                        "§a将会保留最后设置的昵称"
                );
                messenger.send((Player) target, "allow.notify", ListHelper.join(list, "\n"));
                title.send((Player) target, new TitleEntry(
                        messenger.find("allow.main", "§a你获得了修改昵称的权限"),
                        messenger.find("allow.sub", "§a你有300秒的时间")
                ));
                messenger.send(p, "success", "§a操作成功");
            }
        }
        return false;
    }

    private boolean see(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            String next = it.next();
            int i = 1;
            for (Player target : main.getOnline()) {
                String custom = target.getCustomName();
                if (custom != null && stripColor(custom).contains(next)) {
                    p.sendMessage("§6 >" + (i++) + " 玩家 §r" + custom + " §6原id §r" + target.getName());
                }
            }
            return true;
        }
        return false;
    }

    <T> T[] asArray(T... i) {
        return i;
    }

    boolean setFmt(CommandSender p, Iterator<String> i) {
        if (!p.hasPermission("nick.admin")) return false;

        if (!i.hasNext()) {
            p.sendMessage(asArray("* 下列格式代码可组合使用",
                    "- r §r取消",
                    "- b §b粗体",
                    "- o §o斜体",
                    "- m §m中线",
                    "- n §n下线")
            );
            return false;
        }

        val l = i.next();
        Player who;
        if (i.hasNext()) {
            who = Bukkit.getPlayerExact(l);
            if (nil(who)) throw new IllegalStateException("offline");
        } else {
            who = (Player) p;
        }
        int idx = l.length();
        val fmt = new StringBuilder();
        while (idx-- > 0) {
            val cod = l.charAt(idx);
            if (cod == 'r') {
                fmt.setLength((idx = 0));
            } else {
                val col = ChatColor.getByChar(cod);
                if (nil(col) || !col.isFormat()) throw new IllegalArgumentException("fmt");
                fmt.append(col);
            }
        }

        val get = main.get(who);
        main.execute(() -> {
            val nick = nil(get) ? main.fetch(who) : get;
            nick.setFmt(fmt.toString());
            main.save(nick);
            main.process(() -> {
                if (who.isOnline()) main.set(who, nick);
            });
            messenger.send(p, "success", "§a操作成功");
        });

        return true;
    }

    private boolean setColor(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            ChatColor color = ChatColor.valueOf(it.next().toUpperCase());
            if (eq(color, null)) {
                throw new NullPointerException("color");
            }
            if (it.hasNext()) {
                return setColor(p, it.next(), color);
            }
            return setColor(p, color);
        }
        return false;
    }

    private boolean setColor(CommandSender p, String next, ChatColor color) {
        boolean b = p.hasPermission("nick.admin");
        if (b) {
            setColor(p, color, main.getServer().getOfflinePlayer(next));
        }
        return b;
    }

    private boolean setColor(CommandSender p, ChatColor color) {
        boolean b = p instanceof Player && p.hasPermission("nick.set.color");
        if (b) {
            setColor(p, color, (Player) p);
        }
        return b;
    }

    private void setColor(CommandSender p, ChatColor color, OfflinePlayer target) {
        main.execute(() -> {
            Nick nick = main.fetch(target);
            if (eq(color, ChatColor.RESET)) {
                nick.setColor(null);
            } else {
                nick.setColor(color.toString());
            }
            main.save(nick);
            main.process(() -> {
                if (target.isOnline()) main.set((Player) target, nick);
            });
            messenger.send(p, "success", "§a操作成功");
        });
    }

    private boolean set(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            String nick = it.next();
            if (it.hasNext()) {
                return set(p, nick, it.next());
            } else if (p instanceof Player && hasSetPermission(p)) {
                if (main.check(nick)) {
                    set(p, nick, (Player) p);
                } else {
                    p.sendMessage("§c设置失败，可能存在不允许的字符");
                }
            }
        }
        return false;
    }

    private boolean set(CommandSender p, String nick, String name) {
        boolean b = p.hasPermission("nick.admin");
        if (b) {
            set(p, nick, main.getServer().getOfflinePlayer(name));
        }
        return b;
    }

    private void set(CommandSender p, String nick, OfflinePlayer player) {
        main.execute(() -> {
            Nick entity = main.fetch(player);
            entity.setNick(nick);

            main.getDatabase().beginTransaction();

            try {
                main.save(entity);
                main.getDatabase().commitTransaction();
                main.process(() -> set(p, player, entity));
            } catch (Exception e) {
                p.sendMessage("§c设置失败，可能存在重名");
            } finally {
                main.getDatabase().endTransaction();
            }
        });
    }

    private void set(CommandSender p, OfflinePlayer player, Nick nick) {
        if (player.isOnline()) {
            main.set((Player) player, nick);
        }
        messenger.send(p, "success", "§a操作成功");
    }

    private void sendMessage(CommandSender p) {
        if (p instanceof Player) {
            if (hasSetPermission(p)) {
                p.sendMessage("§6/nick set <nick>");
            }
            if (p.hasPermission("nick.admin")) {
                p.sendMessage("§6/nick set <nick> <player>");
                p.sendMessage("§6/nick allow <player>");
                p.sendMessage("§6/nick set-color <color> <player>");
                p.sendMessage("§6/nick set-fmt <fmt> <player>");
            }
            if (p.hasPermission("nick.set.color")) {
                p.sendMessage("§6/nick set-color <color>");
            }
        } else {
            p.sendMessage("§6/nick set <nick> <player>");
            p.sendMessage("§6/nick allow <player>");
        }
    }

    private boolean hasSetPermission(CommandSender p) {
        return allowed.contains(((Player) p).getUniqueId()) || p.hasPermission("nick.set");
    }

    private boolean eq(Object i, Object j) {
        return Objects.equals(i, j);
    }

}
