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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.DocumentException;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;
import pt.webdetails.cpk.utils.CpkUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class CpkCoreService {

  private static final Log logger = LogFactory.getLog( CpkCoreService.class );
  private static final String ENCODING = "UTF-8";

  public CpkCoreService( ICpkEnvironment environment ) {
    CpkEngine.getInstance().init( environment );
  }

  public void createContent( Map<String, Map<String, Object>> bloatedMap )
    throws Exception {

    IAccessControl accessControl = CpkEngine.getInstance().getEnvironment().getAccessControl();
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    logger.debug( "Creating content" );

    // Get the path, remove leading slash
    String path = (String) bloatedMap.get( "path" ).get( "path" );
    IElement element = null;

    if ( path == null || path.equals( "/" ) ) {
      element = CpkEngine.getInstance().getDefaultElement();
      if ( element != null ) {
        String url = element.getId();
        if ( path == null ) {
          // We need to put the http redirection on the right level
          url = CpkEngine.getInstance().getEnvironment().getPluginName() + "/" + url;
        }
        CpkUtils.redirect( response, url );
      }
    }
    if ( path != null ) {
      element = CpkEngine.getInstance().getElement( path.substring( 1 ).toLowerCase() );
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
      throw new Exception( "Unable to get element!" );
    }
  }

  // alias to refresh
  public void reload( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {
    refresh( out, bloatedMap );
  }

  public void refresh( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {
    IAccessControl accessControl = CpkEngine.getInstance().getEnvironment().getAccessControl();
    if ( accessControl.isAdmin() ) {
      logger.info( "Refreshing CPK plugin " + CpkEngine.getInstance().getEnvironment().getPluginName() );
      CpkEngine.getInstance().reload();
      status( out, bloatedMap );
    } else {
      accessControl.throwAccessDenied( (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" ) );
    }
  }



  public void status( OutputStream out, Map<String, Map<String, Object>> bloatedMap )
    throws DocumentException, IOException {


    //final String key = "status";
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    //boolean success = runSystemKettle( key, false, bloatedMap );

    //if ( !success ) {


      logger.debug( "## status ##" );

      logger.info( "Showing status for CPK plugin " + CpkEngine.getInstance().getEnvironment().getPluginName() );
      // Only set the headers if we have access to the response (via parameterProviders).
      if ( response != null ) {
        CpkUtils.setResponseHeaders( response, "text/plain" );
      }
      writeMessage( out, CpkEngine.getInstance().getStatus().getStatus() );
    //}
  }

  public void statusJson( OutputStream out, HttpServletResponse response ) throws DocumentException, IOException {


    logger.info( "Showing status for CPK plugin " + CpkEngine.getInstance().getEnvironment().getPluginName() );

    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, "text/plain" );
    }

    writeMessage( out, CpkEngine.getInstance().getStatus().getStatusJson() );
  }

  public boolean hasElement( String elementId ) {
    Map<String, IElement> elementsMap = CpkEngine.getInstance().getElementsMap();
    return elementsMap.containsKey( elementId.toLowerCase() );
  }

  public Collection<IElement> getElements() {
    return CpkEngine.getInstance().getElements();
  }

  public void getElementsList( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {

    //final String key = "elementsList";
    //boolean success = runSystemKettle( key, false, bloatedMap );


    //if ( !success ) {
      logger.debug( "## getElementsList ##" );
      ObjectMapper mapper = new ObjectMapper();
      String json = null;

      try {
        json = mapper.writeValueAsString( CpkEngine.getInstance().getElements() );
        writeMessage( out, json );
      } catch ( IOException ex ) {
        logger.error( "Error getting json elements", ex );
      }
    //}
  }



  public IElement getDefaultElement() {
    return CpkEngine.getInstance().getDefaultElement();
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
