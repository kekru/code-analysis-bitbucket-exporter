package de.kekru.codeanalysisbb.shell;

import de.kekru.javautils.dependencyinjection.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ShellExecutorService {

  private static final Logger LOG = LoggerFactory.getLogger(ShellExecutorService.class);

  public String execute(List<String> commandParts) {
    String commandJoined = commandParts.stream().collect(Collectors.joining(" "));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Running command: " + commandJoined);
    }

    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command(commandParts);
    processBuilder.redirectErrorStream(true);

    Process process = null;

    try {
      process = processBuilder.start();

      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {

        String commandOutput = reader
            .lines()
            .peek(s -> {
              if (LOG.isDebugEnabled())
                LOG.debug(s);
            })
            .collect(Collectors.joining("\n"));

        int exitCode = process.waitFor();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Exit code from script: " + exitCode);
        }

        if (exitCode != 0) {
          throw new RuntimeException("Command exited with code " + exitCode + ": " +
               commandJoined +
              "\nOutput was:\n " + commandOutput);
        }

        return commandOutput;

      } catch (IOException e) {
        throw new RuntimeException("Shell Command failed: " + commandJoined, e);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Command was interrupted", e);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not start process " + commandJoined, e);
    } finally {
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }
}
