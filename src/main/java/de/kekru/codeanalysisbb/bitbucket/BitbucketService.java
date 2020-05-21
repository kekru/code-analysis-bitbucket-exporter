package de.kekru.codeanalysisbb.bitbucket;

import com.cdancy.bitbucket.rest.domain.common.Error;
import com.cdancy.bitbucket.rest.domain.common.RequestStatus;
import com.cdancy.bitbucket.rest.domain.insights.InsightReport;
import com.cdancy.bitbucket.rest.features.InsightsApi;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.BitbucketConfig;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import de.kekru.codeanalysisbb.serviceregistry.Service;
import de.kekru.codeanalysisbb.utils.FileService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@RequiredArgsConstructor
@Service
public class BitbucketService {

  private static final Logger LOG = LoggerFactory.getLogger(BitbucketService.class);
  private final Config config;
  private final BitbucketThirdPartyService bitbucketThirdPartyService;
  private final FileService fileService;

  public void send(BitbucketReport report) {

    BitbucketConfig bitbucketConfig = config.getBitbucket();
    ReporterConfig reporterConfig = report.getReporterConfig();

    CreateInsightReport createInsightReport = CreateInsightReport.create(
        report.getDetails(),
        report.getLink(),
        report.getLogoUrl(),
        report.getResult(),
        reporterConfig.getTitle(),
        reporterConfig.getReporter(),
        report.getInsightData()
    );

    InsightsApi insightsApi = bitbucketThirdPartyService.getInsightsApi();

    LOG.debug("Sending InsightsApi Report Request: " + createInsightReport);

    InsightReport insightsReportResponse = insightsApi
        .createReport(
            bitbucketConfig.getProject(),
            bitbucketConfig.getRepo(),
            bitbucketConfig.getCommitId(),
            reporterConfig.getKey(),
            createInsightReport
        );

    handleErrors(insightsReportResponse.errors());
    LOG.debug("InsightsReport Response: " + insightsReportResponse);

    CreateAnnotations createAnnotations = CreateAnnotations.create(
        report.getAnnotations()
            .stream()
            .map(a -> updateAnnotation(a, reporterConfig))
            .map(BitbucketAnnotation::toAnnotation)
            .collect(Collectors.toList())
    );

    LOG.debug("Sending Annotations Request: " + createAnnotations);

    RequestStatus annotationsResponse = insightsApi
        .createAnnotations(
            bitbucketConfig.getProject(),
            bitbucketConfig.getRepo(),
            bitbucketConfig.getCommitId(),
            reporterConfig.getKey(),
            createAnnotations
        );

    handleErrors(annotationsResponse.errors());
    LOG.debug("Annotations Response: " + annotationsResponse);
  }

  protected BitbucketAnnotation updateAnnotation(BitbucketAnnotation annotation, ReporterConfig reporterConfig) {
    String pathString = annotation.getPath();

    String relativeFileName = fileService.relativizeAndCleanupPath(pathString, Arrays.asList(
        reporterConfig.getStripBasePathInputXml(),
        config.getWorkDir().toString()
    ));

    annotation.setPath(relativeFileName);
    return annotation;
  }

  private void handleErrors(List<Error> errors) {
    if (errors == null || errors.isEmpty()) {
      return;
    }

    throw new RuntimeException("Bitbucket request has erros: \n" +
        errors
            .stream()
            .map(String::valueOf)
            .collect(Collectors.joining(" \n")));
  }
}
