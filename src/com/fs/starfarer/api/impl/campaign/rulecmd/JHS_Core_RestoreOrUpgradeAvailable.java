package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

//Vexlia: Outside of check for show options it also acts as utility class to get called from other rules commands

public class JHS_Core_RestoreOrUpgradeAvailable extends BaseCommandPlugin {

    static String HullId_StartWith = "JHS_junkhubship_core";
    String mem_memberID = "$JHS_TDU_member";
    String UoR_optionStr_key = "$JHS_core_RestoreOrUpgradeStr";
    String UoR_tooltipStr_key = "$JHS_core_RestoreOrUpgradeTooltip";

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


        boolean restore = restoreAvailable(ruleId, dialog, params, memoryMap);
        boolean upgrade = anyUpgradeAvailable(ruleId, dialog, params, memoryMap);
        if (restore && upgrade) {
            memoryMap.get(MemKeys.GLOBAL).set(UoR_optionStr_key, "Restore or upgrade", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set(UoR_tooltipStr_key, "Explore options to clear d-mods from " + targetMember.getShipName() + " or to apply upgrades to her hull.", 0);
            }
        } else if (restore) {
            memoryMap.get(MemKeys.GLOBAL).set(UoR_optionStr_key, "Restore", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set(UoR_tooltipStr_key, "Explore options to clear d-mods from " + targetMember.getShipName() + ".", 0);
            }
        } else if (upgrade) {
            memoryMap.get(MemKeys.GLOBAL).set(UoR_optionStr_key, "Upgrade", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set(UoR_tooltipStr_key, "Explore options to apply upgrades to " + targetMember.getShipName() + ".", 0);
            }
        }

        return restore || upgrade;
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

