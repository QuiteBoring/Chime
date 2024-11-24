package chime.util;

import chime.Chime;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MathUtil {

    public static double calculateAngleVec2D(Vec3 one, Vec3 two) {
        one = new Vec3(one.xCoord, 0, one.zCoord);
        two = new Vec3(two.xCoord, 0, two.zCoord);

        double oneMagnitude = one.distanceTo(new Vec3(0, 0, 0));
        double twoMagnitude = two.distanceTo(new Vec3(0, 0, 0));

        double deg = Math.toDegrees(Math.acos(one.dotProduct(two)/(oneMagnitude*twoMagnitude)));
        if (Double.isNaN(deg)) {
            return 180;
        }

        return deg;
    }

    public static Vec3 vecMultiply(Vec3 vec, double scale) {
        return new Vec3(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    }

    public static Rotation getRotation(BlockPos pos) {
        Entity player = Chime.MC.thePlayer;
        return getRotation(player.getPositionVector().addVector(0, player.getEyeHeight(), 0), new Vec3(pos).addVector(0.5f, 0.5f, 0.5f), player.rotationYaw);
    }

    public static Rotation getRotation(Vec3 origin, Vec3 point, double currentYaw) {
        double dx = origin.xCoord - point.xCoord;
        double dy = origin.yCoord - point.yCoord;
        double dz = origin.zCoord - point.zCoord;
        double dist = Math.sqrt(dx*dx + dz*dz);

        if (dist == 0) return new Rotation(0F, 0F);
        double pitch = 90 - Math.toDegrees(Math.atan(dist/Math.abs(dy)));
        if (dy < 0) pitch = -pitch;
        double yaw = getYaw(currentYaw, dx, dz);
        return new Rotation((float) yaw, (float) pitch);
    }

    private static double getYaw(double currentYaw, double dx, double dz) {
        double angle =  Math.toDegrees(Math.atan(Math.abs(dx / dz)));
        double yaw;
        if(dx > 0 && dz < 0) {
            yaw = angle;
        } else if(dx > 0 && dz > 0) {
            yaw = 180 - angle;
        } else if (dx < 0 && dz > 0) {
            yaw = 180 + angle;
        } else {
            yaw = 360 - angle;
        }

        double diff = yaw - currentYaw;
        while (diff > 180) {
            yaw -= 360;
            diff = yaw - currentYaw;
        }

        while (diff < -180) {
            yaw += 360;
            diff = yaw - currentYaw;
        }
        return yaw;
    }

}
