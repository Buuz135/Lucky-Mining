package com.buuz135.luckymining;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LMConfig {

    public static final BuilderCodec<LMConfig> CODEC = BuilderCodec.builder(LMConfig.class, LMConfig::new)
            .append(new KeyedCodec<Double>("LuckStartChance", Codec.DOUBLE),
                    (LMConfig, aDouble, extraInfo) -> LMConfig.LuckStartChance = aDouble,
                    (LMConfig, extraInfo) -> LMConfig.LuckStartChance).add()
            .append(new KeyedCodec<Double>("LuckIncreaseChance", Codec.DOUBLE),
                    (LMConfig, aDouble, extraInfo) -> LMConfig.LuckIncreaseChance = aDouble,
                    (LMConfig, extraInfo) -> LMConfig.LuckIncreaseChance).add()
            .append(new KeyedCodec<Integer>("MaxTimeBetweenBlockBreaksInSeconds", Codec.INTEGER),
                    (LMConfig, aDouble, extraInfo) -> LMConfig.MaxTimeBetweenBlockBreaksInSeconds = aDouble,
                    (LMConfig, extraInfo) -> LMConfig.MaxTimeBetweenBlockBreaksInSeconds).add()
            .append(new KeyedCodec<String[]>("WhitelistOres", Codec.STRING_ARRAY),
                    (LMConfig, strings, extraInfo) -> LMConfig.whitelistOres = strings,
                    (LMConfig, extraInfo) -> LMConfig.whitelistOres).add()
            .build();

    private double LuckStartChance = 0.40;
    private double LuckIncreaseChance = 0.02;
    private String[] whitelistOres = new String[]{"Ore_Adamantite_", "Ore_Cobalt_", "Ore_Copper_", "Ore_Gold_", "Ore_Iron_", "Ore_Mithril_", "Ore_Onyxium_", "Ore_Silver_", "Ore_Thorium"};
    private int MaxTimeBetweenBlockBreaksInSeconds = 3;

    public LMConfig() {

    }

    public double getLuckStartChance() {
        return LuckStartChance;
    }

    public double getLuckIncreaseChance() {
        return LuckIncreaseChance;
    }

    public String[] getWhitelistOres() {
        return whitelistOres;
    }

    public int getMaxTime() {
        return MaxTimeBetweenBlockBreaksInSeconds;
    }
}
