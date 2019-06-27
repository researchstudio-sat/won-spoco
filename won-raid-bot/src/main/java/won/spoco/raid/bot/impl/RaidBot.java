package won.spoco.raid.bot.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.spoco.raid.bot.api.RaidFetcher;

public class RaidBot extends EventBot {
    private static final Logger logger = LoggerFactory.getLogger(RaidBot.class);

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        if(!(getBotContextWrapper() instanceof RaidBotContextWrapper)) {
            logger.error("RaidBot does not work without a RaidBotContextWrapper");
            throw new IllegalStateException("RaidBot does not work without a RaidBotContextWrapper");
        }
        RaidFetcher rf = ((RaidBotContextWrapper) getBotContextWrapper()).getRaidFetcher();
        rf.getActiveRaids();

        BotBehaviour messageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        messageCommandBehaviour.activate();
    }
}
