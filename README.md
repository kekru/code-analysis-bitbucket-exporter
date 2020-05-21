# no-ci-plugin

CI Integration zwischen Bitbucket, Jenkins, Sonar... nur über Http Apis und keine Plugins  

Based on [cdancy/bitbucket-rest](https://github.com/cdancy/bitbucket-rest)

## Run App

```bash
.\gradlew build run
```


## Example Configuration

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
    key: no-ci-plugin pmd
    title: PMD Code Analysis Report
    reporter: PMD
```
