package Vexlia.JunkHubStart.hullmods;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class JHS_FleetGantry extends BaseHullMod implements HullModFleetEffect  {
    public static final String MOD_KEY = "JHS_FleetGantry_bonus";

    public static final float BURN_BONUS = 2.0F;

    private static final float MAINTENANCE_MULT_VAGABOND = 0.85F;
    private static final float DP_BONUS = 0.1F;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (Global.getSector().getPlayerFleet() != null) {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

            for(FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
            {
                member.getStats().getSuppliesPerMonth().modifyMult(MOD_KEY, MAINTENANCE_MULT_VAGABOND);
                member.getStats().getSuppliesToRecover().modifyMult(MOD_KEY, 1-DP_BONUS);
                float baseCost = member.getStats().getSuppliesToRecover().getBaseValue();
                float reduction = baseCost * DP_BONUS;
                member.getStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(MOD_KEY, -reduction);
            }
        }
    }

    @Override
    public void advanceInCampaign(CampaignFleetAPI fleet) {
    }

    @Override
    public boolean withAdvanceInCampaign() {
        return false;
    }

    public boolean withOnFleetSync() {
        return true;
    }

    public void onFleetSync(CampaignFleetAPI fleet) {
        boolean gantry_active = false;
        for(FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()){
            if(!gantry_active && member.getVariant().hasHullMod("JHS_FleetGantry")){
                gantry_active = true;
            }
        }
        if (gantry_active) {
            fleet.getStats().getDynamic().getMod(Stats.SUSTAINED_BURN_BONUS).modifyFlat(MOD_KEY, BURN_BONUS);
        } else {
            fleet.getStats().getDynamic().getMod(Stats.SUSTAINED_BURN_BONUS).unmodifyFlat(MOD_KEY);
        }
    }

    @Override
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

        tooltip.addPara("%s to fleet's ship Deployment point cost." ,
                pad, pColor, "-" + (int) (DP_BONUS*100) + "%");

        tooltip.addPara("%s to fleet consumptions of supplied for after combat recovery." ,
                pad, pColor, "-" + (int) (DP_BONUS*100) + "%");

        tooltip.addPara("%s to fleet consumptions of supplied for monthly maintenance." ,
                pad, pColor, "-" + (int) (100-MAINTENANCE_MULT_VAGABOND*100) + "%");

        tooltip.addPara("%s to the burn level of fleet's the \"Sustained Burn\" ability." ,
                pad, pColor, "+" + (int) (BURN_BONUS));

    }
}
