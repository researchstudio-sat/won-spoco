package won.spoco.raidbot.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RaidViennaListResponse {
    @JsonProperty(value = "data", required = true)
    private List<RaidVienna> raids;

    public List<RaidVienna> getRaids() {
        return raids;
    }
}
