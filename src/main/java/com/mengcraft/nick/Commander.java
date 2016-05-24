package com.mengcraft.nick;

import com.mengcraft.nick.entity.Nick;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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

    public Commander(Main main) {
        this.main = main;
        title = Title.of(main);
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
            } else if (eq(next, "see")) {
                return see(p, it);
            } else if (eq(next, "allow")) {
                return allow(p, it);
            } else if (eq(next, "reload")) {
                return reload(p, it);
            }
        } else {
            sendMessage(p);
        }
        return false;
    }

    private boolean reload(CommandSender p, Iterator<String> it) {
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
                ((Player) target).sendMessage(new String[]{
                        "§a你获得了修改昵称的权限",
                        "§a你有五分钟的时间来修改",
                        "§a将会保留最后设置的昵称"
                });
                title.send((Player) target, new TitleEntry("§a你获得了修改昵称的权限", "§a你有300秒的时间"));
                p.sendMessage("§a操作成功");
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
            nick1.setOrigin(stripColor(nick));

            main.getDatabase().beginTransaction();

            try {
                main.save(nick1);
                main.getDatabase().commitTransaction();
                main.process(() -> set(p, target, nick));
            } catch (Exception e) {
                p.sendMessage("§c设置失败，可能存在重名");
            } finally {
                main.getDatabase().endTransaction();
            }
        });
    }

    private void set(CommandSender p, OfflinePlayer target, String nick) {
        if (target.isOnline()) {
            main.set((Player) target, nick);
        }
        p.sendMessage("§a设置成功");
    }

    private void sendMessage(CommandSender p) {
        if (p instanceof Player) {
            if (hasSetPermission(p)) {
                p.sendMessage("§6/nick set <nick>");
            }
            if (p.hasPermission("nick.admin")) {
                p.sendMessage("§6/nick set <nick> <player>");
                p.sendMessage("§6/nick allow <player>");
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
