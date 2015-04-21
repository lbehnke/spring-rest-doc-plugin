package com.apporiented.apidoc;

import org.springframework.http.converter.HttpMessageConversionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JaxbMarshallerFactory {
    private static final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<>();
    public static final Marshaller createMarshaller(Class<?> clazz) {
        try {
            JAXBContext jaxbContext = jaxbContexts.get(clazz);
            if (jaxbContext == null) {
                try {
                    jaxbContext = JAXBContext.newInstance(clazz);
                    jaxbContexts.putIfAbsent(clazz, jaxbContext);
                }
                catch (JAXBException ex) {
                    throw new HttpMessageConversionException(
                            "Initialization of JAXBContext for class [" + clazz + "] failed: " + ex.getMessage(), ex);
                }
            }
            Marshaller marshaller = jaxbContext.createMarshaller();
            return marshaller;
        }
        catch (JAXBException ex) {
            throw new HttpMessageConversionException(
                    "Creating Marshaller for class [" + clazz + "] failed: " + ex.getMessage(), ex);
        }
    }

}
