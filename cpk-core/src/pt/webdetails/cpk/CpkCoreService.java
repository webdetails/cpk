/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.elements.AbstractElement;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;
import pt.webdetails.cpk.utils.CpkUtils;

public class CpkCoreService {

  private static final Log logger = LogFactory.getLog( CpkCoreService.class );
  private static final String ENCODING = "UTF-8";
  protected CpkEngine cpkEngine;
  protected ICpkEnvironment cpkEnvironment;

  public CpkCoreService( ICpkEnvironment environment ) {
    this.cpkEnvironment = environment;
  }

  private CpkEngine getCpkEngine() {
    if ( cpkEngine == null ) {
      cpkEngine = CpkEngine.getInstanceWithEnv( cpkEnvironment );
    }

    return cpkEngine;
  }

  public void createContent( Map<String, Map<String, Object>> bloatedMap )
    throws Exception {

    //Make sure the instance is first set so we have pluginUtils
    CpkEngine engine = getCpkEngine();
    IAccessControl accessControl = cpkEnvironment.getAccessControl();
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    logger.debug( "Creating content" );

    // Get the path, remove leading slash
    IPluginUtils pluginUtils = cpkEnvironment.getPluginUtils();
    String path = (String) bloatedMap.get( "path" ).get( "path" );
    IElement element = null;

    if ( path == null || path.equals( "/" ) ) {
      String url = engine.getDefaultElement().getId().toLowerCase();
      if ( path == null ) {
        // We need to put the http redirection on the right level
        url = pluginUtils.getPluginName() + "/" + url;
      }
      CpkUtils.redirect( response, url );
    }
    if ( path != null ) {
      element = engine.getElement( path.substring( 1 ).toLowerCase() );
    }
    if ( element != null ) {
      if ( accessControl.isAllowed( element ) ) {
        element.processRequest( bloatedMap );
      } else {
        accessControl.throwAccessDenied( response );
      }

    } else {
      logger.debug(
        "Unable to get element: " + path + ". This is probably a call to a control CPK operation (reload, status)" );
      throw new NoElementException( "Unable to get element!" );
    }
  }

  // alias to refresh
  public void reload( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {
    refresh( out, bloatedMap );
  }

  public void refresh( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {
    IAccessControl accessControl = cpkEnvironment.getAccessControl();
    if ( accessControl.isAdmin() ) {
      logger.info( "Refreshing CPK plugin " + getPluginName() );
      getCpkEngine().reload();
      status( out, bloatedMap );
    } else {
      accessControl.throwAccessDenied( (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" ) );
    }
  }

  /**
   * This method runs a Job or Transformation (in this order) if it exists inside the "system" folder of the plugin
   *
   * @param filename           Name of the file without extension
   * @param adminOnly          to specify if everyone is supposed to run it
   * @param parameterProviders Required by processRequest of the AbstractElement class
   * @return Returns true if the file exists and there was no problem reading it, false otherwise
   */
  private boolean runSystemKettle( String filename, boolean adminOnly,
                                   Map<String, Map<String, Object>> bloatedMap ) {
    boolean success = false;
    AbstractElement element = new AbstractElement();
    element.setAdminOnly( adminOnly );
    element.setElementType( "Kettle" );


    try {
      File kettleFile = new File(
        cpkEnvironment.getPluginUtils().getPluginDirectory().getAbsolutePath() + "/system/" + filename + ".kjb" );

      if ( kettleFile.exists() ) {
        element.setLocation( kettleFile.getAbsolutePath() );
      } else {
        kettleFile = new File(
          cpkEnvironment.getPluginUtils().getPluginDirectory().getAbsolutePath() + "/system/" + filename + ".ktr" );
        if ( kettleFile.exists() ) {
          element.setLocation( kettleFile.getAbsolutePath() );
        } else {
          return false;
        }
      }
      element.processRequest( bloatedMap );
      success = true;

    } catch ( Exception e ) {
    }

    return success;
  }

  public void status( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {
    final String key = "status";
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    boolean success = runSystemKettle( key, false, bloatedMap );

    if ( !success ) {
      logger.info( "Showing status for CPK plugin " + getPluginName() );
      // Only set the headers if we have access to the response (via parameterProviders).
      if ( response != null ) {
        CpkUtils.setResponseHeaders( response, "text/plain" );
      }
      writeMessage( out, getCpkEngine().getStatus() );
    }
  }

  public void statusJson( OutputStream out, HttpServletResponse response ) throws DocumentException, IOException {
    logger.info( "Showing status for CPK plugin " + getPluginName() );

    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, "text/plain" );
    }

    writeMessage( out, getCpkEngine().getStatusJson() );
  }

  public boolean hasElement( String elementId ) {
    TreeMap<String, IElement> elementsMap = getCpkEngine().getElementsMap();
    return elementsMap.containsKey( elementId.toLowerCase() );
  }

  public IElement[] getElements() {
    IElement[] elements = new IElement[] { };
    CpkEngine engine = getCpkEngine();

    if ( engine != null ) {
      TreeMap<String, IElement> elementsMap = engine.getElementsMap();
      if ( elementsMap != null ) {
        Collection<IElement> values = elementsMap.values();
        if ( values != null ) {
          return values.toArray( elements );
        }
      }
    } else {
      logger.error( "cpkEngine is null..." );
    }

    return elements;
  }

  public void getElementsList( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    final String key = "elementsList";
    boolean success = runSystemKettle( key, false, bloatedMap );
    if ( !success ) {
      writeMessage( out, getCpkEngine().getElementsJson() );
    }
  }

  public IElement getElement( String elementId ) {
    return getCpkEngine().getElement( elementId );
  }

  public String getPluginName() {
    return cpkEnvironment.getPluginName();
  }

  private void writeMessage( OutputStream out, String message ) {
    try {
      out.write( message.getBytes( ENCODING ) );
      out.flush();
    } catch ( IOException ex ) {
      logger.error( "Error writing message", ex );
    }
  }

  public RestRequestHandler getRequestHandler() {
    return Router.getBaseRouter();
  }


}
