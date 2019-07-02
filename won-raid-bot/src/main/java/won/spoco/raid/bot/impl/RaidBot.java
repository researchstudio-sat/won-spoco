package won.spoco.raid.bot.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.trigger.ActionOnTriggerEventListener;
import won.bot.framework.eventbot.action.impl.trigger.BotTrigger;
import won.bot.framework.eventbot.action.impl.trigger.BotTriggerEvent;
import won.bot.framework.eventbot.action.impl.trigger.StartBotTriggerCommandEvent;
import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.open.OpenCommandEvent;
import won.bot.framework.eventbot.event.impl.command.open.OpenCommandResultEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.protocol.model.Connection;
import won.spoco.raid.bot.action.CreateRaidAtomAction;
import won.spoco.raid.bot.action.DeleteRaidAtomAction;
import won.spoco.raid.bot.api.RaidFetcher;
import won.spoco.raid.bot.event.CreateRaidAtomEvent;
import won.spoco.raid.bot.event.DeleteRaidAtomEvent;
import won.spoco.raid.bot.event.EditRaidAtomEvent;
import won.spoco.raid.bot.model.Raid;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class RaidBot extends EventBot {
    private static final Logger logger = LoggerFactory.getLogger(RaidBot.class);

    @Autowired


    @Value("${raidbot.fetchInterval}")
    private int raidFetchInterval; //in seconds
    @Value("${raidbot.sanitizeInterval}")
    private int raidSanitizeInterval; //in seconds
    @Value("${raidbot.expirationThreshold}")
    private int raidExpirationThreshold; //in seconds
    @Value("${raidbot.phaseOut}")
    private boolean phaseOut;

    @Override
    protected void initializeEventListeners() {

        EventListenerContext ctx = getEventListenerContext();
        if (!(getBotContextWrapper() instanceof RaidBotContextWrapper)) {
            logger.error(getBotContextWrapper().getBotName() + " does not work without a RaidBotContextWrapper");
            throw new IllegalStateException(getBotContextWrapper().getBotName() + " does not work without a RaidBotContextWrapper");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) getBotContextWrapper();
        EventBus bus = ctx.getEventBus();

        BotBehaviour messageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        messageCommandBehaviour.activate();

        if(!phaseOut) {
            BotTrigger fetchRaidsTrigger = new BotTrigger(ctx, Duration.ofSeconds(raidFetchInterval));
            fetchRaidsTrigger.activate();

            bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, fetchRaidsTrigger,
                    new BaseEventBotAction(ctx) {
                        @Override
                        protected void doRun(Event event, EventListener executingListener) throws Exception {
                            logger.debug("Fetching Raids ------------------------------------------------------------------");
                            RaidFetcher rf = botContextWrapper.getRaidFetcher();
                            List<Raid> activeRaids = rf.getActiveRaids();

                            for (Raid activeRaid : activeRaids) {
                                if (botContextWrapper.raidExists(activeRaid)) {
                                    Raid storedRaid = botContextWrapper.getRaid(activeRaid);
                                    if (storedRaid.hasUpdatedInformation(activeRaid)) {
                                        logger.debug(activeRaid.getId() + ": Raid exists: (" + botContextWrapper.getAtomUriForRaid(storedRaid) + "): Information has changed: New Information: " + activeRaid + " / Old Information: " + storedRaid);
                                        bus.publish(new EditRaidAtomEvent(activeRaid));
                                    } else {
                                        logger.debug(activeRaid.getId() + ": Raid exists: (" + botContextWrapper.getAtomUriForRaid(storedRaid) + "): Information has not changed: " + activeRaid);
                                    }
                                } else {
                                    logger.debug(activeRaid.getId() + ": Raid is new, storing information: " + activeRaid);
                                    bus.publish(new CreateRaidAtomEvent(activeRaid));
                                }
                            }
                            logger.debug("---------------------------------------------------------------------------------");
                            logger.debug("  ");
                        }
                    }));
            bus.publish(new StartBotTriggerCommandEvent(fetchRaidsTrigger));
        } else {
            logger.info(botContextWrapper.getBotName() + " is in PhaseOut mode, no new Raids will be fetched, only existing Raids will be sanitized");
        }

        BotTrigger sanitizeRaidsTrigger = new BotTrigger(ctx, Duration.ofSeconds(raidSanitizeInterval));
        sanitizeRaidsTrigger.activate();

        bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, sanitizeRaidsTrigger,
                new BaseEventBotAction(ctx) {
                    @Override
                    protected void doRun(Event event, EventListener executingListener) throws Exception {
                        logger.debug("Sanitizing Raids ----------------------------------------------------------------");
                        Collection<Raid> storedRaids = botContextWrapper.getAllRaids();

                        if(storedRaids.size() > 0) {
                            long expirationThreshold = System.currentTimeMillis() + raidExpirationThreshold * 1000;
                            for (Raid storedRaid : storedRaids) {
                                if (storedRaid.isExpired(expirationThreshold)) {
                                    logger.debug(storedRaid.getId() + ": Raid is expired, proceed to remove: " + "("+botContextWrapper.getAtomUriForRaid(storedRaid)+") :" + storedRaid);
                                    bus.publish(new DeleteRaidAtomEvent(storedRaid));
                                } else {
                                    logger.debug(storedRaid.getId() + ": Raid is still active, do not remove: " + "("+botContextWrapper.getAtomUriForRaid(storedRaid)+") :"  + storedRaid);
                                }
                            }
                        } else {
                            if(phaseOut) {
                                logger.info(botContextWrapper.getBotName() + " is in PhaseOut mode and has been cleared, you may shut down the bot now");
                            } else {
                                logger.info("no Raids stored");
                            }
                        }
                        logger.debug("---------------------------------------------------------------------------------");
                        logger.debug("  ");
                    }
                }));

        bus.publish(new StartBotTriggerCommandEvent(sanitizeRaidsTrigger));

        bus.subscribe(CreateRaidAtomEvent.class, new ActionOnEventListener(ctx, new CreateRaidAtomAction(ctx)));
        bus.subscribe(DeleteRaidAtomEvent.class, new ActionOnEventListener(ctx, new DeleteRaidAtomAction(ctx)));

        bus.subscribe(EditRaidAtomEvent.class, new ActionOnEventListener(ctx, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                //TODO: UPDATE THE RAID-ATOM
            }
        }));

        bus.subscribe(ConnectFromOtherAtomEvent.class, new ActionOnEventListener(ctx, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                EventListenerContext ctx = getEventListenerContext();
                if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper) || !(event instanceof ConnectFromOtherAtomEvent)) {
                    logger.error(ctx.getBotContextWrapper().getBotName() + ": ConnectFromOtherAtomEvent does not work without a RaidBotContextWrapper and ConnectFromOtherAtomEvent");
                    throw new IllegalStateException(ctx.getBotContextWrapper().getBotName() + ": ConnectFromOtherAtomEvent does not work without a RaidBotContextWrapper and ConnectFromOtherAtomEvent");
                }

                Connection con = ((ConnectFromOtherAtomEvent) event).getCon();
                try {
                    String message = "Welcome to the Raid GroupChat!";
                    final OpenCommandEvent openCommandEvent = new OpenCommandEvent(con, message);
                    ctx.getEventBus().subscribe(OpenCommandResultEvent.class, new ActionOnFirstEventListener(ctx,
                            new CommandResultFilter(openCommandEvent), new BaseEventBotAction(ctx) {
                        @Override
                        protected void doRun(Event event, EventListener executingListener) throws Exception {
                            OpenCommandResultEvent connectionMessageCommandResultEvent = (OpenCommandResultEvent) event;
                            if (!connectionMessageCommandResultEvent.isSuccess()) {
                                logger.error("Failure when trying to open a received Request: " + connectionMessageCommandResultEvent.getMessage());
                            }
                        }
                    }));
                    ctx.getEventBus().publish(openCommandEvent);
                } catch (Exception te) {
                    logger.error(te.getMessage(), te);
                }
            }
        }));
    }
}
