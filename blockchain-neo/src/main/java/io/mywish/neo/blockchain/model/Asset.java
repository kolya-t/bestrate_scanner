package io.mywish.neo.blockchain.model;

import java.util.HashMap;
import java.util.Map;

public enum Asset {
    NEO,
    GAS;

    private static Map<String, Asset> ASSET_BY_ID = new HashMap<String, Asset>() {{
        ASSET_BY_ID.put("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", NEO);
        ASSET_BY_ID.put("602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7", GAS);
    }};

    public static Asset getAssetById(String assetId) {
        return ASSET_BY_ID.get(assetId);
    }
}
