package won.spoco.raid.bot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RaidVienna extends Raid implements Serializable {

    @JsonCreator
    public RaidVienna(
            @JsonProperty(value = "raidId", required = true) String id,
            @JsonProperty(value = "name", required = true) String gymName,
            @JsonProperty(value = "boss", required = true) Boss boss,
            @JsonProperty(value = "location", required = true) CustomLocation location,
            @JsonProperty(value = "startTime", required = true) String hatchTimeString,
            @JsonProperty(value = "endTime", required = true) String endTimeString
    ) {
        super(
            id,
            (boss.id < 0) ? boss.id*(-1) : -1,
            (boss.id > 0) ? boss.id : -1,
            null,
            gymName,
            location.lat,
            location.lng,
            null,
            false,
            Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.parse(hatchTimeString))),
            Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.parse(endTimeString)))
        );
    }


    static class CustomLocation {
        @JsonProperty(value = "lat", required = true)
        @JsonFormat(shape= JsonFormat.Shape.STRING)
        private double lat;

        @JsonProperty(value = "lon", required = true)
        @JsonFormat(shape= JsonFormat.Shape.STRING)
        private double lng;
    }

    static class Boss {
        @JsonProperty(value = "id", required = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private int id;
    }
}
