package won.spoco.raid.bot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import won.protocol.util.DefaultAtomModelWrapper;
import won.spoco.raid.bot.model.Raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaidFetcherImpl implements RaidFetcher {
    private static final Logger logger = LoggerFactory.getLogger(RaidFetcherImpl.class);

    private final String endpoint;
    private final String token;

    private RestTemplate restTemplate;

    public RaidFetcherImpl(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
        restTemplate = new RestTemplate();
    }

    @Override
    public List<Raid> getActiveRaids() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);

        ResponseEntity<List<Raid>> responseEntity = restTemplate.exchange(endpoint + "?token=" + token, HttpMethod.GET, null, new ParameterizedTypeReference<List<Raid>>(){});

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            if(logger.isTraceEnabled()) {
                logger.trace("getActiveRaids was successful");
                logger.trace("---- ResponseEntity.getBody(): ----");
                for (Raid raid : responseEntity.getBody()) {
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
