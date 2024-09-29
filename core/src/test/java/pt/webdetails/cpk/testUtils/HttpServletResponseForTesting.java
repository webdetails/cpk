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


package pt.webdetails.cpk.testUtils;

import org.springframework.mock.web.DelegatingServletOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class HttpServletResponseForTesting implements HttpServletResponse {
  private OutputStream outputStream;
  private int status;

  public HttpServletResponseForTesting( OutputStream outputStream ) {
    this.outputStream = outputStream;
  }


  @Override public void addCookie( Cookie cookie ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public boolean containsHeader( String s ) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String encodeURL( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String encodeRedirectURL( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String encodeUrl( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String encodeRedirectUrl( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public int getStatus() {
    return this.status;
  }

  @Override public Collection<String> getHeaderNames() {
    return null;
  }

  @Override public Collection<String> getHeaders( String name ) {
    return null;
  }

  @Override public String getHeader( String name ) {
    return null;
  }

  @Override public void sendError( int i, String s ) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void sendError( int i ) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void sendRedirect( String s ) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setDateHeader( String s, long l ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void addDateHeader( String s, long l ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setHeader( String s, String s2 ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void addHeader( String s, String s2 ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setIntHeader( String s, int i ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void addIntHeader( String s, int i ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setStatus( int i ) {
    this.status = i;
  }

  @Override public void setStatus( int i, String s ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String getCharacterEncoding() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public String getContentType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public ServletOutputStream getOutputStream() throws IOException {
    return new DelegatingServletOutputStream( outputStream );
  }

  @Override public PrintWriter getWriter() throws IOException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setCharacterEncoding( String s ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setContentLength( int i ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setContentType( String s ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setBufferSize( int i ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public int getBufferSize() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void flushBuffer() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void resetBuffer() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public boolean isCommitted() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void reset() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public void setLocale( Locale locale ) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public Locale getLocale() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
