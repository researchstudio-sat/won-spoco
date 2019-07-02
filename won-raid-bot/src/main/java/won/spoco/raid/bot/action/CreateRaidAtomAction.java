package won.spoco.raid.bot.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.DateTimeUtils;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.*;
import won.spoco.core.protocol.vocabulary.WXPOGO;
import won.spoco.raid.bot.event.CreateRaidAtomEvent;
import won.spoco.raid.bot.impl.RaidBotContextWrapper;
import won.spoco.raid.bot.model.Raid;

import java.math.RoundingMode;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CreateRaidAtomAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(CreateRaidAtomAction.class);

    public CreateRaidAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (!(ctx.getBotContextWrapper() instanceof RaidBotContextWrapper) || !(event instanceof CreateRaidAtomEvent)) {
            logger.error("CreateRaidAtomAction does not work without a RaidBotContextWrapper and CreateRaidAtomEvent");
            throw new IllegalStateException("CreateRaidAtomAction does not work without a RaidBotContextWrapper and CreateRaidAtomEvent");
        }
        RaidBotContextWrapper botContextWrapper = (RaidBotContextWrapper) ctx.getBotContextWrapper();
        CreateRaidAtomEvent createRaidAtomEvent = (CreateRaidAtomEvent) event;

        final Raid raidToCreate = createRaidAtomEvent.getRaid();

        if (botContextWrapper.getAtomUriForRaid(raidToCreate) != null) {
            logger.warn("RaidAtom already exists, URI: " + botContextWrapper.getAtomUriForRaid(raidToCreate));
            return;
        }

        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
        final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
        Dataset dataset = this.generateRaidAtomStructure(atomURI, raidToCreate);
        logger.debug("creating atom on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage createAtomMessage = createWonMessage(atomURI, dataset);
        EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
        EventBus bus = ctx.getEventBus();
        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                logger.debug("atom creation successful, new atom URI is {}", atomURI);
                bus.publish(new AtomCreatedEvent(atomURI, wonNodeUri, dataset, null));
                botContextWrapper.addRaid(raidToCreate, atomURI);
            }
        };
        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                String textMessage = WonRdfUtils.MessageUtils
                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom creation failed for atom URI {}, original message URI {}: {}", new Object[] {
                        atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage });
                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
                botContextWrapper.removeRaid(raidToCreate);
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendWonMessage(createAtomMessage);
        logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
    }

    private Dataset generateRaidAtomStructure(URI atomURI, Raid raid) {
        DefaultAtomModelWrapper atomModelWrapper = new DefaultAtomModelWrapper(atomURI);
        Resource atom = atomModelWrapper.getAtomModel().createResource(atomURI.toString());

        //Default Data & Information
        atom.addProperty(RDF.type, SCHEMA.PLANACTION);

        Resource object = atom.getModel().createResource();
        object.addProperty(RDF.type, SCHEMA.EVENT);
        object.addProperty(SCHEMA.ABOUT, WXPOGO.PokemonGo);
        atom.addProperty(SCHEMA.OBJECT, object);


        //Raid Information
        Resource raidResource = atom.getModel().createResource();
        if(raid.getHatchDate() != null) {
            raidResource.addProperty(SCHEMA.VALID_FROM, DateTimeUtils.toLiteral(raid.getHatchDate(), atom.getModel()));
        }
        raidResource.addProperty(SCHEMA.VALID_THROUGH, DateTimeUtils.toLiteral(raid.getEndDate(), atom.getModel()));

        if(raid.getLevel() > 0) {
            raidResource.addLiteral(WXPOGO.level, raid.getLevel());
        }
        if(raid.getPokedexId() != -1) {
            raidResource.addLiteral(WXPOGO.pokemonId, raid.getPokedexId());
        }
        if(raid.getPokemonForm() != null) {
            raidResource.addLiteral(WXPOGO.pokemonForm, raid.getPokemonForm());
        }
        atom.addProperty(WXPOGO.raid, raidResource);

        //Gym Information
        if(raid.getGymInfo() != null) {
            atom.addProperty(SCHEMA.DESCRIPTION, raid.getGymInfo());
            atom.addProperty(DC.description, raid.getGymInfo());
        }

        if(raid.isGymEx()) {
            atom.addLiteral(WXPOGO.gymEx, true);
        }

        Resource raidLocation = atom.getModel().createResource();

        raidLocation.addProperty(RDF.type, SCHEMA.PLACE);

        DecimalFormat df = new DecimalFormat("##.######");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        String nwlat = df.format(raid.getGymLat());
        String nwlng = df.format(raid.getGymLng());
        String selat = df.format(raid.getGymLat());
        String selng = df.format(raid.getGymLng());
        String lat = df.format(raid.getGymLat());
        String lng = df.format(raid.getGymLng());
        String name = raid.getGymName();
        Resource boundingBoxResource = atom.getModel().createResource();
        Resource nwCornerResource = atom.getModel().createResource();
        Resource seCornerResource = atom.getModel().createResource();
        Resource geoResource = atom.getModel().createResource();
        raidLocation.addProperty(SCHEMA.NAME, name);
        raidLocation.addProperty(SCHEMA.GEO, geoResource);
        geoResource.addProperty(RDF.type, SCHEMA.GEOCOORDINATES);
        geoResource.addProperty(SCHEMA.LATITUDE, lat);
        geoResource.addProperty(SCHEMA.LONGITUDE, lng);
        RDFDatatype bigdata_geoSpatialDatatype = new BaseDatatype("http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon");
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

        atomModelWrapper.addSocket("#GroupSocket", WXGROUP.GroupSocketString);
        atomModelWrapper.setDefaultSocket("#GroupSocket");
        atomModelWrapper.addFlag(WONMATCH.NoHintForMe);

        return atomModelWrapper.copyDataset();
    }
}
