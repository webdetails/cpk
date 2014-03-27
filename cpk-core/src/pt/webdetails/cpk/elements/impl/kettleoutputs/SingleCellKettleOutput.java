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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class SingleCellKettleOutput extends KettleOutput {

  public SingleCellKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );
  }

  @Override
  public boolean needsRowListener() {
    return true;
  }

  @Override
  public void processResult() {
    this.logger.debug( "Process Single Cell - print it" );

    // TODO - make sure this is correct

    try {

      Object result = getRows().get( 0 )[ 0 ];
      if ( result != null ) {
        OutputStream out = this.getOut();
        out.write( result.toString().getBytes( ENCODING ) );
        out.flush();
      }

    } catch ( UnsupportedEncodingException ex ) {
      this.logger.error( "Unsupported encoding.", ex );
    } catch ( IOException ex ) {
      this.logger.error( "IO Error processing single cell kettle output.", ex );
    }

  }

  @Override
  public void processResult( KettleResult result ) {
    super.processResult( result );
    this.processResult();
  }
}
