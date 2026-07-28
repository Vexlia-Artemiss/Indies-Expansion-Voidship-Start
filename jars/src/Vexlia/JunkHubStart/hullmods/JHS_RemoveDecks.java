package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

//Hidden Hullmod
public class JHS_RemoveDecks extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNumFighterBays().modifyMult(id, 0);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

}
