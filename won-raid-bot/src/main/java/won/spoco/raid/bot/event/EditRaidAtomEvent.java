package won.spoco.raid.bot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.spoco.raid.bot.model.Raid;

public class EditRaidAtomEvent extends BaseEvent {
    private final Raid raid;

    public EditRaidAtomEvent(Raid raid) {
        this.raid = raid;
    }

    public Raid getRaid() {
        return raid;
    }
}
