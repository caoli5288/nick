package com.mengcraft.nick;

import lombok.val;
import org.bukkit.ChatColor;

/**
 * Created on 17-3-30.
 */
public class $ {

    public static boolean nil(Object any) {
        return any == null;
    }

    public static void valid(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

    public static String mix2Fmt(String fmt, String add) {
        if (nil(fmt)) return "";
        val out = new StringBuilder(fmt);
        int idx = add.length();
        while (--idx > -1) {
            val col = String.valueOf(add.charAt(idx));
            if (out.indexOf(col) == -1) out.append(col);
        }
        return out.toString();
    }

    public static String fmt2Col(String i) {
        if (nil(i) || i.isEmpty()) return "";
        val b = new StringBuilder();
        int idx = i.length();
        while (--idx > -1) {
            b.append(ChatColor.getByChar(i.charAt(idx)));
        }
        return b.toString();
    }

    public static void validFmt(char fmt) {
        val co = ChatColor.getByChar(fmt);
        valid(nil(co) || !co.isFormat(), "fmt");
    }

}
