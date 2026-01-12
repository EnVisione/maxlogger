package com.enviouse.maxlogger.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class TextUtils {
    private TextUtils() {}

    public static Component info(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.AQUA);
    }

    public static Component success(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.GREEN);
    }

    public static Component warn(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.YELLOW);
    }

    public static Component error(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.RED);
    }
}

