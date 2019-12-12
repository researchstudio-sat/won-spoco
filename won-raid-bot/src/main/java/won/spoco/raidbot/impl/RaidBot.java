package won.spoco.raidbot.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandResultEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.bot.framework.extensions.serviceatom.ServiceAtomBehaviour;
import won.bot.framework.extensions.serviceatom.ServiceAtomExtension;
import won.protocol.model.Connection;
import won.spoco.raidbot.action.CreateRaidAtomAction;
import won.spoco.raidbot.action.DeleteRaidAtomAction;
import won.spoco.raidbot.action.ModifyRaidAtomAction;
import won.spoco.raidbot.api.RaidFetcher;
import won.spoco.raidbot.api.model.Raid;
import won.spoco.raidbot.context.RaidBotContextWrapper;
import won.spoco.raidbot.event.CreateRaidAtomEvent;
import won.spoco.raidbot.event.DeleteRaidAtomEvent;
import won.spoco.raidbot.event.ModifyRaidAtomEvent;
import won.spoco.raidbot.impl.model.ContextRaid;

public class RaidBot extends EventBot implements ServiceAtomExtension {
    private static final Logger logger = LoggerFactory.getLogger(RaidBot.class);

    private ServiceAtomBehaviour serviceAtomBehaviour;

    @Value("${raidbot.fetchInterval}")
    private int raidFetchInterval; // in seconds
    @Value("${raidbot.sanitizeInterval}")
    private int raidSanitizeInterval; // in seconds
    @Value("${raidbot.expirationThreshold}")
    private int raidExpirationThreshold; // in seconds
    @Value("${raidbot.phaseOut}")
    private boolean phaseOut;

    @Override
    public ServiceAtomBehaviour getServiceAtomBehaviour() {
        return this.serviceAtomBehaviour;
    }

