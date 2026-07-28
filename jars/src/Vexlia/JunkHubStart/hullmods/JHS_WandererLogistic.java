package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class JHS_WandererLogistic extends BaseHullMod {
    private static final float FUEL_ADDED_MAX = 2000.0F;
    private static final float FUEL_ADDED_MIN = 200.0F;
    private static final float CARGO_ADDED_MAX = 2000.0F;
    private static final float CARGO_ADDED_MIN = 2000.0F;
    private static final float MAINTENANCE_MULT = 0.6F;
    private static final float FUEL_USE_MULT = 0.7F;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (Global.getSector().getPlayerFleet() != null) {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            int fleetNumber = fleet.getFleetData().getNumMembers();
            if (fleetNumber <= 5) {
                stats.getFuelMod().modifyFlat(id, CARGO_ADDED_MAX);
                stats.getCargoMod().modifyFlat(id, FUEL_ADDED_MAX);
                stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_MULT);
                stats.getFuelUseMod().modifyMult(id, FUEL_USE_MULT);
            } else {
                stats.getFuelMod().unmodifyFlat(id);
                stats.getCargoMod().unmodifyFlat(id);
                stats.getSuppliesPerMonth().unmodifyMult(id);
                stats.getFuelUseMod().unmodifyMult(id);
            }
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) FUEL_ADDED_MAX + "";
        if (index == 1) return "" + (int) CARGO_ADDED_MAX + "";
        if (index == 2) return "" + (100 - (int) (MAINTENANCE_MULT*100f)) + "%";
        if (index == 3) return "" + (int) (100f - FUEL_USE_MULT*100f) + "%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
    }
}
