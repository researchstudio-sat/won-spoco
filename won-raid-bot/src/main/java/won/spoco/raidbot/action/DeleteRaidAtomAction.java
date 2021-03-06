package won.spoco.raidbot.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractDeleteAtomAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.util.WonRdfUtils;
import won.spoco.raidbot.context.RaidBotContextWrapper;
import won.spoco.raidbot.event.DeleteRaidAtomEvent;
import won.spoco.raidbot.impl.model.ContextRaid;

import java.net.URI;

public class DeleteRaidAtomAction extends AbstractDeleteAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(DeleteRaidAtomAction.class);

    public DeleteRaidAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) {
        EventListenerContext ctx = getEventListenerContext();
        if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper) || !(event instanceof DeleteRaidAtomEvent)) {
            logger.error("DeleteRaidAtomAction does not work without a RaidBotContextWrapper and DeleteRaidAtomEvent");
            throw new IllegalStateException("DeleteRaidAtomAction does not work without a RaidBotContextWrapper and DeleteRaidAtomEvent");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) ctx.getBotContextWrapper();
        DeleteRaidAtomEvent deleteRaidAtomEvent = (DeleteRaidAtomEvent) event;

        final ContextRaid raidToDelete = deleteRaidAtomEvent.getContextRaid();

        if (botContextWrapper.getAtomUriForRaid(raidToDelete) == null) {
            logger.warn("RaidAtom does not exist in the botContext(must have been deleted) no deletion possible: " + raidToDelete);
            botContextWrapper.removeRaid(raidToDelete);
            return;
        }

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI(); //FIXME: MIGHT TAKE THE WRONG NODEURI
        final URI atomURI = botContextWrapper.getAtomUriForRaid(raidToDelete);
        logger.debug("deleting atom on won node {} with uri {} ", wonNodeUri, atomURI);
        WonMessage deleteAtomMessage = ctx.getWonMessageSender().prepareMessage(buildWonMessage(atomURI)); //FIXME: MIGHT TAKE THE WRONG NODEURI

        EventListener successCallback = event1 -> {
            logger.debug("atom deletion successful, URI was {}", atomURI);
            botContextWrapper.removeRaid(raidToDelete);
            EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
        };
        EventListener failureCallback = event12 -> {
            String textMessage = WonRdfUtils.MessageUtils
                    .getTextMessage(((FailureResponseEvent) event12).getFailureMessage());
            logger.error("atom deletion failed for atom URI {}, original message URI {}: {}", atomURI, ((FailureResponseEvent) event12).getOriginalMessageURI(), textMessage);
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(deleteAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", deleteAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendMessage(deleteAtomMessage);
        logger.debug("atom deletion message sent with message URI {}", deleteAtomMessage.getMessageURI());
    }
}
