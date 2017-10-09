package com.ems.publictransport.rest.util;

import de.schildbach.pte.AbstractNavitiaProvider;
import de.schildbach.pte.NetworkProvider;
import org.apache.deltaspike.core.api.config.ConfigProperty;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;


@RequestScoped
public class ProviderUtil {

    @Inject
    @ConfigProperty(name = "providerkey.navitia")
    private String navitiaKey;

    @Inject
    @ConfigProperty(name = "providerkey.bvg")
    private String bvgKey;

    public NetworkProvider getObjectForProvider(String providerName) {
        if(providerName == null || providerName.length() < 1)
            return null;
        try {
            Class<?> providerClass = Class.forName("de.schildbach.pte." + providerName + "Provider");
            if(providerClass.isAssignableFrom(AbstractNavitiaProvider.class))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(navitiaKey);
            }
            if(providerName.equals("Bvg"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(bvgKey);
            }
            return (NetworkProvider)providerClass.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }


}
