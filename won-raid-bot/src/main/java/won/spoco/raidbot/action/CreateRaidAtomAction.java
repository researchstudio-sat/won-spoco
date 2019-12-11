package won.spoco.raidbot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.serviceatom.ServiceAtomContext;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.spoco.raidbot.context.RaidBotContextWrapper;
import won.spoco.raidbot.event.CreateRaidAtomEvent;
import won.spoco.raidbot.impl.model.ContextRaid;
import won.spoco.raidbot.util.RaidAtomModelWrapper;

import java.net.URI;

public class CreateRaidAtomAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(CreateRaidAtomAction.class);

    public CreateRaidAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) {
        EventListenerContext ctx = getEventListenerContext();
        if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper) || !(event instanceof CreateRaidAtomEvent)) {
            logger.error("CreateRaidAtomAction does not work without a RaidBotContextWrapper and CreateRaidAtomEvent");
            throw new IllegalStateException(
                    "CreateRaidAtomAction does not work without a RaidBotContextWrapper and CreateRaidAtomEvent");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) ctx.getBotContextWrapper();
        CreateRaidAtomEvent createRaidAtomEvent = (CreateRaidAtomEvent) event;

        // Get URI of service atom, to set as holder of created atom
        ServiceAtomContext serviceAtomContext = (ServiceAtomContext) ctx.getBotContextWrapper();
        final URI serviceAtomURI = serviceAtomContext.getServiceAtomUri();

        final ContextRaid raidToCreate = createRaidAtomEvent.getContextRaid();

        if (botContextWrapper.getAtomUriForRaid(raidToCreate) != null) {
            logger.warn("RaidAtom already exists, URI: " + botContextWrapper.getAtomUriForRaid(raidToCreate));
            return;
        }

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
        final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
        Dataset dataset = new RaidAtomModelWrapper(atomURI, raidToCreate).copyDataset();
        logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage createAtomMessage = ctx.getWonMessageSender().prepareMessage(createWonMessage(atomURI, dataset));
        EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
        EventBus bus = ctx.getEventBus();
        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                logger.debug("atom creation successful, new atom URI is {}", atomURI);
                bus.publish(new AtomCreatedEvent(atomURI, wonNodeUri, dataset, null));
                botContextWrapper.addRaid(raidToCreate, atomURI);
            }
        };
        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                String textMessage = WonRdfUtils.MessageUtils
                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom creation failed for atom URI {}, original message URI {}: {}", atomURI,
                        ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
                botContextWrapper.removeRaid(raidToCreate);
                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendMessage(createAtomMessage);
        logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
    }
}
