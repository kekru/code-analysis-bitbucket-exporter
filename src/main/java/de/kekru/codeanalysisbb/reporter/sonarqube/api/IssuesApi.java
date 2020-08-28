package de.kekru.codeanalysisbb.reporter.sonarqube.api;

import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarSearch;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig.SonarqubeAuthenticationFilter;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.rest.annotations.RequestFilters;


// https://sonarcloud.io/web_api/api/issues
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/issues")
@RequestFilters(SonarqubeAuthenticationFilter.class)
public interface IssuesApi {

    @Named("api:issues:search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GET
    SonarSearch search(
        @Nullable @QueryParam("p") int page,
        @Nullable @QueryParam("ps") int pageMaxSize,
        @Nullable @QueryParam("branch") String branch,
        @Nullable @QueryParam("componentKeys") String componentKeys);
}
