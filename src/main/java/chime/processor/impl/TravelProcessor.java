package chime.processor.impl;

import chime.util.BlockUtil;
import chime.util.MathUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import chime.path.PathElm;
import chime.path.impl.TravelNode;
import chime.path.impl.TravelVector;
import chime.processor.Processor;

import java.util.ArrayList;
import java.util.List;

public class TravelProcessor extends Processor {

    @Override
    public void process(List<PathElm> elms) {
        List<PathElm> newPath = new ArrayList<>();

        PathIter:
        for(int a = 0 ; a < elms.size() ; a++) {
            PathElm elm = elms.get(a);

            if (!(elm instanceof TravelNode)) {
                newPath.add(elm);
                continue;
            }

            TravelNode start = (TravelNode)elms.get(a);

            for (int b = elms.size() - 1 ; b > a ; b--) {
                if (!(elms.get(b) instanceof TravelNode)) {
                    continue;
                }

                TravelNode end = (TravelNode)elms.get(b);

                if (shouldOptimise(start, end)) {
                    a = b;
                    newPath.add(new TravelVector(start, end));
                    continue PathIter;
                }
            }

            newPath.add(elm);
        }

        elms.clear();
        elms.addAll(newPath);
    }

    public boolean shouldOptimise(TravelNode start, TravelNode end) {
        if (start.getY() != end.getY()) return false;

        Vec3 startVec = new Vec3(start.getBlockPos());
        Vec3 endVec = new Vec3(end.getBlockPos());
        Vec3 differenceVector = endVec.subtract(startVec);
        Vec3 normalDelta = differenceVector.normalize();
        List<BlockPos> blocksWithinVector = new ArrayList<>();

        for(int scale = 0 ; scale < endVec.distanceTo(startVec) ; scale++) {
            Vec3 blockVec = startVec.add(MathUtil.vecMultiply(normalDelta, scale));
            BlockPos blockPos = BlockUtil.toBlockPos(blockVec);

            if (!blocksWithinVector.contains(blockPos)) blocksWithinVector.add(blockPos);
        }

        if (!blocksWithinVector.contains(BlockUtil.toBlockPos(endVec))) blocksWithinVector.add(BlockUtil.toBlockPos(endVec));
        blocksWithinVector.remove(BlockUtil.toBlockPos(startVec));

        for (BlockPos block : blocksWithinVector) {
            BlockPos[] surroundings = getBlockPos(block);

            for (BlockPos surroundingBlock : surroundings) {
                if (BlockUtil.isBlockSolid(surroundingBlock)) {
                    return false;
                }
            }

            if (!BlockUtil.isBlockSolid(block.subtract(new Vec3i(0, 1, 0)))) return false;
        }

        return true;
    }

    private BlockPos[] getBlockPos(BlockPos block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        return new BlockPos[] {
            new BlockPos(x+1, y, z+1),
            new BlockPos(x, y, z+1),
            new BlockPos(x-1, y, z+1),
            new BlockPos(x+1, y, z),
            new BlockPos(x, y, z),
            new BlockPos(x-1, y, z),
            new BlockPos(x+1, y, z-1),
            new BlockPos(x, y, z-1),
            new BlockPos(x-1, y, z-1),
            new BlockPos(x+1, y+1, z+1),
            new BlockPos(x, y+1, z+1),
            new BlockPos(x-1, y+1, z+1),
            new BlockPos(x+1, y+1, z),
            new BlockPos(x, y+1, z),
            new BlockPos(x-1, y+1, z),
            new BlockPos(x+1, y+1, z-1),
            new BlockPos(x, y+1, z-1),
            new BlockPos(x-1, y+1, z-1),
        };
    }


}
