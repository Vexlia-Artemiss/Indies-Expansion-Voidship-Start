package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class JHS_Core_RestoreAvailable extends JHS_Core_RestoreOrUpgradeAvailable {

    static String HullId_StartWith = "JHS_Junkhub_core";
    String mem_memberID = "$JHS_TDU_member";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

        if (!Global.getSector().getMemoryWithoutUpdate().contains(mem_memberID)) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.getHullSpec().getHullId().startsWith(HullId_StartWith)) {
                    Global.getSector().getMemoryWithoutUpdate().set(mem_memberID, member.getId());
                    break;
                }
            }
        }

        FleetMemberAPI targetMember = null;
        if (Global.getSector().getMemoryWithoutUpdate().contains(mem_memberID)) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            String targetId = Global.getSector().getMemoryWithoutUpdate().getString(mem_memberID);
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.getId().contentEquals(targetId)) {
                    targetMember = member;
                    break;
                }
            }
        }

        return restoreAvailable(ruleId, dialog, params, memoryMap);
    }

    protected boolean restoreAvailable(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(mem_memberID)) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            boolean found = false;
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.getHullSpec().getHullId().startsWith(HullId_StartWith)) {
                    Global.getSector().getMemoryWithoutUpdate().set(mem_memberID, member.getId());
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String targetId = Global.getSector().getMemoryWithoutUpdate().getString(mem_memberID);
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(targetId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        int numDMods = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }

        //Strings for options and tooltips
        memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreShip", targetMember.getShipName(), 0);
        if (numDMods > 1) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreModsStr", "one random", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreModsStr", "the last", 0);
        }
        long startingDMods = 7;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$uwStartingDMods")) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong("$uwStartingDMods");
        }
        if (numDMods == 0) {
            return false;
        }

        float needed = targetMember.getVariant().getHullSpec().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 6f / (float) startingDMods; //Ship has 8 starting dmods, so we have 2/8 or 25% discount
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreCostStr", Misc.getWithDGS(needed), 0);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if ((entity.getMarket() != null) && !entity.getMarket().hasSpaceport()) {
            return false;
        }

        RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
        if (!level.isAtWorst(RepLevel.SUSPICIOUS)) {
            return false;
        }

        return needed > 0;
    }

}
