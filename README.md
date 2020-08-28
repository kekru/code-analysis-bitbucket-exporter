# Code Analysis Bitbucket Exporter

Send analysis reports of PMD, Spotbugs, Sonarqube and others to Bitbucket Code Insights - via API, no plugin installation required

Based on [cdancy/bitbucket-rest](https://github.com/cdancy/bitbucket-rest)  
View [Bitbucket Code Insights](https://confluence.atlassian.com/bitbucketserver/code-insights-966660485.html) to find out more about the Bitbucket feature. 

The workflow is always:

+ Create the reports with your standard tools
+ Use this exporter to send the reports to Bitbucket Insights.  
  You always need an open Pull-Request in Bitbucket, otherwise results will not be shown

Tested with Bitbucket Server 6.6.3

Currently supported analysis reports:

+ [PMD](https://pmd.github.io/)
+ [Spotbugs](https://spotbugs.github.io/)
+ [Sonarqube](https://www.sonarqube.org/)

## Develop

If you want to develop or contribute to this project, see [CONTRIBUTE.md](./CONTRIBUTE.md)

## Configuration

The configuration takes place in a file called `code-analysis-bb.yml`.  
This is an example configuration

```yml
# Optional: Working directory, defaults to the current dir 
workDir: /home/me/my-project
# Optional: Whether to exit with an exception (or error exit code), when the quality gate is broken, default to false
breakExecutionOnQualityGate: false

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
    # Optional:
    # path that should be added as prefix to source files, after being shortened by "stripBasePathInputXml"
    # e.g. src/main/java/de/kekru/Main.java will become new/sub-directory/src/main/java/de/kekru/Main.java
    # Defaults to empty string 
    addBasePathPrefix: new/sub-directory
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
    addBasePathPrefix:
    enabled: true
    key: spotbugs-key
    title: Spotbugs Code Analysis Report
    reporter: Spotbugs
    qualityGate:
      highCount: 1
      mediumCount: null
      lowCount: null

  sonarqube:
    enabled: true
    stripBasePathInputXml: /home/me/my-project
    addBasePathPrefix:
    key: sonar-key
    title: Sonarqube Report
    reporter: Sonarqube
    # Optional: URL of your Sonarqube server
    # Defaults to the value of "serverUrl" in report-task.txt
    serverUrl: https://sonarqube.example.com
    # Login Access Token for Sonarqube
    login: <Login Token in Sonarqube>
    # Optional: Key of the analysed project in Sonarqube
    # Defaults to the value of "projectKey" in report-task.txt
    projectKey: "my-project-name-in-sonarqube"
    # Optional: Branch of the analysed project in Sonarqube
    # Defaults to "master"
    branch: master
    # report-task.txt file that was created when sonarqube analysis finished
    # "ceTaskId" from inside the file is required
    # Path is relative to workDir (see above) 
    reportTaskFile: "build/sonar/report-task.txt"
```

All properties can be overridden using environment variables or Java system properties.    
They need to be prefixed with `codeanalysisbb`

+ Env vars must be devided by `_`  
  `codeanalysisbb_bitbucket_token=yourToken`
+ Java system properties Env vars must be devided by `.`  
  `codeanalysisbb.bitbucket.token=yourToken`

If you have another location for your config file set the location in an env var or Java system property with name `codeAnalysisBBConfigFile`.  
e.g. `export codeAnalysisBBConfigFile="some/other/dir/code-analysis-bb.yml"`

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

## Artifacts served via JitPack

The artifacts are served via JitPak. Be sure to add it as remote repository for Gradle and Maven

```groovy
buildscript {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.github.kekru:code-analysis-bitbucket-exporter:0.1.0"
    }
}
```

## Integrate in Gradle

This example configures PMD and spotbugs in Gradle and adds the `code-analysis-bitbucket-exporter` to export the results to Bitbucket insights.

`build.gradle`  

```groovy
// Add code-analysis-bitbucket-exporter from jitpack as a buildscript dependency
buildscript {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        // Buildlog: https://jitpack.io/com/github/kekru/code-analysis-bitbucket-exporter/<versionnumber>/build.log
        classpath "com.github.kekru:code-analysis-bitbucket-exporter:0.1.0"
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

## Integrate in Maven

This example configures PMD and spotbugs in Gradle and adds the `code-analysis-bitbucket-exporter` to export the results to Bitbucket insights.

pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>...</groupId>
  <artifactId>...</artifactId>
  <packaging>...</packaging>
  <version>...</version>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.source>1.8</maven.compiler.source>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.6.0</version>
        <executions>
          <execution>
            <id>exportToBitbucket</id>
            <goals>
              <goal>java</goal>
            </goals>
            <configuration>
              <includeProjectDependencies>false</includeProjectDependencies>
              <includePluginDependencies>true</includePluginDependencies>
              <mainClass>de.kekru.codeanalysisbb.CodeAnalysisBitbucketExporter</mainClass>
              <systemProperties>
                <systemProperty>
                  <key>codeanalysisbb.workDir</key>
                  <value>${project.basedir}</value>
                </systemProperty>
                <!-- set inputsXmls for reporters (can also be set in 'code-analysis-bb.yml') -->
                <systemProperty>
                  <key>codeanalysisbb.reporter.pmd.inputXmls</key>
                  <value>target/pmd.xml</value>
                </systemProperty>
                <systemProperty>
                  <key>codeanalysisbb.reporter.spotbugs.inputXmls</key>
                  <value>target/spotbugs-detailed.xml</value>
                </systemProperty>
              </systemProperties>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>com.github.kekru</groupId>
            <artifactId>code-analysis-bitbucket-exporter</artifactId>
            <version>0.1.0</version>
          </dependency>
          <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>25.1-jre</version>
          </dependency>
        </dependencies>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-site-plugin</artifactId>
        <version>3.9.0</version>
      </plugin>
    </plugins>
  </build>

  <reporting>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-pmd-plugin</artifactId>
        <version>3.13.0</version>
        <configuration>
          <includeTests>true</includeTests>
          <rulesets>category/java/errorprone.xml,category/java/bestpractices.xml</rulesets>
        </configuration>
      </plugin>
      <plugin>
        <groupId>com.github.spotbugs</groupId>
        <artifactId>spotbugs-maven-plugin</artifactId>
        <version>4.0.0</version>
        <configuration>
          <includeTests>true</includeTests>
          <effort>Max</effort>
          <spotbugsXmlOutput>true</spotbugsXmlOutput>
          <spotbugsXmlOutputFilename>spotbugs-detailed.xml</spotbugsXmlOutputFilename>
          <failOnError>false</failOnError>
        </configuration>
      </plugin>
    </plugins>
  </reporting>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>1.7.9</version>
    </dependency>
  </dependencies>

  <pluginRepositories>
    <pluginRepository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </pluginRepository>
  </pluginRepositories>
</project>
```

Be sure to add all other settings in `code-analysis-bb.yml`.  
Be sure the current commit is the HEAD of a branch on Bitbucket and you have an open Pull Request for that branch.

Run `mvn package site exec:java@exportToBitbucket`.  
`site` creates the reports and `exec:java@exportToBitbucket` sends them to Bitbucket.

View the Pull Request. In the overview tab, there should be the report results.


## Integrate in Gradle with Sonarqube


This example configures Sonarqube in Gradle and adds the `code-analysis-bitbucket-exporter` to export the results to Bitbucket insights.

`build.gradle`  

```groovy
// Add code-analysis-bitbucket-exporter from jitpack as a buildscript dependency
buildscript {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.github.kekru:code-analysis-bitbucket-exporter:0.1.0"
    }
}

plugins {
    id "org.sonarqube" version "2.6.2"
}

sonarqube {
    properties {
        property 'sonar.jacoco.reportPaths', 'build/jacoco/test.exec'
        property 'sonar.junit.reportPaths', 'build/test-results/test'
        property "sonar.sourceEncoding", "UTF-8"
        property "sonar.host.url", "https://sonarqube.example.com"
        property "sonar.login", "123456789"
        property "sonar.verbose", "true"
        property "sonar.issuesReport.html.enable", "true"
        property "sonar.projectKey", "my-project-name-in-sonarqube"
    }
}

// add task to export the reports to Bitbucket
task exportToBitbucket {
    group 'verification'
    doLast {
        // set workDir, otherwise it may be anywhere in gradles cache folders
        System.setProperty("codeanalysisbb.workDir", projectDir.absolutePath)
        // set reportTaskFile location (can also be set in 'code-analysis-bb.yml')
        System.setProperty("codeanalysisbb.reporter.sonarqube.reportTaskFile", "build/sonar/report-task.txt")
        println "Send Code Analysis Report to Bitbucket"
        de.kekru.codeanalysisbb.CodeAnalysisBitbucketExporter.run()
    }
}
```

Be sure to add all other settings in `code-analysis-bb.yml`.  
Be sure the current commit is the HEAD of a branch on Bitbucket and you have an open Pull Request for that branch.

Run `./gradlew sonarqube` to run sonarqube analysis.  
When analysis is done, a file `build/sonar/report-task.txt` is created.  
Now you can run `./gradlew exportToBitbucket`.  

View the Pull Request. In the overview tab, there should be the report results.

Tested with Sonarqube 7.9.4
