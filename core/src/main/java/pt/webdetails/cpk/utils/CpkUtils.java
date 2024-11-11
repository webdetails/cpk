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


package pt.webdetails.cpk.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

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

  public static Response redirect( String url ) throws URISyntaxException {
    return Response.temporaryRedirect( new URI( url ) ).build();
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

  public static void send( HttpServletResponse response, InputStream fileInputStream, String mimeTypes, String fileName, boolean sendAsAttachment ) {
    Integer contentLength = null;
    try {
      contentLength = fileInputStream.available();
    } catch ( IOException e ) {
      logger.error( "Failed setting attachment size.", e );
    }

    send( response, fileInputStream, mimeTypes, fileName, sendAsAttachment, contentLength );
  }

  public static void send( HttpServletResponse response, InputStream fileInputStream, String mimeTypes,
                     String fileName, boolean sendAsAttachment, Integer contentLength ) {
    if ( mimeTypes != null && !mimeTypes.isEmpty() ) {
      response.setContentType( mimeTypes );
    }

    String disposition = sendAsAttachment ? "attachment" : "inline";
    String fileParam = fileName != null && !fileName.isEmpty() ? "; filename=" + fileName : "";
    response.setHeader( "Content-disposition", disposition + fileParam );

    if ( contentLength != null ) {
      response.setContentLength( contentLength );
    }

    try {
      IOUtils.copy( fileInputStream, response.getOutputStream() );
      response.getOutputStream().flush();
      fileInputStream.close();
    } catch ( Exception ex ) {
      logger.error( "Failed to copy file to outputstream: " + ex );
    }
  }

  public static Response.Status getEquivalentStatusFromHttpServletResponse( HttpServletResponse response ) {
    if ( response != null ) {
      Response.Status equivalentStatus = Response.Status.fromStatusCode( response.getStatus() );
      if ( equivalentStatus != null ) {
        return equivalentStatus;
      }
    }
    return Response.Status.OK;
  }

}




