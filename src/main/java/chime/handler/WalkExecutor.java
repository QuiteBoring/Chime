package chime.handler;

import chime.Chime;
import chime.calculator.PathFinder;
import chime.calculator.config.PathConfig;
import chime.calculator.util.Node;
import chime.path.PathElm;
import chime.path.impl.FallNode;
import chime.path.impl.JumpNode;
import chime.path.impl.TravelNode;
import chime.path.impl.TravelVector;
import chime.processor.ProcessorManager;
import chime.render.PathRender;
import chime.target.WalkTarget;
import chime.target.impl.FallTarget;
import chime.target.impl.JumpTarget;
import chime.target.impl.TravelTarget;
import chime.target.impl.TravelVectorTarget;
import chime.util.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class WalkExecutor {

    private boolean isActive;
    private List<PathElm> path;
    private WalkTarget currentTarget;
    private PathConfig oldConfig;

    public void walk(PathConfig config) {
        List<Node> nodes = PathFinder.findPath(config);
        path = ProcessorManager.process(nodes);

        if (path.isEmpty()) {
            isActive = false;
            currentTarget = null;
            return;
        }

        if (config.render) {
            PathRender.getInstance().setPath(path);
        } else {
            PathRender.getInstance().clearPath();
        }

        oldConfig = config;
        currentTarget = null;
        isActive = true;
    }

    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || Chime.MC.thePlayer == null) return;
        if (!isActive) return;
        if (path.isEmpty()) {
            currentTarget = null;
            KeyBindUtil.setMovement(false, false, false, false);
            RotationHandler.getInstance().reset();

            if (!oldConfig.end.equals(BlockUtil.getPlayerBlockPos())) {
                oldConfig.start = BlockUtil.getPlayerBlockPos();
                if (oldConfig.end.equals(oldConfig.prevEnd)) {
                    LogUtil.sendError("Unable to find a viable path.");
                    isActive = false;
                    return;
                }

                oldConfig.prevEnd = oldConfig.end;
                walk(oldConfig);
            } else {
                isActive = false;
            }

            return;
        }

        if (currentTarget == null) currentTarget = getCurrentTarget(path.get(0));
        WalkTarget playerOnTarget;
        if (!((playerOnTarget = onTarget()) == null)) currentTarget = playerOnTarget;

        while (tick(currentTarget)) {
            if (!path.isEmpty()) path.remove(0);
            if (path.isEmpty()) {
                currentTarget = null;
                KeyBindUtil.setMovement(false, false, false, false);
                RotationHandler.getInstance().reset();

                if (!oldConfig.end.equals(BlockUtil.getPlayerBlockPos())) {
                    oldConfig.start = BlockUtil.getPlayerBlockPos();
                    if (oldConfig.end.equals(oldConfig.prevEnd)) {
                        LogUtil.sendError("Unable to find a viable path.");
                        return;
                    }

                    oldConfig.prevEnd = oldConfig.end;
                    walk(oldConfig);
                } else {
                    isActive = false;
                }

                return;
            }
            
            currentTarget = getCurrentTarget(path.get(0));
        }

        KeyBinding.setKeyBindState(Chime.MC.gameSettings.keyBindSprint.getKeyCode(), true);
        Rotation angles;

        try {
            angles = MathUtil.getRotation(currentTarget.getCurrentTarget());
        } catch (Exception ignored) {
            angles = new Rotation(Chime.MC.thePlayer.rotationYaw, 10F);
        }

        if (timer.hasElasped(200, true) && Chime.MC.thePlayer.onGround) RotationHandler.getInstance().easeTo(angles.yaw, currentTarget instanceof JumpTarget ? -10 : 10, 500);
        pressKeys(angles.yaw);
    }


    
    private void pressKeys(double targetYaw) {
        double difference = targetYaw - Chime.MC.thePlayer.rotationYaw;

        if (22.5 > difference && difference > -22.5) { // Forwards
            KeyBindUtil.setMovement(true, false, false, false);
        } else if (-22.5 > difference && difference > -67.5) { // Forwards+Right
            KeyBindUtil.setMovement(true, true, false, false);
        } else if (-67.5 > difference && difference > -112.5) { // Right
            KeyBindUtil.setMovement(false, true, false, false);
        } else if (-112.5 > difference && difference > -157.5) { // Backwards + Right
            KeyBindUtil.setMovement(false, true, true, false);
        } else if ((-157.5 > difference && difference > -180) ||
                (180 > difference && difference > 157.5)) { // Backwards
            KeyBindUtil.setMovement(false, false, true, false);
        } else if (67.5 > difference && difference > 22.5) { // Forwards + Left
            KeyBindUtil.setMovement(true, false, false, true);
        } else if (112.5 > difference && difference > 67.5) { // Left
            KeyBindUtil.setMovement(false, false, false, true);
        } else if (157.5 > difference && difference > 112.5) { // Backwards+Left
            KeyBindUtil.setMovement(false, false, true, true);
        }
    }

    private WalkTarget onTarget() {
        for (int i = 0; i < path.size(); i++) {
            PathElm elm = path.get(i);
            if (elm.playerOn(Chime.MC.thePlayer.getPositionVector())) {
                if (elm == currentTarget.getElm()) return null;

                if (path.size() > i + 1 && !(elm instanceof TravelVector) && !(elm instanceof JumpNode)) {
                    path.subList(0, i + 1).clear();
                } else {
                    path.subList(0, i).clear();
                }

                KeyBinding.setKeyBindState(Chime.MC.gameSettings.keyBindJump.getKeyCode(), false);
                return getCurrentTarget(path.get(0));
            }
        }

        return null;
    }

    private boolean tick(WalkTarget current) {
        Vec3 offset = new Vec3(Chime.MC.thePlayer.motionX, 0, Chime.MC.thePlayer.motionZ);
        Vec3 temp = offset;
        offset.add(temp);

        for (int i = 0; i < 12; i++) {
            offset = offset.add((temp = MathUtil.vecMultiply(temp, 0.54600006f)));
        }

        return current.tick(offset, Chime.MC.thePlayer.getPositionVector());
    }

    private WalkTarget getCurrentTarget(PathElm elm) {
        if (elm instanceof FallNode) return new FallTarget((FallNode) elm);
        if (elm instanceof TravelNode) return new TravelTarget((TravelNode) elm);
        if (elm instanceof TravelVector) return new TravelVectorTarget((TravelVector) elm);
        if (elm instanceof JumpNode) {
            if (path.size() > 1) return new JumpTarget((JumpNode) elm, getCurrentTarget(path.get(1)));
            return new JumpTarget((JumpNode) elm, null);
        }

        return null;
    }

    public boolean isActive() {
        return isActive;
    }

    private static WalkExecutor instance;
    public static WalkExecutor getInstance() {
        if (instance == null) {
            instance = new WalkExecutor();
            MinecraftForge.EVENT_BUS.register(instance);
        }

        return instance;
    }

}
