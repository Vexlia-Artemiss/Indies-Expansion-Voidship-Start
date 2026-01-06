package com.fs.starfarer.api.impl.campaign.rulecmd;

import Vexlia.JunkHubStart.JHS_IDs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class JHS_ShieldModuleRestoreAvailable extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String memberFlag = "$JHS_TDU_member";
        ShipVariantAPI shieldModule = Global.getSettings().getVariant("JHS_junkhubship_shield_module_Basic");

        if (!Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String BigChungus_Id = Global.getSector().getMemoryWithoutUpdate().getString(JHS_IDs.memberFlag);
        //targetMember is our main hull!!!
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(BigChungus_Id)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        if (targetMember.getVariant().isStockVariant()) {
            ShipVariantAPI v = targetMember.getVariant().clone();
            v.setSource(VariantSource.REFIT);
            v.setHullVariantId(Misc.genUID());
            targetMember.setVariant(v, false, false);
        }

        ShipVariantAPI curShieldModule = targetMember.getVariant().getModuleVariant("WS0009");

        return !curShieldModule.getHullSpec().getHullId().equals(shieldModule.getHullSpec().getHullId());
    }
}
