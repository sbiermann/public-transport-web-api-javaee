package com.ems.publictransport.rest;

import com.ems.publictransport.rest.util.ProviderUtil;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.VagfrProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.SuggestLocationsResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@RequestScoped
@Path("station")
public class StationController {

    @Inject
    private ProviderUtil providerUtil;

    @Path("suggest")
    @GET
    public Response suggest(@NotNull @QueryParam("q") final String query, @QueryParam("provider") String providerName,
                            @QueryParam("locationType") String stationType) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new VagfrProvider();
        if (provider == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Provider " + providerName + " not found or can not instantiated...").build();
        try {
            SuggestLocationsResult suggestLocations = provider.suggestLocations(query);
            if (SuggestLocationsResult.Status.OK.equals(suggestLocations.status)) {
                Iterator<Location> iterator = suggestLocations.getLocations().iterator();
                LocationType locationType = getLocationType(stationType);
                List<Location> resultList = new ArrayList<>();
                if (locationType == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("LocationType " + stationType + " not found or can not instantiated...").build();
                } else if (!LocationType.ANY.equals(locationType)) {
                    while (iterator.hasNext()) {
                        Location loc = iterator.next();
                        if (locationType.equals(loc.type)) {
                            resultList.add(loc);
                        }
                    }
                } else {
                    resultList.addAll(suggestLocations.getLocations());
                }
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(resultList).build();
            } else {
                return Response.status(Response.Status.REQUEST_TIMEOUT).entity("Remote Service is down or temporarily not available").build();
            }
        } catch (SocketTimeoutException e) {
            return Response.status(Response.Status.GATEWAY_TIMEOUT).entity("Timeout, Provider " + providerName + " not responding in 15 seconds").build();
        }
    }


    private LocationType getLocationType(String locationType) {
        if (locationType == null || "*".equals(locationType)) {
            return LocationType.ANY;
        } else {
            try {
                return LocationType.valueOf(locationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

}
