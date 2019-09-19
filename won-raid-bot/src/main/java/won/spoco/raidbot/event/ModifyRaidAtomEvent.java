package won.spoco.raidbot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raidbot.impl.model.ContextRaid;

public class ModifyRaidAtomEvent extends BaseEvent {
    private final ContextRaid contextRaid;

    public ModifyRaidAtomEvent(ContextRaid contextRaid) {
        this.contextRaid = contextRaid;
    }

    public ContextRaid getContextRaid() {
        return contextRaid;
    }
}
