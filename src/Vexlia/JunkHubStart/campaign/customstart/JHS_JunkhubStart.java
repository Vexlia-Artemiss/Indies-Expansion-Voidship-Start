package Vexlia.JunkHubStart.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JHS_JunkhubStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(Arrays.asList(new String[]{
        "acs_junkhubship_module_standard"
    }));

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        ExerelinSetupData.getInstance().freeStart = true;
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);

        FactionAPI faction = Global.getSector().getFaction(Factions.SCAVENGERS);
        ships.add(getShip(faction, ShipRoles.FREIGHTER_MEDIUM));
        ships.add(getShip(faction, ShipRoles.TANKER_SMALL));

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);
        Nex_NGCFinalize.addStartingDModScript(memoryMap.get(MemKeys.LOCAL));

        data.addScript(() -> {

            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            Random random = new Random(NexUtils.getStartingSeed());

            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.getHullId().contentEquals("acs_junkhubship_module")) {
                    ShipVariantAPI v = member.getVariant().clone();
                    v.setSource(VariantSource.REFIT);
                    v.setHullVariantId(Misc.genUID());
                    member.setVariant(v, false, false);

                    member.getVariant().addPermaMod(HullMods.COMP_STRUCTURE);
                    for (int i = 0; i < 10; i++) {
                        DModManager.addDMods(member, true, 5, random);
                        if (member.getVariant().hasHullMod(HullMods.DEGRADED_DRIVE_FIELD)) {
                            member.getVariant().removePermaMod(HullMods.DEGRADED_DRIVE_FIELD);
                        }
                        if (member.getVariant().hasHullMod(HullMods.INCREASED_MAINTENANCE)) {
                            member.getVariant().removePermaMod(HullMods.INCREASED_MAINTENANCE);
                        }
                        if (member.getVariant().hasHullMod(HullMods.ERRATIC_INJECTOR)) {
                            member.getVariant().removePermaMod(HullMods.ERRATIC_INJECTOR);
                        }
                        if (member.getVariant().hasHullMod("degraded_life_support")) {
                            member.getVariant().removePermaMod("degraded_life_support");
                        }
                        if (member.getVariant().hasHullMod("faulty_auto")) {
                            member.getVariant().removePermaMod("faulty_auto");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_automation")) {
                            member.getVariant().removePermaMod("vayra_damaged_automation");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_everything")) {
                            member.getVariant().removePermaMod("vayra_damaged_everything");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_lifesupport")) {
                            member.getVariant().removePermaMod("vayra_damaged_lifesupport");
                        }
                    }
                    member.setShipName("The Junk Machine");
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

            if(fleet.getCargo().getCrew() < fleet.getFleetData().getMinCrew()){
                fleet.getCargo().addCommodity(Commodities.CREW, (fleet.getFleetData().getMinCrew() - fleet.getCargo().getCrew()) * 1.05f);
            }

            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        });

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
