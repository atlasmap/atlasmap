/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.csv.service;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.csv.core.CsvConfig;
import io.atlasmap.csv.core.CsvFieldReader;
import io.atlasmap.csv.v2.CsvInspectionResponse;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/csv/")
public class CsvService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvService.class);

    @Context
    private ResourceContext resourceContext;

    protected byte[] toJson(Object value) {
        try {
            return Json.mapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Simple", description = "Simple hello service")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Return a response"))
    public String simpleHelloWorld(@QueryParam("from") String from) {
        return "Got it! " + from;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    @Operation(summary = "Inspect CSV", description = "Inspect a CSV instance and return a Document object")
    @RequestBody(description = "Csv", content = @Content(mediaType = "text/csv", schema = @Schema(implementation = String.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CsvInspectionResponse.class)),
        description = "Return a Document object"))
    public Response inspect(InputStream request, @QueryParam("format") String format, @QueryParam("delimiter") String delimiter,
                            @QueryParam("firstRecordAsHeader") Boolean firstRecordAsHeader,
                            @QueryParam("skipRecordHeader") Boolean skipHeaderRecord,
                            @QueryParam("headers") String headers,
                            @QueryParam("commentMarker") String commentMarker,
                            @QueryParam("escape") String escape,
                            @QueryParam("ignoreEmptyLines") Boolean ignoreEmptyLines,
                            @QueryParam("ignoreHeaderCase") Boolean ignoreHeaderCase,
                            @QueryParam("ignoreSurroundingSpaces") Boolean ignoreSurroundingSpaces,
                            @QueryParam("nullString") String nullString,
                            @QueryParam("quote") String quote,
                            @QueryParam("allowDuplicateHeaderNames") Boolean allowDuplicateHeaderNames,
                            @QueryParam("allowMissingColumnNames") Boolean allowMissingColumnNames) throws IOException  {
        long startTime = System.currentTimeMillis();

        CsvInspectionResponse response = new CsvInspectionResponse();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Options: delimiter={}, firstRecordAsHeader={}", delimiter, firstRecordAsHeader);
            }
            CsvConfig csvConfig = new CsvConfig(format);
            if (delimiter != null) {
                csvConfig.setDelimiter(delimiter.charAt(0));
            }
            csvConfig.setFirstRecordAsHeader(firstRecordAsHeader);
            csvConfig.setSkipHeaderRecord(skipHeaderRecord);
            csvConfig.setHeaders(headers);
            if (commentMarker != null) {
                csvConfig.setCommentMarker(commentMarker.charAt(0));
            }
            if (escape != null) {
                csvConfig.setEscape(escape.charAt(0));
            }
            csvConfig.setIgnoreEmptyLines(ignoreEmptyLines);
            csvConfig.setIgnoreHeaderCase(ignoreHeaderCase);
            csvConfig.setIgnoreSurroundingSpaces(ignoreSurroundingSpaces);
            csvConfig.setNullString(nullString);
            if (quote != null) {
                csvConfig.setQuote(quote.charAt(0));
            }
            csvConfig.setAllowDuplicateHeaderNames(allowDuplicateHeaderNames);
            csvConfig.setAllowMissingColumnNames(allowMissingColumnNames);

            CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
            csvFieldReader.setDocument(request);

            Document document = csvFieldReader.readSchema();
            response.setCsvDocument(document);
            request.close();
        } catch (Exception e) {
            LOG.error("Error inspecting CSV: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            request.close();;
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(("Response: {}" + new ObjectMapper().writeValueAsString(response)));
        }
        return Response.ok().entity(toJson(response)).build();
    }
}
