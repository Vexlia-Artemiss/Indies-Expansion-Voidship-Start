package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class JHS_FleetGantry_collapsed extends BaseHullMod implements HullModFleetEffect {

    private static final float MAINTENANCE_MULT_VAGABOND = 0.95f;
    private static final float DP_BONUS = 0.05f;

    public static final String MOD_KEY = "JHS_FleetGantry_collapsed_bonus";

    // the fleet buff code is mostly stolen from Approlight (which very much inspired the effect)
    // it's way cleaner than the "use a manager campaign plugin" method that I was going to use
    // (this also presents a clean way for hullmods to buff all sorts of other ship stats for your whole fleet)

    //IE: I stole the codes from Apex which in turn stole from Approlight, either way thanks
    //Vexlia: Copy of IEP version without frigate stuff

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
    public void advanceInCampaign(CampaignFleetAPI fleet) {}

    @Override
    public boolean withAdvanceInCampaign() { return false; }

    @Override
    public boolean withOnFleetSync() { return false; }

    @Override
    public void onFleetSync(CampaignFleetAPI fleet) {
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

        tooltip.addPara("%s effects on the burn level of fleet's \"Sustained Burn\" ability." ,
                pad, nColor, "Doesn't provide");

    }

}

