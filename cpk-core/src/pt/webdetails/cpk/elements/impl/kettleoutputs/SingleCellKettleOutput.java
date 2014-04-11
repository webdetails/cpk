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


import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class SingleCellKettleOutput extends KettleOutput {

  private String mimeType;

  public SingleCellKettleOutput( HttpServletResponse response, boolean download, String mimeType ) {
    super( response, download );

    this.mimeType = mimeType;
  }

  @Override
  public void processResult( KettleResult result ) {
    this.logger.debug( "Process Single Cell - print it" );

    try {
      Object cell = result.getRows().get( 0 ).getData()[ 0 ];
      if ( cell != null ) {
        CpkUtils.setResponseHeaders( this.getResponse(), this.mimeType );
        OutputStream out = this.getOut();
        out.write( cell.toString().getBytes( ENCODING ) );
        out.flush();
      }

    } catch ( UnsupportedEncodingException ex ) {
      this.logger.error( "Unsupported encoding.", ex );
    } catch ( IOException ex ) {
      this.logger.error( "IO Error processing single cell kettle output.", ex );
    }
  }
}
