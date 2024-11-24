package chime.render;

import chime.handler.FlyExecutor;
import chime.handler.WalkExecutor;
import chime.path.*;
import chime.path.impl.JumpNode;
import chime.path.impl.TravelVector;
import chime.util.BlockUtil;
import chime.util.RenderUtil;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PathRender {

    private List<PathElm> path = new ArrayList<>();
    private List<Vec3> flyPath = new ArrayList<>();

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!path.isEmpty() && WalkExecutor.getInstance().isActive()) {
            Node lastNode = null;
            for (PathElm elm : path) {

                if (elm instanceof Node) {
                    if (elm instanceof JumpNode) {
                        RenderUtil.drawFilledEsp(((Node) elm).getBlockPosUnder().subtract(new Vec3i(0, 1, 0)), new Color(115, 255, 115));
                    }

                    if (lastNode != null) {
                        List<Vec3> lines = new ArrayList<>();
                        lines.add(new Vec3(lastNode.getBlockPos()).subtract(0, 0.5, 0));
                        lines.add(new Vec3(((Node) elm).getBlockPos()).subtract(0, 0.5, 0));
                        RenderUtil.drawLines(lines, 2f, event.partialTicks, new Color(138, 206, 255).getRGB());
                    }

                    lastNode = (Node) elm;
                }

                if (elm instanceof TravelVector) {
                    Node from = ((TravelVector) elm).getFrom();
                    Node to = ((TravelVector) elm).getTo();

                    List<Vec3> lines = new ArrayList<>();
                    if (lastNode != null) lines.add(new Vec3(lastNode.getBlockPos()).subtract(0, 0.5, 0));

                    lines.add(new Vec3(from.getBlockPos()).subtract(0, 0.5, 0));
                    lines.add(new Vec3(to.getBlockPos()).subtract(0, 0.5, 0));
                    RenderUtil.drawLines(lines, 2f, event.partialTicks, new Color(138, 206, 255).getRGB());
                    RenderUtil.drawFilledEsp(from.getBlockPosUnder(), new Color(138, 206, 255));
                    RenderUtil.drawFilledEsp(to.getBlockPosUnder(), new Color(138, 206, 255));
                    lastNode = to;
                }
            }
        } else if (!flyPath.isEmpty() && FlyExecutor.getInstance().isActive()) {
            RenderUtil.drawLines(flyPath, 2f, event.partialTicks, new Color(138, 206, 255).getRGB());
            RenderUtil.drawFilledEsp(BlockUtil.toBlockPos(flyPath.get(0)), new Color(115, 255, 115));
            RenderUtil.drawFilledEsp(BlockUtil.toBlockPos(flyPath.get(flyPath.size()-1)), new Color(138, 206, 255));
        }
    }

    public void setPath(List<PathElm> path) {
        this.path = path;
    }

    public void setFlyPath(List<Vec3> path) {
        this.flyPath = path;
    }

    public void clearPath() {
        this.path.clear();
        this.flyPath.clear();
    }

    private static PathRender instance;
    public static PathRender getInstance() {
        if (instance == null) {
            instance = new PathRender();
            MinecraftForge.EVENT_BUS.register(instance);
        }

        return instance;
    }

}
