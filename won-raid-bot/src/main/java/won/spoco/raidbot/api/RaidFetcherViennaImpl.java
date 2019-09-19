package won.spoco.raidbot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import won.spoco.raidbot.api.model.RaidVienna;
import won.spoco.raidbot.api.model.RaidViennaListResponse;

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

