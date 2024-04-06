package studio.magemonkey.genesis.addon.limiteduses;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.conditions.GenesisCondition;
import studio.magemonkey.genesis.core.conditions.GenesisConditionSet;
import studio.magemonkey.genesis.core.conditions.GenesisConditionType;
import studio.magemonkey.genesis.core.conditions.GenesisSingleCondition;
import studio.magemonkey.genesis.events.GenesisCheckStringForFeaturesEvent;
import studio.magemonkey.genesis.events.GenesisPlayerPurchasedEvent;
import studio.magemonkey.genesis.events.GenesisRegisterTypesEvent;
import studio.magemonkey.genesis.events.GenesisTransformStringEvent;
import studio.magemonkey.genesis.managers.misc.InputReader;
import studio.magemonkey.genesis.managers.misc.StringManipulationLib;
import studio.magemonkey.genesis.misc.TimeTools;

@RequiredArgsConstructor
public class GenesisListener implements Listener {
    private final LimitedUses        plugin;
    private final LimitedUsesManager manager;

    public void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            manager.loadPlayer(p);
        }
    }

    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            manager.unloadPlayer(p, true, false);
        }
        manager.save();
        manager.unloadAll();
    }


    @EventHandler
    public void onRegisterTypes(GenesisRegisterTypesEvent e) {
        new GenesisConditionTypeUses(manager).register();
        new GenesisConditionTypeCooldown(manager).register();
    }

    @EventHandler
    public void onItemPurchased(GenesisPlayerPurchasedEvent e) {
        boolean b = false;
        if (hasConditionUses(e.getShopItem())) {
            manager.progressUse(e.getPlayer(), e.getShop(), e.getShopItem());
            b = true;
        }

        if (hasConditionCooldown(e.getShopItem())) {
            manager.progressCooldown(e.getPlayer(), e.getShop(), e.getShopItem());
            b = true;
        }

        if (b) {
            plugin.getGenesis().getAPI().updateInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void transformString(GenesisTransformStringEvent event) {
        Player p = event.getTarget();
        if (p != null && event.getShop() != null && event.getShopItem() != null) {

            String text = event.getText();

            if (text.contains("%uses%")) {
                long uses = manager.detectUsedAmount(p, event.getShop(), event.getShopItem());
                text = text.replace("%uses%", String.valueOf(uses));
            }
            if (text.contains("%uses_")) {
                String variable = StringManipulationLib.figureOutVariable(text, "uses", 0);
                long   uses     = manager.detectUsedAmount(p, variable);
                text = text.replace("%uses_" + variable + "%", String.valueOf(uses));
            }

            if (text.contains("%cooldown_")) {
                String     variable = StringManipulationLib.figureOutVariable(text, "cooldown", 0);
                GenesisBuy buy      = manager.getShopItem(variable);
                if (buy != null) {
                    long                   time         = manager.detectLastUseDelay(p, buy.getShop(), buy);
                    long                   time_to_wait = 0;
                    GenesisSingleCondition c            = getCondition(buy.getCondition(), "cooldown");
                    if (c != null) {
                        if (c.getConditionType().equalsIgnoreCase(">") || c.getConditionType()
                                .equalsIgnoreCase("over")) {
                            time_to_wait = InputReader.getInt(c.getCondition(), 0) * 1000L;
                        }
                    }
                    long time_left = time_to_wait - time;
                    text = text.replace("%cooldown_" + variable + "%",
                            TimeTools.transform(Math.max(0, time_left / 1000)));
                }
            }

            event.setText(text);
        }
    }

    @EventHandler
    public void checkString(GenesisCheckStringForFeaturesEvent event) {
        String s = event.getText();
        if (s.contains("%uses%") || s.contains("%uses_") || s.contains("%cooldown_")) {
            event.approveFeature();
        }
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.loadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        manager.unloadPlayer(e.getPlayer(), true, false);
    }

    @EventHandler
    public void onKicked(PlayerKickEvent e) {
        if (e.isCancelled()) {
            return;
        }
        manager.unloadPlayer(e.getPlayer(), true, false);
    }


    public boolean hasConditionUses(GenesisBuy buy) {
        GenesisCondition condition = buy.getCondition();
        return getCondition(condition, "uses") != null;
    }

    public boolean hasConditionCooldown(GenesisBuy buy) {
        GenesisCondition condition = buy.getCondition();
        return getCondition(condition, "cooldown") != null;
    }

    private GenesisSingleCondition getCondition(GenesisCondition condition, String conditiontype) {
        if (condition != null) {
            if (condition instanceof GenesisConditionSet) {
                GenesisConditionSet set = (GenesisConditionSet) condition;
                for (GenesisCondition c : set.getConditions()) {
                    GenesisSingleCondition subcondition = getCondition(c, conditiontype);
                    if (subcondition != null) {
                        return subcondition;
                    }
                }
            } else {
                if (condition instanceof GenesisSingleCondition) {
                    GenesisSingleCondition c = (GenesisSingleCondition) condition;
                    if (c.getType() == GenesisConditionType.detectType(conditiontype)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }


}
