/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cpk.elements.impl.kettleoutputs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public abstract class KettleOutput implements IKettleOutput {

  protected Log logger = LogFactory.getLog( this.getClass() );
  protected final String ENCODING = "UTF-8";

  private OutputStream out;
  private HttpServletResponse response;

  private boolean download;

  protected OutputStream getOut() { return this.out; }

  protected KettleOutput( HttpServletResponse response, boolean download ) {
    this.response = response;
    this.download = download;

    try {
      this.out = response.getOutputStream();
    } catch ( IOException ex ) {
      this.logger.error( "Something went wrong on the KettleOutput class initialization.", ex );
    }
  }

  // TODO: rename this getter... this says if a result is to be returned as an attachment / download file
  protected boolean getDownload() { return this.download; }

  protected HttpServletResponse getResponse() { return this.response; }


}
