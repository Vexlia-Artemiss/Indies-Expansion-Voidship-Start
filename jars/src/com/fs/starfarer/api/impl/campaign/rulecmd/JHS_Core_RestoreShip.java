package com.fs.starfarer.api.impl.campaign.rulecmd;

import Vexlia.JunkHubStart.JHS_IDs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;
import java.util.Random;

// For ships core
public class JHS_Core_RestoreShip extends BaseCommandPlugin {

    private final String numberNames[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString(JHS_IDs.memberFlag);
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

        int numDMods = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }
        long startingDMods = 7;
        if (Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.StartingDmods)) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong(JHS_IDs.StartingDmods);
        }
        if (numDMods == 0) {
            return false;
        }

        Random rand;
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$JHS_RestoreSeed")) {
            rand = new Random();
        } else {
            rand = Misc.getRandom(Global.getSector().getMemoryWithoutUpdate().getLong("$JHS_RestoreSeed"), numDMods);
        }

        float needed = targetMember.getHullSpec().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 7f / (float) startingDMods;
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        if (targetMember.getVariant().isStockVariant()) {
            ShipVariantAPI v = targetMember.getVariant().clone();
            v.setSource(VariantSource.REFIT);
            v.setHullVariantId(Misc.genUID());
            targetMember.setVariant(v, false, false);
        }
        int randDMod = rand.nextInt(numDMods);
        int idx = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                if (idx == randDMod) {
                    targetMember.getVariant().removePermaMod(modId);
                    memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreRemovedDMod", modSpec.getDisplayName(), 0);
                    break;
                }
                idx++;
            }
        }


        //Strings for rules text and tooltip.
        if (numDMods > 1) {
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreAfterStr", "partially restored", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreAfterStr", "fully restored", 0);
        }

        if (numDMods > 11) {
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStrHighlight", numDMods - 1, 0);
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStr", (numDMods - 1) + " d-mods remain.", 0);
        } else if (numDMods >= 2) {
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStrHighlight", Misc.ucFirst(numberNames[(numDMods - 1)]), 0);
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStr", Misc.ucFirst(numberNames[(numDMods - 1)]) + " d-mods remain.", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStrHighlight", "", 0);
            memoryMap.get(MemKeys.GLOBAL).set("$JHS_RestoreNumDModsStr", "", 0);
        }

        //For credits
        if (needed > 0) {
            playerFleet.getCargo().getCredits().subtract(needed);
            MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
            memory.set("$credits", (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
            memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
        }

        return true;
    }
}
