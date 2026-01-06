package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class JHS_VoidshipCommand extends BaseHullMod {

    public static final float RECOVERY_BONUS = 300f;
    public static final float ECM_BONUS = 3f;
    public static final float EW_PENALTY_MULT = 0.25f;
    public static final String MOD_ID = "JHS_VoidshipCommand_mod";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, ECM_BONUS);
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyMult(id, 1-EW_PENALTY_MULT);
    }


    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
        if (manager == null) return;

        DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
        if (member == null) return; // happens in refit screen etc

        boolean apply = ship == engine.getPlayerShip();
        PersonAPI commander = null;
        if (member.getMember() != null) {
            commander = member.getMember().getFleetCommander();
            if (member.getMember().getFleetCommanderForStats() != null) {
                commander = member.getMember().getFleetCommanderForStats();
            }
        }
        apply |= commander != null && ship.getCaptain() == commander;

        if (apply) {
            ship.getMutableStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat(MOD_ID, RECOVERY_BONUS * 0.01f);
        } else {
            ship.getMutableStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).unmodify(MOD_ID);
        }

        ship.ensureClonedStationSlotSpec();
        java.util.List<ShipAPI> children_list = ship.getChildModulesCopy();
        for (ShipAPI child : children_list){
            SyncWings(child, ship);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) RECOVERY_BONUS + "%";
        if (index == 1) return "" + (int) ECM_BONUS + "%";
        if (index == 2) return "" + (int) (EW_PENALTY_MULT*100) + "%";
        return null;
    }

    // Based on S&W pack code
    // Mirror parent's fighter commands
    void SyncWings(ShipAPI child, ShipAPI parent){

        if (child.hasLaunchBays()) {
            if (parent.getAllWings().isEmpty() && (Global.getCombatEngine().getPlayerShip() != parent || !Global.getCombatEngine().isUIAutopilotOn()))
                parent.setPullBackFighters(false); // otherwise module fighters will only defend if AI parent has no bays
            if (child.isPullBackFighters() ^ parent.isPullBackFighters()) {
                child.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            }
            if (child.getAIFlags() != null) {
                if (((Global.getCombatEngine().getPlayerShip() == parent) || (parent.getAIFlags() == null))
                        && (parent.getShipTarget() != null)) {
                    child.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getShipTarget());
                } else if ((parent.getAIFlags() != null)
                        && parent.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET)
                        && (parent.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
                    child.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET));
                } else if (parent.getShipTarget() != null){
                    child.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getShipTarget());
                }
            }
        }
    }

}
