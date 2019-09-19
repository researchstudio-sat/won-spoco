package won.spoco.raidbot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raidbot.impl.model.ContextRaid;

public class CreateRaidAtomEvent extends BaseEvent {
    private final ContextRaid contextRaid;

    public CreateRaidAtomEvent(ContextRaid contextRaid) {
        this.contextRaid = contextRaid;
    }

    public ContextRaid getContextRaid() {
        return contextRaid;
    }
}
