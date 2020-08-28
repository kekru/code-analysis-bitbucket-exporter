package de.kekru.codeanalysisbb.reporter.sonarqube.api;

import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates.SonarProjectStatusResult;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig.SonarqubeAuthenticationFilter;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jclouds.rest.annotations.RequestFilters;

// https://sonarcloud.io/web_api/api/qualitygates
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/qualitygates")
@RequestFilters(SonarqubeAuthenticationFilter.class)
public interface QualityGatesApi {

    @Named("api:qualitygates:project_status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("project_status")
    @GET
    SonarProjectStatusResult getProjectStatus(@QueryParam("analysisId") String analysisId);
}
