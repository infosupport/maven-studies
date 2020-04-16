package org.apache.maven.wrapper;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.Properties;

/**
 * @author Hans Dockter
 */
public class MavenWrapperMain
{                                                  
    private static final String POM_PROPERTIES = "/META-INF/maven/org.apache.maven/maven-wrapper/pom.properties";

    public static final String DEFAULT_MAVEN_USER_HOME = System.getProperty( "user.home" ) + "/.m2";

    public static final String MAVEN_USER_HOME_PROPERTY_KEY = "maven.user.home";

    public static final String MAVEN_USER_HOME_ENV_KEY = "MAVEN_USER_HOME";

    public static final String MVNW_VERBOSE = "MVNW_VERBOSE";

    public static final String MVNW_USERNAME = "MVNW_USERNAME";

    public static final String MVNW_PASSWORD = "MVNW_PASSWORD";

    public static final String MVNW_REPOURL = "MVNW_REPOURL";

    public static final String MVN_PATH =  
        "org/apache/maven/apache-maven/" + wrapperVersion() + "/apache-maven-" + wrapperVersion() + "-bin.zip";

    public static void main( String[] args )
        throws Exception
    {
        Path wrapperJar = wrapperJar();
        Path propertiesFile = wrapperProperties( wrapperJar );
        Path rootDir = rootDir( wrapperJar );

        String wrapperVersion = wrapperVersion();

        try 
        {
            addSystemProperties( rootDir );
        }
        catch ( AccessControlException e )
        {
            // no problem, just missing: permission java.util.PropertyPermission "*", "read,write";
        }

        WrapperExecutor wrapperExecutor = WrapperExecutor.forWrapperPropertiesFile( propertiesFile, System.out );
        wrapperExecutor.execute( args, new Installer( new DefaultDownloader( "mvnw", wrapperVersion ),
                                                      new PathAssembler( mavenUserHome() ) ),
                                 new BootstrapMainStarter() );
    }

    private static void addSystemProperties( Path rootDir )
    {
        SystemPropertiesHandler.getSystemProperties( mavenUserHome().resolve( "maven.properties" ) ).entrySet().stream()
            .forEach( e -> System.setProperty( e.getKey(), e.getValue() ) );

        SystemPropertiesHandler.getSystemProperties( rootDir.resolve( "maven.properties" ) ).entrySet().stream()
            .forEach( e -> System.setProperty( e.getKey(), e.getValue() ) );
    }

    private static Path rootDir( Path wrapperJar )
    {
        return wrapperJar.getParent().getParent().getParent();
    }

    private static Path wrapperProperties( Path wrapperJar )
    {
        return wrapperJar().resolveSibling( wrapperJar.getFileName().toString().replaceFirst( "\\.jar$",
                                                                                              ".properties" ) );
    }

    private static Path wrapperJar()
    {
        try
        {
            URI location = MavenWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            return Paths.get( location );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
    }

    static String wrapperVersion()
    {
        try 
        {
            try ( InputStream resourceAsStream =
               MavenWrapperMain.class.getResourceAsStream( POM_PROPERTIES ) )
            {
            
                if ( resourceAsStream == null )
                {
                    return "3.7.0-SNAPSHOT";
    //                throw new RuntimeException( "No maven properties found." );
                }
                    Properties mavenProperties = new Properties();
                    mavenProperties.load( resourceAsStream );
                    String version = mavenProperties.getProperty( "version" );
                    if ( version == null )
                    {
                        throw new RuntimeException( "No version number specified in build receipt resource." );
                    }
                    return version;
                }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Could not determine wrapper version.", e );
        }
    }

    private static Path mavenUserHome()
    {
        String mavenUserHome = System.getProperty( MAVEN_USER_HOME_PROPERTY_KEY );
        if ( mavenUserHome != null )
        {
            return Paths.get( mavenUserHome );
        }
        
        mavenUserHome = System.getenv( MAVEN_USER_HOME_ENV_KEY );
        if ( mavenUserHome  != null )
        {
            return Paths.get( mavenUserHome );
        }
        else
        {
            return Paths.get( DEFAULT_MAVEN_USER_HOME );
        }
    }
}
