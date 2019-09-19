package won.spoco.raidbot.api;

import won.spoco.raidbot.api.model.Raid;

import java.util.List;

public interface RaidFetcher {

    <R extends Raid> List<R> getActiveRaids();
}
