package won.spoco.raid.bot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raid.bot.model.Raid;

public class CreateRaidAtomEvent extends BaseEvent {
    private final Raid raid;

    public CreateRaidAtomEvent(Raid raid) {
        this.raid = raid;
    }

    public Raid getRaid() {
        return raid;
    }
}
