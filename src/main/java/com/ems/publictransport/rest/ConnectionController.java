package com.ems.publictransport.rest;

import com.ems.publictransport.rest.resource.TripData;
import com.ems.publictransport.rest.util.ProviderUtil;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NvbwProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsContext;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Trip;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import java.util.Date;
import java.util.List;

@RequestScoped
@Path("connection")
@Produces(MediaType.APPLICATION_JSON)
public class ConnectionController {
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

    @Inject
    private ProviderUtil providerUtil;

    @GET
    public Response connection(@NotNull @QueryParam("from") String from, @NotNull @QueryParam("to") String to, @QueryParam("provider") String providerName,
                               @NotNull @QueryParam("product") String product, @QueryParam("timeOffset") @DefaultValue("0") int timeOffset) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        Date plannedDepartureTime = new Date();
        plannedDepartureTime.setTime(new Date().getTime() + timeOffset * 60 * 1000);
        char[] products = product.toCharArray();
        QueryTripsResult efaData = provider.queryTrips(new Location(LocationType.STATION, from), null, new Location(LocationType.STATION, to), plannedDepartureTime, true, Product.fromCodes(products), null, null, null, null);

        if (efaData.status.name().equals("OK")) {
            List<TripData> list = filterTrips(efaData.trips, from, to, "normal", plannedDepartureTime);

            if (list.size() < 1) {
                List<TripData> retryList = findMoreTrips(efaData.context, from, to, "normal", provider, plannedDepartureTime);
                if (retryList.size() < 1)
                    return Response.status(Response.Status.NOT_FOUND).entity("No trip found.").build();
                else
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(retryList).build();
            } else
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(list).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
    }


