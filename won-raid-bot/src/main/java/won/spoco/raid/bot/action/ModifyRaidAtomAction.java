package won.spoco.raid.bot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractModifyAtomAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.spoco.raid.bot.api.model.Raid;
import won.spoco.raid.bot.event.ModifyRaidAtomEvent;
import won.spoco.raid.bot.impl.RaidBotContextWrapper;
import won.spoco.raid.bot.util.RaidAtomModelWrapper;

import java.net.URI;

public class ModifyRaidAtomAction extends AbstractModifyAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(ModifyRaidAtomAction.class);

    public ModifyRaidAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) {
        EventListenerContext ctx = getEventListenerContext();
        if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper) || !(event instanceof ModifyRaidAtomEvent)) {
            logger.error("ModifyRaidAtomAction does not work without a RaidBotContextWrapper and ModifyRaidAtomEvent");
            throw new IllegalStateException("ModifyRaidAtomAction does not work without a RaidBotContextWrapper and ModifyRaidAtomEvent");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) ctx.getBotContextWrapper();
        ModifyRaidAtomEvent modifyRaidAtomEvent = (ModifyRaidAtomEvent) event;

        final Raid modifiedRaid = modifyRaidAtomEvent.getRaid();

        if (botContextWrapper.getAtomUriForRaid(modifiedRaid) == null) {
            logger.warn("RaidAtom does not exist in the botContext(must have been deleted) no modification possible: " + modifiedRaid);
            botContextWrapper.removeRaid(modifiedRaid);
            return;
        }

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        final URI atomURI = botContextWrapper.getAtomUriForRaid(modifiedRaid);
        Dataset dataset = new RaidAtomModelWrapper(atomURI, modifiedRaid).copyDataset();
        logger.debug("modify atom on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage modifyAtomMessage = buildWonMessage(atomURI, dataset);

        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                logger.debug("atom modification successful, atom URI of modified atom is {}", atomURI);
                botContextWrapper.addRaid(modifiedRaid, atomURI);
            }
        };
        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                String textMessage = WonRdfUtils.MessageUtils
                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom modificaiton failed for atom URI {}, original message URI {}: {}", atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(modifyAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", modifyAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendWonMessage(modifyAtomMessage);
        logger.debug("atom modify message sent with message URI {}", modifyAtomMessage.getMessageURI());
    }
}
