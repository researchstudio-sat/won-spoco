package won.spoco.raid.bot.impl;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.bot.context.BotContextWrapper;
import won.spoco.raid.bot.api.RaidFetcher;

public class RaidBotContextWrapper extends BotContextWrapper {
    private RaidFetcher raidFetcher;

    public RaidBotContextWrapper(BotContext botContext, String botName, RaidFetcher raidFetcher) {
        super(botContext, botName);
        this.raidFetcher = raidFetcher;
    }

    public RaidFetcher getRaidFetcher(){
        return raidFetcher;
    }
}
