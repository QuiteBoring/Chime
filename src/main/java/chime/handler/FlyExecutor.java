package chime.handler;

import chime.Chime;
import chime.calculator.config.PathConfig;
import chime.calculator.util.FlyNodeProcessor;
import chime.mixin.PathFinderAccessor;
import chime.render.PathRender;
import chime.util.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class FlyExecutor {

    private boolean active = false;
    private int index = 0;
    private List<BlockPos> path = new ArrayList<>();
    private List<Vec3> smoothedPath = new ArrayList<>();
    private final FlyNodeProcessor flyNodeProcessor = new FlyNodeProcessor();
    private final PathFinder pathFinder = new PathFinder(flyNodeProcessor);
    private static final Vec3[] BLOCK_SIDE_MULTIPLIERS = new Vec3[]{
            new Vec3(0.05, 0, 0.05),
            new Vec3(0.05, 0, 0.95),
            new Vec3(0.95, 0, 0.05),
            new Vec3(0.95, 0, 0.95)
    };

    public void fly(PathConfig config) {
        float maxDistance = (float) Math.min(Chime.MC.thePlayer.getPositionVector().distanceTo(BlockUtil.toVec(config.end)) + 5, 1500);
        PathEntity route = ((PathFinderAccessor) pathFinder).createPath(
            Chime.MC.theWorld,
            Chime.MC.thePlayer,
            config.end.getX(),
            config.end.getY(),
            config.end.getZ(),
            maxDistance
        );

        if (route == null) {
            LogUtil.sendError("Unable to find a viable path.");
            return;
        }

        for (int i = 0; i < route.getCurrentPathLength(); i++) {
            PathPoint pathPoint = route.getPathPointFromIndex(i);
            smoothedPath.add(new Vec3(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord));
        }

        smoothedPath = smoothPath(smoothedPath);
        path.clear();
        smoothedPath.forEach((it) -> {
            BlockPos pos = BlockUtil.toBlockPos(it);
            if (!path.contains(pos)) path.add(pos);
        });

        if (config.render) PathRender.getInstance().setFlyPath(BlockUtil.toVecList(path));
        index = 0;
        active = true;
    }

    public void stop() {
        KeyBindUtil.stopMovement();
        RotationHandler.getInstance().reset();
        active = false;
        smoothedPath.clear();
    }

    public boolean isActive() {
        return active;
    }

    private Clock launch = new Clock();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Chime.MC.thePlayer == null) return;
        if (!active || smoothedPath.isEmpty()) return;

        BlockPos playerPos = BlockUtil.getPlayerBlockPos();
        BlockPos target = path.get(index);

        if (playerPos.distanceSq(BlockUtil.toBlockPos(smoothedPath.get(smoothedPath.size()-1))) < 2) {
            stop();
            return;
        }

        if (playerPos.distanceSq(target) < 1) {
            index++;
            return;
        }

        if (Chime.MC.thePlayer.capabilities.allowFlying && !Chime.MC.thePlayer.capabilities.isFlying) {
            if (Chime.MC.thePlayer.onGround) Chime.MC.thePlayer.jump();
            Chime.MC.thePlayer.capabilities.isFlying = true;
            Chime.MC.thePlayer.sendPlayerAbilities();
        }

        if (target.getY() < playerPos.getY()) {
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindSneak, true);
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindJump, false);
        } else if (target.getY() > playerPos.getY()) {
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindJump, true);
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindSneak, false);
        } else {
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindJump, false);
            KeyBindUtil.setState(Chime.MC.gameSettings.keyBindSneak, false);
        }

        Rotation angles;

        try {
            angles = MathUtil.getRotation(target);
        } catch (Exception ignored) {
            angles = new Rotation(Chime.MC.thePlayer.rotationYaw, 10F);
        }

        if (playerPos.getX() != target.getX() || playerPos.getZ() != target.getZ()) {
            RotationHandler.getInstance().easeTo(angles.yaw, 10F, 500);
        }

        pressKeys(angles.yaw);
    }

    private float getPitch() {
        return (float) (9 + Math.random() * 2);
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

    private List<Vec3> smoothPath(List<Vec3> path) {
        if (path.size() < 2) {
            return path;
        }
        List<Vec3> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
        int lowerIndex = 0;
        while (lowerIndex < path.size() - 2) {
            Vec3 start = path.get(lowerIndex);
            Vec3 lastValid = path.get(lowerIndex + 1);
            for (int upperIndex = lowerIndex + 2; upperIndex < path.size(); upperIndex++) {
                Vec3 end = path.get(upperIndex);
                if (traversable(start.addVector(0, 0.1, 0), end.addVector(0, 0.1, 0)) &&
                        traversable(start.addVector(0, 0.9, 0), end.addVector(0, 0.9, 0)) &&
                        traversable(start.addVector(0, 1.1, 0), end.addVector(0, 1.1, 0)) &&
                        traversable(start.addVector(0, 1.9, 0), end.addVector(0, 1.9, 0))) {
                    lastValid = end;
                }
            }
            smoothed.add(lastValid);
            lowerIndex = path.indexOf(lastValid);
        }

        return smoothed;
    }

    private boolean traversable(Vec3 from, Vec3 to) {
        for (Vec3 offset : BLOCK_SIDE_MULTIPLIERS) {
            Vec3 fromVec = new Vec3(from.xCoord + offset.xCoord, from.yCoord + offset.yCoord, from.zCoord + offset.zCoord);
            Vec3 toVec = new Vec3(to.xCoord + offset.xCoord, to.yCoord + offset.yCoord, to.zCoord + offset.zCoord);
            MovingObjectPosition trace = Chime.MC.theWorld.rayTraceBlocks(fromVec, toVec, false, true, false);

            if (trace != null) {
                return false;
            }
        }

        return true;
    }

    private static FlyExecutor instance;
    public static FlyExecutor getInstance() {
        if (instance == null) {
            instance = new FlyExecutor();
            MinecraftForge.EVENT_BUS.register(instance);
        }

        return instance;
    }

}