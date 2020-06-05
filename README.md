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
  # Optional: Commit id for which the report should be exported
  # Must be pushed to Bitbucket before
  # Defaults to the current commitId (by running 'git rev-parse HEAD')
  commitId: 500bf0068609dc0521b69731396b2ee7d66ce10c

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
    # Optional: QualityGate when to mark report as failed
    # Defaults to "mark failed when there is at least one high finding" 
    qualityGate:
      highCount: 1
      mediumCount: null
      lowCount: null

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
    qualityGate:
      highCount: 1
      mediumCount: null
      lowCount: null
```

All properties can be overridden using environment variables or Java system properties.    
They need to be prefixed with `codeanalysisbb`

+ Env vars must be devided by `_`  
  `codeanalysisbb_bitbucket_token=yourToken`
+ Java system properties Env vars must be devided by `.`  
  `codeanalysisbb.bitbucket.token=yourToken`


## Minimal Configuration

A minimal configuration will look like this.  
`code-analysis-bb.yml`  

```yml
bitbucket:
  endPoint: https://bitbucket.example.com/
  project: some-project
  repo: some-repo

reporter:
  pmd:
    inputXmls:
      - "build/reports/pmd/main.xml"
      - "build/reports/pmd/test.xml"
    key: pmd-analysis
    title: PMD Code Analysis Report
    reporter: PMD

  spotbugs:
    inputXmls:
      - "build/reports/spotbugs/main.xml"
      - "build/reports/spotbugs/test.xml"
    key: spotbugs-key
    title: Spotbugs Code Analysis Report
    reporter: Spotbugs
```

Then set your Bitbucket access token as environment variable:

```bash
export codeanalysisbb_bitbucket_token=yourToken
```

## Integrate in Gradle

This example configures PMD and spotbugs in Gradle and adds the `code-analysis-bitbucket-exporter` to export the results to Bitbucket insights.

`build.gradle`  

```groovy
// Add code-analysis-bitbucket-exporter from jitpack as a buildscript dependency
buildscript {
    repositories {
        mavenLocal()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        // Buildlog: https://jitpack.io/com/github/kekru/code-analysis-bitbucket-exporter/<versionnumber>/build.log
        classpath "com.github.kekru:code-analysis-bitbucket-exporter:10a17fc693a182b870cfa53d82788bb857e53e72"
    }
}

// add and configure PMD and spotbugs

plugins {
    id 'pmd'
    id "com.github.spotbugs" version "4.2.0"
}

pmd {
    // Configuration see: https://docs.gradle.org/current/dsl/org.gradle.api.plugins.quality.PmdExtension.html
    consoleOutput = true
    toolVersion = "6.21.0"
    rulePriority = 5
    ruleSets = ["category/java/errorprone.xml", "category/java/bestpractices.xml"]
    ignoreFailures = true
    sourceSets = [sourceSets.main, sourceSets.test]
}

// https://github.com/spotbugs/spotbugs-gradle-plugin#readme
spotbugs {
    toolVersion = '4.0.3'
    ignoreFailures = true
}

// add task to export the reports to Bitbucket
task exportToBitbucket {
    dependsOn pmdMain, pmdTest, spotbugsMain, spotbugsTest
    group 'verification'
    doLast {
        // set workDir, otherwise it may be anywhere in gradles cache folders
        System.setProperty("codeanalysisbb.workDir", projectDir.absolutePath)
        // set inputsXmls for reporters (can also be set in 'code-analysis-bb.yml')
        System.setProperty("codeanalysisbb.reporter.pmd.inputXmls", "build/reports/pmd/main.xml, build/reports/pmd/test.xml")
        System.setProperty("codeanalysisbb.reporter.spotbugs.inputXmls", "build/reports/spotbugs/main.xml, build/reports/spotbugs/test.xml");
        println "Send Code Analysis Report to Bitbucket"
        de.kekru.codeanalysisbb.CodeAnalysisBitbucketExporter.run()
    }
}
```

Be sure to add all other settings in `code-analysis-bb.yml`.  
Be sure the current commit is the HEAD of a branch on Bitbucket and you have an open Pull Request for that branch.

Run `./gradlew exportToBitbucket`

View the Pull Request. In the overview tab, there should be the report results.  

Tested with Bitbucket Server 6.6.3
