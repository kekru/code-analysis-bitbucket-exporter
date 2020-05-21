package de.kekru.nociplugin.reporter;

import de.kekru.nociplugin.bitbucket.datamodel.BitbucketReport;
import de.kekru.nociplugin.config.Config;
import de.kekru.nociplugin.pmd.Pmd;
import de.kekru.nociplugin.reporter.interf.Reporter;
import de.kekru.nociplugin.serviceregistry.Service;
import de.kekru.nociplugin.spotbugs.BugCollection;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpotbugsReporter implements Reporter {

  private final Config config;

  @Override
  public BitbucketReport getBitbucketReport() {
    List<BugCollection> bugs = config.getReporter().getSpotbugs().getInputXmls()
        .stream()
        .map(File::new)
        .map(this::readSpotbugs)
        .collect(Collectors.toList());

    // Logo: https://spotbugs.github.io/images/logos/spotbugs_icon_only_zoom_256px.png

    System.out.println(bugs);
    return null;
  }

  private BugCollection readSpotbugs(File reportFile) {
    if (!reportFile.exists()) {
      throw new RuntimeException("File not found: " + reportFile);
    }

    try {
      return (BugCollection) JAXBContext.newInstance(BugCollection.class)
          .createUnmarshaller()
          .unmarshal(reportFile);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read Spotbugs report: " + reportFile, e);
    }
  }
}
