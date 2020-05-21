package de.kekru.codeanalysisbb.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class FileServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(FileServiceTest.class);

  @Parameters(name = "{index}: Modify {0} with {1}, expect {2}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "C:\\Users\\someone\\workspace\\codeanalysisbb\\src\\main\\java\\de\\kekru\\codeanalysisbb\\config\\ConfigProvider.java",
            Arrays.asList(
                "C:\\Users\\someone\\workspace\\codeanalysisbb"
            ),
            "src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "C:\\Users\\someone\\workspace\\codeanalysisbb\\src\\main\\java\\de\\kekru\\codeanalysisbb\\config\\ConfigProvider.java",
            Arrays.asList(
                "C:\\Users\\someone\\workspace\\codeanalysisbb\\",
                "C:\\Users\\someone\\workspace\\codeanalysisbb\\"
            ),
            "src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "C:\\Users\\someone\\workspace\\codeanalysisbb\\src\\main\\java\\de\\kekru\\codeanalysisbb\\config\\ConfigProvider.java",
            Arrays.asList(
                "C:/Users/someone/workspace/codeanalysisbb",
                "C:\\does-not-exist"
            ),
            "src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "src\\main\\java\\de\\kekru\\codeanalysisbb\\config\\ConfigProvider.java",
            Arrays.asList(
                "C:/Users/someone/workspace/codeanalysisbb",
                "C:\\does-not-exist"
            ),
            "src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "/home/someone/src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java",
            Arrays.asList(
                "/home/someone/src/main/java"
            ),
            "de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "/home/someone/src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java",
            Arrays.asList(
                "/home/does-not-exist/src/main/java",
                "/home/someone/src/main/java",
                "/home/someone/src/main/java"
            ),
            "de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "/home/someone/src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java",
            Arrays.asList(
                "/home/someone/src/main/java/"
            ),
            "de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
        {
            "/home/someone/src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java",
            Arrays.asList(
                null,
                "",
                "/home/someone"
            ),
            "src/main/java/de/kekru/codeanalysisbb/config/ConfigProvider.java"
        },
    });
  }

  @Parameter(0)
  public String target;

  @Parameter(1)
  public List<String> prefixes;

  @Parameter(2)
  public String result;

  @Test
  public void relativizeAndCleanupPath() {
    FileService fileservice = new FileService();

    LOG.debug(String.format("\nTarget path: %s\nStrip prefixes: %s\nExpect: %s",
        target, prefixes, result));

    String modifiedPath = fileservice
        .relativizeAndCleanupPath(target, prefixes);

    assertEquals(result, modifiedPath);
  }
}
