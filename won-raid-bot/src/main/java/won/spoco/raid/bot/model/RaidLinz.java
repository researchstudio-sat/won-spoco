package won.spoco.raid.bot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class RaidLinz extends Raid implements Serializable {
    @JsonCreator
    public RaidLinz(
            @JsonProperty(value = "slack_ts", required = true) String id,
            @JsonProperty(value = "raid_lvl") int level,
            @JsonProperty(value = "raid_pkm") String pokemonIdString,
            @JsonProperty(value = "raid_hatchtime") long hatchTimeInSeconds,
            @JsonProperty(value = "raid_endtime", required = true) long endTimeInSeconds,
            @JsonProperty(value = "gym_latitude", required = true) double gymLat,
            @JsonProperty(value = "gym_longitude", required = true) double gymLng,
            @JsonProperty(value = "gym_name") String gymName,
            @JsonProperty(value = "gym_info") String gymInfo,
            @JsonProperty(value = "gym_ex") boolean gymEx
    ) {
        super(
            id,
            level,
            pokemonIdString,
            null,
            gymName,
            gymLat,
            gymLng,
            gymInfo,
            gymEx,
            new Date(hatchTimeInSeconds*1000), //for whatever reason the unix timestamp is in seconds and not millis
            new Date(endTimeInSeconds*1000) //for whatever reason the unix timestamp is in seconds and not millis
        );
    }
}
