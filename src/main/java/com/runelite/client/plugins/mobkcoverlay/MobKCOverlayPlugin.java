package net.runelite.client.plugins.mobkcoverlay;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Mob KC Overlay",
        description = "Tracks kill count for a chosen NPC and shows it in an overlay.",
        tags = {"mob", "kc", "killcount", "overlay", "npc"}
)
public class MobKCOverlayPlugin extends Plugin
{
    private static final String CONFIG_GROUP = "mobkcoverlay";
    private static final String CONFIG_TRACKED_NPC_NAME = "trackedNpcName";
    private static final String CONFIG_MANUAL_ADD_KC = "manualAddKc";
    private static final String MENU_OPTION_SHOW_KC = "Show KC";

    @Inject
    private Client client;

    @Inject
    private MobKCOverlayConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MobKCOverlayOverlay overlay;

    @Inject
    private ConfigManager configManager;

    // Internal state
    private String currentNpcName;
    private int currentKillCount;
    private Instant lastKillTime;

    // NPCs that are (or were) actually linked to you (you attacked them or they attacked you)
    private final Set<Integer> myTargets = new HashSet<>();

    @Provides
    MobKCOverlayConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(MobKCOverlayConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        myTargets.clear();
        loadTrackedNpcFromConfig();
    }

    @Override
    protected void shutDown() throws Exception
    {
        saveCurrentNpcKc();
        overlayManager.remove(overlay);
        myTargets.clear();
        currentNpcName = null;
        currentKillCount = 0;
        lastKillTime = null;
    }

    // -----------------------
    //  CONFIG / MEMORY LOGIC
    // -----------------------

    private void loadTrackedNpcFromConfig()
    {
        String cfgName = config.trackedNpcName();
        if (cfgName != null)
        {
            cfgName = cfgName.trim();
        }

        if (cfgName == null || cfgName.isEmpty())
        {
            currentNpcName = null;
            currentKillCount = 0;
            lastKillTime = null;
            return;
        }

        switchTrackedNpc(cfgName);
    }

