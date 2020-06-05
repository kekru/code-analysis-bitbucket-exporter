package de.kekru.codeanalysisbb.bitbucket;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.features.InsightsApi;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.BitbucketConfig;
import de.kekru.javautils.dependencyinjection.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BitbucketThirdPartyService {

  private BitbucketClient client;
  private final Config config;

  public InsightsApi getInsightsApi() {
    return getClient().api().insightsApi();
  }

  private BitbucketClient getClient() {
    if (client == null) {
      BitbucketConfig bitbucketConfig = config.getBitbucket();

      client = BitbucketClient.builder()
          .endPoint(bitbucketConfig.getEndPoint())
          .token(bitbucketConfig.getToken())
          .build();
    }

    return client;
  }

}
