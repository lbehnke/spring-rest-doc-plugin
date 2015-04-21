spring-rest-doc-plugin
----------------------

Maven 3 plugin that creates an API documentation as JSON or XML document.

Usage:

        <plugin>
            <groupId>com.apporiented</groupId>
            <artifactId>spring-rest-doc-plugin</artifactId>
            <configuration>
                <apiBaseUrl>http://localhost:8080/api</apiBaseUrl>
                <apiVersion>${project.version}</apiVersion>
                <apiPackages>
                    <!-- Comma separated list of packes that contain DTOs and REST controllers -->
                    <!-- Optionally, the apidoc modules describes itself -->
                    com.apporiented.rest.apidoc.model
                </apiPackages>
                <apiFormat>JSON</apiFormat>
                <apiFile>${project.build.directory}/mbtht-api.json</apiFile>
                <!--
                <apiFormat>XML</apiFormat>
                <apiFile>${project.build.directory}/apidoc.xml</apiFile>
                -->
            </configuration>
            <dependencies>
                <!-- Dependencies that contain DTOs and REST controller classes go here! -->
            </dependencies>
        </plugin>
