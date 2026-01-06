package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Vexlia.JunkHubStart.JHS_IDs;

// For both modules
// JHS_Modules_RestoreShip <side>
// side value can be "left" or "right"
public class JHS_Modules_RestoreShip extends BaseCommandPlugin {

    private static final String numberNames[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {


        String Side = params.get(0).getString(memoryMap);

        ShipVariantAPI module = null;

        if (!Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString(JHS_IDs.memberFlag);
        //targetMember is our main hull!!!
        FleetMemberAPI coreMember = null;
        //Find main hull to get modules from
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(timId)) {
                coreMember = member;
                break;
            }
        }
        if (coreMember == null) {
            return false;
        }

        if(Side == "left") {
            module = coreMember.getVariant().getModuleVariant("WS0001");
        }

        if(Side == "right") {
            module = coreMember.getVariant().getModuleVariant("WS0002");
        }

        if (module == null) {
            return false;
        }

        int numDMods = 0;
        for (String modId : module.getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }
        long startingDMods = 4;
        if (Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.StartingDmods)) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong("$uwStartingDMods");
        }
        if (numDMods == 0) {
            return false;
        }

        Random rand;
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwRestoreSeed")) {
            rand = new Random();
        } else {
            rand = Misc.getRandom(Global.getSector().getMemoryWithoutUpdate().getLong("$uwRestoreSeed"), numDMods);
        }

        float needed = module.getHullSpec().getBaseHull().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 7f / (float) startingDMods;
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        if (module.isStockVariant()) {
            ShipVariantAPI v = module.clone();
            v.setSource(VariantSource.REFIT);
            v.setHullVariantId(Misc.genUID());
            module = v;
        }

        int randDMod = rand.nextInt(numDMods);
        int idx = 0;
        for (String modId : module.getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                if (idx == randDMod) {
                    module.removePermaMod(modId);
                    memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreRemovedDMod", modSpec.getDisplayName(), 0);
                    break;
                }
                idx++;
            }
        }

        //Strings for rules texts and tooltips.
        //Same memory IDs across code as they should be called independently
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
