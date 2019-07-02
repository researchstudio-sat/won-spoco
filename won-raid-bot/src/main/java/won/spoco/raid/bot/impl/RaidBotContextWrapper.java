package won.spoco.raid.bot.impl;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.bot.context.BotContextWrapper;
import won.spoco.raid.bot.api.RaidFetcher;
import won.spoco.raid.bot.model.Raid;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RaidBotContextWrapper extends BotContextWrapper {
    private RaidFetcher raidFetcher;
    public String raidIdToRaidMap = getBotName() + ":raidIdToRaidMap";
    public String raidIdToAtomUriMap = getBotName() + ":raidIdToAtomUriMap";

    public RaidBotContextWrapper(BotContext botContext, String botName, RaidFetcher raidFetcher) {
        super(botContext, botName);
        this.raidFetcher = raidFetcher;
    }

    public RaidFetcher getRaidFetcher(){
        return raidFetcher;
    }

    public boolean raidExists(Raid raid) {
        return raidExists(raid.getId());
    }

    public boolean raidExists(String id) {
        return getRaid(id) != null;
    }

    public Raid getRaid(Raid raid) {
        return getRaid(raid.getId());
    }

    public Raid getRaid(String id) {
        return (Raid) this.getBotContext().loadFromObjectMap(raidIdToRaidMap, id);
    }

    public void addRaid(Raid raid, URI atomURI) {
        this.getBotContext().saveToObjectMap(raidIdToAtomUriMap, raid.getId(), atomURI);
        this.getBotContext().saveToObjectMap(raidIdToRaidMap, raid.getId(), raid);
    }

    public void removeRaid(Raid raid) {
        removeRaid(raid.getId());
    }

    public void removeRaid(String raidId) {
        this.getBotContext().removeFromObjectMap(raidIdToRaidMap, raidId);
        this.getBotContext().removeFromObjectMap(raidIdToAtomUriMap, raidId);
    }

    public Map<String, Raid> getAllRaidsMap() {
        Map<String, Object> raidObjectMap = this.getBotContext().loadObjectMap(raidIdToRaidMap);
        Map<String, Raid> raidMap = new HashMap<>(raidObjectMap.size());

        for (Object raidObject : raidObjectMap.values()) {
            Raid raid = (Raid) raidObject;
            raidMap.put(raid.getId(), raid);
        }

        return raidMap;
    }

    public Collection<Raid> getAllRaids() {
        return getAllRaidsMap().values();
    }


    public URI getAtomUriForRaid(Raid raid) {
        return getAtomUriForRaid(raid.getId());
    }

    public URI getAtomUriForRaid(String raidId) {
        return (URI) getBotContext().loadFromObjectMap(raidIdToAtomUriMap, raidId);
    }
}
