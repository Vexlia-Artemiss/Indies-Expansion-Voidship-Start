package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class JHS_L_Module_CarrierConvert extends BaseHullMod
{
    //Goes on Core and affects logistic and combat stats

    //Reduces fuel capacity slightly
    //Reduced cargo space severely
    //increases DP by few points

    //WIP

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        /*
        if (ship.getHullSpec().getBaseHullId().equals("apex_spectrum") || ship.getHullSpec().getBaseHullId().equals("apex_backscatter")) {
            String spriteId = ship.getHullSpec().getBaseHullId() + "_fuel";
            SpriteAPI sprite = Global.getSettings().getSprite(ship.getHullSpec().getBaseHullId(), spriteId, false);
            if (sprite != null) {
                float x = ship.getSpriteAPI().getCenterX();
                float y = ship.getSpriteAPI().getCenterY();
                float alpha = ship.getSpriteAPI().getAlphaMult();
                float angle = ship.getSpriteAPI().getAngle();
                Color color = ship.getSpriteAPI().getColor();
                ship.setSprite(ship.getHullSpec().getBaseHullId(), spriteId);
                ship.getSpriteAPI().setCenter(x, y);
                ship.getSpriteAPI().setAlphaMult(alpha);
                ship.getSpriteAPI().setAngle(angle);
                ship.getSpriteAPI().setColor(color);
            }

        }

        */

    }
}
