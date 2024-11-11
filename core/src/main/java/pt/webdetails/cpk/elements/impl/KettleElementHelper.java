/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cpk.elements.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.variables.VariableSpace;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpk.CpkEngine;
import pt.webdetails.cpk.ICpkEnvironment;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KettleElementHelper {

  private static final Log logger = LogFactory.getLog( KettleElementHelper.class );

  private static final String CPK_PLUGIN_ID = "cpk.plugin.id";
  private static final String CPK_SOLUTION_SYSTEM_DIR = "cpk.solution.system.dir";
  private static final String CPK_PLUGIN_DIR = "cpk.plugin.dir";
  private static final String CPK_PLUGIN_SYSTEM_DIR = "cpk.plugin.system.dir";
  private static final String CPK_WEBAPP_DIR = "cpk.webapp.dir";

  private static final String CPK_SESSION_PARAM_PREFIX = "cpk.session.";
  private static final String CPK_SESSION_USERNAME = "cpk.session.username";
  private static final String CPK_SESSION_ROLES = "cpk.session.roles";
  private static final String ROLES_TAG = "roles";

  private static final Collection<String> INJECTED_PARAM_SET = new ArrayList<String>( Arrays.asList(
    CPK_PLUGIN_ID, CPK_SOLUTION_SYSTEM_DIR, CPK_PLUGIN_DIR, CPK_PLUGIN_SYSTEM_DIR,
    CPK_WEBAPP_DIR, CPK_SESSION_USERNAME, CPK_SESSION_ROLES ) );

  private static final String REQUEST_PARAM_PREFIX = "param";


  /**
   * The character used to separate transformation in parameter names
   * e.g. cpk.solution.dir|uriPathEncode
   */
  public static final char TRANSFORMATION_SEPARATOR = '|';

  public static final String TRANSFORMATION_URI_PATH_ENCODE = "uriPathEncode";
  public static final String TRANSFORMATION_URI_PATH_DECODE = "uriPathDecode";


  // region Properties
  /**
   *
   * @return The transformation functions that can be applied to an injected parameter
   */
  public static Map<String, Function<String, String>> getTransformations() {
    if ( transformations == null ) {
      transformations = new HashMap<String, Function<String, String>>();
    }

    return transformations;
  }
  public static void setTransformations( Map<String, Function<String, String>> value ) {
    transformations = value;
  }
  private static Map<String, Function<String, String>> transformations;


  public static ICpkEnvironment getPluginEnvironment() {
    if ( pluginEnvironment == null ) {
      // use environment from cpk engine singleton as default
      pluginEnvironment = CpkEngine.getInstance().getEnvironment();
    }

    return pluginEnvironment;
  }
  public static void setPluginEnvironment( ICpkEnvironment environment ) {
    pluginEnvironment = environment;
    initCachedInjectedParameters( environment );
  }
  private static ICpkEnvironment pluginEnvironment;

  /**
   * Injected parameter cache is shared between kettle elements
   * @return
   */
  private static Map<String, String> getInjectedParameterCache() {
    if ( injectedParameterCache == null ) {
      initCachedInjectedParameters( getPluginEnvironment() );
    }

    return injectedParameterCache;
  }
  private static Map<String, String> injectedParameterCache;

  // endregion

  // region Initializers
  static {
    initTransformations();
  }
  // endregion


  private static void initCachedInjectedParameters( ICpkEnvironment pluginEnvironment ) {
    injectedParameterCache = new HashMap<String, String>();

    String pluginDirPath = pluginEnvironment.getContentAccessFactory().getPluginSystemReader( null ).fetchFile( null ).getFullPath();
    File pluginDir = new File( pluginDirPath );
    cacheDirParameterValue( CPK_PLUGIN_DIR, pluginDirPath );

    File pluginSystemDir = getChildDirectory( pluginDir, "system" );
    cacheDirParameterValue( CPK_PLUGIN_SYSTEM_DIR, pluginSystemDir );

    File solutionSystemDir = pluginDir.getParentFile();
    cacheDirParameterValue( CPK_SOLUTION_SYSTEM_DIR, solutionSystemDir );

    String webAppDir = pluginEnvironment.getWebAppDir();
    cacheDirParameterValue( CPK_WEBAPP_DIR, webAppDir );

    getInjectedParameterCache().put( CPK_PLUGIN_ID, pluginEnvironment.getPluginName() );
  }

  private static void cacheDirParameterValue( String parameterName, File parameterDir ) {
    if ( parameterDir != null ) {
      cacheDirParameterValue( parameterName, parameterDir.getAbsolutePath() );
    }
  }

  private static void cacheDirParameterValue( String parameterName, String path ) {
    String decodeDirPath = path;
    if ( path != null ) {
      decodeDirPath = new File( path ).toURI().getPath();
    }
    getInjectedParameterCache().put( parameterName, decodeDirPath );
  }

  /**
   * Initializes transformation functions available to modify kettle parameters.
   * At the moment this transformations can only be applied to injected parameters.
   */
  private static void initTransformations() {
    Map<String, Function<String, String>> transformations = getTransformations( );

    /**
     * Encodes a string path according to RFC3986
     */
    transformations.put( TRANSFORMATION_URI_PATH_ENCODE, new Function<String, String>() {
      @Override public String call( String path ) {
        return new File( path ).toURI().toASCIIString();
      }
    } );

    /**
     * Decodes a string path according to RFC3986
     */
    transformations.put( TRANSFORMATION_URI_PATH_DECODE, new Function<String, String>() {
      @Override public String call( String path ) {
        return UriBuilder.fromPath( path ).build(  ).getPath();
      }
    } );
  }

  private static File getChildDirectory( File parent, String childDirectoryName ) {
    if ( !parent.isDirectory() ) {
      return null;
    }

    File[] children = parent.listFiles();
    if ( children == null ) {
      return null;
    }

    for ( File child : children ) {
      if ( child.isDirectory() && child.getName().equals( childDirectoryName ) ) {
        return child;
      }
    }

    return null;
  }

  public static boolean hasParameter( NamedParams params, String paramName ) {
    for ( String name : params.listParameters() ) {
      if ( name.equals( paramName ) ) {
        return true;
      }
    }

    return false;
  }

  public static void setParameterValue( NamedParams params, String paramName, String paramValue ) {
    if ( !hasParameter( params, paramName ) ) {
      logger.warn( "Param '" + paramName + "' doesn't exist in the Kettle job/transformation" );
      return;
    }

    try {
      params.setParameterValue( paramName, paramValue );
      logger.debug( "Set param '" + paramName + "' = '" + paramValue + "'" );
    } catch ( UnknownParamException e ) {
      // Should not happen, given the above hasParameter() test.
    }
  }

  public static String getParameterDefaultValue( NamedParams params, String paramName ) {
    String value = null;
    try {
      value = params.getParameterDefault( paramName );
    } catch ( UnknownParamException e ) {
      // ignore it
    }
    return value;
  }

  public static String getParameterValue( NamedParams params, String paramName ) {
    String value = null;
    try {
      value = params.getParameterValue( paramName );
    } catch ( UnknownParamException e ) {
      // ignore it
    }
    return value;
  }

  private static String arrayToJson( String fieldName, String[] array ) {
    if ( array != null ) {
      JsonFactory factory = new JsonFactory();
      StringWriter writer = new StringWriter();
      JsonGenerator generator = null;
      try {
        generator = factory.createGenerator( writer );
        generator.writeStartObject();
        generator.writeFieldName( fieldName );
        generator.writeStartArray();
        for ( String element : array ) {
          generator.writeString( element );
        }
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
        return writer.toString();
      } catch ( IOException e ) {
        logger.error( "Failed to convert array that contains '" + fieldName + "' to JSON" );
      }
    }
    return null;
  }

  private static String getInjectedParameterCurrentValue( String paramName ) {
    // session parameter
    if ( paramName.startsWith( CPK_SESSION_PARAM_PREFIX ) ) {
      IUserSession userSession = getPluginEnvironment().getSessionUtils().getCurrentSession();
      if ( userSession != null ) {
        // username
        if ( paramName.equals( CPK_SESSION_USERNAME ) ) {
          return userSession.getUserName();
        }
        // roles
        if ( paramName.equals( CPK_SESSION_ROLES ) ) {
          String[] roles = userSession.getAuthorities();
          return arrayToJson( ROLES_TAG, roles );
        }
        // any other session variable
        String varName = paramName.substring( CPK_SESSION_PARAM_PREFIX.length() );
        logger.debug( "Getting session variable '" + varName + "'" );
        return userSession.getStringParameter( varName );
      }
    }

    // other known parameters should be in cache, otherwise the value is null
    return getInjectedParameterCache().get( paramName );
  }

  /**
   *
   * @return The parameters which value are to be injected by CPK.
   */
  public static Map<String, String> getInjectedParameters( NamedParams params ) {
    Map<String, String> parameters = new HashMap<String, String>();
    for ( String parameter : params.listParameters() ) {
      String parameterName = getName( parameter );
      if ( isInjectedParameter( parameterName ) ) {
        Iterable<Function<String, String>> transformations = getParameterTransformations( parameter );
        String value = getInjectedParameterCurrentValue( parameterName );
        value = apply( value, transformations );
        parameters.put( parameter, value );
      }
    }
    return parameters;
  }

  /**
   * Interface for kettle parameter Transformation types
   * @param <T> Input type of the function.
   * @param <TResult> Output type of the function.
   */
  private interface Function<T, TResult> {
    TResult call( T arg );
  }

  /**
   * Checks if the value for the given parameter is injected by CPK.
   * @param paramName The name of the parameter to check for value injection.
   * @return
   */
  private static boolean isInjectedParameter( String paramName ) {
    return INJECTED_PARAM_SET.contains( paramName )
      || paramName.startsWith( CPK_SESSION_PARAM_PREFIX );
  }

  /**
   * @param parameter The composed parameter (parameter name and transformation names). E.G.: "cpk.plugin.dir|uriPathEncode"
   * @return the transformation functions to be applied to a given parameter.
   */
  private static Iterable<Function<String, String>> getParameterTransformations( String parameter ) {
    String[] splited = parameter.split( "\\" + TRANSFORMATION_SEPARATOR );

    // if parameter has no transformations
    if ( splited.length < 2 ) {
      return Collections.emptyList();
    }

    List<Function<String, String>> transformations = new ArrayList<Function<String, String>>( splited.length - 1 );
    // ignore first entry which is the parameter name
    for ( int i = 1; i < splited.length; i++ ) {
      String transformationName = splited[i];
      Function<String, String> transformation = getTransformations().get( transformationName );
      if ( transformation == null ) {
        logger.error( "CPK injected variable transformation " + transformationName + " is invalid." );
      } else {
        transformations.add( transformation );
      }
    }

    return transformations;
  }

  /**
   *
   * @param parameter
   * @return the name of a parameter from the composed parameter (parameter name and transformation names).
   * E.G.: "cpk.plugin.dir|uriPathEncode" returns "cpk.plugin.dir"
   */
  private static String getName( String parameter ) {
    int separatorIndex;
    if ( ( separatorIndex = parameter.indexOf( TRANSFORMATION_SEPARATOR ) ) <= -1 ) {
      return parameter;
    }
    return parameter.substring( 0, separatorIndex );
  }

  /**
   *
   * @param value The initial value to apply the transformation functions.
   * @param transformations The transformation functions to apply.
   * @return the result of applying the transformation functions to the parameter value.
   * E.G.: with a function array [f0, f1, f2] the following function composition would be applied f2(f1(f0(value)))
   */
  private static String apply( String value, Iterable<Function<String, String>> transformations ) {
    for ( Function<String, String> function : transformations ) {
      value = function.call( value );
    }
    return value;
  }

  /**
   * @param params The target parameters to set.
   * @param kettleParams The source parameters.
   */
  public static void setKettleParameterValues( NamedParams params, Map<String, String> kettleParams ) {
    if ( kettleParams != null ) {
      for ( Map.Entry<String, String> parameter : kettleParams.entrySet() ) {
        setParameterValue( params, parameter.getKey(), parameter.getValue() );
      }
    }
  }

  /**
   * Parses the map obtained from a httpRequest to get the kettle parameter name/value pairs.
   * @param requestParams the map obtained from the httpRequest.
   * @return The processed kettle parameter name/value.
   */
  public static Map<String, String> getKettleParameters( Map<String, Object> requestParams ) {
    if ( requestParams == null ) {
      return Collections.emptyMap();
    }

    Map<String, String> parameters = new HashMap<String, String>();
    String paramName;
    String paramValue;
    for ( Map.Entry<String, Object> entry : requestParams.entrySet() ) {
      if ( entry.getKey().startsWith( REQUEST_PARAM_PREFIX ) ) {
        paramName = entry.getKey().substring( REQUEST_PARAM_PREFIX.length() );
        paramValue = entry.getValue().toString();
        parameters.put( paramName, paramValue );
      }
    }
    return parameters;
  }

  // debug only
  public static void dump( NamedParams params, VariableSpace vars, String desc, boolean show ) {
    logger.debug( desc + " has " + params.listParameters().length + " param(s) and "
      + vars.listVariables().length + " var(s)" );
    if ( show ) {
      String[] parameters = params.listParameters();
      Arrays.sort( parameters );
      for ( String param : parameters )  {
        logger.debug( "  param " + param + " = " + getParameterValue( params, param ) );
      }
      String[] variables = vars.listVariables();
      Arrays.sort( variables );
      for ( String var : variables )  {
        logger.debug( "  var " + var + " = " + vars.getVariable( var ) );
      }
    }
  }

}
