package Vexlia.JunkHubStart;

import com.fs.starfarer.api.BaseModPlugin;

import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.impl.codex.CodexEntryPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JunkHubStart_ModPlugin extends BaseModPlugin {

    boolean serious_mode = false;

    @Override
    public void onAboutToLinkCodexEntries() {
        super.onAboutToLinkCodexEntries();

        JHS_CodexLinks();
    }

    private void JHS_CodexLinks(){
        createReciprocalLink(CodexDataV2.getShipEntryId("JHS_angryChungus"), CodexDataV2.getWeaponEntryId("JHS_angryChungus_launcher"));
        createReciprocalLink(CodexDataV2.getShipEntryId("JHS_smallChungus"), CodexDataV2.getWeaponEntryId("JHS_smallChungus_launcher"));
    }

    private static void createReciprocalLink(String entryIdOne, String entryIdTwo) {
        CodexEntryPlugin entryOne = CodexDataV2.getEntry(entryIdOne);
        CodexEntryPlugin entryTwo = CodexDataV2.getEntry(entryIdTwo);

        entryOne.addRelatedEntry(entryTwo);
        entryTwo.addRelatedEntry(entryOne);
    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
    }


    @Override
    public void onGameLoad(boolean newGame) {

    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        // Add your code here, or delete this method (it does nothing unless you add code)
    }


    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
}
