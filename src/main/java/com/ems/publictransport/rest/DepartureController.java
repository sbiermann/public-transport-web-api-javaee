package com.ems.publictransport.rest;

import com.ems.publictransport.rest.resource.DepartureData;
import com.ems.publictransport.rest.util.ProviderUtil;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.VagfrProvider;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
@Path("departure")
@Produces(MediaType.APPLICATION_JSON)
public class DepartureController {
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

    private static int counter = 0;

    @Inject
    @ConfigProperty(name = "thingsspeak.key")
    private String thingspeakKey;

    @Inject
    @ConfigProperty(name = "thingsspeak.channel")
    private String thingspeakChannel;

    @Inject
    private ProviderUtil providerUtil;
    
    @GET
    public Response departure(@NotNull @QueryParam("from") String from, @QueryParam("provider") String providerName, @QueryParam("limit") @DefaultValue("10") int limit) throws IOException {
        try {
            NetworkProvider provider = getNetworkProvider(providerName);
            if (provider == null)
                return Response.status(Response.Status.NOT_FOUND).entity("Provider " + providerName + " not found or can not instantiated...").build();
            QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
            if (efaData.status.name().equals("OK")) {
                List<DepartureData> list = new ArrayList<>();
                if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                    for (StationDepartures stationDeparture : efaData.stationDepartures) {
                        list.addAll(convertDepartures(stationDeparture));
                    }
                    Collections.sort(list);
                } else
                    list.addAll(convertDepartures(efaData.findStationDepartures(from)));
                if (list.size() > limit)
                    list = list.subList(0, limit);
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(list).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
        } finally {
            counter++;
        }
    }

    @Path("FHEM")
    @GET
    public Response departureFHEM(@NotNull @QueryParam(value = "from") String from, @QueryParam("provider") String providerName,
                                  @QueryParam("limit") @DefaultValue("10") int limit) throws IOException {
        try {
            NetworkProvider provider = getNetworkProvider(providerName);
            if (provider == null)
                return Response.status(Response.Status.NOT_FOUND).entity("Provider " + providerName + " not found or can not instantiated...").build();
            QueryDeparturesResult efaData = provider.queryDepartures(from, new Date(), 120, true);
            if (efaData.status.name().equals("OK")) {
                String data = "";
                if (efaData.findStationDepartures(from) == null && !efaData.stationDepartures.isEmpty()) {
                    List<DepartureData> list = new ArrayList<>();
                    for (StationDepartures stationDeparture : efaData.stationDepartures) {
                        list.addAll(convertDepartures(stationDeparture));
                    }
                    Collections.sort(list);
                    StringBuffer sb = new StringBuffer();
                    sb.append("[");
                    int count = 0;
                    for (DepartureData departureData : list) {
                        sb.append("[\"" + departureData.getNumber() + "\",\"" + departureData.getTo() + "\",\"" + departureData.getDepartureTimeInMinutes() + "\"],");
                        count++;
                        if (count >= limit)
                            break;
                    }
                    String lines = sb.toString();
                    data = lines.substring(0, lines.lastIndexOf(',')) + "]";
                } else
                    data = convertDeparturesFHEM(efaData.findStationDepartures(from), limit);
                if(data == null || data.isEmpty())
                    return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(data).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("EFA error status: " + efaData.status.name()).build();
        } finally {
            counter++;
        }

    }

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void sendStatistics() {
        if (thingspeakKey == null || thingspeakKey.isEmpty())
            return;
        String url = "http://api.thingspeak.com/update?key=";
        url += thingspeakKey;
        url += "&" + thingspeakChannel + "=";
        url += counter;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        counter = 0;

    }

    private NetworkProvider getNetworkProvider(String providerName) {
        NetworkProvider provider;
        if (providerName != null) {
            provider = providerUtil.getObjectForProvider(providerName);
        } else
            provider = new VagfrProvider();
        return provider;
    }

    private String convertDeparturesFHEM(StationDepartures stationDepartures, int limit) {
        if(stationDepartures == null)
            return null;
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        Calendar cal = Calendar.getInstance();
        int count = 0;
        for (Departure departure : stationDepartures.departures) {
            long time = 0;
            if (departure.predictedTime != null && departure.predictedTime.after(departure.plannedTime)) {
                time = departure.predictedTime.getTime();
            } else {
                time = departure.plannedTime.getTime();
            }
            time = (time - cal.getTimeInMillis());
            float depMinutes = (float) time / 1000 / 60;
            sb.append("[\"" + departure.line.label + "\",\"" + departure.destination.name + "\",\"" + (int) Math.ceil(depMinutes) + "\"],");
            count++;
            if (count >= limit)
                break;
        }
        String lines = sb.toString();
        return lines.substring(0, lines.lastIndexOf(',')) + "]";
    }


    private List<DepartureData> convertDepartures(StationDepartures stationDepartures) {
        Calendar cal = Calendar.getInstance();
        List<DepartureData> list = new ArrayList();
        for (Departure departure : stationDepartures.departures) {
            DepartureData data = new DepartureData();
            data.setTo(departure.destination.name);
            data.setToId(departure.destination.id);
            if(departure.line.product != null)
                data.setProduct(departure.line.product.toString());
            else
                data.setProduct("Unknown");
            data.setNumber(departure.line.label);
            if (departure.position != null)
                data.setPlatform(departure.position.name);
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

}
