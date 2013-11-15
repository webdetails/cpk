package pt.webdetails.cpk.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: joao Date: 10/30/13 Time: 4:36 PM To change this template use File | Settings |
 * File Templates.
 */
public class CpkUtils {
  protected static Log logger = LogFactory.getLog( CpkUtils.class );

  public static void setResponseHeaders( HttpServletResponse response, final String mimeType ) {
    setResponseHeaders( response, mimeType, 0, null, 0 );
  }

  public static void setResponseHeaders( HttpServletResponse response, final String mimeType,
                                         final String attachmentName ) {
    setResponseHeaders( response, mimeType, 0, attachmentName, 0 );
  }

  public static void setResponseHeaders( HttpServletResponse response, final String mimeType,
                                         final String attachmentName,
                                         long attachmentSize ) {
    setResponseHeaders( response, mimeType, 0, attachmentName, attachmentSize );

  }

  public static void setResponseHeaders( HttpServletResponse response, final String mimeType, final int cacheDuration,
                                         final String attachmentName, long attachmentSize ) {


    if ( response == null ) {
      logger.warn( "Parameter 'httpresponse' not found!" );
      return;
    }

    if ( mimeType != null ) {
      response.setHeader( "Content-Type", mimeType );
    }

    if ( attachmentName != null ) {
      response.setHeader( "content-disposition", "attachment; filename=" + attachmentName );
    } // Cache?

    if ( attachmentSize > 0 ) {
      response.setHeader( "Content-Length", String.valueOf( attachmentSize ) );
    }

    if ( cacheDuration > 0 ) {
      response.setHeader( "Cache-Control", "max-age=" + cacheDuration );
    } else {
      response.setHeader( "Cache-Control", "max-age=0, no-store" );
    }
  }

  public static void redirect( HttpServletResponse response, String url ) {

    if ( response == null ) {
      logger.error( "response not found" );
      return;
    }
    try {
      response.sendRedirect( url );
    } catch ( IOException e ) {
      logger.error( "could not redirect", e );
    }
  }


  public static Map<String, Object> getRequestParameters(
    Map<String, Map<String, Object>> bloatedMap ) {
    return bloatedMap.get( "request" );
  }


  public static Map<String, Object> getPathParameters( Map<String, Map<String, Object>> bloatedMa ) {
    return bloatedMa.get( "path" );
  }

  public static OutputStream getResponseOutputStream( HttpServletResponse response ) throws IOException {
    return response.getOutputStream();
  }


}




