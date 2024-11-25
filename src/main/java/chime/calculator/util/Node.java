package chime.calculator.util;

import chime.Chime;
import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

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

        int parentX = parent.getX();
        int parentZ = parent.getZ();
        int parentY = parent.getY();
        boolean isJumpNode = parent.isJumpNode();
        boolean isFallNode = parent.isFallNode();
        BlockPos belowBlock = new BlockPos(x, y - 1, z);
        BlockPos aboveBlock = new BlockPos(x, y + 1, z);
        BlockPos parentBelow = new BlockPos(parentX, parentY - 1, parentZ);

        if (parentX != x && parentZ != z && !canMoveDiagonal()) return false;
        if (!BlockUtil.isSpaceAvailable(blockPos)) return false;
        if (isJumpNode && BlockUtil.noJump(belowBlock)) return false;
        if (isJumpNode && parentX != x && parentZ != z) return false;
        if (BlockUtil.isBlockSolid(blockPos) || BlockUtil.isBlockSolid(aboveBlock)) return false;
        if (isFallNode && parentY == y) return false;
        if (BlockUtil.isBlockSolid(belowBlock)) return true;

        if (parentY == y - 1 && BlockUtil.isBlockSolid(new BlockPos(x, y - 2, z))) {
            setJumpNode(true);
            return true;
        }

        if (isFallNode && y == parentY - 1) {
            setFallNode(true);
            return true;
        }

        if (parentY == y && BlockUtil.isBlockSolid(parentBelow)) {
            setFallNode(true);
            return true;
        }

        return false;
    }

    private boolean canMoveDiagonal() {
        List<BlockPos> adjacent = getSimilar(
                Arrays.asList(
                        parent.blockPos.add(1, 0, 0),
                        parent.blockPos.add(-1, 0, 0),
                        parent.blockPos.add(0, 0, 1),
                        parent.blockPos.add(0, 0, -1)
                ),
                Arrays.asList(
                        blockPos.add(1, 0, 0),
                        blockPos.add(-1, 0, 0),
                        blockPos.add(0, 0, 1),
                        blockPos.add(0, 0, -1)
                )
        );

        return adjacent.size() > 1 &&
                BlockUtil.isFree(adjacent.get(0), Chime.MC.theWorld) &&
                BlockUtil.isFree(adjacent.get(1), Chime.MC.theWorld);
    }

    private void calculateHeuristic(Node endNode) {
        double d1 = endNode.getX() - x;
        double d2 = endNode.getY() - y;
        double d3 = endNode.getZ() - z;

        this.hCost = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
    }

    private List<BlockPos> getSimilar(List<BlockPos> adjPos, List<BlockPos> adjParent) {
        Set<BlockPos> adjParentSet = new HashSet<>(adjParent);
        return adjPos.stream()
                .filter(adjParentSet::contains)
                .collect(Collectors.toList());
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
