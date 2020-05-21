package de.kekru.nociplugin.bitbucket;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.features.InsightsApi;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import de.kekru.nociplugin.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.nociplugin.bitbucket.datamodel.BitbucketReport;
import de.kekru.nociplugin.config.Config;
import de.kekru.nociplugin.config.Config.BitbucketConfig;
import de.kekru.nociplugin.config.interf.ReporterConfig;
import de.kekru.nociplugin.serviceregistry.Service;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Service
public class BitbucketService {

  private final Config config;
  private final BitbucketThirdPartyService bitbucketThirdPartyService;

  public void send(BitbucketReport report) {

    BitbucketConfig bitbucketConfig = config.getBitbucket();
    ReporterConfig reporter = config.getReporter().getPmd();

    CreateInsightReport createInsightReport = CreateInsightReport.create(
        report.getDetails(),
        report.getLink(),
        report.getLogoUrl(),
        report.getResult(),
        reporter.getTitle(),
        reporter.getReporter(),
        report.getInsightData()
    );

    InsightsApi insightsApi = bitbucketThirdPartyService.getInsightsApi();

    insightsApi
        .createReport(
            bitbucketConfig.getProject(),
            bitbucketConfig.getRepo(),
            bitbucketConfig.getCommitId(),
            reporter.getKey(),
            createInsightReport
        );

    insightsApi.createAnnotations(
        bitbucketConfig.getProject(),
        bitbucketConfig.getRepo(),
        bitbucketConfig.getCommitId(),
        reporter.getKey(),
        CreateAnnotations.create(
            report.getAnnotations()
                .stream()
                .map(this::updateAnnotation)
                .map(BitbucketAnnotation::toAnnotation)
                .collect(Collectors.toList())
        )
    );
  }

  private BitbucketAnnotation updateAnnotation(BitbucketAnnotation annotation) {
    Path file = Paths.get(annotation.getPath());
    String relativeFileName = config.getWorkDir().relativize(file).toString();
    relativeFileName = relativeFileName.replace("\\", "/");
    annotation.setPath(relativeFileName);
    return annotation;
  }

}
