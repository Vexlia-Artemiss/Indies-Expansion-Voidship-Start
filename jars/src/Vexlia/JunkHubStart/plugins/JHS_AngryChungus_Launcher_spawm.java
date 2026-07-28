package Vexlia.JunkHubStart.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;


public class JHS_AngryChungus_Launcher_spawm extends BaseEveryFrameCombatPlugin {

    private static final Logger log = Logger.getLogger(JHS_AngryChungus_Launcher_spawm.class);
    private CombatEngineAPI engine;
    private final IntervalUtil DUMB = new IntervalUtil(1.5F, 1.5F);
    private final IntervalUtil interval = new IntervalUtil(1.5F, 1.5F);

    private float timer = 0f;       //Timer
    private float duration = 0.05f; //Duration of the effect

    private static final String acs_bigmookShipID = "JHS_angryChungus_Balanced"; //Ship should have no crew requirement or its cr locked to 0
    private static final String acs_bigmookProjectileID = "JHS_angryChungus_shot";

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        List<DamagingProjectileAPI> projectiles_copy = new ArrayList(projectiles);

        Iterator<DamagingProjectileAPI> iter = projectiles_copy.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI projectile = iter.next();

            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            switch (projectile.getProjectileSpecId()) {
                case acs_bigmookProjectileID: {
                    if (projectile.getElapsed() >= 1f) {
                        Vector2f location = new Vector2f(projectile.getLocation());
                        ShipAPI ship = projectile.getSource();
                        float angle = projectile.getFacing();
                        int owner = projectile.getOwner();

                        engine.addSmokeParticle(projectile.getLocation(), new Vector2f(10, 10), 100, 0f, 3, new Color(110, 105, 105, 255));
                        engine.addSmokeParticle(projectile.getLocation(), new Vector2f(10, -10), 100, 0f, 3, new Color(159, 155, 155, 255));
                        engine.addSmokeParticle(projectile.getLocation(), new Vector2f(-10, 10), 100, 0f, 3, new Color(80, 73, 73, 255));
                        engine.addSmokeParticle(projectile.getLocation(), new Vector2f(-10, -10), 100, 0f, 3, new Color(96, 94, 101, 255));

                        engine.removeEntity(projectile);
                        CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                        FleetManager.setSuppressDeploymentMessages(true);
                        FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, acs_bigmookShipID);
                        missileMember.getRepairTracker().setCrashMothballed(false);
                        missileMember.getRepairTracker().setMothballed(false);
                        missileMember.getRepairTracker().setCR(1f);
                        missileMember.setOwner(owner);
                        missileMember.setAlly(ship.isAlly());
                        ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 2.5f);
                        missile.setCollisionClass(CollisionClass.SHIP);
                        missile.getVelocity().set(ship.getVelocity());
                        missile.setAngularVelocity(ship.getAngularVelocity());
                    }
                }
                break;

                default:
            }
        }

    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
