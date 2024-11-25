package chime.calculator.util;

import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Node {

    private double hCost;  // Heuristic cost to the end node
    private double gCost;  // Cost from the start node

    private final int x;
    private final int y;
    private final int z;

    private Node parent;
    private BlockPos blockPos;

    private boolean isJumpNode;
    private boolean isFallNode;

    public Node(BlockPos pos, Node parentNode, Node endNode) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockPos = pos;

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public Node(int xRel, int yRel, int zRel, Node parentNode, Node endNode) {
        this.x = xRel + parentNode.getX();
        this.y = yRel + parentNode.getY();
        this.z = zRel + parentNode.getZ();
        this.blockPos = new BlockPos(x, y, z);

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public Node(BlockPos pos, Node endNode) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockPos = pos;

        calculateHeuristic(endNode);
    }

    public Node(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public boolean canBeTraversed() {
        if (parent == null) return false;

        if (!canMoveDiagonal()) return false;
        if (!BlockUtil.isSpaceAvailable(blockPos)) return false;
        if (parent.isJumpNode() && BlockUtil.noJump(new BlockPos(x, y - 1, z))) return false;
        if (parent.isJumpNode() && parent.getX() != x && parent.getZ() != z) return false;
        if (BlockUtil.isBlockSolid(blockPos) || BlockUtil.isBlockSolid(new BlockPos(x, y + 1, z))) return false;
        if (parent.isFallNode() && parent.getY() == y) return false;
        if (BlockUtil.isBlockSolid(new BlockPos(x, y - 1, z))) return true;

        if (parent.blockPos.getY() == y - 1 && BlockUtil.isBlockSolid(new BlockPos(x, y - 2, z))) {
            setJumpNode(true);
            return true;
        }

        if (parent.isFallNode() && y == parent.getY() - 1) {
            setFallNode(true);
            return true;
        }

        if (parent.blockPos.getY() == y && BlockUtil.isBlockSolid(new BlockPos(
                parent.blockPos.getX(),
                parent.blockPos.getY() - 1,
                parent.blockPos.getZ()
        ))) {
            setFallNode(true);
            return true;
        }

        return false;
    }

    private boolean canMoveDiagonal() {
        if (parent.getX() == x || parent.getZ() == z) return false;

        List<BlockPos> diagonalPositions = Arrays.asList(
                new BlockPos(x + 1, y, z + 1),
                new BlockPos(x + 1, y, z - 1),
                new BlockPos(x - 1, y, z + 1),
                new BlockPos(x - 1, y, z - 1)
        );

        for (BlockPos diagonalPos : diagonalPositions) {
            BlockPos adjacentX = new BlockPos(diagonalPos.getX(), y, z);
            BlockPos adjacentZ = new BlockPos(x, y, diagonalPos.getZ());

            if (!BlockUtil.isSpaceAvailable(adjacentX) || !BlockUtil.isSpaceAvailable(adjacentZ)) {
                return false;
            }
        }

        return true;
    }

    private void calculateHeuristic(Node endNode) {
        double d1 = (double) endNode.getX() - x;
        double d2 = (double) endNode.getY() - y;
        double d3 = (double) endNode.getZ() - z;

        this.hCost = Math.sqrt((d1 * d1) + (d2 * d2) + (d3 * d3));
    }

    public double getHCost() {
        return hCost;
    }

    public void setParent(Node parent) {
        this.parent = parent;

        double xDiff = Math.abs((double) x - parent.getX());
        double yDiff = Math.abs((double) y - parent.getY());
        double zDiff = Math.abs((double) z - parent.getZ());

        double moveCost = (xDiff + yDiff + zDiff == 1) ? 1.0 : Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
        this.gCost = parent.getGCost() + moveCost;
    }

    public double getTotalCost() {
        return hCost + gCost;
    }

    public int getX() {
        return x;
    }

    public double getGCost() {
        return gCost;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Node getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return x == other.getX() && y == other.getY() && z == other.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public void setJumpNode(boolean jumpNode) {
        isJumpNode = jumpNode;
    }

    public void setFallNode(boolean fallNode) {
        isFallNode = fallNode;
    }

    public boolean isFallNode() {
        return isFallNode;
    }

    public boolean isJumpNode() {
        return isJumpNode;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

}
