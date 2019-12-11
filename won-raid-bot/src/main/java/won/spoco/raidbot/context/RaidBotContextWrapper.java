package won.spoco.raidbot.context;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.bot.context.BotContextWrapper;
import won.bot.framework.extensions.serviceatom.ServiceAtomContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomEnabledBotContextWrapper;
import won.spoco.raidbot.api.RaidFetcher;
import won.spoco.raidbot.api.model.Raid;
import won.spoco.raidbot.impl.model.ContextRaid;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaidBotContextWrapper extends ServiceAtomEnabledBotContextWrapper implements ServiceAtomContext {
    private final List<RaidFetcher> raidFetcherList;

    private final String raidIdToRaidMap;
    private final String raidIdToAtomUriMap;

    public RaidBotContextWrapper(BotContext botContext, String botName, List<RaidFetcher> raidFetcherList) {
        super(botContext, botName);
        this.raidFetcherList = raidFetcherList;
        this.raidIdToRaidMap = botName + ":raidIdToRaidMap";
        this.raidIdToAtomUriMap = botName + "raidIdToAtomUriMap";
    }

    public List<RaidFetcher> getRaidFetcherList() {
        return raidFetcherList;
    }

    public boolean raidExists(Raid raid) {
        return raidExists(raid.getId());
    }

    public boolean raidExists(String id) {
        return getRaid(id) != null;
    }

    public ContextRaid getRaid(Raid raid) {
        return getRaid(raid.getId());
    }

    public ContextRaid getRaid(String id) {
        return (ContextRaid) this.getBotContext().loadFromObjectMap(raidIdToRaidMap, id);
    }

    public void addRaid(ContextRaid contextRaid, URI atomURI) {
        this.getBotContext().saveToObjectMap(raidIdToAtomUriMap, contextRaid.getId(), atomURI);
        this.getBotContext().saveToObjectMap(raidIdToRaidMap, contextRaid.getId(), contextRaid);
    }

    public void removeRaid(ContextRaid contextRaid) {
        removeRaid(contextRaid.getId());
    }

    public void removeRaid(String raidId) {
        this.getBotContext().removeFromObjectMap(raidIdToRaidMap, raidId);
        this.getBotContext().removeFromObjectMap(raidIdToAtomUriMap, raidId);
    }

    public Map<String, ContextRaid> getAllRaidsMap() {
        Map<String, Object> raidObjectMap = this.getBotContext().loadObjectMap(raidIdToRaidMap);
        Map<String, ContextRaid> raidMap = new HashMap<>(raidObjectMap.size());

        for (Object raidObject : raidObjectMap.values()) {
            ContextRaid raid = (ContextRaid) raidObject;
            raidMap.put(raid.getId(), raid);
        }

        return raidMap;
    }

    public Collection<ContextRaid> getAllRaids() {
        return getAllRaidsMap().values();
    }

    public URI getAtomUriForRaid(ContextRaid contextRaid) {
        return getAtomUriForRaid(contextRaid.getId());
    }

    public URI getAtomUriForRaid(String raidId) {
        return (URI) getBotContext().loadFromObjectMap(raidIdToAtomUriMap, raidId);
    }
}