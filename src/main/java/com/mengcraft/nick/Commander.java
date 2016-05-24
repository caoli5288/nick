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

    public Commander(Main main) {
        this.main = main;
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
            }
        } else {
            sendMessage(p);
        }
        return false;
    }

    private boolean set(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            String nick = it.next();
            if (it.hasNext()) {
                return set(p, nick, it.next());
            }
        }
        return false;
    }

    private boolean set(CommandSender p, String nick, String next) {
        if (p.hasPermission("nick.admin")) {
            OfflinePlayer target = main.getServer().getOfflinePlayer(next);
            main.execute(() -> {
                Nick nick1 = main.fetch(target);
                nick1.setNick(nick);
                nick1.setOrigin(stripColor(nick));

                main.getDatabase().beginTransaction();
                try {
                    main.save(nick1);
                    main.process(() -> set(p, target, nick));
                } catch (Exception e) {
                    p.sendMessage("§c设置失败，可能存在重名");
                } finally {
                    main.getDatabase().endTransaction();
                }
            });
        }
        return false;
    }

    private void set(CommandSender p, OfflinePlayer target, String nick) {
        if (target.isOnline()) {
            main.set(((Player) target), nick);
        }
        p.sendMessage("§a设置成功");
    }

    private void sendMessage(CommandSender p) {
        if (p instanceof Player) {
            if (allowed.contains(((Player) p).getUniqueId()) || p.hasPermission("nick.set")) {
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

    private boolean eq(Object i, Object j) {
        return i == j || (i != null && i.equals(j));
    }

}
