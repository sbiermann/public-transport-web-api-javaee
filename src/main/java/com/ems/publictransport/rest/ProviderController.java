package com.ems.publictransport.rest;

import com.ems.publictransport.rest.resource.Provider;
import de.schildbach.pte.AbstractEfaProvider;
import de.schildbach.pte.AbstractNetworkProvider;
import de.schildbach.pte.NetworkProvider;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@RequestScoped
@Path("provider")
@Produces(MediaType.APPLICATION_JSON)
public class ProviderController {

    private static Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @PostConstruct
    public void postConstruct()
    {
         logger.info("post construct called");
    }


    @GET
    public Response providerlist() throws IOException {
        List<Provider> list = new ArrayList();
        Set<Class<? extends NetworkProvider>> reflection = new Reflections("de.schildbach.pte").getSubTypesOf(NetworkProvider.class);
        for (Class<? extends NetworkProvider> implClass : reflection) {
            if(implClass.getSimpleName().startsWith("Abstract"))
                continue;
            Provider provider = new Provider();
            provider.setName(implClass.getSimpleName().substring(0, implClass.getSimpleName().indexOf("Provider")));
            provider.setClass(implClass.getSimpleName());
            list.add(provider);
        }
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(list).build();
    }

}
