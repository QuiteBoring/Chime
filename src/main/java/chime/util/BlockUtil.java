package chime.util;

import chime.Chime;
import chime.calculator.util.Node;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockUtil {

    public static BlockPos toBlockPos(Vec3 vec) {
        return new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static BlockPos getPlayerBlockPos() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        BlockPos pos = new BlockPos(
            Math.floor(player.posX),
            Math.floor(player.posY),
            Math.floor(player.posZ)
        );

        if (isBlockSolid(pos)) {
            isBlockSolid(pos = pos.add(0, 1, 0));
        }

        return pos;
    }

    public static boolean isFree(BlockPos blockpos, IBlockAccess blockaccess) {
        IBlockState blockState = blockaccess.getBlockState(blockpos);
        Block block = blockState.getBlock();

        return !blockHasCollision(blockpos, blockState, block, blockaccess);
    }

    public static boolean isFree(float x, float y, float z, IBlockAccess blockaccess) {
        BlockPos blockpos = new BlockPos(x, y, z);
        IBlockState blockState = blockaccess.getBlockState(blockpos);
        Block block = blockState.getBlock();

        return !blockHasCollision(blockpos, blockState, block, blockaccess);
    }

    public static boolean blockHasCollision(BlockPos blockPos, IBlockState blockState, Block block, IBlockAccess blockAccess) {
        if (block.equals(Blocks.air) || block.equals(Blocks.water) || block.equals(Blocks.flowing_water)) {
            return false;
        }

        if (block.equals(Blocks.brown_mushroom) || block.equals(Blocks.red_mushroom) || block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem) || block.equals(Blocks.reeds)) {
            return false;
        }

        if (block.equals(Blocks.ladder)) {
            return false;
        }

        try {
            return !block.isPassable(blockAccess, blockPos) || block.getCollisionBoundingBox((World) blockAccess, blockPos, blockState) != null;
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean noJump(BlockPos block) {
        Block theBlock = Chime.MC.theWorld.getBlockState(block).getBlock();
        return theBlock instanceof BlockFence || theBlock instanceof BlockWall;
    }

    public static boolean isBlockSolid(BlockPos block) {
        Block theBlock = Chime.MC.theWorld.getBlockState(block).getBlock();
        return theBlock.isBlockSolid(Minecraft.getMinecraft().theWorld, block, null) ||
                theBlock instanceof BlockSlab ||
                theBlock instanceof BlockStainedGlass ||
                theBlock instanceof BlockPane ||
                theBlock instanceof BlockPistonExtension ||
                theBlock instanceof BlockEnderChest ||
                theBlock instanceof BlockTrapDoor ||
                theBlock instanceof BlockPistonBase ||
                theBlock instanceof BlockChest ||
                theBlock instanceof BlockStairs ||
                theBlock instanceof BlockCactus ||
                theBlock instanceof BlockGlass ||
                theBlock instanceof BlockSkull ||
                theBlock instanceof BlockSand ||
                theBlock instanceof BlockFence ||
                theBlock instanceof BlockWall ||
                theBlock == Blocks.cobblestone_wall;
    }

    public static boolean isColliding() {
        AxisAlignedBB ABB = Chime.MC.thePlayer.getEntityBoundingBox();
        List<AxisAlignedBB> boxes = getBlocks();

        for (AxisAlignedBB box : boxes) {
            if (box.intersectsWith(ABB)) return true;
        }

        return false;
    }

    public static List<AxisAlignedBB> getBlocks() {
        BlockPos cords = new BlockPos(Chime.MC.thePlayer.posX, Chime.MC.thePlayer.posY, Chime.MC.thePlayer.posZ);
        List<AxisAlignedBB> boxes = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos pos = cords.add(x, y, z);
                    Block block = Chime.MC.theWorld.getBlockState(pos).getBlock();

                    if (block.getBlockBoundsMaxY() != 1.0 || block == Block.getBlockById(0)) continue;

                    boxes.add(new AxisAlignedBB(
                            pos.getX() - 0.01,
                            pos.getY(),
                            pos.getZ() - 0.01,
                            pos.getX() + 1.01,
                            pos.getY() + 1.0,
                            pos.getZ() + 1.01
                    ));
                }
            }
        }

        return boxes;
    }

    public static Vec3 toVec(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static List<Vec3> toVecList(List<BlockPos> path) {
        return path.stream().map((it) -> new Vec3(it.getX(), it.getY(), it.getZ())).collect(Collectors.toList());
    }

    public static boolean isSpaceAvailable(BlockPos blockPos) {
        Block block1 = Chime.MC.theWorld.getBlockState(blockPos.up(1)).getBlock();
        Block block2 = Chime.MC.theWorld.getBlockState(blockPos.up(2)).getBlock();
    
        return block1.isPassable(Chime.MC.theWorld, blockPos.up(1))
                && block2.isPassable(Chime.MC.theWorld, blockPos.up(2))
    }

}
