package won.spoco.raidbot.impl.model;

import won.spoco.raidbot.api.model.Raid;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ContextRaid implements Serializable {
    private final String id;
    private int level;
    private int pokedexId;
    private final String pokemonForm;
    private final double gymLat;
    private final double gymLng;
    private final String gymName;
    private final String gymInfo;
    private final boolean gymEx;
    private final Date hatchDate;
    private final Date endDate;

    public ContextRaid(String id, int level, int pokedexId, String pokemonForm, String gymName, double gymLat, double gymLng, String gymInfo, boolean gymEx, Date hatchDate, Date endDate) {
        this.id = id;
        this.level = level;
        this.pokedexId = pokedexId;
        this.pokemonForm = pokemonForm;
        this.gymName = gymName;
        this.gymLat = gymLat;
        this.gymLng = gymLng;
        this.gymInfo = gymInfo;
        this.gymEx = gymEx;
        this.hatchDate = hatchDate;
        this.endDate = endDate;
    }

    // Helper Methods

    public final boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public final boolean isExpired(long currentTimeInMillis) {
        return new Date(currentTimeInMillis).after(getEndDate());
    }

    public final boolean hasUpdatedInformation(Raid raid) {
        return hasUpdatedInformation(raid.buildContextRaid());
    }

    public final boolean hasUpdatedInformation(ContextRaid contextRaid) {
        if (this == contextRaid) return false;
        if (contextRaid == null) return false;

        return !(getLevel() == contextRaid.getLevel() &&
                Objects.equals(getHatchDate(), contextRaid.getHatchDate()) &&
                Objects.equals(getEndDate(), contextRaid.getEndDate()) &&
                Double.compare(contextRaid.getGymLat(), getGymLat()) == 0 &&
                Double.compare(contextRaid.getGymLng(), getGymLng()) == 0 &&
                isGymEx() == contextRaid.isGymEx() &&
                Objects.equals(this.getId(), contextRaid.getId()) &&
                this.getPokedexId() == contextRaid.getPokedexId() &&
                Objects.equals(getGymName(), contextRaid.getGymName()) &&
                Objects.equals(getGymInfo(), contextRaid.getGymInfo()));
    }

    // Overrides
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if(o instanceof ContextRaid) {
            ContextRaid contextRaid = (ContextRaid) o;
            return Objects.equals(this.getId(), contextRaid.getId());
        } else if(o instanceof Raid) {
            Raid raid = (Raid) o;
            return Objects.equals(this.getId(), raid.getId());
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "ContextRaid{" +
                "id='" + id + '\'' +
                ", level=" + level +
                (this.getPokedexId() != -1 ? ", pokedexId='" + this.getPokedexId() + '\'' : "") +
                ", hatchDate=" + this.getHatchDate() +
                ", endDate=" + this.getEndDate() +
                ", gymLat=" + gymLat +
                ", gymLng=" + gymLng +
                ", gymName='" + gymName + '\'' +
                ", gymInfo='" + gymInfo + '\'' +
                ", gymEx=" + gymEx +
                '}';
    }

    //Generic getters
    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getPokedexId() {
        return pokedexId;
    }

    public String getPokemonForm() {
        return pokemonForm;
    }

    public double getGymLat() {
        return gymLat;
    }

    public double getGymLng() {
        return gymLng;
    }

    public String getGymName() {
        return gymName;
    }

    public String getGymInfo() {
        return gymInfo;
    }

    public boolean isGymEx() {
        return gymEx;
    }

    public Date getHatchDate() {
        return hatchDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
