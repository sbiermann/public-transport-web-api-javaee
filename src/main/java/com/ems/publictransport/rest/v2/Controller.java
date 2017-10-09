/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ems.publictransport.rest.v2;

import com.ems.publictransport.rest.v2.model.DepartureData;
import com.ems.publictransport.rest.v2.model.Provider;
import com.ems.publictransport.rest.v2.model.ProviderEnum;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;
import de.schildbach.pte.dto.SuggestLocationsResult;

import javax.enterprise.context.RequestScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author constantin
 */
@RequestScoped
@Path("v2")
@Produces(MediaType.APPLICATION_JSON)
public class Controller {


    @Path("provider")
    @GET
    public Response providerlist() throws IOException {
        List<Provider> list = new ArrayList();
        for (ProviderEnum each : ProviderEnum.values()) {
            list.add(each.asProvider());
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(list).build();
    }

    @Path("station/nearby")
    @GET
    public Response findNearbyLocations(@QueryParam("provider") String providerName) {
        NetworkProvider networkProvider = getNetworkProvider(providerName);
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(networkProvider != null ? networkProvider.defaultProducts() : "").build();
    }


    @Path("station/suggest")
    @GET
    public Response suggest(@NotNull @QueryParam("q") final String query, @QueryParam("provider") String providerName,
                            @QueryParam("locationType") String stationType) throws IOException {
        NetworkProvider provider = getNetworkProvider(providerName);
        if (provider == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Provider " + providerName + " not found or can not instantiated...").build();

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
    }

    @Path("departure")
    @GET
    public Response departure(@NotNull @QueryParam("from") String from, @QueryParam("provider") String providerName,
                              @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("numberFilter") String numberFilter,
                              @QueryParam("toFilter") String toFilter) throws IOException {
        NetworkProvider provider = getNetworkProvider(providerName);
        if (provider == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Provider " + providerName + " not found or can not instantiated...").build();
        }
        QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
        if (efaData.status.name().equals("OK")) {
            List<DepartureData> list = new ArrayList<>();
            if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                for (StationDepartures stationDeparture : efaData.stationDepartures) {
                    list.addAll(convertDepartures(stationDeparture, numberFilter, toFilter));
                }
                Collections.sort(list);
            } else {
                list.addAll(convertDepartures(efaData.findStationDepartures(from), numberFilter, toFilter));
            }
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(list).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();

    }

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");


    private List<DepartureData> convertDepartures(StationDepartures stationDepartures, String numberFilter, String toFilter) {
        Calendar cal = Calendar.getInstance();
        List<DepartureData> list = new ArrayList();
        for (Departure departure : stationDepartures.departures) {
            if (!isIncluded(departure, numberFilter, toFilter)) {
                continue;
            }
            DepartureData data = new DepartureData();
            data.setMessage(departure.message);
            data.setTo(departure.destination.name);
            data.setToId(departure.destination.id);
            data.setProduct(departure.line.product.toString());
            data.setNumber(departure.line.label);
            if (departure.position != null) {
                data.setPlatform(departure.position.name);
            }
            long time;
            //Predicted time
            if (departure.predictedTime != null && departure.predictedTime.after(departure.plannedTime)) {
                data.setDepartureTime(df.format(departure.predictedTime));
                data.setDepartureTimestamp(departure.predictedTime.getTime());
                data.setDepartureDelay((departure.predictedTime.getTime() - departure.plannedTime.getTime()) / 1000 / 60);
                time = departure.predictedTime.getTime();
            } else {
                data.setDepartureTime(df.format(departure.plannedTime));
                data.setDepartureTimestamp(departure.plannedTime.getTime());
                time = departure.plannedTime.getTime();
            }
            time = (time - cal.getTimeInMillis());
            float depMinutes = (float) time / 1000 / 60;
            data.setDepartureTimeInMinutes((int) Math.ceil(depMinutes));
            list.add(data);
        }
        return list;
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

    private boolean isIncluded(Departure stationDeparture, String numberFilter, String toFilter) {
        if (toFilter != null && !toFilter.equals("*")) {
            Location dest = stationDeparture.destination;
            if (!(dest != null && dest.name != null && toFilter.equalsIgnoreCase(dest.name))) {
                return false;
            }
        }
        if (numberFilter != null && !numberFilter.equals("*")) {
            Line line = stationDeparture.line;
            if (!(line != null && line.label != null && numberFilter.equalsIgnoreCase(line.label))) {
                return false;
            }
        }
        return true;
    }

    private NetworkProvider getNetworkProvider(String providerName) {
        try {
            if (providerName != null) {
                ProviderEnum provider = ProviderEnum.valueOf(providerName.toUpperCase());
                return provider.newNetworkProvider();
            }
        } catch (RuntimeException e) {
        }
        return null;
    }

}
