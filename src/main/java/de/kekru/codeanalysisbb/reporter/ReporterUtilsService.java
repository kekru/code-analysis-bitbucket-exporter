package de.kekru.codeanalysisbb.reporter;

import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketType;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporterUtilsService {

  public String getDetailsStringFromAnnotations(List<BitbucketAnnotation> annotations) {
    if (annotations.isEmpty()) {
      return "Detected no problems";
    }

    Map<BitbucketSeverity, List<BitbucketAnnotation>> mapSeverity = groupBySeverity(annotations);
    Map<BitbucketType, List<BitbucketAnnotation>> mapType = groupByType(annotations);

    String result = "Overall report summary:\n\nFindings by Severity:\n";

    result += mapSeverity.entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(entry -> entry.getKey() + " findings: " + entry.getValue().size())
        .collect(Collectors.joining("\n"));

    result += "\n\nThese findings are divided into these types:\n";
    result += mapType.entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(entry -> entry.getKey() + " findings: " + entry.getValue().size())
        .collect(Collectors.joining("\n"));

    return result + "\n\nBelow only findings in changed lines are listed\n";
  }

  public Map<BitbucketType, List<BitbucketAnnotation>> groupByType(
      List<BitbucketAnnotation> annotations) {
    return annotations.stream()
        .collect(Collectors.groupingBy(BitbucketAnnotation::getType, Collectors.toList()));
  }

  public Map<BitbucketSeverity, List<BitbucketAnnotation>> groupBySeverity(
      List<BitbucketAnnotation> annotations) {
    return annotations.stream()
        .collect(Collectors.groupingBy(BitbucketAnnotation::getSeverity, Collectors.toList()));
  }
}
