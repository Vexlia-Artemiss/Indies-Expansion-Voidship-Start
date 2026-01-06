package com.fs.starfarer.api.impl.campaign.rulecmd;

import Vexlia.JunkHubStart.JHS_IDs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.rulecmd.JHS_Core_RestoreOrUpgradeAvailable.UpgradeType;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static Vexlia.JunkHubStart.JHS_IDs.memberFlag;

// JHS_Core_UpgradeShip <upgradeId>
public class JHS_Core_UpgradeShip extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString(memberFlag);
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

        String upgradeId = params.get(0).getString(memoryMap);
        UpgradeType upgrade = UpgradeType.getUpgrade(upgradeId);
        if (upgrade == null) {
            return false;
        }

        float needed = UpgradeType.getCost(upgrade, targetMember.getVariant());
        boolean swapped = false;
        EnumSet<UpgradeType> currentUpgrades = UpgradeType.getCurrentUpgrades(targetMember.getVariant());
        currentUpgrades.add(upgrade);
        switch (upgrade) {
            case W1_CHOOSE_MODERNISED_WEAPON_SLOTS -> {
                if (currentUpgrades.contains(UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS)) {
                    currentUpgrades.remove(UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS);
                    swapped = true;
                }
            }
            case W2_CHOOSE_MODERNISED_WING_SLOTS -> {
                if (currentUpgrades.contains(UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS)) {
                    currentUpgrades.remove(UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS);
                    swapped = true;
                }
            }

            case M1_CHOOSE_CARRIER_LEFT_MODULE -> {
                if (currentUpgrades.contains(UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE)) {
                    currentUpgrades.remove(UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.M3_CHOOSE_BALANCED_MODULES)) {
                    currentUpgrades.remove(UpgradeType.M3_CHOOSE_BALANCED_MODULES);
                    swapped = true;
                }
            }
            case M2_CHOOSE_LOGISTIC_RIGHT_MODULE -> {
                if (currentUpgrades.contains(UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE)) {
                    currentUpgrades.remove(UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.M3_CHOOSE_BALANCED_MODULES)) {
                    currentUpgrades.remove(UpgradeType.M3_CHOOSE_BALANCED_MODULES);
                    swapped = true;
                }
            }
            case M3_CHOOSE_BALANCED_MODULES -> {
                if (currentUpgrades.contains(UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE)) {
                    currentUpgrades.remove(UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE)) {
                    currentUpgrades.remove(UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE);
                    swapped = true;
                }
            }
            default -> {
            }
        }

        ShipHullSpecAPI hullSpec = UpgradeType.getHullSpec(currentUpgrades);
        MutableCharacterStatsAPI charStats = null;
        if (Global.getSector().getPlayerPerson() != null) {
            charStats = Global.getSector().getPlayerPerson().getStats();
        }

        if (upgrade == UpgradeType.U4_FIXED_WEAPON_SLOTS_40_OP) {
            /* Large slot does not support small weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
        }
        if ((upgrade == UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS) && swapped) {
            /* Clear disappearing modular flight decks */
            for (String wingId : targetMember.getVariant().getFittedWings()) {
                playerFleet.getCargo().addFighters(wingId, 1);
            }
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);

            /* Avoid over-OP */
            int OP = targetMember.getVariant().computeOPCost(charStats);
            int maxOP = targetMember.getVariant().getHullSpec().getOrdnancePoints(charStats) - 10;
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxCapacitors(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxVents(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().clearHullMods();
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                for (String slotId : targetMember.getVariant().getFittedWeaponSlots()) {
                    WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec(slotId);
                    if (weapon != null) {
                        playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
                    }
                }
                targetMember.getVariant().clear();
            }
        }
        if (upgrade == UpgradeType.U4_FIXED_WEAPON_SLOTS_40_OP) {
            /* Large composite slot does not support medium weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.MEDIUM)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Large slots do not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0004");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0004");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0007");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0007");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Medium hybrid/composite slots do not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0005");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0005");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0006");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0006");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0008");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0008");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0009");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0009");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0010");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0010");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
        }

        //hullSpec used to set mounts and track upgrades
        targetMember.getVariant().setHullSpecAPI(hullSpec);

        ShipVariantAPI leftModule = targetMember.getVariant().getModuleVariant("WS0001").clone();


        leftModule.setSource(VariantSource.REFIT);
        leftModule.setHullVariantId(Misc.genUID());

        ShipVariantAPI rightModule = targetMember.getVariant().getModuleVariant("WS0002").clone();
        rightModule.setSource(VariantSource.REFIT);
        rightModule.setHullVariantId(Misc.genUID());
        targetMember.getHullSpec().setDescriptionPrefix(UpgradeType.getDescriptionString(currentUpgrades));

        if (upgrade == UpgradeType.U1_FIXED_ALL_WINGS) {
            targetMember.getVariant().removePermaMod("JHS_RemoveDecks");
            targetMember.getVariant().removeMod("JHS_RemoveDecks");
            targetMember.getVariant().setWingId(0, "borer_wing");
            targetMember.getVariant().setWingId(1, "borer_wing");
        }
        if (upgrade == UpgradeType.U2_FIXED_DRONE_LAUNCHERS) {
            //targetMember.getVariant().addWeapon("WS0001", "uw_diablo");
            //targetMember.getVariant().addWeapon("WS0001", "uw_diablo");
        }
        if (upgrade == UpgradeType.U3_FIXED_FLEET_GANTRY) {
            targetMember.getVariant().removePermaMod("JHS_collapsedFleetGantry");
            targetMember.getVariant().removeMod("JHS_collapsedFleetGantry");

            targetMember.getVariant().addPermaMod("acs_fleetlogistic2");
        }
        if (upgrade == UpgradeType.U4_FIXED_WEAPON_SLOTS_40_OP) {
            targetMember.getVariant().addPermaMod("hbi");

            rightModule.addPermaMod("hbi");
            leftModule.addPermaMod("hbi");
        }
        if (upgrade == UpgradeType.U5_FIXED_TARGETING_CORE) {
            targetMember.getVariant().removeMod(HullMods.INTEGRATED_TARGETING_UNIT);
            targetMember.getVariant().addPermaMod(HullMods.ADVANCED_TARGETING_CORE);

            rightModule.removePermaMod(HullMods.INTEGRATED_TARGETING_UNIT);
            rightModule.addPermaMod(HullMods.ADVANCED_TARGETING_CORE);
            leftModule.removePermaMod(HullMods.INTEGRATED_TARGETING_UNIT);
            leftModule.addPermaMod(HullMods.ADVANCED_TARGETING_CORE);
        }

        if((upgrade == UpgradeType.P1_ADDED_LOGISTICS)){
            targetMember.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
            targetMember.getVariant().addPermaMod(HullMods.EFFICIENCY_OVERHAUL);
        }
        if((upgrade == UpgradeType.P2_ADDED_VOIDSHIP_COMMAND)){
            targetMember.getVariant().addPermaMod(JHS_IDs.VoidshipCommand);
        }

        if (upgrade == UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS) {
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);
            targetMember.getVariant().addPermaMod("JHS_RemoveDecks");
            targetMember.getVariant().addPermaMod(HullMods.ARMOREDWEAPONS);
            targetMember.getVariant().addPermaMod(HullMods.AUTOREPAIR);
        }
        if (upgrade == UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS) {
            if (swapped) {
                targetMember.getVariant().addPermaMod(HullMods.ARMOREDWEAPONS);
                targetMember.getVariant().addPermaMod(HullMods.AUTOREPAIR);
            }
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);

            targetMember.getVariant().addPermaMod("bdeck");
        }

        if (upgrade == UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE) {
            if (swapped) {
                rightModule.removePermaMod(JHS_IDs.R_Module_RemoveDecks);
                rightModule.removeMod(JHS_IDs.R_Module_RemoveDecks);
            }
            leftModule.addPermaMod(JHS_IDs.L_Module_AddDecks);
            leftModule.addMod(JHS_IDs.L_Module_AddDecks);
        }
        if (upgrade == UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE) {
            if (swapped) {
                leftModule.removePermaMod(JHS_IDs.L_Module_AddDecks);
                leftModule.removeMod(JHS_IDs.L_Module_AddDecks);
            }

            rightModule.addMod(JHS_IDs.R_Module_RemoveDecks);
            rightModule.addPermaMod(JHS_IDs.R_Module_RemoveDecks);
        }
        if (upgrade == UpgradeType.M3_CHOOSE_BALANCED_MODULES) {
            if (swapped) {
                leftModule.removePermaMod(JHS_IDs.L_Module_AddDecks);
                leftModule.removeMod(JHS_IDs.L_Module_AddDecks);

                rightModule.removePermaMod(JHS_IDs.R_Module_RemoveDecks);
                rightModule.removeMod(JHS_IDs.R_Module_RemoveDecks);
            }
            targetMember.getVariant().addMod(HullMods.AUXILIARY_THRUSTERS);
            targetMember.getVariant().addMod(HullMods.UNSTABLE_INJECTOR);
        }
        targetMember.getVariant().setModuleVariant("WS0001", leftModule);
        targetMember.getVariant().setModuleVariant("WS0002", rightModule);

        targetMember.setStatUpdateNeeded(true);

        if (needed > 0) {
            playerFleet.getCargo().getCredits().subtract(needed);
            MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
            memory.set("$credits", (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
            memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
        }

        return true;
    }




}
