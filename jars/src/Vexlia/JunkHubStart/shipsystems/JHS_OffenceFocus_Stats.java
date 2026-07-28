package Vexlia.JunkHubStart.shipsystems;

import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public class JHS_OffenceFocus_Stats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 80f;
    public static final float FLUX_REDUCTION = 40f;

    public static final float DAMAGE_BONUS_PERCENT = 30f;

    public static final float MAX_SPEED_REDUCTION = 10f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        float mult = 1f + (ROF_BONUS * 0.01f) * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, mult);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));

        float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
        stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);

        float speedPercent = MAX_SPEED_REDUCTION * effectLevel;
        stats.getMaxSpeed().modifyFlat(id, -speedPercent);
//		ShipAPI ship = (ShipAPI)stats.getEntity();
//		ship.blockCommandForOneFrame(ShipCommand.FIRE);
//		ship.setHoldFireOneFrame(true);
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);

        stats.getEnergyWeaponDamageMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = ROF_BONUS * effectLevel;

        float Energy_bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;

        float speedPercent = MAX_SPEED_REDUCTION * effectLevel;
        if (index == 0) {
            return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
        }
        if (index == 1) {
            return new StatusData("ballistic flux use -" + (int) FLUX_REDUCTION + "%", false);
        }
        if (index == 3) {
            return new StatusData("+" + (int) Energy_bonusPercent + "% energy weapon damage" , false);
        }
        if (index == 4) {
            return new StatusData("-" + (int) speedPercent + " to ship's max speed" , false);
        }

        return null;
    }
}
