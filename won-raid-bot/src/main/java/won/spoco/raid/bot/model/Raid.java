package won.spoco.raid.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Raid implements Serializable {
    @JsonProperty(value = "slack_ts", required = true)
    private String id;
    @JsonProperty(value = "raid_lvl", required = true)
    private int level;
    @JsonProperty(value = "raid_pkm")
    private String pokemonId; //should be int but is String due to multiple different datatype returns (fault in api)
    private String pokemonForm; //not represented in the API just yet
    @JsonProperty(value = "raid_status", required = true)
    private String status;
    @JsonProperty(value = "raid_hatchtime")
    private long hatchTimeInSeconds; //for whatever reason the unix timestamp is in seconds and not millis
    @JsonProperty(value = "raid_endtime", required = true)
    private long endTimeInSeconds; //for whatever reason the unix timestamp is in seconds and not millis

    @JsonProperty(value = "gym_latitude", required = true)
    private double gymLat;
    @JsonProperty(value = "gym_longitude", required = true)
    private double gymLng;
    @JsonProperty(value = "gym_name", required = true)
    private String gymName;
    @JsonProperty("gym_info")
    private String gymInfo;
    @JsonProperty("gym_ex")
    private boolean gymEx;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return pokemonId if String is parsable to integer, if it is not then we return -1
     */
    public int getPokedexId() {
        try {
            return Integer.valueOf(pokemonId);
        }catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setPokemonId(String pokemonId) {
        this.pokemonId = pokemonId;
    }

    public String getPokemonForm() {
        return pokemonForm;
    }

    public void setPokemonForm(String pokemonForm) {
        this.pokemonForm = pokemonForm;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getGymLat() {
        return gymLat;
    }

    public void setGymLat(double gymLat) {
        this.gymLat = gymLat;
    }

    public double getGymLng() {
        return gymLng;
    }

    public void setGymLng(double gymLng) {
        this.gymLng = gymLng;
    }

    public String getGymName() {
        return gymName;
    }

    public void setGymName(String gymName) {
        this.gymName = gymName;
    }

    public String getGymInfo() {
        return gymInfo;
    }

    public void setGymInfo(String gymInfo) {
        this.gymInfo = gymInfo;
    }

    public boolean isGymEx() {
        return gymEx;
    }

    public void setGymEx(boolean gymEx) {
        this.gymEx = gymEx;
    }

    public boolean isHatched() {
        return !"egg".equals(status) && "boss".equals(status);
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }
    public boolean isExpired(long currentTimeInMillis) {
        return new Date(currentTimeInMillis).after(getEndDate());
    }

    public Date getHatchDate() {
        if(this.hatchTimeInSeconds == 0) {
            return null;
        }
        return new Date(this.hatchTimeInSeconds*1000);
    }
    public Date getEndDate() {
        return new Date(this.endTimeInSeconds*1000);
    }

    public boolean hasUpdatedInformation(Object o) {
        if (this == o) return false;
        if (o == null || getClass() != o.getClass()) return false;
        Raid raid = (Raid) o;
        return !(level == raid.level &&
                hatchTimeInSeconds == raid.hatchTimeInSeconds &&
                endTimeInSeconds == raid.endTimeInSeconds &&
                Double.compare(raid.gymLat, gymLat) == 0 &&
                Double.compare(raid.gymLng, gymLng) == 0 &&
                gymEx == raid.gymEx &&
                id.equals(raid.id) &&
                Objects.equals(pokemonId, raid.pokemonId) &&
                status.equals(raid.status) &&
                Objects.equals(gymName, raid.gymName) &&
                Objects.equals(gymInfo, raid.gymInfo));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Raid raid = (Raid) o;
        return id.equals(raid.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Raid{" +
                "id='" + id + '\'' +
                ", level=" + level +
                (this.getPokedexId() != -1 ? ", pokedexId='" + this.getPokedexId() + '\'' : "") +
                ", isHatched=" + this.isHatched() +
                ", hatchDate=" + this.getHatchDate() +
                ", endDate=" + this.getEndDate() +
                ", gymLat=" + gymLat +
                ", gymLng=" + gymLng +
                ", gymName='" + gymName + '\'' +
                ", gymInfo='" + gymInfo + '\'' +
                ", gymEx=" + gymEx +
                '}';
    }
}
