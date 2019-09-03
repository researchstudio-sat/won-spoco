package won.spoco.raid.bot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raid.bot.impl.model.ContextRaid;

public class CreateRaidAtomEvent extends BaseEvent {
    private final ContextRaid contextRaid;

    public CreateRaidAtomEvent(ContextRaid contextRaid) {
        this.contextRaid = contextRaid;
    }

    public ContextRaid getContextRaid() {
        return contextRaid;
    }
}
