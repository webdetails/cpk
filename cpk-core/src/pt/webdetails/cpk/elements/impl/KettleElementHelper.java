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
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpk.CpkEngine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class KettleElementHelper {

  private static final String[] RESERVED_PREFIX_SET = { "_", "cpk." };
  private static final String CPK_PLUGIN_ID = "cpk.plugin.id";
  private static final String CPK_SOLUTION_SYSTEM_DIR = "cpk.solution.system.dir";
  private static final String CPK_PLUGIN_DIR = "cpk.plugin.dir";
  private static final String CPK_PLUGIN_SYSTEM_DIR = "cpk.plugin.system.dir";
  private static final String CPK_WEBAPP_DIR = "cpk.webapp.dir";
  private static final String CPK_SESSION_USERNAME = "cpk.session.username";
  private static final String CPK_SESSION_ROLES = "cpk.session.roles";
  private static final String CPK_EXECUTE_AT_START = "cpk.executeAtStart";
  private static final String PARAM_PREFIX = "param";
  private static HashMap<String, String> initParameters;

  static {
    // get current plugin dir
    File pluginDir = CpkEngine.getInstance().getEnvironment().getPluginUtils().getPluginDirectory();
    String pluginPath = pluginDir.getAbsolutePath();

    initParameters = new HashMap<String, String>();
    initParameters.put( CPK_PLUGIN_ID, CpkEngine.getInstance().getEnvironment().getPluginName() );
    initParameters.put( CPK_SOLUTION_SYSTEM_DIR, pluginDir.getParentFile().getAbsolutePath() );
    initParameters.put( CPK_PLUGIN_DIR, pluginPath );
    initParameters.put( CPK_PLUGIN_SYSTEM_DIR, pluginPath + File.separator + "system" );
    initParameters.put( CPK_WEBAPP_DIR, CpkEngine.getInstance().getEnvironment().getWebAppDir() );
  }

  public static Map<String, String> getDefaultParameters() {
    return initParameters;
  }

  public static String getExecuteAtStartParameter() {
    return CPK_EXECUTE_AT_START;
  }

  public static Map<String, String> getUserSessionParameters() {
    HashMap<String, String> userSessionParameters = new HashMap<String, String>();
    // get current user session
    IUserSession userSession = CpkEngine.getInstance().getEnvironment().getSessionUtils().getCurrentSession();
    if ( userSession != null ) {
      userSessionParameters.put( CPK_SESSION_USERNAME, userSession.getUserName() );
      userSessionParameters.put( CPK_SESSION_ROLES, StringUtils.join( userSession.getAuthorities(), "," ) );
    }
    return userSessionParameters;
  }

  private static boolean isReservedParameter( String name ) {
    for ( String prefix : RESERVED_PREFIX_SET ) {
      if ( name.startsWith( prefix ) ) {
        return true;
      }
    }
    return false;
  }

  public static Map<String, String> getUserDefinedParameters( Map<String, Object> params ) {
    HashMap<String, String> userDefinedParameters = new HashMap<String, String>();
    if ( params != null ) {
      String name;
      for ( String key : params.keySet() ) {
        name = key.substring( PARAM_PREFIX.length() );
        if ( key.startsWith( PARAM_PREFIX ) && !isReservedParameter( name ) ) {
          userDefinedParameters.put( name, params.get( key ).toString() );
        }
      }
    }
    return userDefinedParameters;
  }

  // TODO: remove in kettleOutput refactor
  public static enum KettleType {
    JOB, TRANSFORMATION
  }
}
