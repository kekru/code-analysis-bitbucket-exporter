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
import de.kekru.javautils.dependencyinjection.Service;
import de.kekru.codeanalysisbb.utils.CodeAnalysisBitbucketException;
import de.kekru.codeanalysisbb.utils.FileService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        shorten(report.getDetails(), 2000),
        report.getLink(),
        report.getLogoUrl(),
        report.getResult(),
        shorten(reporterConfig.getTitle(), 450),
        shorten(reporterConfig.getReporter(), 450),
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

    if (report.getAnnotations().isEmpty()) {
      LOG.debug("No findings present. Not sending annotations for report.");

    } else {

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
  }

  protected BitbucketAnnotation updateAnnotation(BitbucketAnnotation annotation, ReporterConfig reporterConfig) {
    String pathString = annotation.getPath();

    String relativeFileName = fileService.relativizeAndCleanupPath(pathString, Arrays.asList(
        reporterConfig.getStripBasePathInputXml(),
        config.getWorkDir()
    ));

    annotation.setMessage(shorten(annotation.getMessage(), 2000));
    annotation.setPath(relativeFileName);
    return annotation;
  }

  private String shorten(String s, int maxLength) {
    if (StringUtils.length(s) > maxLength) {
      return StringUtils.substring(s, 0, maxLength);
    }
    return s;
  }

  private void handleErrors(List<Error> errors) {
    if (errors == null || errors.isEmpty()) {
      return;
    }

    throw new CodeAnalysisBitbucketException("Bitbucket request has erros: \n" +
        errors
            .stream()
            .map(String::valueOf)
            .collect(Collectors.joining(" \n")));
  }
}
