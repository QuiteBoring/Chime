package chime.target.impl;

import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import chime.path.PathElm;
import chime.path.impl.TravelNode;
import chime.target.WalkTarget;

public class TravelTarget extends WalkTarget {

    TravelNode node;

    public TravelTarget(TravelNode node) {
        this.node = node;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos) {
        setCurrentTarget(node.getBlockPos());
        Vec3 dest = new Vec3(node.getBlockPos()).addVector(0.5d, 0d, 0.5d);
        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);
        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));
        return (predicatedPositionDistance > destPositionDistance && angle < PREDICTED_MOTION_ANGLE) || BlockUtil.getPlayerBlockPos().equals(BlockUtil.toBlockPos(dest));
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }

}
