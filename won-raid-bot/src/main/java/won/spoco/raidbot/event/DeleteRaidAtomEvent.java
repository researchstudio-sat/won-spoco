package won.spoco.raidbot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raidbot.impl.model.ContextRaid;

public class DeleteRaidAtomEvent extends BaseEvent {
    private final ContextRaid contextRaid;

    public DeleteRaidAtomEvent(ContextRaid contextRaid) {
        this.contextRaid = contextRaid;
    }

    public ContextRaid getContextRaid() {
        return contextRaid;
    }
}
