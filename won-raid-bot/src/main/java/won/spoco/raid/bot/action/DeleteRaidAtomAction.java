package won.spoco.raid.bot.action;

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
import won.spoco.raid.bot.api.model.Raid;
import won.spoco.raid.bot.event.DeleteRaidAtomEvent;
import won.spoco.raid.bot.impl.RaidBotContextWrapper;

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

        final Raid raidToDelete = deleteRaidAtomEvent.getRaid();

        if (botContextWrapper.getAtomUriForRaid(raidToDelete) == null) {
            logger.warn("RaidAtom does not exist in the botContext(must have been deleted) no deletion possible: " + raidToDelete);
            botContextWrapper.removeRaid(raidToDelete);
            return;
        }

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        final URI atomURI = botContextWrapper.getAtomUriForRaid(raidToDelete);
        logger.debug("deleting atom on won node {} with uri {} ", wonNodeUri, atomURI);
        WonMessage deleteAtomMessage = buildWonMessage(atomURI);

        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                logger.debug("atom deletion successful, URI was {}", atomURI);
                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
                botContextWrapper.removeRaid(raidToDelete);
            }
        };
        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                String textMessage = WonRdfUtils.MessageUtils
                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom deletion failed for atom URI {}, original message URI {}: {}", atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(deleteAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", deleteAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendWonMessage(deleteAtomMessage);
        logger.debug("atom deletion message sent with message URI {}", deleteAtomMessage.getMessageURI());
    }
}
