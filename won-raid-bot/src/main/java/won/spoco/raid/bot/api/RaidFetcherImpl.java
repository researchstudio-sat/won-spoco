package won.spoco.raid.bot.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
    public List<String> getActiveRaids() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(endpoint + "?token=" + token, String.class);

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            logger.debug("getActiveRaids was successful");
            logger.debug("---- ResponseEntity.getBody(): ----");
            logger.debug(responseEntity.getBody());
            logger.debug("---- ------------------------- -----");
        } else {
            logger.warn("getActiveRaids was not successful");
        }
        return null;
    }
}
