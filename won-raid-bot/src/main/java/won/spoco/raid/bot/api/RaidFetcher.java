package won.spoco.raid.bot.api;

import won.spoco.raid.bot.model.Raid;

import java.util.List;

public interface RaidFetcher {

    public List<Raid> getActiveRaids();
}
