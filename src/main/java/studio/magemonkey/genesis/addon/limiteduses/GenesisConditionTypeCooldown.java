package studio.magemonkey.genesis.addon.limiteduses;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.core.conditions.GenesisConditionTypeNumber;

@RequiredArgsConstructor
public class GenesisConditionTypeCooldown extends GenesisConditionTypeNumber {
    private final LimitedUsesManager manager;

    @Override
    public double getNumber(GenesisBuy shopItem, GenesisShopHolder holder, Player p) {
        return manager.detectLastUseDelay(p, shopItem.getShop(), shopItem) / 1000D;
    }

    @Override
    public boolean dependsOnPlayer() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"cooldown", "delay"};
    }


    @Override
    public void enableType() {
    }
}
