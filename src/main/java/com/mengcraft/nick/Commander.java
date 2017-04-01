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

import static com.mengcraft.nick.$.nil;
import static java.util.Arrays.asList;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.stripColor;

/**
 * Created on 16-5-24.
 */
public class Commander implements CommandExecutor {

    private final List<UUID> allowed = new ArrayList<>();
    private final NickPlugin main;
    private final Title title;
    private final Messenger messenger;

    public Commander(NickPlugin main) {
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
            String next = it.next().toLowerCase();
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
            } else if (next.equals("show")) {
                hide(p, false);
                return true;
            } else if (next.equals("hide")) {
                hide(p, true);
                return true;
            }
        } else {
            sendMessage(p);
        }
        return false;
    }

    private void hide(CommandSender p, boolean hide) {
        main.exec(() -> {
            val player = ((Player) p);
            val nick = main.get(player);
            $.valid($.nil(nick.getNick()), "nil");
            if (!(nick.isHide() == hide)) {
                nick.setHide(hide);
                main.process(() -> {
                    if (player.isOnline()) main.set(player, nick);
                });
                main.persist(nick);
                messenger.send(p, "hide", "§a显示模式已切换");
            }
        });
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
                main.process(6000, () -> allowed.remove(target.getUniqueId()));
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
            for (Player target : main.getAll()) {
                String custom = target.getCustomName();
                if (!$.nil(custom) && stripColor(custom).contains(next)) {
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
                    "- l §l粗体",
                    "- o §o斜体",
                    "- m §m中线",
                    "- n §n下线")
            );
            return false;
        }

        val l = i.next();
        Player who;
        if (i.hasNext()) {
            who = Bukkit.getPlayerExact(i.next());
            if (nil(who)) throw new IllegalStateException("offline");
        } else {
            who = (Player) p;
        }
        val out = new StringBuilder();
        int idx = l.length();
        while (--idx > -1) {
            val fmt = l.charAt(idx);
            if (fmt == 'r') {
                out.setLength((idx = 0));
            } else {
                $.validFmt(fmt);
                out.append(fmt);
            }
        }

        val get = main.get(who);
        main.exec(() -> {
            val nick = nil(get) ? main.get(who) : get;
            if (out.length() == 0) {
                nick.setFmt("");
            } else {
                nick.setFmt($.mix2Fmt(nick.getFmt(), out.toString()));
            }
            main.persist(nick);
            main.process(() -> {
                if (who.isOnline()) main.set(who, nick);
            });
            messenger.send(p, "success", "§a操作成功");
        });

        return true;
    }

    private boolean setColor(CommandSender p, Iterator<String> it) {
        if (it.hasNext()) {
            val col = ChatColor.valueOf(it.next().toUpperCase());
            if (!(col == RESET) && !col.isColor()) {// RESET is  not color
                throw new NullPointerException("color");
            }
            if (it.hasNext()) {
                return setColor(p, it.next(), col);
            }
            return setColor(p, col);
        }
        return false;
    }

    private boolean setColor(CommandSender p, String who, ChatColor color) {
        boolean b = p.hasPermission("nick.admin");
        if (b) {
            setColor(p, color, Bukkit.getPlayerExact(who));
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
        $.valid($.nil(p), "offline");
        main.exec(() -> {
            Nick nick = main.get(target);
            if (color == RESET) {
                nick.setColor("");
            } else {
                nick.setColor(color.toString());
            }
            main.persist(nick);
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
            set(p, nick, Bukkit.getPlayerExact(name));
        }
        return b;
    }

    private void set(CommandSender p, String nick, OfflinePlayer player) {
        $.valid($.nil(player), "offline");
        main.exec(() -> {
            Nick entity = main.get(player);
            entity.setNick(nick);

            main.getDatabase().beginTransaction();

            try {
                main.persist(entity);
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
            p.sendMessage("§6/nick show");
            p.sendMessage("§6/nick hide");
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
