package chime.target.impl;

import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import chime.path.PathElm;
import chime.path.impl.TravelVector;
import chime.target.WalkTarget;

public class TravelVectorTarget extends WalkTarget {

    TravelVector node;
    boolean baseReached = false;

    public TravelVectorTarget(TravelVector node) {
        this.node = node;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos) {
        if (!baseReached) {
            if (node.playerOn(playerPos)) {
                baseReached = true;
            } else if (playerPos.distanceTo(BlockUtil.toVec(node.getFrom().getBlockPos()))
                    < playerPos.distanceTo(BlockUtil.toVec(node.getTo().getBlockPos()))) {
                baseReached = true;
            }
        }

        BlockPos destBlockPos = baseReached ? node.getTo().getBlockPos() : node.getFrom().getBlockPos();
        setCurrentTarget(destBlockPos);
        Vec3 dest = new Vec3(destBlockPos).addVector(0.5d, 0d, 0.5d);
        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);
        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));

        if ((predicatedPositionDistance > destPositionDistance && angle < PREDICTED_MOTION_ANGLE) ||
                BlockUtil.getPlayerBlockPos().equals(BlockUtil.toBlockPos(dest))) {
            if (!baseReached) {
                baseReached = true;
                setCurrentTarget(node.getTo().getBlockPos());
            } else return true;
        }

        return false;
    }

    public BlockPos getNodeBlockPos() {
        return node.getFrom().getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }

}
