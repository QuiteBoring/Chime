package chime;

import chime.debug.DebugCommand;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "chime", useMetadata=true)
public class Chime {

    public static Minecraft MC = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new DebugCommand());
    }

}
