package com.mengcraft.nick;

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
import java.util.UUID;

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
        title = Title.of(main);
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
                messenger.sendList((Player) target, "allow.notify", list);
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
        boolean b = p.hasPermission("color.admin");
        if (b) {
            setColor(p, color, main.getServer().getOfflinePlayer(next));
        }
        return b;
    }

    private boolean setColor(CommandSender p, ChatColor color) {
        boolean b = p instanceof Player && p.hasPermission("color.set.color");
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

    private boolean set(CommandSender p, String nick, String next) {
        boolean b = p.hasPermission("nick.admin");
        if (b) {
            set(p, nick, main.getServer().getOfflinePlayer(next));
        }
        return b;
    }

    private void set(CommandSender p, String nick, OfflinePlayer target) {
        main.execute(() -> {
            Nick nick1 = main.fetch(target);
            nick1.setNick(nick);

            main.getDatabase().beginTransaction();

            try {
                main.save(nick1);
                main.getDatabase().commitTransaction();
                main.process(() -> set(p, target, nick1));
            } catch (Exception e) {
                p.sendMessage("§c设置失败，可能存在重名");
            } finally {
                main.getDatabase().endTransaction();
            }
        });
    }

    private void set(CommandSender p, OfflinePlayer target, Nick nick) {
        if (target.isOnline()) {
            main.set((Player) target, nick);
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
        return i == j || (i != null && i.equals(j));
    }

}
