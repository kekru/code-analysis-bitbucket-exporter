# Develop, Contribute

Feel free to either write an issue or implement a pull request.

## Building Locally

To build locally run

```bash
./gradlew build publishToMavenLocal -x test 
```

The result is available under `de.kekru.codeanalysisbb:code-analysis-bitbucket-exporter:0.0.1-SNAPSHOT` in your local Maven m2 repository

## Get build from JitPack

To get build artifacts from any commit, you can use [JitPack](https://jitpack.io/).  
First create a fork of this repo and push your change to a branch.  

Assuming your fork is `github.com/your-name/code-analysis-bitbucket-exporter` and your commit id is `bcfe41f439e3bf19304ee79b01f4f07e3a2f14ca` 
then you can get the artifact like this: 

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    classpath "com.github.your-name:code-analysis-bitbucket-exporter:bcfe41f439e3bf19304ee79b01f4f07e3a2f14ca"
}
```

You can find a build log under `https://jitpack.io/com/github/your-name/code-analysis-bitbucket-exporter/bcfe41f439e3bf19304ee79b01f4f07e3a2f14ca/build.log`

JitPack will build the artifact when it is requested for the first time.

## Adding a new reporter

To add a new reporter service, you should do the following

+ add a new `public class <Xyz>Reporter implements Reporter` under [src/main/java/de/kekru/codeanalysisbb/reporter](./src/main/java/de/kekru/codeanalysisbb/reporter)
+ add a new Config for your Reporter in [Config.java](./src/main/java/de/kekru/codeanalysisbb/config/Config.java)  
  and don't forget to add it to the `getActiveReporters()` method in Config.java
+ add a test under [src/test/java/de/kekru/codeanalysisbb/reporter](./src/test/java/de/kekru/codeanalysisbb/reporter)  
  See PmdReporterIntegrationTest.java as a template
+ add the new Config to the description in [README.md](./README.md)

Now create a `code-analysis-bb.yml` with your config options in the root of the project and run

```bash
./gradlew build run -x test
```

## Dependency Injection

Dependecy injection is a very basic implementation via `ServiceRegistry.get(Class)` from [kekru/java-utils](https://github.com/kekru/java-utils).  
`Class` must be annotated with `@Service`. Then it will create an instance by calling the constructor.  
If there are parameters in the constructor, the necessary other objects will be created first.  
All created objects are singletons.  

So if you need another service, just add it as an argument to your service's constructor.

## Http Client

When calling other servers via http, then Apacha jclouds is used, because it already comes with com.cdancy:bitbucket-rest library (which is used for Bitbucket communication)  
An example for jclouds can be found under [src/main/java/de/kekru/codeanalysisbb/reporter/sonarqube](./src/main/java/de/kekru/codeanalysisbb/reporter/sonarqube)
