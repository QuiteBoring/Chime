package chime.util;

import chime.Chime;
import net.minecraft.client.settings.KeyBinding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class KeyBindUtil {

    public static final KeyBinding[] allKeys = {
        Chime.MC.gameSettings.keyBindAttack,
        Chime.MC.gameSettings.keyBindUseItem,
        Chime.MC.gameSettings.keyBindBack,
        Chime.MC.gameSettings.keyBindForward,
        Chime.MC.gameSettings.keyBindLeft,
        Chime.MC.gameSettings.keyBindRight,
        Chime.MC.gameSettings.keyBindJump,
        Chime.MC.gameSettings.keyBindSneak,
        Chime.MC.gameSettings.keyBindSprint,
    };

    public static void rightClick() {
        if (!invoke(Chime.MC, "func_147121_ag")) {
            invoke(Chime.MC, "rightClickMouse");
        }
    }

    public static boolean invoke(Object object, String methodName) {
        try {
            final Method method = object.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(object);
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    public static void setMovement(boolean w, boolean a, boolean s, boolean d) {
        setState(Chime.MC.gameSettings.keyBindForward, w);
        setState(Chime.MC.gameSettings.keyBindLeft, a);
        setState(Chime.MC.gameSettings.keyBindBack, s);
        setState(Chime.MC.gameSettings.keyBindRight, d);
    }

    public static void setMovement(boolean j, boolean s) {
        setState(Chime.MC.gameSettings.keyBindJump, j);
        setState(Chime.MC.gameSettings.keyBindSneak, s);
    }

    public static void holdThese(KeyBinding... keyBinding) {
        releaseAllExcept(keyBinding);
        for (KeyBinding key : keyBinding) {
            if (key != null)
                setState(key, true);
        }
    }

    public static void releaseAllExcept(KeyBinding... keyBinding) {
        for (KeyBinding key : allKeys) {
            if (key != null && !contains(keyBinding, key) && key.isKeyDown()) {
                setState(key, false);
            }
        }
    }

    public static void setState(KeyBinding key, boolean pressed) {
        if (key == null) return;
        if (pressed) {
            if (!key.isKeyDown()) {
                KeyBinding.onTick(key.getKeyCode());
                KeyBinding.setKeyBindState(key.getKeyCode(), true);
            }
        } else {
            if (key.isKeyDown()) {
                KeyBinding.setKeyBindState(key.getKeyCode(), false);
            }
        }
    }

    public static boolean contains(KeyBinding[] keyBinding, KeyBinding key) {
        for (KeyBinding keyBind : keyBinding) {
            if (keyBind != null && keyBind == key)
                return true;
        }
        return false;
    }


    public static void stopMovement() {
        stopMovement(false);
    }

    public static void stopMovement(boolean ignoreAttack) {
        setState(Chime.MC.gameSettings.keyBindForward, false);
        setState(Chime.MC.gameSettings.keyBindBack, false);
        setState(Chime.MC.gameSettings.keyBindRight, false);
        setState(Chime.MC.gameSettings.keyBindLeft, false);

        if (!ignoreAttack) {
            setState(Chime.MC.gameSettings.keyBindAttack, false);
            setState(Chime.MC.gameSettings.keyBindUseItem, false);
        }

        setState(Chime.MC.gameSettings.keyBindSneak, false);
        setState(Chime.MC.gameSettings.keyBindJump, false);
        setState(Chime.MC.gameSettings.keyBindSprint, false);
    }

}
