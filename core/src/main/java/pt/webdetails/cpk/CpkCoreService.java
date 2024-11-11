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

package pt.webdetails.cpk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.elements.impl.KettleResultKey;
import pt.webdetails.cpk.security.IAccessControl;
import pt.webdetails.cpk.utils.CpkUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class CpkCoreService {

  private static final Log logger = LogFactory.getLog( CpkCoreService.class );
  private static final String ENCODING = "UTF-8";
  private static final String HTTP_RESPONSE_KEY = "httpresponse";
  private static final String PATH_KEY = "path";
  private static final String TEXT_PLAIN_CONTENT = "text/plain";

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

  public void createContent( Map<String, Map<String, Object>> bloatedMap ) throws Exception {
    final ICpkEnvironment environment = this.getEngine().getEnvironment();
    final HttpServletResponse response = (HttpServletResponse) bloatedMap.get( PATH_KEY ).get( HTTP_RESPONSE_KEY );

    final String path = getPath( bloatedMap );

    logger.debug( "Creating content" );

    final boolean isDefaultElementPath = "".equals( path ) || "/".equals( path );

    final IElement element = isDefaultElementPath ? this.getDefaultElement() : getElement( path );
    if ( element != null ) {
      if ( isDefaultElementPath ) {
        final String url = getElementUrl( element, path );

        CpkUtils.redirect( response, url );
        return;
      }

      final IAccessControl accessControl = environment.getAccessControl();
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

  private String getPath( Map<String, Map<String, Object>> bloatedMap ) {
    String path = (String) bloatedMap.get( PATH_KEY ).get( PATH_KEY );

    if ( path == null ) {
      return "";
    }

    // Get the path, remove leading slash
    if ( path.startsWith( "/" ) && path.length() > 1 ) {
      return path.substring( 1 );
    }

    return path;
  }

  public String getLocalizationResourceContent( String elementId, String resource ) throws Exception {
    final String resourcePath = getLocalizationResource( elementId, resource );

    return new String( Files.readAllBytes( Paths.get( resourcePath ) ) );
  }

  String getLocalizationResource( String elementId, String resource ) throws Exception {
    if ( !hasElement( elementId ) ) {
      throw new Exception( "Element \"" + elementId + "\" does not exist." );
    }

    final String dashboardRelativePath = getElementRelativePath( elementId );

    final Collection<File> resourceList = getPluginUtils().getPluginResources( dashboardRelativePath, true, resource );
    if ( resourceList.isEmpty() ) {
      throw new FileNotFoundException();
    }

    final File localizationResource = (File) resourceList.toArray()[ 0 ];

    return localizationResource.getPath();
  }

  String getElementRelativePath( String elementId ) throws FileNotFoundException {
    final IElement element = getElement( elementId );
    final String location = element.getLocation();

    return getPluginUtils().getPluginRelativeDirectory( location, false );
  }

  IPluginUtils getPluginUtils() {
    return getEngine().getEnvironment().getPluginUtils();
  }

  // alias to refresh
  public void reload( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    refresh( out, bloatedMap );
  }

  public String reload( Map<String, Map<String, Object>> bloatedMap ) {
    return refresh( bloatedMap );
  }

  public void refresh( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    String resultFromRefresh = refresh( bloatedMap );
    writeMessage( out, resultFromRefresh );
  }

  public String refresh( Map<String, Map<String, Object>> bloatedMap ) {
    IAccessControl accessControl = this.getEngine().getEnvironment().getAccessControl();

    if ( accessControl.isAdmin() ) {
      this.getEngine().reload();
      return status( bloatedMap );
    } else {
      accessControl.throwAccessDenied( (HttpServletResponse) bloatedMap.get( PATH_KEY ).get( HTTP_RESPONSE_KEY ) );
    }

    return null;
  }

  public void clearKettleResultsCache() {
    ICache<KettleResultKey, KettleResult> cache = this.getEngine().getKettleResultCache();

    if ( cache != null ) {
      cache.clear();
    }
  }

  public void status( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    String status = status( bloatedMap );
    writeMessage( out, status );
  }

  public String status( Map<String, Map<String, Object>> bloatedMap ) {
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( PATH_KEY ).get( HTTP_RESPONSE_KEY );

    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, TEXT_PLAIN_CONTENT );
    }

    return this.getEngine().getStatus().getStatus();
  }

  public void statusJson( OutputStream out, HttpServletResponse response ) {
    String json = statusJson( response );
    writeMessage( out, json );
  }

  public String statusJson( HttpServletResponse response ) {
    // Only set the headers if we have access to the response (via parameterProviders).
    if ( response != null ) {
      CpkUtils.setResponseHeaders( response, TEXT_PLAIN_CONTENT );
    }

    return this.getEngine().getStatus().getStatusJson();
  }

  public boolean hasElement( String elementId ) {
    final Map<String, IElement> elementsMap = this.getEngine().getElementsMap();

    return elementsMap.containsKey( elementId.toLowerCase() );
  }

  public Collection<IElement> getElements() {
    return this.getEngine().getElements();
  }

  public void getElementsList( OutputStream out, Map<String, Map<String, Object>> bloatedMap ) {
    String elements = getElementsList();
    if ( elements != null ) {
      writeMessage( out, elements );
    }
  }

  public String getElementsList() {
    ObjectMapper mapper = new ObjectMapper();

    try {
      return mapper.writeValueAsString( this.getEngine().getElements() );
    } catch ( IOException ex ) {
      logger.error( "Error getting json elements", ex );
    }
    return null;
  }

  public IElement getDefaultElement() {
    return this.getEngine().getDefaultElement();
  }

  IElement getElement( String path ) {
    return this.getEngine().getElement( path.toLowerCase() );
  }

  private String getElementUrl( IElement element, String path ) {
    final ICpkEnvironment environment = this.getEngine().getEnvironment();

    final String url = element.getId();

    if ( "".equals( path ) ) {
      // We need to put the http redirection on the right level
      return  environment.getPluginName() + "/" + url;
    }

    return url;
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
