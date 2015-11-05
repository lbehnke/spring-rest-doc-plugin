package com.apporiented.apidoc;

import com.apporiented.rest.apidoc.factory.DocumentationFactory;
import com.apporiented.rest.apidoc.factory.impl.DefaultDocumentationFactory;
import com.apporiented.rest.apidoc.model.Documentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Mojo(name="apidoc", requiresProject=true, defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ApiDocMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject project;

    @Parameter( property = "apiVersion", defaultValue = "" )
	private String apiVersion;

    @Parameter( property = "apiBaseUrl", required = true )
    private String apiBaseUrl;

    @Parameter( property = "apiPackages", required = true )
	private String apiPackages;

    @Parameter( property = "apiFormat", defaultValue = "JSON" )
    private OutputFormat apiFormat;

    @Parameter( property = "apiFile", defaultValue = "${project.build.directory}/apidoc.json")
    private File apiFile;

    /**
     * Define which documentation properties are to be included
     * in the output file.
     */
    @Parameter( property = "apiInclude", defaultValue = "NON_NULL" )
    private JsonInclude.Include outInclude ;

    /**
     * Pretty print output document.
     */
    @Parameter( property = "apiIndent", defaultValue = "true" )
    private boolean outIndent;

    @Override
	public void execute() throws MojoExecutionException, MojoFailureException {

        DocumentationFactory documentationFactory = new DefaultDocumentationFactory();

		try {
            /* Create documentation DTO */
            prepareClasspath();
            List<String> packageList = getPackageList();
            Documentation doc = documentationFactory.createDocumentation(apiVersion, apiBaseUrl, packageList.toArray(new String[packageList.size()]));
            String docContent = null;

            /* Handle JSON output */
            if (apiFormat == OutputFormat.JSON) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
                mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
                mapper.setSerializationInclusion(outInclude);
                if (outIndent) {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                AnnotationIntrospector intro = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
                mapper.setAnnotationIntrospector(intro);
                docContent = mapper.writeValueAsString(doc);
            }

            /* Handle XML output */
            else if (apiFormat == OutputFormat.XML) {
                Marshaller marshaller = JaxbMarshallerFactory.createMarshaller(Documentation.class);
                if (outIndent) {
                    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                }
                StringWriter writer = new StringWriter();
                marshaller.marshal(doc, writer);
                docContent = writer.getBuffer().toString();
            }

            /* Write to file */
            Files.write(docContent, apiFile, Charsets.UTF_8);

		} catch (Exception e) {
            throw new MojoExecutionException("Creating API documentation failed. Please check apidoc annotations.", e);
		}
		
	}

    private List<String> getPackageList() {
        List<String> packageList = new ArrayList<>();
        String[] elems = apiPackages.split("[ ,;\\s]+");
        for (String elem : elems) {
            elem = elem.trim();
            if (elem.length() > 0) {
                packageList.add(elem);
                getLog().info("Registered package " + elem);
            }
        }
        return packageList;
    }

    private void prepareClasspath() throws DependencyResolutionRequiredException, MalformedURLException {
        getLog().info("Preparing classpath...");
        List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
        URL[] runtimeClasspathElementsUrls = new URL[runtimeClasspathElements.size()];
        for (int i = 0; i < runtimeClasspathElementsUrls.length; i++) {
            String urlStr = runtimeClasspathElements.get(i);
            getLog().info("   classpath URL: " + urlStr);
            runtimeClasspathElementsUrls[i] = new File(urlStr).toURI().toURL();
        }
        URLClassLoader urlClassLoader = new URLClassLoader(runtimeClasspathElementsUrls, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }


    public enum OutputFormat {
        JSON, XML
    }
}
