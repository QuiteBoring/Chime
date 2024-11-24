package chime.path.impl;

import chime.util.BlockUtil;
import net.minecraft.util.Vec3;
import chime.path.Node;
import chime.path.PathElm;

public class FallNode extends Node implements PathElm {

    public FallNode(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public boolean playerOn(Vec3 playerPos) {
        return BlockUtil.toBlockPos(playerPos).equals(getBlockPos());
    }

}
