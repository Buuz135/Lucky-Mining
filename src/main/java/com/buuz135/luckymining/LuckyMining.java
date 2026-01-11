package com.buuz135.luckymining;


import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import javax.annotation.Nonnull;
import java.io.IOException;


public class LuckyMining extends JavaPlugin {

    private final Config<LMConfig> config;


    public LuckyMining(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("LuckyMining", LMConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();
        this.config.save();
        this.getEntityStoreRegistry().registerSystem(new BreakBlockEventSystem(config));
    }


}
