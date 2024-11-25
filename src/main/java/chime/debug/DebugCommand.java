package chime.debug;

import chime.calculator.config.PathConfig;
import chime.handler.FlyExecutor;
import chime.handler.WalkExecutor;
import chime.util.BlockUtil;
import chime.util.LogUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public class DebugCommand extends CommandBase {

    private BlockPos block;

    @Override
    public String getCommandName() {
        return "chime";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "chime <sp|walk|fly>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("sp")) {
                block = BlockUtil.getPlayerBlockPos();
                LogUtil.sendDebug("Saved current position!");
            } else if (args[0].equalsIgnoreCase("walk")) {
                new Thread(() -> WalkExecutor.getInstance().walk(new PathConfig.Walk(
                        block,
                        true,
                        true,
                        true,
                        10000
                ))).start();
            } else if (args[0].equalsIgnoreCase("fly")) {
                new Thread(() -> FlyExecutor.getInstance().fly(new PathConfig.Fly(
                        block,
                        true,
                        true,
                        10000L
                ))).start();
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

}
