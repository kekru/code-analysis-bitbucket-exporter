# Code Analysis Bitbucket Exporter

Send analysis reports of PMD, checkstyle and others to Bitbucket Insights - via API, no plugin installation required

Based on [cdancy/bitbucket-rest](https://github.com/cdancy/bitbucket-rest)

## Run App

```bash
.\gradlew build run
```


## Example Configuration

`code-analysis-bb.yml`

```yml
bitbucket:
  endPoint: https://bitbucket.example.com/
  token: <Bitbucket Access Token>
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
```
