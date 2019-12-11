package won.spoco.raidbot.util;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import won.protocol.util.DateTimeUtils;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WONMATCH;
import won.protocol.vocabulary.WXGROUP;
import won.protocol.vocabulary.WXHOLD;
import won.spoco.core.protocol.vocabulary.WXPOGO;
import won.spoco.raidbot.impl.model.ContextRaid;

import java.math.RoundingMode;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class RaidAtomModelWrapper extends DefaultAtomModelWrapper {
    public RaidAtomModelWrapper(String atomUri, ContextRaid contextRaid) {
        this(URI.create(atomUri), contextRaid);
    }

    public RaidAtomModelWrapper(URI atomUri, ContextRaid contextRaid) {
        super(atomUri);

        Resource atom = this.getAtomModel().createResource(atomUri.toString());

        // Default Data & Information
        atom.addProperty(RDF.type, SCHEMA.PLANACTION);

        Resource object = atom.getModel().createResource();
        object.addProperty(RDF.type, SCHEMA.EVENT);
        object.addProperty(SCHEMA.ABOUT, WXPOGO.PokemonGo);
        atom.addProperty(SCHEMA.OBJECT, object);

        // Raid Information
        Resource raidResource = atom.getModel().createResource();
        if (contextRaid.getHatchDate() != null) {
            raidResource.addProperty(SCHEMA.VALID_FROM,
                    DateTimeUtils.toLiteral(contextRaid.getHatchDate(), atom.getModel()));
        }
        raidResource.addProperty(SCHEMA.VALID_THROUGH,
                DateTimeUtils.toLiteral(contextRaid.getEndDate(), atom.getModel()));

        if (contextRaid.getLevel() > 0) {
            raidResource.addLiteral(WXPOGO.level, contextRaid.getLevel());
        }
        if (contextRaid.getPokedexId() > 0) {
            raidResource.addLiteral(WXPOGO.pokemonId, contextRaid.getPokedexId());
        }
        if (contextRaid.getPokemonForm() != null) {
            raidResource.addLiteral(WXPOGO.pokemonForm, contextRaid.getPokemonForm());
        }
        atom.addProperty(WXPOGO.raid, raidResource);

        // Gym Information
        if (contextRaid.getGymInfo() != null) {
            atom.addProperty(SCHEMA.DESCRIPTION, contextRaid.getGymInfo());
            atom.addProperty(DC.description, contextRaid.getGymInfo());
        }

        if (contextRaid.isGymEx()) {
            atom.addLiteral(WXPOGO.gymEx, true);
        }

        Resource raidLocation = atom.getModel().createResource();

        raidLocation.addProperty(RDF.type, SCHEMA.PLACE);

        DecimalFormat df = new DecimalFormat("##.######");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        String nwlat = df.format(contextRaid.getGymLat());
        String nwlng = df.format(contextRaid.getGymLng());
        String selat = df.format(contextRaid.getGymLat());
        String selng = df.format(contextRaid.getGymLng());
        String lat = df.format(contextRaid.getGymLat());
        String lng = df.format(contextRaid.getGymLng());
        String name = contextRaid.getGymName();
        Resource boundingBoxResource = atom.getModel().createResource();
        Resource nwCornerResource = atom.getModel().createResource();
        Resource seCornerResource = atom.getModel().createResource();
        Resource geoResource = atom.getModel().createResource();
        raidLocation.addProperty(SCHEMA.NAME, name);
        raidLocation.addProperty(SCHEMA.GEO, geoResource);
        geoResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
        geoResource.addProperty(SCHEMA.LATITUDE, lat);
        geoResource.addProperty(SCHEMA.LONGITUDE, lng);
        RDFDatatype bigdata_geoSpatialDatatype = new BaseDatatype(
                "http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon");
        geoResource.addProperty(WONCON.geoSpatial, lat + "#" + lng, bigdata_geoSpatialDatatype);
        raidLocation.addProperty(WONCON.boundingBox, boundingBoxResource);
        boundingBoxResource.addProperty(WONCON.northWestCorner, nwCornerResource);
        nwCornerResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
        nwCornerResource.addProperty(SCHEMA.LATITUDE, nwlat);
        nwCornerResource.addProperty(SCHEMA.LONGITUDE, nwlng);
        boundingBoxResource.addProperty(WONCON.southEastCorner, seCornerResource);
        seCornerResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
        seCornerResource.addProperty(SCHEMA.LATITUDE, selat);
        seCornerResource.addProperty(SCHEMA.LONGITUDE, selng);

        atom.addProperty(SCHEMA.LOCATION, raidLocation);

        this.addSocket("#HoldableSocket", WXHOLD.HoldableSocketString);
        this.addSocket("#GroupSocket", WXGROUP.GroupSocketString);
        this.setDefaultSocket("#GroupSocket");
        this.addFlag(WONMATCH.NoHintForMe);
    }
}
