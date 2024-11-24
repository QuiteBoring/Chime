package chime.target.impl;

import chime.Chime;
import chime.util.BlockUtil;
import chime.util.Timer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import chime.path.PathElm;
import chime.path.impl.JumpNode;
import chime.target.WalkTarget;

public class JumpTarget extends WalkTarget {

    JumpNode node;
    WalkTarget next;

    boolean originalYSet;
    int originalY;

    int wait = 0;

    public JumpTarget(JumpNode node, WalkTarget next) {
        this.node = node;
        this.next = next;
    }
    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos) {
        if (!originalYSet) {
            originalYSet = true;
            originalY = (int)playerPos.yCoord;
        }

        if (next == null) return true;
        setCurrentTarget(next.getNodeBlockPos());

        wait++;
        if (wait < 2) return false;

        KeyBinding.setKeyBindState(Chime.MC.gameSettings.keyBindJump.getKeyCode(), shouldJump());
        KeyBinding.setKeyBindState(Chime.MC.gameSettings.keyBindSprint.getKeyCode(), !shouldJump());

        if ((int)playerPos.yCoord - originalY == 1 && BlockUtil.isBlockSolid(new BlockPos(playerPos).subtract(new Vec3i(0, 1, 0)))) {
            KeyBinding.setKeyBindState(Chime.MC.gameSettings.keyBindJump.getKeyCode(), false);
            return true;
        }

        return false;
    }

    private static final Timer jumpCooldown = new Timer();
    public static boolean shouldJump() {
        return Math.abs(Chime.MC.thePlayer.motionX) + Math.abs(Chime.MC.thePlayer.motionZ) < 1 && jumpCooldown.hasElasped(250, true) && BlockUtil.isColliding();
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }

}
