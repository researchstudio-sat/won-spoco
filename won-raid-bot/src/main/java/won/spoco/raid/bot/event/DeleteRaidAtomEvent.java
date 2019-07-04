package won.spoco.raid.bot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raid.bot.api.model.Raid;

public class DeleteRaidAtomEvent extends BaseEvent {
    private final Raid raid;

    public DeleteRaidAtomEvent(Raid raid) {
        this.raid = raid;
    }

    public Raid getRaid() {
        return raid;
    }
}
