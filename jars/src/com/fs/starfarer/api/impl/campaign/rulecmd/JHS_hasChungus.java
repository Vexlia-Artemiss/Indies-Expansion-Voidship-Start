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

import static Vexlia.JunkHubStart.JHS_IDs.memberFlag;

public class JHS_hasChungus extends BaseCommandPlugin{
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
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant().getHullSpec().getHullId().startsWith(Chungus_hull_id_startwith)) {
                targetMember = member;
                break;
            }
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString(memberFlag);
        FleetMemberAPI remeberedMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(timId)) {
                remeberedMember = member;
                break;
            }
        }

        if(targetMember != remeberedMember)
        {
            return true;
        }

        return false;
    }
}
