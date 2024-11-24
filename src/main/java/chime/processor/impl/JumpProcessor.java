package chime.processor.impl;

import chime.Chime;
import chime.path.Node;
import chime.path.PathElm;
import chime.path.impl.JumpNode;
import chime.path.impl.TravelVector;
import chime.processor.Processor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class JumpProcessor extends Processor {

    @Override
    public void process(List<PathElm> elms) {
        List<PathElm> newPath = new ArrayList<>();

        for (PathElm elm : elms) {
            if (elm instanceof JumpNode) {
                BlockPos pos = ((JumpNode) elm).getBlockPosUnder().subtract(new Vec3i(0, 1, 0));
                PathElm nextElm = nextElement(elms, elm);

                if (nextElm != null) {
                    if (nextElm instanceof Node) {
                        BlockPos nextPos = ((Node) nextElm).getBlockPosUnder();
                        if (needJump(pos, nextPos)) continue;
                    } else if (nextElm instanceof TravelVector) {
                        BlockPos fromPos = ((TravelVector) nextElm).getFrom().getBlockPosUnder();
                        BlockPos toPos = ((TravelVector) nextElm).getTo().getBlockPosUnder();
                        if (needJump(pos, fromPos) || needJump(pos, toPos)) continue;
                    }
                } else {
                    continue;
                }
            }

            newPath.add(elm);
        }

        elms.clear();
        elms.addAll(newPath);
    }

    public static <T> T nextElement(List<T> list,T element){
        int nextIndex = list.indexOf(element) + 1;
        return list.size() > nextIndex ? list.get(nextIndex) : null;
    }

    private boolean needJump(BlockPos pos, BlockPos nextPos) {
        IBlockState state = Chime.MC.theWorld.getBlockState(pos);
        IBlockState nextState = Chime.MC.theWorld.getBlockState(nextPos);

        Block block = state.getBlock();
        Block nextBlock = nextState.getBlock();

        if (nextBlock instanceof BlockStairs) {
            if (nextState.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM) {
                EnumFacing facing = nextState.getValue(BlockStairs.FACING);
                BlockPos neededPos = getNeededPos(nextPos, facing);
                return neededPos == null || (pos.getX() == neededPos.getX() && pos.getZ() == neededPos.getZ());
            }
        } else if (nextBlock instanceof BlockSlab) {
            if (((BlockSlab) nextBlock).isDouble()) return false;
            if (block instanceof BlockSlab && !((BlockSlab) block).isDouble()) return nextState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
            return nextState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM;
        }

        return nextPos.getY() - pos.getY() < 1;
    }

    private BlockPos getNeededPos(BlockPos nextPos, EnumFacing facing) {
        if (facing == EnumFacing.NORTH) {
            return new BlockPos(nextPos.getX(), nextPos.getY()-1, nextPos.getZ()+1);
        } else if (facing == EnumFacing.EAST) {
            return new BlockPos(nextPos.getX()-1, nextPos.getY()-1, nextPos.getZ());
        } else if (facing == EnumFacing.SOUTH) {
            return new BlockPos(nextPos.getX(), nextPos.getY()-1, nextPos.getZ()-1);
        } else if (facing == EnumFacing.WEST) {
            return new BlockPos(nextPos.getX()+1, nextPos.getY()-1, nextPos.getZ());
        }

        return null;
    }

}