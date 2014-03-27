/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*                
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed unde r the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.variables.VariableSpace;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpk.CpkEngine;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class KettleElementHelper {

  private static final Log logger = LogFactory.getLog( KettleElementHelper.class );

  private static final String CPK_PARAM_PREFIX = "cpk.";
  private static final String CPK_PLUGIN_ID = "cpk.plugin.id";
  private static final String CPK_SOLUTION_SYSTEM_DIR = "cpk.solution.system.dir";
  private static final String CPK_PLUGIN_DIR = "cpk.plugin.dir";
  private static final String CPK_PLUGIN_SYSTEM_DIR = "cpk.plugin.system.dir";
  private static final String CPK_WEBAPP_DIR = "cpk.webapp.dir";

  private static final String CPK_SESSION_PARAM_PREFIX = "cpk.session.";
  private static final String CPK_SESSION_USERNAME = "cpk.session.username";
  private static final String CPK_SESSION_ROLES = "cpk.session.roles";

  private static final String CPK_EXECUTE_AT_START = "cpk.executeAtStart";

  private static final String[] BASE_PARAM_SET = { CPK_PLUGIN_ID, CPK_PLUGIN_DIR, CPK_PLUGIN_SYSTEM_DIR,
    CPK_SOLUTION_SYSTEM_DIR, CPK_WEBAPP_DIR, CPK_SESSION_USERNAME, CPK_SESSION_ROLES };
  private static final String[] RESERVED_PARAM_PREFIX_SET = { "_", CPK_PARAM_PREFIX };
  private static final String REQUEST_PARAM_PREFIX = "param";

  private static HashMap<String, String> parameterCache;


  static {
    // KettleElementHelper is a static helper class, so the parameter cache is shared between kettle elements
    File pluginDir = CpkEngine.getInstance().getEnvironment().getPluginUtils().getPluginDirectory();
    parameterCache = new HashMap<String, String>();
    parameterCache.put( CPK_PLUGIN_ID, CpkEngine.getInstance().getEnvironment().getPluginName() );
    parameterCache.put( CPK_PLUGIN_DIR, pluginDir.getAbsolutePath() );
    parameterCache.put( CPK_PLUGIN_SYSTEM_DIR, pluginDir.getAbsolutePath() + File.separator + "system" );
    parameterCache.put( CPK_SOLUTION_SYSTEM_DIR, pluginDir.getParentFile().getAbsolutePath() );
    parameterCache.put( CPK_WEBAPP_DIR, CpkEngine.getInstance().getEnvironment().getWebAppDir() );
  }

  public static void addParameterDefinition( NamedParams params, String paramName, String defaultValue ) {
    try {
      params.addParameterDefinition( paramName, defaultValue, null );
      logger.debug( "Added kettle param '" + paramName + "' with default value '" + defaultValue + "'" );
    } catch ( DuplicateParamException e ) {
      logger.debug( "Kettle param '" + paramName + "' already exists" );
    }
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
    try {
      params.setParameterValue( paramName, paramValue );
    } catch ( UnknownParamException e ) {
      // ignore it
    }
  }

  public static String getParameterDefault( NamedParams params, String paramName ) {
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

  public static void addBaseParameters( NamedParams params ) {
    for ( String paramName : BASE_PARAM_SET ) {
      addParameterDefinition( params, paramName, null );
    }
  }

  public static boolean isExecuteAtStart( NamedParams params ) {
    return Boolean.parseBoolean( getParameterDefault( params, CPK_EXECUTE_AT_START ) );
  }

  private static String getCurrentValue( String paramName ) {
    // session parameter
    if ( paramName.startsWith( CPK_SESSION_PARAM_PREFIX ) ) {
      IUserSession userSession = CpkEngine.getInstance().getEnvironment().getSessionUtils().getCurrentSession();
      if ( userSession != null ) {
        // username
        if ( paramName.equals( CPK_SESSION_USERNAME ) ) {
          return userSession.getUserName();
        }
        // roles
        if ( paramName.equals( CPK_SESSION_ROLES ) ) {
          return StringUtils.join( userSession.getAuthorities(), "," );
        }
        // any other session variable
        String varName = paramName.substring( CPK_SESSION_PARAM_PREFIX.length() );
        logger.debug( "Getting session variable '" + varName + "'" );
        return userSession.getStringParameter( varName );
      }
    }

    // other known parameters should be in cache, otherwise the value is null
    return parameterCache.get( paramName );
  }

  public static void updateParameters( NamedParams params ) {
    for ( String paramName : params.listParameters() ) {
      if ( paramName.startsWith( CPK_PARAM_PREFIX ) ) {
        setParameterValue( params, paramName, getCurrentValue( paramName ) );
      }
    }
  }

  private static boolean isReservedName( String name ) {
    for ( String prefix : RESERVED_PARAM_PREFIX_SET ) {
      if ( name.startsWith( prefix ) ) {
        return true;
      }
    }
    return false;
  }

  private static boolean addRequestParameter( NamedParams params, String paramName, String paramValue ) {
    if ( isReservedName( paramName ) ) {
      logger.warn( "Request param '" + paramName + "' uses a reserved name in the Kettle job/transformation" );
      return false;
    }
    if ( !hasParameter( params, paramName ) ) {
      logger.warn( "Request param '" + paramName + "' doesn't exist in the Kettle job/transformation" );
      return false;
    }
    setParameterValue( params, paramName, paramValue );
    logger.debug( "Added request param '" + paramName + "' = '" + paramValue + "'" );
    return true;
  }

  public static Collection<String> addRequestParameters( NamedParams params, Map<String, Object> requestParams ) {
    LinkedList<String> addedParamNames = new LinkedList<String>();
    if ( requestParams != null ) {
      String paramName;
      String paramValue;
      for ( String key : requestParams.keySet() ) {
        if ( key.startsWith( REQUEST_PARAM_PREFIX ) ) {
          paramName = key.substring( REQUEST_PARAM_PREFIX.length() );
          paramValue = requestParams.get( key ).toString();
          if ( addRequestParameter( params, paramName, paramValue ) ) {
            addedParamNames.add( paramName );
          }
        }
      }
    }
    return addedParamNames;
  }

  /**
   *
   * @param kettleParams The parameters to add (set value) to params.
   * @return The parameters that were added.
   */
  public static Collection<String> addKettleParameters( NamedParams params, Map<String, String> kettleParams ) {
    LinkedList<String> addedParamNames = new LinkedList<String>();
    if ( kettleParams != null ) {
      for ( Map.Entry<String, String> parameter : kettleParams.entrySet() ) {
        if ( addRequestParameter( params, parameter.getKey(),  parameter.getValue() ) ) {
          addedParamNames.add( parameter.getKey() );
        }
      }
    }
    return addedParamNames;
  }

  /**
   * Parses the map obtained from a httpRequest to get the kettle parameter name/value pairs.
   * @param requestParams the map obtained from the httpRequest.
   * @return The processed kettle parameter name/value.
   */
  public static Map<String, String> getKettleParameters( Map<String, Object> requestParams ) {
    Map<String, String> parameters = new HashMap<String, String>();
    if ( requestParams != null ) {
      String paramName;
      String paramValue;
      for ( String key : requestParams.keySet() ) {
        if ( key.startsWith( REQUEST_PARAM_PREFIX ) ) {
          paramName = key.substring( REQUEST_PARAM_PREFIX.length() );
          paramValue = requestParams.get( key ).toString();
          parameters.put( paramName, paramValue );
        }
      }
    }
    return parameters;
  }


  public static void clearParameters( NamedParams params, Collection<String> paramNames ) {
    if ( paramNames != null ) {
      for ( String paramName : paramNames ) {
        setParameterValue( params, paramName, null );
        logger.debug( "Cleared request param '" + paramName + "'" );
      }
    }
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

  // TODO: remove in kettleOutput refactor
  public static enum KettleType {
    JOB, TRANSFORMATION
  }
}