    @Path("esp")
    @GET
    public Response departureEsp(@NotNull @QueryParam("from") String from, @NotNull @QueryParam("to") String to,
                                 @QueryParam("provider") String providerName, @NotNull @QueryParam(value = "product") String product,
                                 @QueryParam("timeOffset") @DefaultValue("0") int timeOffset) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        Date plannedDepartureTime = new Date();
        plannedDepartureTime.setTime(new Date().getTime() + timeOffset * 60 * 1000);
        char[] products = product.toCharArray();
        QueryTripsResult efaData = provider.queryTrips(new Location(LocationType.STATION, from), null, new Location(LocationType.STATION, to), plannedDepartureTime, true, Product.fromCodes(products), null, null, null, null);
        if (efaData.status.name().equals("OK")) {
            List<TripData> list = filterTrips(efaData.trips, from, to, "esp", plannedDepartureTime);

            if (list.size() < 1) {
                List<TripData> retryList = findMoreTrips(efaData.context, from, to, "esp", provider, plannedDepartureTime);
                if (retryList.size() < 1)
                    return Response.status(Response.Status.NOT_FOUND).entity("No trip found.").build();
                else
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                            .entity("{\"connections\":[{\"from\":{\"departureTime\":\"" + retryList.get(0).getDepartureTime() + "\",\"plannedDepartureTimestamp\":"
                                    + retryList.get(0).getPlannedDepartureTimestamp() + ",\"delay\":" + retryList.get(0).getDepartureDelay() / 60 + ",\"to\": \""
                                    + retryList.get(0).getTo() + "\" }}]}").build();
            } else
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity("{\"connections\":[{\"from\":{\"departureTime\":\""
                        + list.get(0).getDepartureTime() + "\",\"plannedDepartureTimestamp\":" + list.get(0).getPlannedDepartureTimestamp() + ",\"delay\":"
                        + list.get(0).getDepartureDelay() / 60 + ",\"to\": \"" + list.get(0).getTo() + "\" }}]}").build();

        }
        return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
    }

    @Path("FHEM")
    @GET
    public Response departureFHEM(@NotNull @QueryParam("from") String from, @NotNull @QueryParam("to") String to,
                                 @QueryParam("provider") String providerName, @NotNull @QueryParam(value = "product") String product,
                                 @QueryParam("limit") @DefaultValue("10") int limit) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        Date plannedDepartureTime = new Date();
        char[] products = product.toCharArray();
        QueryTripsResult efaData = provider.queryTrips(new Location(LocationType.STATION, from), null, new Location(LocationType.STATION, to), plannedDepartureTime, true, Product.fromCodes(products), null, null, null, null);
        if (efaData.status.name().equals("OK")) {
            List<TripData> list = filterTrips(efaData.trips, from, to, "normal", plannedDepartureTime);
            list.addAll(findMoreTrips(efaData.context, from, to, "normal", provider, plannedDepartureTime));
            String data = convertDeparturesFHEM(list,limit);
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(data).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
    }

    private String convertDeparturesFHEM(List<TripData> stationDepartures, int limit) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        Calendar cal = Calendar.getInstance();
        int count = 0;
        for (TripData departure : stationDepartures) {
            long time = departure.getDepartureTimestamp();
            time = (time - cal.getTimeInMillis());
            float depMinutes = (float) time / 1000 / 60;
            sb.append("[\"" + departure.getNumber() + "\",\"" + departure.getTo() + "\",\"" + (int) Math.ceil(depMinutes) + "\"],");
            count++;
            if (count >= limit)
                break;
        }
        String lines = sb.toString();
        return lines.substring(0, lines.lastIndexOf(',')) + "]";
    }


    @Path("raw")
    @GET
    public List<Trip> test(@NotNull @QueryParam("from") String from, @NotNull @QueryParam("to") String to, @QueryParam("provider") String providerName,
                           @NotNull @QueryParam("product") String product, @QueryParam("timeOffset") @DefaultValue("0") int timeOffset) throws IOException {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new NvbwProvider();
        Date plannedDepartureTime = new Date();
        plannedDepartureTime.setTime(new Date().getTime() + timeOffset * 60 * 1000);
        char[] products = product.toCharArray();
        QueryTripsResult efaData = provider.queryTrips(new Location(LocationType.STATION, from), null, new Location(LocationType.STATION, to), plannedDepartureTime, true, Product.fromCodes(products), null, null, null, null);

        return efaData.trips;
    }

    private List<TripData> filterTrips(List<Trip> trips, String from, String to, String mode, Date plannedDepartureTime) {
        List<TripData> list = new ArrayList();
        for (Trip trip : trips) {
            Trip.Public leg = trip.getFirstPublicLeg();

            if (leg != null) {
                Date departureTime = leg.getDepartureTime();
                if (departureTime.after(plannedDepartureTime) && leg.departure.id.equals(from) && leg.arrival.id.equals(to) && !leg.departureStop.departureCancelled) {
                    TripData data = new TripData();
                    data.setFrom(trip.from.name);
                    data.setFromId(trip.from.id);
                    data.setTo(trip.to.name);
                    data.setToId(trip.to.id);
                    data.setProduct(leg.line.product.toString());
                    data.setNumber(leg.line.label);

                    //Planned time
                    data.setPlannedDepartureTime(df.format(leg.departureStop.plannedDepartureTime));
                    data.setPlannedDepartureTimestamp(leg.departureStop.plannedDepartureTime.getTime());

                    if (mode.equals("esp") && leg.departureStop.getDepartureDelay() / 1000 >= 60) {
                        //Correct time, because trams with delay arrive most time earlier
                        Date correctedTime = new Date(leg.departureStop.predictedDepartureTime.getTime() - 60000);
                        data.setDepartureTime(df.format((correctedTime)));
                        data.setDepartureTimestamp(correctedTime.getTime());

                    } else {
                        //Predicted time
                        if(leg.departureStop.predictedDepartureTime != null) {
                            data.setDepartureTime(df.format((leg.departureStop.predictedDepartureTime)));
                            data.setDepartureTimestamp(leg.departureStop.predictedDepartureTime.getTime());
                        }
                    }

                    if(leg.departureStop.getDepartureDelay() != null)
                        data.setDepartureDelay(leg.departureStop.getDepartureDelay() / 1000);

                    list.add(data);
                }

            }

        }
        return list;
    }


    private List<TripData> findMoreTrips(QueryTripsContext context, String from, String to, String mode, NetworkProvider provider, Date plannedDepartureTime) {
        List<TripData> data = new ArrayList();
        QueryTripsContext newContext = context;
        int count = 0;
        try {
            while (data.size() < 1) {
                if (count == 3)
                    break;
                else {
                    QueryTripsResult efaData = provider.queryMoreTrips(newContext, true);
                    newContext = efaData.context;
                    data = filterTrips(efaData.trips, from, to, mode, plannedDepartureTime);
                    count++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
