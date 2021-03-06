package won.spoco.raidbot.action;

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
import won.spoco.raidbot.context.RaidBotContextWrapper;
import won.spoco.raidbot.event.ModifyRaidAtomEvent;
import won.spoco.raidbot.impl.model.ContextRaid;
import won.spoco.raidbot.util.RaidAtomModelWrapper;

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

        final ContextRaid modifiedRaid = modifyRaidAtomEvent.getContextRaid();

        if (botContextWrapper.getAtomUriForRaid(modifiedRaid) == null) {
            logger.warn("RaidAtom does not exist in the botContext(must have been deleted) no modification possible: " + modifiedRaid);
            botContextWrapper.removeRaid(modifiedRaid);
            return;
        }
        final URI atomURI = botContextWrapper.getAtomUriForRaid(modifiedRaid);
        Dataset dataset = new RaidAtomModelWrapper(atomURI, modifiedRaid).copyDataset();
        logger.debug("modify atom {} with content {} ", atomURI, StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage modifyAtomMessage = ctx.getWonMessageSender().prepareMessage(buildWonMessage(atomURI, dataset));

        EventListener successCallback = event1 -> {
            logger.debug("atom modification successful, atom URI of modified atom is {}", atomURI);
            botContextWrapper.addRaid(modifiedRaid, atomURI);
        };
        EventListener failureCallback = event12 -> {
            String textMessage = WonRdfUtils.MessageUtils
                    .getTextMessage(((FailureResponseEvent) event12).getFailureMessage());
            logger.error("atom modification failed for atom URI {}, original message URI {}: {}", atomURI, ((FailureResponseEvent) event12).getOriginalMessageURI(), textMessage);
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(modifyAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", modifyAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendMessage(modifyAtomMessage);
        logger.debug("atom modify message sent with message URI {}", modifyAtomMessage.getMessageURI());
    }
}
