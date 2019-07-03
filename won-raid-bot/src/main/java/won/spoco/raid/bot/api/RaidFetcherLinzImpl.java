package won.spoco.raid.bot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import won.spoco.raid.bot.model.RaidLinz;

import java.util.Collections;
import java.util.List;

public class RaidFetcherLinzImpl implements RaidFetcher {
    private static final Logger logger = LoggerFactory.getLogger(RaidFetcherLinzImpl.class);

    private final String endpoint;
    private final String token;

    private final RestTemplate restTemplate;

    public RaidFetcherLinzImpl(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
        restTemplate = new RestTemplate();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);
    }

    @Override
    public List<RaidLinz> getActiveRaids() {
        ResponseEntity<List<RaidLinz>> responseEntity = restTemplate.exchange(endpoint + "?token=" + token, HttpMethod.GET, null, new ParameterizedTypeReference<List<RaidLinz>>(){});

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            if(logger.isTraceEnabled()) {
                logger.trace("getActiveRaids was successful");
                logger.trace("---- ResponseEntity.getBody(): ----");
                for (RaidLinz raid : responseEntity.getBody()) {
                    logger.trace(raid.toString());
                }
                logger.trace("---- ------------------------- -----");
            }
            return responseEntity.getBody();
        } else {
            logger.warn("getActiveRaids was not successful, returning empty List");
            return Collections.emptyList();
        }
    }
}
