package chime.handler;

import chime.Chime;
import chime.util.Rotation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RotationHandler {

    private Rotation startRot = null;
    private Rotation endRot = null;
    private long startTime = -1;
    private long endTime = -1;
    private boolean done = true;

    public void easeTo(float yaw, float pitch, long time) {
        done = false;
        startRot = new Rotation(Chime.MC.thePlayer.rotationYaw, Chime.MC.thePlayer.rotationPitch);
        endRot = new Rotation(yaw, pitch);
        startTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis() + time;
    }

    public void reset() {
        done = false;
    }

    public boolean isDone() {
        return done;
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (System.currentTimeMillis() <= endTime) {
            Chime.MC.thePlayer.rotationYaw = interpolate(startRot.yaw, endRot.yaw);
            Chime.MC.thePlayer.rotationPitch = interpolate(startRot.pitch, endRot.pitch);
        } else if (!done) {
            done = true;
        }
    }

    private float interpolate(float start, float end) {
        return (end - start) * easeOutCubic((float) (System.currentTimeMillis() - startTime) / (endTime - startTime)) + start;
    }

    private float easeOutCubic(double number) {
        return (float) Math.max(0, Math.min(1, 1 - Math.pow(1 - number, 3)));
    }

    private static RotationHandler instance;
    public static RotationHandler getInstance() {
        if (instance == null) {
            instance = new RotationHandler();
            MinecraftForge.EVENT_BUS.register(instance);
        }

        return instance;
    }

}

