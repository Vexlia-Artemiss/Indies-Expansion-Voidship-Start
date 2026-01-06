package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class JHS_ShieldModuleRestore extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String memberFlag = "$JHS_TDU_member";
        ShipVariantAPI shieldModule = Global.getSettings().getVariant("JHS_junkhubship_shield_module_Basic");



        if (!Global.getSector().getMemoryWithoutUpdate().contains(memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString(memberFlag);
        //targetMember is our main hull!!!
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(timId)) {
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

        //targetMember.getStats().getVariant().setModuleVariant("WS0009", shieldModule);
        ShipVariantAPI v = targetMember.getVariant().clone();
        v.setSource(VariantSource.HULL);
        v.setHullVariantId(Misc.genUID());
        v.getStationModules().put("WS0009", shieldModule.getHullVariantId());
        targetMember.setVariant(v, false, false);
        targetMember.setStatUpdateNeeded(true);

        float needed = 100000; //Shield module credit cost

        //For credits
        if (needed > 0) {
            playerFleet.getCargo().getCredits().subtract(needed);
            MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
            memory.set("$credits", (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
            memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
        }

        playerFleet.getFleetData().getMembersListCopy().add(Global.getSettings().createFleetMember(FleetMemberType.SHIP, shieldModule));

        playerFleet.getFleetData().setSyncNeeded();
        playerFleet.getFleetData().syncIfNeeded();
        return true;
    }
}
