package chime.util;

import chime.Chime;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class LogUtil {

    public static void sendSuccess(String message) {
        LogUtil.sendLog(EnumChatFormatting.DARK_GREEN + "Chime » " + EnumChatFormatting.GREEN + message);
    }

    public static void sendError(String message) {
        LogUtil.sendLog(EnumChatFormatting.DARK_RED + "Chime » " + EnumChatFormatting.RED + message);
    }

    public static void sendWarning(String message) {
        LogUtil.sendLog(EnumChatFormatting.GOLD + "Chime » " + EnumChatFormatting.YELLOW + message);
    }

    public static void sendDebug(String message) {
        LogUtil.sendLog(EnumChatFormatting.DARK_AQUA + "Chime » " + EnumChatFormatting.AQUA + message);
    }

    private static void sendLog(String message) {
        Chime.MC.thePlayer.addChatMessage(new ChatComponentText(message));
    }

}
