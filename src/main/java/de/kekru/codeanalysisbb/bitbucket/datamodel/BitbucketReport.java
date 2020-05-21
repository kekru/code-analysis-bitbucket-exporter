package de.kekru.codeanalysisbb.bitbucket.datamodel;

import com.cdancy.bitbucket.rest.domain.insights.InsightReportData;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BitbucketReport {

  private final ReporterConfig reporterConfig;

  private final String details;

  private String link;

  private String logoUrl;

  private final CreateInsightReport.RESULT result;

  @Builder.Default
  private final List<InsightReportData> insightData = new LinkedList<>();

  @Builder.Default
  private final List<BitbucketAnnotation> annotations = new LinkedList<>();

  public void addInsightData(InsightReportData entry) {
    insightData.add(entry);
  }

  public void addAnnotation(BitbucketAnnotation annotation) {
    annotations.add(annotation);
  }
}