    @Override
    protected void initializeEventListeners() {

        EventListenerContext ctx = getEventListenerContext();
        if (!(getBotContextWrapper() instanceof RaidBotContextWrapper)) {
            logger.error(getBotContextWrapper().getBotName() + " does not work without a RaidBotContextWrapper");
            throw new IllegalStateException(
                    getBotContextWrapper().getBotName() + " does not work without a RaidBotContextWrapper");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) getBotContextWrapper();
        EventBus bus = ctx.getEventBus();

        BotBehaviour messageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        messageCommandBehaviour.activate();

        serviceAtomBehaviour = new ServiceAtomBehaviour(ctx);
        serviceAtomBehaviour.activate();

        if (!phaseOut) {
            BotTrigger fetchRaidsTrigger = new BotTrigger(ctx, Duration.ofSeconds(raidFetchInterval));
            fetchRaidsTrigger.activate();

            bus.subscribe(BotTriggerEvent.class,
                    new ActionOnTriggerEventListener(ctx, fetchRaidsTrigger, new BaseEventBotAction(ctx) {
                        @Override
                        protected void doRun(Event event, EventListener executingListener) {
                            List<RaidFetcher> raidFetcherList = botContextWrapper.getRaidFetcherList();
                            for (RaidFetcher rf : raidFetcherList) {

                                logger.debug("Fetching Raids: Fetcher - {}", rf.toString());
                                List<Raid> activeRaids = rf.getActiveRaids();

                                for (Raid activeRaid : activeRaids) {
                                    if (botContextWrapper.raidExists(activeRaid)) {
                                        ContextRaid contextRaid = botContextWrapper.getRaid(activeRaid);
                                        if (contextRaid.hasUpdatedInformation(activeRaid)) {
                                            logger.debug("Fetching Raids: " + activeRaid.getId() + ": Raid exists: ("
                                                    + botContextWrapper.getAtomUriForRaid(contextRaid)
                                                    + "): Information has changed: New Information: " + activeRaid
                                                    + " / Old Information: " + contextRaid);
                                            bus.publish(new ModifyRaidAtomEvent(activeRaid.buildContextRaid()));
                                        } else {
                                            logger.trace("Fetching Raids: " + activeRaid.getId() + ": Raid exists: ("
                                                    + botContextWrapper.getAtomUriForRaid(contextRaid)
                                                    + "): Information has not changed: " + activeRaid);
                                        }
                                    } else {
                                        logger.debug("Fetching Raids: " + activeRaid.getId()
                                                + ": Raid is new, storing information: " + activeRaid);
                                        bus.publish(new CreateRaidAtomEvent(activeRaid.buildContextRaid()));
                                    }
                                }
                            }
                        }
                    }));
            bus.publish(new StartBotTriggerCommandEvent(fetchRaidsTrigger));
        } else {
            logger.info(botContextWrapper.getBotName()
                    + " is in PhaseOut mode, no new Raids will be fetched, only existing Raids will be sanitized");
        }

        BotTrigger sanitizeRaidsTrigger = new BotTrigger(ctx, Duration.ofSeconds(raidSanitizeInterval));
        sanitizeRaidsTrigger.activate();

        bus.subscribe(BotTriggerEvent.class,
                new ActionOnTriggerEventListener(ctx, sanitizeRaidsTrigger, new BaseEventBotAction(ctx) {

                    @Override
                    protected void doRun(Event event, EventListener executingListener) {
                        Collection<ContextRaid> storedRaids = botContextWrapper.getAllRaids();

                        if (storedRaids.size() > 0) {
                            long expirationThreshold = System.currentTimeMillis() + raidExpirationThreshold * 1000;
                            for (ContextRaid contextRaid : storedRaids) {
                                if (contextRaid.isExpired(expirationThreshold)) {
                                    logger.debug("Sanitizing Raids: " + contextRaid.getId()
                                            + ": Raid is expired, proceed to remove: " + "("
                                            + botContextWrapper.getAtomUriForRaid(contextRaid) + ") :" + contextRaid);
                                    bus.publish(new DeleteRaidAtomEvent(contextRaid));
                                } else {
                                    logger.trace("Sanitizing Raids: " + contextRaid.getId()
                                            + ": Raid is still active, do not remove: " + "("
                                            + botContextWrapper.getAtomUriForRaid(contextRaid) + ") :" + contextRaid);
                                }
                            }
                        } else {
                            if (phaseOut) {
                                logger.info(botContextWrapper.getBotName()
                                        + " is in PhaseOut mode and has been cleared, you may shut down the bot now");
                            } else {
                                logger.info("Sanitizing Raids: " + "no Raids stored");
                            }
                        }
                    }
                }));

        bus.publish(new StartBotTriggerCommandEvent(sanitizeRaidsTrigger));

        bus.subscribe(CreateRaidAtomEvent.class, new CreateRaidAtomAction(ctx));
        bus.subscribe(DeleteRaidAtomEvent.class, new DeleteRaidAtomAction(ctx));
        bus.subscribe(ModifyRaidAtomEvent.class, new ModifyRaidAtomAction(ctx));

        // filter to prevent reacting to serviceAtom<->ownedAtom events;
        NotFilter noInternalServiceAtomEventFilter = getNoInternalServiceAtomEventFilter();
        bus.subscribe(ConnectFromOtherAtomEvent.class, noInternalServiceAtomEventFilter, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                EventListenerContext ctx = getEventListenerContext();
                if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper)
                        || !(event instanceof ConnectFromOtherAtomEvent)) {
                    logger.error(ctx.getBotContextWrapper().getBotName()
                            + ": ConnectFromOtherAtomEvent does not work without a RaidBotContextWrapper and ConnectFromOtherAtomEvent");
                    throw new IllegalStateException(ctx.getBotContextWrapper().getBotName()
                            + ": ConnectFromOtherAtomEvent does not work without a RaidBotContextWrapper and ConnectFromOtherAtomEvent");
                }

                ConnectFromOtherAtomEvent connectFromOtherAtomEvent = (ConnectFromOtherAtomEvent) event;
                Connection con = ((ConnectFromOtherAtomEvent) event).getCon();
                try {
                    String message = "Welcome to the Raid GroupChat!";
                    final ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(
                            connectFromOtherAtomEvent.getRecipientSocket(), connectFromOtherAtomEvent.getSenderSocket(),
                            message);
                    ctx.getEventBus().subscribe(ConnectCommandEvent.class, new ActionOnFirstEventListener(ctx,
                            new CommandResultFilter(connectCommandEvent), new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) {
                                    ConnectCommandResultEvent connectionMessageCommandResultEvent = (ConnectCommandResultEvent) event;
                                    if (!connectionMessageCommandResultEvent.isSuccess()) {
                                        logger.error("Failure when trying to open a received Request: "
                                                + connectionMessageCommandResultEvent.getMessage());
                                    }
                                }
                            }));
                    ctx.getEventBus().publish(connectCommandEvent);
                } catch (Exception te) {
                    logger.error(te.getMessage(), te);
                }
            }
        });
    }
}
