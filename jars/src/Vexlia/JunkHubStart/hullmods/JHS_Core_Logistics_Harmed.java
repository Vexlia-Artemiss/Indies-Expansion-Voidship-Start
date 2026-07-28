package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;


public class JHS_Core_Logistics_Harmed extends BaseHullMod
{
    //Goes on Core and affects logistic and combat stats

    //Reduces fuel capacity slightly
    //Reduced cargo space severely
    //increases DP by few points

    public static float DP_INCREASE = 10;

    public static float Cargo_Mod = 4000;
    public static float Fuel_Mod = 2000;


    public float computeCRMult(float suppliesPerDep, float dpMod) {
        return 1f + dpMod / suppliesPerDep;
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getCargoMod().modifyFlat(id, -Cargo_Mod);
        stats.getFuelMod().modifyFlat(id, -Fuel_Mod);

        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, DP_INCREASE);
        stats.getSuppliesToRecover().modifyFlat(id, DP_INCREASE);

        if (stats.getFleetMember() != null) {
            float perDep = stats.getFleetMember().getHullSpec().getSuppliesToRecover();
            float mult = computeCRMult(perDep, DP_INCREASE);
            stats.getCRPerDeploymentPercent().modifyMult(id, mult);
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship != null && ship.getMutableStats().getDynamic().getValue(Stats.FORCE_ALLOW_CONVERTED_HANGAR, 0f) > 0f) {
            return true;
        }

        return ship != null && !ship.isFrigate() && ship.getHullSpec().getFighterBays() <= 0 &&
                //ship.getNumFighterBays() <= 0 &&
                !ship.getVariant().hasHullMod(HullMods.CONVERTED_BAY) &&
                !ship.getHullSpec().isPhase();
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

        Color hColor = Misc.getHighlightColor();
        Color pColor = Misc.getPositiveHighlightColor();
        Color nColor = Misc.getNegativeHighlightColor();
        Color dColor = Misc.getDarkHighlightColor();

        final float pad = 10f;
        final float pad2 = 0f;
        final float height = 50f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading("Effects", Alignment.MID, pad);

        tooltip.addPara("%s to ship's Deployment Point cost." ,
                pad, nColor, "+" + (int) (DP_INCREASE));

        tooltip.addPara("%s to ship's required supplied for after combat recovery." ,
                pad, nColor, "+" + (int) (DP_INCREASE));

        tooltip.addPara("%s to ship's cargo capacity" ,
                pad, nColor, "-" + (int) Cargo_Mod);

        tooltip.addPara("%s to ship's fuel capacity" ,
                pad, nColor, "-" + (int) Fuel_Mod);
    }


}
