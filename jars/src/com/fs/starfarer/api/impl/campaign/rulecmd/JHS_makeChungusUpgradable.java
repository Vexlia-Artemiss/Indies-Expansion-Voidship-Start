package com.fs.starfarer.api.impl.campaign.rulecmd;

import Vexlia.JunkHubStart.JHS_IDs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class JHS_makeChungusUpgradable extends BaseCommandPlugin{
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(JHS_IDs.memberFlag)) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String Chungus_hull_id_startwith = "JHS_junkhubship_core";
        //targetMember is our main hull!!!
        //We use first Big Chungus we can find
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant().getHullSpec().getHullId().startsWith(Chungus_hull_id_startwith)) {
                targetMember = member;
                break;
            }
        }


        Global.getSector().getMemoryWithoutUpdate().set("$JHS_TDU_member", targetMember.getId()); //To reference in text?
        //Global.getSector().getMemoryWithoutUpdate().set("$JHS_hasJunkhub", true); //For potential implementation of it as rare hulk
        Global.getSector().getMemoryWithoutUpdate().set("$JHS_RestoreSeed", Misc.genRandomSeed()); //For discount D-mods removal

        //I don't want to mess with D-mods, so ignore that
        Global.getSector().getMemoryWithoutUpdate().set("$JYS_core_startingDmods", (long) 0); //For discount D-mods removal

        return false;
    }
}
