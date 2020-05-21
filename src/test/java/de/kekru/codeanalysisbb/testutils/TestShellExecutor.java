package de.kekru.codeanalysisbb.testutils;

import de.kekru.codeanalysisbb.shell.ShellExecutorService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestShellExecutor extends ShellExecutorService {

  private static final Logger LOG = LoggerFactory.getLogger(TestShellExecutor.class);

  @Setter
  private String mockedGitCommitId;

  @Override
  public String execute(List<String> commandParts) {
    String command = commandParts.stream().collect(Collectors.joining(" "));

    if (command.equals("git rev-parse HEAD")) {
      LOG.debug("Return mocked git commit id: " + mockedGitCommitId);
      return mockedGitCommitId;
    }

    return super.execute(commandParts);
  }
}
