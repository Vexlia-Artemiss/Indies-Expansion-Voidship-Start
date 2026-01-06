package Vexlia.JunkHubStart.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.Nex_NGCFinalize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import exerelin.utilities.NexUtils;

import java.util.*;

public class JHS_UltraJunkhubStart extends CustomStart {

    String StartingHullID = "JHS_junkhubship_core_m3";
    String StartingVariantID = "JHS_junkhubship_core_Start";

    protected List<String> ships = new ArrayList<>(Arrays.asList(new String[]{
            StartingVariantID,
            "acs_junkhubship_module_standard"
    }));

    List<String> moduleDmodlist = new ArrayList<>();
    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        ExerelinSetupData.getInstance().freeStart = true;
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);

        FactionAPI faction = Global.getSector().getFaction(Factions.SCAVENGERS);
        ships.add(getShip(faction, ShipRoles.FREIGHTER_MEDIUM));
        ships.add(getShip(faction, ShipRoles.TANKER_SMALL));

        moduleDmodlist.add(HullMods.COMP_STRUCTURE);
        moduleDmodlist.add(HullMods.COMP_HULL);
        moduleDmodlist.add(HullMods.FAULTY_GRID);
        moduleDmodlist.add(HullMods.COMP_ARMOR);
        moduleDmodlist.add(HullMods.GLITCHED_SENSORS);

        moduleDmodlist.add(HullMods.FRAGILE_SUBSYSTEMS);
        moduleDmodlist.add("damaged_mounts");

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);
        Nex_NGCFinalize.addStartingDModScript(memoryMap.get(MemKeys.LOCAL));

        data.addScript(() -> {

            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            Random random = new Random(NexUtils.getStartingSeed());

            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.getHullId().contentEquals(StartingHullID)) {
                    ShipVariantAPI v = member.getVariant().clone();
                    v.setSource(VariantSource.REFIT);
                    v.setHullVariantId(Misc.genUID());
                    member.setVariant(v, false, false);

                    member.getVariant().addPermaMod(HullMods.COMP_STRUCTURE);
                    DModManager.addDMods(member, true, 7, random);

                    for (int i = 0; i < 10; i++) {

                        if (member.getVariant().hasHullMod(HullMods.DEGRADED_DRIVE_FIELD)) {
                            member.getVariant().removePermaMod(HullMods.DEGRADED_DRIVE_FIELD);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.INCREASED_MAINTENANCE)) {
                            member.getVariant().removePermaMod(HullMods.INCREASED_MAINTENANCE);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.ERRATIC_INJECTOR)) {
                            member.getVariant().removePermaMod(HullMods.ERRATIC_INJECTOR);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod("degraded_life_support")) {
                            member.getVariant().removePermaMod("degraded_life_support");
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod("faulty_auto")) {
                            member.getVariant().removePermaMod("faulty_auto");
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_automation")) {
                            member.getVariant().removePermaMod("vayra_damaged_automation");
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_everything")) {
                            member.getVariant().removePermaMod("vayra_damaged_everything");
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_lifesupport")) {
                            member.getVariant().removePermaMod("vayra_damaged_lifesupport");
                            DModManager.addDMods(member, true, 1, random);
                        }
                    }

                    ShipVariantAPI leftModule = member.getVariant().getModuleVariant("WS0001");
                    ShipVariantAPI rightModule = member.getVariant().getModuleVariant("WS0002");

                    if(leftModule != null) {
                        List<String> nonaddedlist = moduleDmodlist;

                        for(int i = 0; i < 4; i++) {
                            int randDMod = new Random().nextInt(nonaddedlist.size());
                            leftModule.addPermaMod(nonaddedlist.get(randDMod));
                            nonaddedlist.remove(randDMod);
                        }
                    }
                    if(rightModule != null) {
                        moduleDmodlist.add(HullMods.DEFECTIVE_MANUFACTORY);
                        moduleDmodlist.add(HullMods.DAMAGED_DECK);

                        List<String> nonaddedlist = moduleDmodlist;
                        for(int i = 0; i < 4; i++) {
                            int randDMod = new Random().nextInt(nonaddedlist.size());
                            rightModule.addPermaMod(nonaddedlist.get(randDMod));
                            nonaddedlist.remove(randDMod);
                        }
                    }

                    Global.getSector().addScript(new EveryFrameScript() {
                        private boolean done = false;

                        @Override
                        public boolean isDone() {
                            return done;
                        }

                        @Override
                        public boolean runWhilePaused() {
                            return true;
                        }

                        @Override
                        public void advance(float amount) {
                            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                                if (member.getHullId().contentEquals(StartingHullID)) {
                                    if (member.getShipName().contentEquals("The Grand Chungus")) {
                                        done = true;
                                        int numDMods = 0;
                                        for (String modId : member.getVariant().getHullMods()) {
                                            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                                            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                                                numDMods++;
                                            }
                                        }
                                        Global.getSector().getMemoryWithoutUpdate().set("$JHS_TDU_member", member.getId()); //To reference in text?
                                        //Global.getSector().getMemoryWithoutUpdate().set("$JHS_hasJunkhub", true); //For potential implementation of it as rare hulk
                                        Global.getSector().getMemoryWithoutUpdate().set("$JHS_RestoreSeed", Misc.genRandomSeed()); //For discount D-mods removal
                                        Global.getSector().getMemoryWithoutUpdate().set("$JYS_core_startingDmods", (long) numDMods); //For discount D-mods removal
                                        break;
                                    }
                                    member.setShipName("The Grand Chungus");
                                }
                            }
                        }
                    });


                } else {
                    ShipVariantAPI v = member.getVariant().clone();
                    v.setSource(VariantSource.REFIT);
                    v.setHullVariantId(Misc.genUID());
                    member.setVariant(v, false, false);

                    DModManager.addDMods(member, true, 3, random);
                    for (int i = 0; i < 5; i++) {
                        if (member.getVariant().hasHullMod(HullMods.DEGRADED_ENGINES)) {
                            member.getVariant().removePermaMod(HullMods.DEGRADED_ENGINES);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.INCREASED_MAINTENANCE)) {
                            member.getVariant().removePermaMod(HullMods.INCREASED_MAINTENANCE);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.ERRATIC_INJECTOR)) {
                            member.getVariant().removePermaMod(HullMods.ERRATIC_INJECTOR);
                            DModManager.addDMods(member, true, 1, random);
                        }
                    }
                }
            }
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        });

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
