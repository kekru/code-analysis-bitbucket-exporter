# Code Analysis Bitbucket Exporter

Send analysis reports of PMD, checkstyle and others to Bitbucket Insights - via API, no plugin installation required

Based on [cdancy/bitbucket-rest](https://github.com/cdancy/bitbucket-rest)

## Run App

```bash
.\gradlew build run
```

## Configuration

The configuration takes place in a file called `code-analysis-bb.yml`.  
This is an example configuration

```yml
# Optional: Working directory, defaults to the current dir 
workDir: /home/me/my-project

bitbucket:
  # URL of your Bitbucket server
  endPoint: https://bitbucket.example.com/
  # Bitbucket Access Token (READ access is enough)
  token: <Bitbucket Access Token>
  # Your Bitbucket project
  # If you have a repo under your username, use "~username"
  project: some-project
  # Your Bitbucket repository inside the Bitbucket project
  repo: some-repo

reporter:
  # Optional: Add exporter for the PMD reporter
  pmd:
    # Xml files that should be exported to Bitbucket
    # Paths are relative to workDir (see above) 
    inputXmls:
      - "build/reports/pmd/main.xml"
      - "build/reports/pmd/test.xml"
    # Optional:
    # path that should be stripped out of the inputXmls filenames, otherwise Bitbucket will not find it
    # e.g. /home/me/my-project/src/main/java/de/kekru/Main.java will become src/main/java/de/kekru/Main.java
    # Defaults to workDir
    stripBasePathInputXml: /home/me/my-project
    # Optional: Whether this reporter is enabled, defaults to true
    enabled: true
    # URL-safe key to identify an analyser in Bitbucket
    key: pmd-analysis
    # Title of the report
    title: PMD Code Analysis Report
    # Name of the reporter
    reporter: PMD

  # See 'pmd'
  spotbugs:
    inputXmls:
      - "build/reports/spotbugs/main.xml"
      - "build/reports/spotbugs/test.xml"
    stripBasePathInputXml: /home/me/my-project
    enabled: true
    key: spotbugs-key
    title: Spotbugs Code Analysis Report
    reporter: Spotbugs
```

All properties can be overridden using environment variables or Java system properties.    
They need to be prefixed with `codeanalysisbb`

+ Env vars must be devided by `_`  
  `codeanalysisbb_bitbucket_token=yourToken`
+ Java system properties Env vars must be devided by `.`  
  `codeanalysisbb.bitbucket.token=yourToken`