    private void switchTrackedNpc(String newName)
    {
        if (newName == null)
        {
            return;
        }

        newName = newName.trim();
        if (newName.isEmpty())
        {
            return;
        }

        // Save KC for the previous NPC
        saveCurrentNpcKc();

        // Load KC for the new NPC
        currentNpcName = newName;
        currentKillCount = loadKillCountFor(newName);
        lastKillTime = null;

        // Update config so the text field shows the current mob
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_TRACKED_NPC_NAME, newName);
    }

    private void saveCurrentNpcKc()
    {
        if (currentNpcName == null || currentNpcName.isEmpty())
        {
            return;
        }
        saveKillCountFor(currentNpcName, currentKillCount);
    }

    private String makeKcKey(String npcName)
    {
        // Simple key: lowercase, non-alphanumeric -> underscore
        String base = npcName.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        return "kc_" + base;
    }

    private int loadKillCountFor(String npcName)
    {
        String key = makeKcKey(npcName);
        Integer val = configManager.getConfiguration(CONFIG_GROUP, key, Integer.class);
        return val == null ? 0 : val;
    }

    private void saveKillCountFor(String npcName, int kc)
    {
        String key = makeKcKey(npcName);
        configManager.setConfiguration(CONFIG_GROUP, key, kc);
    }

    // Detect config text change + manual KC adjust
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        String cfgName = config.trackedNpcName();
        if (cfgName != null)
        {
            cfgName = cfgName.trim();
        }

        // If config is cleared, save and clear state
        if ((cfgName == null || cfgName.isEmpty()) &&
                currentNpcName != null && !currentNpcName.isEmpty())
        {
            saveCurrentNpcKc();
            currentNpcName = null;
            currentKillCount = 0;
            lastKillTime = null;
        }
        // If config name changed, switch tracked NPC
        else if (cfgName != null && !cfgName.isEmpty() &&
                (currentNpcName == null || !cfgName.equals(currentNpcName)))
        {
            switchTrackedNpc(cfgName);
        }

        // Manual KC adjustment from config
        int manualAdd = config.manualAddKc();
        if (manualAdd != 0 && currentNpcName != null && !currentNpcName.isEmpty())
        {
            currentKillCount += manualAdd;
            if (currentKillCount < 0)
            {
                currentKillCount = 0;
            }

            // Save immediately so it's persisted
            saveCurrentNpcKc();

            // Reset the field so it doesn't keep adding each tick
            configManager.setConfiguration(CONFIG_GROUP, CONFIG_MANUAL_ADD_KC, 0);
        }
    }

    // -----------------------
    //  RIGHT-CLICK MENU
    // -----------------------

    private boolean isNpcMenuAction(int type)
    {
        return type == MenuAction.NPC_FIRST_OPTION.getId()
                || type == MenuAction.NPC_SECOND_OPTION.getId()
                || type == MenuAction.NPC_THIRD_OPTION.getId()
                || type == MenuAction.NPC_FOURTH_OPTION.getId()
                || type == MenuAction.NPC_FIFTH_OPTION.getId()
                || type == MenuAction.EXAMINE_NPC.getId();
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!isNpcMenuAction(event.getType()))
        {
            return;
        }

        // Avoid adding duplicate "Show KC" entries for the same target
        MenuEntry[] entries = client.getMenuEntries();
        for (MenuEntry e : entries)
        {
            if (MENU_OPTION_SHOW_KC.equals(e.getOption()) && event.getTarget().equals(e.getTarget()))
            {
                return;
            }
        }

        client.createMenuEntry(-1)
                .setOption(MENU_OPTION_SHOW_KC)
                .setTarget(event.getTarget())
                .setType(MenuAction.RUNELITE)
                .setIdentifier(event.getIdentifier())
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1());
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!event.getMenuOption().equals(MENU_OPTION_SHOW_KC)
                || event.getMenuAction() != MenuAction.RUNELITE)
        {
            return;
        }

        String rawTarget = event.getMenuTarget();
        String cleaned = cleanNpcNameFromTarget(rawTarget);
        if (cleaned == null || cleaned.isEmpty())
        {
            return;
        }

        switchTrackedNpc(cleaned);
    }

    private String cleanNpcNameFromTarget(String target)
    {
        if (target == null)
        {
            return null;
        }

        // Remove color tags etc.
        String noTags = Text.removeTags(target);

        // Strip off level info in parentheses, e.g. "Bloodveld (level-113)"
        int parenIdx = noTags.indexOf('(');
        if (parenIdx != -1)
        {
            noTags = noTags.substring(0, parenIdx);
        }

        return noTags.trim();
    }

    private String cleanNpcNameFromName(String name)
    {
        if (name == null)
        {
            return null;
        }

        // Some NPC names can also include tags/extra info
        return cleanNpcNameFromTarget(name);
    }

    // -----------------------
    //  KILL TRACKING + AUTO SWITCH
    // -----------------------

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

        Actor source = event.getSource();
        Actor target = event.getTarget();

        // YOU start interacting with an NPC (you attack it / talk to it / etc)
        if (source == client.getLocalPlayer() && target instanceof NPC)
        {
            NPC npc = (NPC) target;
            myTargets.add(npc.getIndex());

            // Auto-switch tracking if enabled
            if (config.automaticTracking())
            {
                String cleaned = cleanNpcNameFromName(npc.getName());
                if (cleaned != null && !cleaned.isEmpty())
                {
                    switchTrackedNpc(cleaned);
                }
            }
            return;
        }

        // An NPC starts interacting with YOU (it attacks you)
        if (source instanceof NPC && target == client.getLocalPlayer())
        {
            NPC npc = (NPC) source;
            myTargets.add(npc.getIndex());
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        if (!isTracking())
        {
            return;
        }

        NPC npc = event.getNpc();
        if (npc == null || npc.getName() == null)
        {
            return;
        }

        // Only count if this NPC was actually engaged with you
        if (!myTargets.contains(npc.getIndex()))
        {
            return;
        }

        String npcNameLower = npc.getName().toLowerCase();
        String trackedLower = currentNpcName.toLowerCase();

        if (npcNameLower.contains(trackedLower))
        {
            currentKillCount++;
            lastKillTime = Instant.now();
        }

        // Once it despawns, stop tracking it
        myTargets.remove(npc.getIndex());
    }

    // ===== Methods used by the overlay =====

    public boolean isTracking()
    {
        return currentNpcName != null && !currentNpcName.isEmpty();
    }

    public String getCurrentNpcName()
    {
        return currentNpcName;
    }

    public int getCurrentKillCount()
    {
        return currentKillCount;
    }

    public Instant getLastKillTime()
    {
        return lastKillTime;
    }
}
