package org.grnet.cat.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.cat.dtos.InformativeResponse;
import org.grnet.cat.dtos.pagination.PageResource;
import org.grnet.cat.services.IntegrationService;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.grnet.cat.api.filters.Registration;
import org.grnet.cat.constraints.StringEnumeration;
import org.grnet.cat.dtos.OrganisationResponseDto;
import org.grnet.cat.enums.Source;

@Path("/v1/integrations")
@Authenticated
public class IntegrationsEndpoint {

    @Inject
    IntegrationService integrationService;

    @Tag(name = "Integration")
    @Operation(
            summary = "Retrieve a list of integration sources for Organisations.",
            description = "This endpoint returns a list of integration sources, to retrieve organisations")
    @APIResponse(
            responseCode = "200",
            description = "List of Integrations",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = List.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/organisations")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response organisationSources() {

        var integrations = integrationService.getOrganisationSources();

        return Response.ok().entity(integrations).build();
    }

    @Tag(name = "Integration")
    @Operation(
            summary = "Retrieve an organisation by querying the integration source. For ROR the organisation can be queried by Id, name, acronym , aliases. For EOSC the organisation can be queried by ID",
            description = "This endpoint returns an Organisation from a defined integration source, by providing it's Id")
    @APIResponse(
            responseCode = "200",
            description = "Organisation ",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = OrganisationResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))

    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Organisation Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "501",
            description = "Not Implemented.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/organisations/{source}/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response organisationBySource(
            @Parameter(
                    description = "The Source from where the organisation will be retrieved.",
                    required = true,
                    example = "ROR",
                    schema = @Schema(type = SchemaType.STRING))
            @Valid
            @StringEnumeration(enumClass = Source.class, message = "organisation_source")
            @PathParam("source") String source,
            @Valid
            @Size(min = 2, message = "Value must be at least 2 characters.")
            @Parameter(
                    description = "The query value to search the Organisation to be retrieved.",
                    required = true,
                    example = "00tjv0s33",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("query") String query,

            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.") @QueryParam("page") int page,
            @Context UriInfo uriInfo
    ) {
        Source enumSource = Source.valueOf(source);
        switch (enumSource) {
            case EOSC:
                OrganisationResponseDto eoscOrg = integrationService.getOrganisation(Source.valueOf(source), query);
                return Response.ok().entity(eoscOrg).build();
            case ROR:
                PageResource rorOrg = integrationService.getOrganisation(query, page, uriInfo);
                return Response.ok().entity(rorOrg).build();
            case RE3DATA:
                OrganisationResponseDto re3dataOrg = integrationService.getOrganisation(Source.valueOf(source), query);
                return Response.ok().entity(re3dataOrg).build();
            default:
                return null;
        }
    }
}