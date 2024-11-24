package chime.calculator.config;

import chime.util.BlockUtil;
import net.minecraft.util.BlockPos;

public class PathConfig {

    public BlockPos start, end, prevEnd;
    public boolean longDistance, render, rotate;
    public long timeout;
    public Integer iterations;

    public PathConfig(
        BlockPos end,
        boolean render,
        boolean rotate,
        long timeout
    ) {
        this.start = BlockUtil.getPlayerBlockPos();
        this.end = end;
        this.longDistance = false;
        this.render = render;
        this.rotate = rotate;
        this.timeout = timeout;
    }

    public PathConfig(
            BlockPos end,
            boolean longDistance,
            boolean render,
            boolean rotate,
            int iterations
    ) {
        this.start = BlockUtil.getPlayerBlockPos();
        this.end = end;
        this.longDistance = longDistance;
        this.render = render;
        this.rotate = rotate;
        this.iterations = iterations;
    }

}
