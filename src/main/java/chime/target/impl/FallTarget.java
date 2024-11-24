package chime.target.impl;

import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import chime.path.PathElm;
import chime.path.impl.FallNode;
import chime.target.WalkTarget;

public class FallTarget extends WalkTarget {

    FallNode node;

    public FallTarget(FallNode node) {
        this.node = node;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos) {
        setCurrentTarget(node.getBlockPos());

        playerPos = new Vec3(playerPos.xCoord, 0, playerPos.zCoord);
        Vec3 dest = new Vec3(node.getX(), 0, node.getZ()).addVector(0.5d, 0d, 0.5d);
        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);

        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));
        return (predicatedPositionDistance > destPositionDistance && angle < PREDICTED_MOTION_ANGLE) || BlockUtil.toBlockPos(playerPos).equals(BlockUtil.toBlockPos(dest));
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }
}
