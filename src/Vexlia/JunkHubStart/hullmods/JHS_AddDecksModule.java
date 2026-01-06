package Vexlia.JunkHubStart.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.*;

//Hidden Hullmod
public class JHS_AddDecksModule extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNumFighterBays().modifyFlat(id, 6f);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        String spriteId = ship.getHullSpec().getBaseHullId() + "_carrier";
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

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }
}
