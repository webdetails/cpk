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


package pt.webdetails.cpk.elements.impl.kettleoutputs;


import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class SingleCellKettleOutput extends KettleOutput {

  @Override
  public void processResult( KettleResult result ) {
    this.logger.debug( "Process Single Cell - print it" );

    try {
      // at least one row to process?
      if ( !result.getRows().isEmpty() ) {
        Object cell = result.getRows().get( 0 ).getData()[ 0 ];
        byte[] resultContent = cell.toString().getBytes( ENCODING );
        int attachmentSize = resultContent.length;
        ByteArrayInputStream resultInputStream = new ByteArrayInputStream( resultContent );
        String mimeType = this.getConfiguration().getMimeType();
        String defaultAttachmentName = this.getConfiguration().getAttachmentName();
        String attachmentName = defaultAttachmentName != null ? defaultAttachmentName : "singleCell";

        CpkUtils.send( this.getResponse(), resultInputStream, mimeType, attachmentName,
          this.getConfiguration().getSendResultAsAttachment(), attachmentSize );
      }
    } catch ( UnsupportedEncodingException ex ) {
      this.logger.error( "Unsupported encoding.", ex );
    }
  }

}
