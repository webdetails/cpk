/*!
* Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company.  All rights reserved.
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
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.elements.impl.KettleResultKey;
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

  private CpkEngine engine;

  public CpkEngine getEngine() {
    return this.engine;
  }

  public CpkCoreService setEngine( CpkEngine engine ) {
    this.engine = engine;
    return this;
  }

  public CpkCoreService( ICpkEnvironment environment ) {
    // by default use the CpkEngine singleton.
    this.setEngine( CpkEngine.getInstance() );
    this.getEngine().init( environment );
  }

  public void createContent( Map<String, Map<String, Object>> bloatedMap )
    throws Exception {

    IAccessControl accessControl = this.getEngine().getEnvironment().getAccessControl();
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    logger.debug( "Creating content" );

    // Get the path, remove leading slash
    String path = (String) bloatedMap.get( "path" ).get( "path" );
    IElement element = null;

    if ( path == null || path.equals( "/" ) ) {
      element = this.getEngine().getDefaultElement();
      if ( element != null ) {
        String url = element.getId();
        if ( path == null ) {
          // We need to put the http redirection on the right level
          url = this.getEngine().getEnvironment().getPluginName() + "/" + url;
        }

        CpkUtils.redirect( response, url );
      }
    }

    if ( path != null ) {
      element = this.getEngine().getElement( path.substring( 1 ).toLowerCase() );
    }

    if ( element != null ) {
      if ( accessControl.isAllowed( element ) ) {
        element.processRequest( bloatedMap );
      } else {
        accessControl.throwAccessDenied( response );
      }

    } else {
      final String elementNotFound = "Unable to get element: " + path + ". "
        + "This is probably a call to a control CPK operation (reload, status)";

      logger.debug( elementNotFound );

      throw new Exception( "Unable to get element! - " + path );
    }
  }

  // alias to refresh
  public void reload( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    refresh( out, bloatedMap );
  }

  public void refresh( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    IAccessControl accessControl = this.getEngine().getEnvironment().getAccessControl();

    if ( accessControl.isAdmin() ) {
      logger.info( "Refreshing CPK plugin " + this.getEngine().getEnvironment().getPluginName() );
      this.getEngine().reload();
      status( out, bloatedMap );
    } else {
      accessControl.throwAccessDenied( (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" ) );
    }
  }

  public void clearKettleResultsCache() {
    ICache<KettleResultKey, KettleResult> cache = this.getEngine().getKettleResultCache();

    if ( cache != null ) {
      cache.clear();
    }
  }

  public void status( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );

    logger.debug( "## status ##" );
    logger.info( "Showing status for CPK plugin " + this.getEngine().getEnvironment().getPluginName() );

    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, "text/plain" );
    }

    writeMessage( out, this.getEngine().getStatus().getStatus() );
  }

  public void statusJson( OutputStream out, HttpServletResponse response ) {
    logger.info( "Showing status for CPK plugin " + this.getEngine().getEnvironment().getPluginName() );

    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, "text/plain" );
    }

    writeMessage( out, this.getEngine().getStatus().getStatusJson() );
  }

  public boolean hasElement( String elementId ) {
    Map<String, IElement> elementsMap = this.getEngine().getElementsMap();
    return elementsMap.containsKey( elementId.toLowerCase() );
  }

  public Collection<IElement> getElements() {
    return this.getEngine().getElements();
  }

  public void getElementsList( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    logger.debug( "## getElementsList ##" );

    ObjectMapper mapper = new ObjectMapper();

    try {
      String json = mapper.writeValueAsString( this.getEngine().getElements() );

      writeMessage( out, json );
    } catch ( IOException ex ) {
      logger.error( "Error getting json elements", ex );
    }
  }

  public IElement getDefaultElement() {
    return this.getEngine().getDefaultElement();
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
