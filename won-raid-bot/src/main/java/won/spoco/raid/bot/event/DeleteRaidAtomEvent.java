package won.spoco.raid.bot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raid.bot.impl.model.ContextRaid;

public class DeleteRaidAtomEvent extends BaseEvent {
    private final ContextRaid contextRaid;

    public DeleteRaidAtomEvent(ContextRaid contextRaid) {
        this.contextRaid = contextRaid;
    }

    public ContextRaid getContextRaid() {
        return contextRaid;
    }
}
