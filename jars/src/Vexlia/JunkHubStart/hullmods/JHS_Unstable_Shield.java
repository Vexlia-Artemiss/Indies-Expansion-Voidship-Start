package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Random;

public class JHS_Unstable_Shield extends BaseHullMod {

    public static float SHIELD_BONUS_UNFOLD = 100f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);
    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
    }

    float rand;
    boolean shieldOn;
    boolean runOnce;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        rand -= amount;

        if(rand <= 0) {
            shieldOn = new Random().nextBoolean();

            rand = new Random().nextFloat(2, 10);
            runOnce = true;
        }

        if(shieldOn && runOnce){
            float randS = new Random().nextFloat(1, 5);
            rand += randS;

            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, randS);
            ship.getAIFlags().removeFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS);
            if(ship.getShield().isOff()){
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }

            runOnce = false;
        }

        if(!shieldOn && runOnce){
            float randS = new Random().nextFloat(1, 3);
            rand += randS;
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, randS);
            ship.getAIFlags().removeFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);

            if(ship.getShield().isOn()){
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }

            runOnce = false;
        }

    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
        final Color green = new Color(55,245,65,255);
        final Color yellow = new Color(255, 240, 0,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        final float height = 50f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading("Effects", Alignment.MID, pad);

        tooltip.addPara("Shield power supply fluctuates unpredictably resulting in every 2-10 seconds shield being randomly forced to turn on or off.", padList, Misc.getHighlightColor(),"2-10","randomly", "turn on or off");
        tooltip.addPara("If shield been forced to turn on, it can't be lowered for 1-5 seconds", padList,  Misc.getHighlightColor(),"turn on", "2-6");
        tooltip.addPara("If shield been forced to turn off, it can't be raised for 1-3 seconds", padList, Misc.getHighlightColor(),"turn off", "2-4");
        String highli = "+ (int) SHIELD_BONUS_UNFOLD + %%.";
        tooltip.addPara( "Unstable nature of power supply surprisingly efficient for raising shield, increasing shield raising speed by %s", padList, Misc.getHighlightColor(),""+ (int) SHIELD_BONUS_UNFOLD + "%");
    }
}

