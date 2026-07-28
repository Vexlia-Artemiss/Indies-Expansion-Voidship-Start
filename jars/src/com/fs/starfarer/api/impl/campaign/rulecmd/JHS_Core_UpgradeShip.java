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
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static Vexlia.JunkHubStart.JHS_IDs.memberFlag;

// JHS_Core_UpgradeShip <upgradeId>
public class JHS_Core_UpgradeShip extends BaseCommandPlugin {

    private static final Logger log = Logger.getLogger(JHS_Core_UpgradeShip.class);

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

            case S1_CHOOSE_BURN_DRIVE_SYSTEM -> {
                if (currentUpgrades.contains(UpgradeType.S2_CHOOSE_OFFENCE_FOCUS_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S2_CHOOSE_OFFENCE_FOCUS_SYSTEM);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.S3_CHOOSE_TARGETING_FEED_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S3_CHOOSE_TARGETING_FEED_SYSTEM);
                    swapped = true;
                }
            }
            case S2_CHOOSE_OFFENCE_FOCUS_SYSTEM -> {
                if (currentUpgrades.contains(UpgradeType.S1_CHOOSE_BURN_DRIVE_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S1_CHOOSE_BURN_DRIVE_SYSTEM);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.S3_CHOOSE_TARGETING_FEED_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S3_CHOOSE_TARGETING_FEED_SYSTEM);
                    swapped = true;
                }
            }
            case S3_CHOOSE_TARGETING_FEED_SYSTEM -> {
                if (currentUpgrades.contains(UpgradeType.S1_CHOOSE_BURN_DRIVE_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S1_CHOOSE_BURN_DRIVE_SYSTEM);
                    swapped = true;
                }
                if (currentUpgrades.contains(UpgradeType.S2_CHOOSE_OFFENCE_FOCUS_SYSTEM)) {
                    currentUpgrades.remove(UpgradeType.S2_CHOOSE_OFFENCE_FOCUS_SYSTEM);
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


        //Check mounts as upgrade changes sizes
        if (upgrade == UpgradeType.U4_FIXED_WEAPON_SLOTS_40_OP) {
            /* Medium ballistic slot does not support small non-ballistic weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0011");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && !(weapon.getType().equals(WeaponType.BALLISTIC))) {
                targetMember.getVariant().clearSlot("WS0011");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Medium ballistic slot does not support small non-ballistic weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0012");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && !(weapon.getType().equals(WeaponType.BALLISTIC))) {
                targetMember.getVariant().clearSlot("WS0012");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Large universal slot does not support medium ballistic weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0063");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.MEDIUM)) {
                targetMember.getVariant().clearSlot("WS0063");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }


            /* Large slot does not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0064");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0064");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Large slot does not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Large slot does not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

        }

        if((upgrade == UpgradeType.P1_ADDED_LOGISTICS)) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.EFFICIENCY_OVERHAUL))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.EFFICIENCY_OVERHAUL), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.EFFICIENCY_OVERHAUL);
            } else if (targetMember.getVariant().hasHullMod(HullMods.EFFICIENCY_OVERHAUL)) {
                targetMember.getVariant().removeMod(HullMods.EFFICIENCY_OVERHAUL);
            }

            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.SOLAR_SHIELDING))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.SOLAR_SHIELDING), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.SOLAR_SHIELDING);
            } else if (targetMember.getVariant().hasHullMod(HullMods.SOLAR_SHIELDING)) {
                targetMember.getVariant().removeMod(HullMods.SOLAR_SHIELDING);
            }
        }

        if((upgrade == UpgradeType.P2_ADDED_VOIDSHIP_COMMAND)) {
            //For if Operations Center was s-modded
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.OPERATIONS_CENTER))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.OPERATIONS_CENTER), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.OPERATIONS_CENTER);
            } else if (targetMember.getVariant().hasHullMod(HullMods.OPERATIONS_CENTER)) {
                targetMember.getVariant().removeMod(HullMods.OPERATIONS_CENTER);
            }
        }

        // Check mounts and clear wing slots - same for swap and pure upgrade as slot changes are same in both cases
        if ((upgrade == UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS)) {
            boolean removed_weapon = false;
            boolean removed_wings = false;

            /* Clear disappearing modular flight decks */
            if(!targetMember.getVariant().getFittedWings().isEmpty()) {
                removed_wings = true;
                for (String wingId : targetMember.getVariant().getFittedWings()) {
                    playerFleet.getCargo().addFighters(wingId, 1);
                }
            }
            // To do, actually go over mounts and make sure all legit

            // Check all smalls but 4 hybrids for composite weapons.
            Collection<String> slots = targetMember.getVariant().getFittedWeaponSlots();
            for(String current_slot : slots) {
                if (!current_slot.equals("WS0045") && !current_slot.equals("WS0046") && !current_slot.equals("WS0047") && !current_slot.equals("WS0048")) {
                    removed_weapon = true;
                    WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec(current_slot);
                    if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && (weapon.getType().equals(WeaponType.COMPOSITE))) {
                        targetMember.getVariant().clearSlot(current_slot);
                        playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
                    }
                }
            }


            if(removed_weapon) {
                dialog.getTextPanel().addPara("Incompatible weapons been removed and added to fleet cargo");
            }

            // We don't need over OP, as we don't lower it but I'll leave it just in case
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

        //Check mounts if swapping, no need to check for direct upgrade as they the same
        if ((upgrade == UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS) && swapped) {
            boolean removed_any = false;

            // Check all smallS but 4 hybrids for energy weapons.
            Collection<String> slots = targetMember.getVariant().getFittedWeaponSlots();
            for(String current_slot : slots) {
                if (!current_slot.equals("WS0045") && !current_slot.equals("WS0046") && !current_slot.equals("WS0047") && !current_slot.equals("WS0048")) {
                    WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec(current_slot);
                    if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && (weapon.getType().equals(WeaponType.ENERGY))) {
                        removed_any = true;
                        targetMember.getVariant().clearSlot(current_slot);
                        playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
                    }
                }
            }

            /* Medium ballistic slot does not support small hybrid weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0011");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && !(weapon.getType().equals(WeaponType.HYBRID))) {
                removed_any = true;
                targetMember.getVariant().clearSlot("WS0011");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            /* Medium ballistic slot does not support energy weapons */
            if ((weapon != null) && (weapon.getSize() == WeaponSize.MEDIUM) && !(weapon.getType().equals(WeaponType.ENERGY))) {
                removed_any = true;
                targetMember.getVariant().clearSlot("WS0011");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Medium ballistic slot does not support small hybrid weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0012");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL) && !(weapon.getType().equals(WeaponType.HYBRID))) {
                removed_any = true;
                targetMember.getVariant().clearSlot("WS0012");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            /* Medium ballistic slot does not support energy weapons */
            if ((weapon != null) && (weapon.getSize() == WeaponSize.MEDIUM) && !(weapon.getType().equals(WeaponType.ENERGY))) {
                removed_any = true;
                targetMember.getVariant().clearSlot("WS0012");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            //Text to inform player to check their refit.
            if(removed_any) {
                dialog.getTextPanel().addPara("Incompatible weapons been removed and added to fleet cargo");
            }
        }

        if ((upgrade == UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE) && swapped) {
            boolean removed_any = false;

            ShipVariantAPI leftModule = targetMember.getVariant().getModuleVariant("WS0001").clone();
            leftModule.setSource(VariantSource.REFIT);
            leftModule.setHullVariantId(Misc.genUID());

            ShipVariantAPI rightModule = targetMember.getVariant().getModuleVariant("WS0002").clone();
            rightModule.setSource(VariantSource.REFIT);
            rightModule.setHullVariantId(Misc.genUID());

            if(!leftModule.getFittedWings().isEmpty()){
                removed_any = true;
                for (String wingId : leftModule.getFittedWings()) {
                    playerFleet.getCargo().addFighters(wingId, 1);
                }
            }

            if(!rightModule.getFittedWings().isEmpty()){
                removed_any = true;
                for (String wingId : rightModule.getFittedWings()) {
                    playerFleet.getCargo().addFighters(wingId, 1);
                }
            }

            if(removed_any) {
                dialog.getTextPanel().addPara("Wings from one or both modules been removed and added to fleet cargo");
            }
        }


        if ((upgrade == UpgradeType.M3_CHOOSE_BALANCED_MODULES) && swapped) {
            boolean removed_any = false;

            ShipVariantAPI leftModule = targetMember.getVariant().getModuleVariant("WS0001").clone();
            leftModule.setSource(VariantSource.REFIT);
            leftModule.setHullVariantId(Misc.genUID());

            if(!leftModule.getFittedWings().isEmpty()){
                removed_any = true;
                for (String wingId : leftModule.getFittedWings()) {
                    playerFleet.getCargo().addFighters(wingId, 1);
                }
            }

            if(removed_any) {
                dialog.getTextPanel().addPara("Wings from left module been removed and added to fleet cargo");
            }
        }


            //hullSpec used to set mounts and track upgrades
        log.log(Priority.INFO, "Tried to set hullspec to: " + hullSpec.getHullId());
        targetMember.getVariant().setHullSpecAPI(hullSpec);

        ShipVariantAPI leftModule = targetMember.getVariant().getModuleVariant("WS0001").clone();
        leftModule.setSource(VariantSource.REFIT);
        leftModule.setHullVariantId(Misc.genUID());

        ShipVariantAPI rightModule = targetMember.getVariant().getModuleVariant("WS0002").clone();
        rightModule.setSource(VariantSource.REFIT);
        rightModule.setHullVariantId(Misc.genUID());

        if (upgrade == UpgradeType.U1_FIXED_ALL_WINGS) {
            targetMember.getVariant().removePermaMod("JHS_RemoveDecks");
            targetMember.getVariant().removeMod("JHS_RemoveDecks");
            targetMember.getVariant().setWingId(0, "borer_wing");
            targetMember.getVariant().setWingId(1, "thunder_wing");
        }
        if (upgrade == UpgradeType.U2_FIXED_DRONE_LAUNCHERS) {
            //targetMember.getVariant().addWeapon("WS0001", "uw_diablo");
            //targetMember.getVariant().addWeapon("WS0001", "uw_diablo");
        }
        if (upgrade == UpgradeType.U3_FIXED_FLEET_GANTRY) {
            targetMember.getVariant().removePermaMod("JHS_FleetGantry_collapsed");
            targetMember.getVariant().removeMod("JHS_FleetGantry_collapsed");

            targetMember.getVariant().addMod("JHS_FleetGantry");
            targetMember.getVariant().addPermaMod("JHS_FleetGantry");
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
            rightModule.removeMod(HullMods.INTEGRATED_TARGETING_UNIT);
            rightModule.addPermaMod(HullMods.ADVANCED_TARGETING_CORE);

            leftModule.removePermaMod(HullMods.INTEGRATED_TARGETING_UNIT);
            leftModule.removeMod(HullMods.INTEGRATED_TARGETING_UNIT);
            leftModule.addPermaMod(HullMods.ADVANCED_TARGETING_CORE);
        }

        if((upgrade == UpgradeType.P1_ADDED_LOGISTICS)){
            targetMember.getVariant().addMod(HullMods.SOLAR_SHIELDING);
            targetMember.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
            targetMember.getVariant().addMod(HullMods.EFFICIENCY_OVERHAUL);
            targetMember.getVariant().addPermaMod(HullMods.EFFICIENCY_OVERHAUL);
        }
        if((upgrade == UpgradeType.P2_ADDED_VOIDSHIP_COMMAND)){
            targetMember.getVariant().addMod(JHS_IDs.VoidshipCommand);
            targetMember.getVariant().addPermaMod(JHS_IDs.VoidshipCommand);

            targetMember.getVariant().removePermaMod(HullMods.OPERATIONS_CENTER);
            targetMember.getVariant().removeMod(HullMods.OPERATIONS_CENTER);
        }
        if (upgrade == UpgradeType.W1_CHOOSE_MODERNISED_WEAPON_SLOTS) {
            if (swapped) {
                targetMember.getVariant().removePermaMod("bdeck");
                targetMember.getVariant().removeMod("bdeck");
            }
            targetMember.getVariant().addMod("JHS_RemoveSomeDecks");
            targetMember.getVariant().addPermaMod("JHS_RemoveSomeDecks");
        }
        if (upgrade == UpgradeType.W2_CHOOSE_MODERNISED_WING_SLOTS) {
            if (swapped) {
                targetMember.getVariant().removePermaMod("JHS_RemoveSomeDecks");
                targetMember.getVariant().removeMod("JHS_RemoveSomeDecks");
            }
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);

            targetMember.getVariant().addPermaMod("bdeck");
        }

        if (upgrade == UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE || upgrade == UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE
            || upgrade == UpgradeType.M3_CHOOSE_BALANCED_MODULES) {

            leftModule.addMod("reduced_explosion");
            leftModule.addPermaMod("reduced_explosion");

            rightModule.addMod("reduced_explosion");
            rightModule.addPermaMod("reduced_explosion");
        }

        if (upgrade == UpgradeType.M1_CHOOSE_CARRIER_LEFT_MODULE) {
            targetMember.getVariant().removePermaMod("JHS_Core_Logistics_Buffed");
            targetMember.getVariant().removeMod("JHS_Core_Logistics_Buffed");

            targetMember.getVariant().addMod("JHS_Core_Logistics_Harmed");
            targetMember.getVariant().addPermaMod("JHS_Core_Logistics_Harmed");

            rightModule.removePermaMod(JHS_IDs.R_Module_RemoveDecks);
            rightModule.removeMod(JHS_IDs.R_Module_RemoveDecks);

            leftModule.addMod(JHS_IDs.L_Module_AddDecks);
            leftModule.addPermaMod(JHS_IDs.L_Module_AddDecks);
            leftModule.removeMod(JHS_IDs.RemoveDecks);
            leftModule.removePermaMod(JHS_IDs.RemoveDecks);
        }
        if (upgrade == UpgradeType.M2_CHOOSE_LOGISTIC_RIGHT_MODULE) {
            targetMember.getVariant().removeMod("JHS_Core_Logistics_Harmed");
            targetMember.getVariant().removePermaMod("JHS_Core_Logistics_Harmed");

            targetMember.getVariant().addMod("JHS_Core_Logistics_Buffed");
            targetMember.getVariant().addPermaMod("JHS_Core_Logistics_Buffed");

            leftModule.removePermaMod(JHS_IDs.L_Module_AddDecks);
            leftModule.removeMod(JHS_IDs.L_Module_AddDecks);
            leftModule.addMod(JHS_IDs.RemoveDecks);
            leftModule.addPermaMod(JHS_IDs.RemoveDecks);

            rightModule.addMod(JHS_IDs.R_Module_RemoveDecks);
            rightModule.addPermaMod(JHS_IDs.R_Module_RemoveDecks);
        }
        if (upgrade == UpgradeType.M3_CHOOSE_BALANCED_MODULES) {
            if (swapped) {
                targetMember.getVariant().removePermaMod("JHS_Core_Logistics_Buffed");
                targetMember.getVariant().removeMod("JHS_Core_Logistics_Buffed");

                targetMember.getVariant().removePermaMod("JHS_Core_Logistics_Harmed");
                targetMember.getVariant().removeMod("JHS_Core_Logistics_Harmed");

                leftModule.removePermaMod(JHS_IDs.L_Module_AddDecks);
                leftModule.removeMod(JHS_IDs.L_Module_AddDecks);
                leftModule.addMod(JHS_IDs.RemoveDecks);
                leftModule.addPermaMod(JHS_IDs.RemoveDecks);

                rightModule.removePermaMod(JHS_IDs.R_Module_RemoveDecks);
                rightModule.removeMod(JHS_IDs.R_Module_RemoveDecks);
            }
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
