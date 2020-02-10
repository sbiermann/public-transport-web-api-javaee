package com.ems.publictransport.rest.util;

import de.schildbach.pte.AbstractNavitiaProvider;
import de.schildbach.pte.NetworkProvider;
import okhttp3.HttpUrl;
import org.apache.deltaspike.core.api.config.ConfigProperty;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;


@RequestScoped
public class ProviderUtil {

    @Inject
    @ConfigProperty(name = "providerkey.navitia")
    private String navitiaKey;

    @Inject
    @ConfigProperty(name = "providerkey.bvg")
    private String bvgKey;

    @Inject
    @ConfigProperty(name = "providerkey.oebb")
    private String oebbKey;

    @Inject
    @ConfigProperty(name = "providerkey.nvv")
    private String nvvKey;

    @Inject
    @ConfigProperty(name = "providerkey.invg")
    private String invgKey;

    @Inject
    @ConfigProperty(name = "providerkey.vgn")
    private String vgnKey;

    @Inject
    @ConfigProperty(name = "providerkey.sh")
    private String shKey;

    @Inject
    @ConfigProperty(name = "providerkey.gvh")
    private String gvhKey;

    @Inject
    @ConfigProperty(name = "providerkey.vbn")
    private String vbnKey;

    @Inject
    @ConfigProperty(name = "providerkey.vmt")
    private String vmtKey;

    @Inject
    @ConfigProperty(name = "providerkey.vrr")
    private String vrrKey;

    @Inject
    @ConfigProperty(name = "providerkey.avvaachen")
    private String avvaachenKey;

    @Inject
    @ConfigProperty(name = "providerkey.vvs")
    private String vvsKey;

    @Inject
    @ConfigProperty(name = "providerkey.kvv")
    private String kvvKey;

    @Inject
    @ConfigProperty(name = "providerkey.zvv")
    private String zvvKey;

    @Inject
    @ConfigProperty(name = "providerkey.lu")
    private String luKey;

    @Inject
    @ConfigProperty(name = "providerkey.dsb")
    private String dsbKey;

    @Inject
    @ConfigProperty(name = "providerkey.se")
    private String seKey;

    @Inject
    @ConfigProperty(name = "providerkey.bahn")
    private String bahnKey;

    @Inject
    @ConfigProperty(name = "providerkey.nasa")
    private String nasaKey;

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
            if(providerName.equals("Oebb"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(oebbKey);
            }
            if(providerName.equals("Nvv"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(nvvKey);
            }
            if(providerName.equals("Invg"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(invgKey);
            }
            if(providerName.equals("Vgn"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(HttpUrl.class).newInstance(HttpUrl.parse(vgnKey));
            }
            if(providerName.equals("Sh"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(shKey);
            }
            if(providerName.equals("Gvh"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(HttpUrl.class).newInstance(HttpUrl.parse(gvhKey));
            }
            if(providerName.equals("Vbn"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(vbnKey);
            }
            if(providerName.equals("Vmt"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(vmtKey);
            }
            if(providerName.equals("Vrr"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(HttpUrl.class).newInstance(HttpUrl.parse(vrrKey));
            }
            if(providerName.equals("AvvAachen"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(avvaachenKey);
            }
            if(providerName.equals("Vvs"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(HttpUrl.class).newInstance(HttpUrl.parse(vvsKey));
            }
            if(providerName.equals("Kvv"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(HttpUrl.class).newInstance(HttpUrl.parse(kvvKey));
            }
            if(providerName.equals("Zvv"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(zvvKey);
            }
            if(providerName.equals("Lu"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(luKey);
            }
            if(providerName.equals("Dsb"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(dsbKey);
            }
            if(providerName.equals("Se"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(seKey);
            }
            if(providerName.equals("Nasa"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class).newInstance(nasaKey);
            }
            if(providerName.equals("Db"))
            {
                return  (NetworkProvider)providerClass.getDeclaredConstructor(String.class, byte[].class).newInstance(bahnKey,"bdI8UVj40K5fvxwf".getBytes("UTF-8"));
            }
            return (NetworkProvider)providerClass.getDeclaredConstructor().newInstance();
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
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


}