    protected boolean anyUpgradeAvailable(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(mem_memberID)) {
            return false;
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

        memoryMap.get(MemKeys.GLOBAL).set("$JHS_UpgradeShip", targetMember.getShipName(), 0);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if ((entity.getMarket() != null) && !entity.getMarket().hasSpaceport()) {
            return false;
        }

        RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
        if (!level.isAtWorst(RepLevel.SUSPICIOUS)) {
            return false;
        }

        EnumSet<UpgradeType> possibleUpgrades = UpgradeType.getPossibleUpgrades(targetMember.getVariant());
        for (UpgradeType upgrade : possibleUpgrades) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwUpgrade" + upgrade.id + "CostStr", Misc.getWithDGS(upgrade.credits), 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwUpgrade" + upgrade.id + "SwapCostStr", Misc.getWithDGS(upgrade.swapCredits), 0);
        }

        return !possibleUpgrades.isEmpty();
    }


    // Giant code to implement custom value and get it
    public static enum UpgradeType {

        //Bringing ship to baseline
        U1_FIXED_ALL_WINGS("U1", 1, 0),
        U2_FIXED_DRONE_LAUNCHERS("U2", 1, 0),
        U3_FIXED_FLEET_GANTRY("U3", 1, 0),
        U4_FIXED_WEAPON_SLOTS_40_OP("U4", 1, 0),
        U5_FIXED_TARGETING_CORE("U5", 1, 0),

        //ADDITIONAL PERMA UPGRADES
        P1_ADDED_LOGISTICS("P1", 1, 0),
        P2_ADDED_VOIDSHIP_COMMAND("P2", 1, 0),

        //SWAPPABLE UPGRADES
        M1_CHOOSE_CARRIER_LEFT_MODULE("M1", 1, 0),
        M2_CHOOSE_LOGISTIC_RIGHT_MODULE("M2", 1, 0),
        M3_CHOOSE_BALANCED_MODULES("M3", 1, 0),

        W1_CHOOSE_MODERNISED_WEAPON_SLOTS("W1", 1, 0),
        W2_CHOOSE_MODERNISED_WING_SLOTS("W2", 1, 0);


        public final String id;
        public final int credits;
        public final int swapCredits;

        UpgradeType(String id, int credits, int swapCredits) {
            this.id = id;
            this.credits = credits;
            this.swapCredits = swapCredits;
        }

        public static EnumSet<UpgradeType> getCurrentUpgrades(ShipVariantAPI variant) {
            EnumSet<UpgradeType> upgrades = EnumSet.noneOf(UpgradeType.class);

            if (variant == null) {
                return upgrades;
            }

            String name = variant.getHullSpec().getHullId();
            String token = HullId_StartWith + "_u";
            if (!name.startsWith(token)) {
                return upgrades;
            }


            if (name.startsWith(token + "1")) {
                token += "1";
                upgrades.add(U1_FIXED_ALL_WINGS);
            }
            if (name.startsWith(token + "2")) {
                token += "2";
                upgrades.add(U2_FIXED_DRONE_LAUNCHERS);
            }
            if (name.startsWith(token + "3")) {
                token += "3";
                upgrades.add(U3_FIXED_FLEET_GANTRY);
            }
            if (name.startsWith(token + "4")) {
                token += "4";
                upgrades.add(U4_FIXED_WEAPON_SLOTS_40_OP);
            }
            if (name.startsWith(token + "5")) {
                token += "5";
                upgrades.add(U5_FIXED_TARGETING_CORE);
            }


            if (name.startsWith(token + "_m1")) {
                token += "_m1";
                upgrades.add(M1_CHOOSE_CARRIER_LEFT_MODULE);
            } else if (name.startsWith(token + "_m2")) {
                token += "_m2";
                upgrades.add(M2_CHOOSE_LOGISTIC_RIGHT_MODULE);
            } else if (name.startsWith(token + "_m3")) {
                token += "_m3";
                upgrades.add(M3_CHOOSE_BALANCED_MODULES);
            } else {
                return upgrades;
            }


            if (name.startsWith(token + "_w1")) {
                token += "_w1";
                upgrades.add(W1_CHOOSE_MODERNISED_WEAPON_SLOTS);
            } else if (name.startsWith(token + "_w2")) {
                token += "_w2";
                upgrades.add(W2_CHOOSE_MODERNISED_WING_SLOTS);
            } else {
                return upgrades;
            }

            return upgrades;
        }

        public static EnumSet<UpgradeType> getPossibleUpgrades(ShipVariantAPI variant) {
            EnumSet<UpgradeType> upgrades = EnumSet.noneOf(UpgradeType.class);

            if (variant == null) {
                return upgrades;
            }

            String name = variant.getHullSpec().getHullId();
            String token = HullId_StartWith;
            if (!name.startsWith(token)) {
                return upgrades;
            }

            upgrades.add(U1_FIXED_ALL_WINGS);
            upgrades.add(U2_FIXED_DRONE_LAUNCHERS);
            upgrades.add(U3_FIXED_FLEET_GANTRY);
            upgrades.add(U4_FIXED_WEAPON_SLOTS_40_OP);
            upgrades.add(U5_FIXED_TARGETING_CORE);

            if (name.startsWith(token + "_u")) {
                token += "_u";
            }
            if (name.startsWith(token + "1")) {
                token += "1";
                upgrades.remove(U1_FIXED_ALL_WINGS);
            }
            if (name.startsWith(token + "2")) {
                token += "2";
                upgrades.remove(U2_FIXED_DRONE_LAUNCHERS);
            }
            if (name.startsWith(token + "3")) {
                token += "3";
                upgrades.remove(U3_FIXED_FLEET_GANTRY);
            }
            if (name.startsWith(token + "4")) {
                token += "4";
                upgrades.remove(U4_FIXED_WEAPON_SLOTS_40_OP);
            }
            if (name.startsWith(token + "5")) {
                token += "5";
                upgrades.remove(U5_FIXED_TARGETING_CORE);
            }

            if (!upgrades.isEmpty()) {
                return upgrades;
            }

            upgrades.add(P1_ADDED_LOGISTICS);
            upgrades.add(P2_ADDED_VOIDSHIP_COMMAND);

            if (name.startsWith(token + "_p1")) {
                token += "_p1";
                upgrades.remove(P1_ADDED_LOGISTICS);
            }

            if (name.startsWith(token + "_p2")) {
                token += "_p2";
                upgrades.remove(P2_ADDED_VOIDSHIP_COMMAND);
            }

            upgrades.add(W1_CHOOSE_MODERNISED_WEAPON_SLOTS);
            upgrades.add(W2_CHOOSE_MODERNISED_WING_SLOTS);

            if (name.startsWith(token + "_w1")) {
                token += "_w1";
                upgrades.remove(W1_CHOOSE_MODERNISED_WEAPON_SLOTS);
            } else if (name.startsWith(token + "_w2")) {
                token += "_w2";
                upgrades.remove(W2_CHOOSE_MODERNISED_WING_SLOTS);
            }

            upgrades.add(M1_CHOOSE_CARRIER_LEFT_MODULE);
            upgrades.add(M2_CHOOSE_LOGISTIC_RIGHT_MODULE);
            upgrades.add(M3_CHOOSE_BALANCED_MODULES);
            if (name.startsWith(token + "_m1")) {
                token += "_m1";
                upgrades.remove(M1_CHOOSE_CARRIER_LEFT_MODULE);
            } else if (name.startsWith(token + "_m2")) {
                token += "_m2";
                upgrades.remove(M2_CHOOSE_LOGISTIC_RIGHT_MODULE);
            } else if (name.startsWith(token + "_m3")) {
                token += "_m3";
                upgrades.remove(M3_CHOOSE_BALANCED_MODULES);
            } else {
                return upgrades;
            }

            return upgrades;
        }

        public static ShipHullSpecAPI getHullSpec(EnumSet<UpgradeType> upgrades) {
            if (upgrades == null) {
                return null;
            }

            String name = HullId_StartWith;

            if (upgrades.contains(U1_FIXED_ALL_WINGS) || upgrades.contains(U2_FIXED_DRONE_LAUNCHERS)
                    || upgrades.contains(U3_FIXED_FLEET_GANTRY) || upgrades.contains(U4_FIXED_WEAPON_SLOTS_40_OP)
                    || upgrades.contains(U5_FIXED_TARGETING_CORE)){
                name += "_u";
            }
            if (upgrades.contains(U1_FIXED_ALL_WINGS)) {
                name += "1";
            }
            if (upgrades.contains(U2_FIXED_DRONE_LAUNCHERS)) {
                name += "2";
            }
            if (upgrades.contains(U3_FIXED_FLEET_GANTRY)) {
                name += "3";
            }
            if (upgrades.contains(U4_FIXED_WEAPON_SLOTS_40_OP)) {
                name += "4";
            }
            if (upgrades.contains(U5_FIXED_TARGETING_CORE)) {
                name += "5";
            }


            if (upgrades.contains(P1_ADDED_LOGISTICS)) {
                name += "_p1";
            } else if (upgrades.contains(P2_ADDED_VOIDSHIP_COMMAND)) {
                name += "_p2";
            }


            if (upgrades.contains(W1_CHOOSE_MODERNISED_WEAPON_SLOTS)) {
                name += "_w1";
            } else if (upgrades.contains(W2_CHOOSE_MODERNISED_WING_SLOTS)) {
                name += "_w2";
            }

            if(!upgrades.contains(M1_CHOOSE_CARRIER_LEFT_MODULE) && !upgrades.contains(M2_CHOOSE_LOGISTIC_RIGHT_MODULE) && !upgrades.contains(M3_CHOOSE_BALANCED_MODULES)){
                name += "_m3";
            }

            if (upgrades.contains(M1_CHOOSE_CARRIER_LEFT_MODULE)) {
                name += "_m1";
            } else if (upgrades.contains(M2_CHOOSE_LOGISTIC_RIGHT_MODULE)) {
                name += "_m2";
            } else if (upgrades.contains(M3_CHOOSE_BALANCED_MODULES)) {
                name += "_m3";
            }

            ShipHullSpecAPI hullSpec;
            try {
                hullSpec = Global.getSettings().getHullSpec(name);
            } catch (Exception e) {
                hullSpec = null;
            }

            return hullSpec;
        }

        public static UpgradeType getUpgrade(String id) {
            if (id == null) {
                return null;
            }

            for (UpgradeType upgrade : UpgradeType.values()) {
                if (upgrade.id.contentEquals(id)) {
                    return upgrade;
                }
            }
            return null;
        }

        public static int getCost(UpgradeType type, ShipVariantAPI variant) {
            if (variant == null) {
                return 0;
            }

            EnumSet<UpgradeType> currentUpgrades = getCurrentUpgrades(variant);
            switch (type) {

                case W1_CHOOSE_MODERNISED_WEAPON_SLOTS -> {
                    if (currentUpgrades.contains(UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS)) {
                        return type.swapCredits;
                    }
                }
                case W2_CHOOSE_MODERNISED_WING_SLOTS -> {
                    if (currentUpgrades.contains(UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS)) {
                        return type.swapCredits;
                    }
                }
                default -> {
                }
            }

            return type.credits;
        }

        //My own addition, to have dinamic getDescriptionPrefix string, instead of setting it throught skin files.
        public static String getDescriptionString(EnumSet<UpgradeType> upgrades) {
            String dynamic_desc = null;

            String BrokenJunk = "The Big Chungus was recently recovered and made space worthy, but any respected spacer will look at it and call it junk or scrap, as most of ship is barely functional and held by luck and constant repairs.";

            String FunctionalJunk = "The Big Chungus was brought from edge of scrap to functional even if heavily degraded ship.";

            String HubshipRestored = "";

            String VoidhubFeared = "The Big Chungus... Only few remember what a space scrap it was, as now hull instills fear and respect as it unique signature gets caught on radars. Surpassing any of it's Djoeng cousins, Big Chungus represent pristine and unreachable superiority in combat and exploration. Shining example of best sector has to offer.";

            if(!upgrades.contains(U1_FIXED_ALL_WINGS) || !upgrades.contains(U2_FIXED_DRONE_LAUNCHERS) || !upgrades.contains(U3_FIXED_FLEET_GANTRY)
                    ||!upgrades.contains(U4_FIXED_WEAPON_SLOTS_40_OP) || !upgrades.contains(U5_FIXED_TARGETING_CORE)){
                dynamic_desc = BrokenJunk;
            }
            else {
                dynamic_desc = FunctionalJunk;
            }

            //All restoration upgrades plus "default" module upgrade
            if(upgrades.size() >= 6){
                dynamic_desc = HubshipRestored;
            }

            //All upgrades installed, no matter the which swithed type
            if(upgrades.size() >= 9){
                dynamic_desc = VoidhubFeared;
            }

            if (upgrades.isEmpty()) {
                return dynamic_desc;
            }

            //Logic to list individual upgrades in description
            if(upgrades.size() <= 6) {
                dynamic_desc += "\n\nThe Big Chungus been modified from its decrepit state with following upgrades: ";

                if (upgrades.contains(U1_FIXED_ALL_WINGS)) {
                    dynamic_desc += "\n\nFixed build in drones and modular wing slots, ";
                }

                if (upgrades.contains(U2_FIXED_DRONE_LAUNCHERS)) {
                    dynamic_desc += "\n\nInstalled build-in drone launchers, ";
                }

                if (upgrades.contains(U3_FIXED_FLEET_GANTRY)) {
                    dynamic_desc += "\n\nInstalled build-in drone launchers, ";
                }

                if (upgrades.contains(U4_FIXED_WEAPON_SLOTS_40_OP)) {
                    dynamic_desc += "\n\nRestored large and medium weapon slots, ";
                }

                if (upgrades.contains(U5_FIXED_TARGETING_CORE)) {
                    dynamic_desc += "\n\nInstalled build-in advanced targeting core, ";
                }
            } else if (upgrades.size() >= 6) {

                dynamic_desc += "\n\nThe Big Chungus is brought on level with Djoeng Class Voidfaring Hubship and exceeds it with following additional upgrades: ";

                if (upgrades.contains(W1_CHOOSE_MODERNISED_WEAPON_SLOTS)) {
                    dynamic_desc += "\n\nHeavily upgraded weapon mounts to use more weapon types, and added build-in hullmod, at cost of wing slots";
                } else if (upgrades.contains(W2_CHOOSE_MODERNISED_WING_SLOTS)) {
                    dynamic_desc += "\n\nFreed up build-in wing slots and added build-in B-Deck, at cost of increased DP";
                }

            }


            return dynamic_desc;
        }
    }
}
