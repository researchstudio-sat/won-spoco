package won.spoco.raid.bot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import won.spoco.raid.bot.api.model.RaidViennaListResponse;
import won.spoco.raid.bot.api.model.RaidVienna;

import java.util.Collections;
import java.util.List;

public class RaidFetcherViennaImpl implements RaidFetcher {
    private static final Logger logger = LoggerFactory.getLogger(RaidFetcherViennaImpl.class);

    private final String requestUrl;
    private final RestTemplate restTemplate;

    public RaidFetcherViennaImpl(String endpoint, String username, String request) {
        requestUrl = endpoint + "?user="+username+"&request="+request;
        restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        restTemplate.getMessageConverters().add(converter);
    }

    public List<RaidVienna> getActiveRaids() {
        String url = requestUrl+"&_="+System.currentTimeMillis();
        ResponseEntity<RaidViennaListResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<RaidViennaListResponse>(){});

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            List<RaidVienna> raids = responseEntity.getBody().getRaids();
            if(logger.isTraceEnabled()) {
                logger.trace("getActiveRaids was successful");
                logger.trace("---- ResponseEntity.getBody(): ----");

                for (RaidVienna raid : raids) {
                    logger.trace(raid.toString());
                }
                logger.trace("---- ------------------------- -----");
            }
            return raids;
        } else {
            logger.warn("getActiveRaids was not successful, returning empty List");
            return Collections.emptyList();
        }
    }
}

