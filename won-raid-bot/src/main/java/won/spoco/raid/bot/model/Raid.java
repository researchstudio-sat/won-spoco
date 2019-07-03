package won.spoco.raid.bot.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public abstract class Raid implements Serializable {
    private String id;
    private int level = -1;
    private int pokedexId = -1;
    private String pokemonForm;
    private double gymLat;
    private double gymLng;
    private String gymName;
    private String gymInfo;
    private boolean gymEx;
    private Date hatchDate;
    private Date endDate;

    Raid(String id, int level, int pokedexId, String pokemonForm, String gymName, double gymLat, double gymLng, String gymInfo, boolean gymEx, Date hatchDate, Date endDate) {
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

    Raid(String id, int level, String pokemonIdString, String pokemonForm, String gymName, double gymLat, double gymLng, String gymInfo, boolean gymEx, Date hatchDate, Date endDate) {
        this.id = id;
        this.level = level;
        try {
            this.pokedexId = Integer.valueOf(pokemonIdString);
        } catch(NumberFormatException e) {
            this.pokedexId = -1;
        }
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
        if (this == raid) return false;
        if (raid == null) return false;

        return !(getLevel() == raid.getLevel() &&
                Objects.equals(getHatchDate(), raid.getHatchDate()) &&
                Objects.equals(getEndDate(), raid.getEndDate()) &&
                Double.compare(raid.getGymLat(), getGymLat()) == 0 &&
                Double.compare(raid.getGymLng(), getGymLng()) == 0 &&
                isGymEx() == raid.isGymEx() &&
                Objects.equals(this.getId(), raid.getId()) &&
                this.getPokedexId() == raid.getPokedexId() &&
                Objects.equals(getGymName(), raid.getGymName()) &&
                Objects.equals(getGymInfo(), raid.getGymInfo()));
    }

    // Generic Getter
    public final String getId() {
        return this.id;
    }

    public final int getLevel(){
        return this.level;
    }

    public final int getPokedexId() {
        return this.pokedexId;
    }

    public final double getGymLat() {
        return this.gymLat;
    }

    public final double getGymLng() {
        return this.gymLng;
    }

    public final String getGymName() {
        return this.gymName;
    }

    public final String getGymInfo() {
        return this.gymInfo;
    }

    public final boolean isGymEx() {
        return this.gymEx;
    }

    public final Date getHatchDate() {
        return this.hatchDate;
    }
    public final Date getEndDate() {
        return this.endDate;
    }

    public final String getPokemonForm() {
        return pokemonForm;
    }

    // Overrides
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Raid)) return false;
        Raid raid = (Raid) o;
        return Objects.equals(this.getId(), raid.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Raid{" +
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
}
