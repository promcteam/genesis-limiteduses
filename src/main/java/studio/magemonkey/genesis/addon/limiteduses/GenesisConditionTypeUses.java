package studio.magemonkey.genesis.addon.limiteduses;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.core.conditions.GenesisConditionTypeNumber;

@RequiredArgsConstructor
public class GenesisConditionTypeUses extends GenesisConditionTypeNumber {

    private final LimitedUsesManager manager;

    @Override
    public double getNumber(GenesisBuy shopItem, GenesisShopHolder holder, Player p) {
        return manager.detectUsedAmount(p, shopItem.getShop(), shopItem);
    }

    @Override
    public boolean dependsOnPlayer() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"uses", "use", "consumes"};
    }


    @Override
    public void enableType() {
    }
}
