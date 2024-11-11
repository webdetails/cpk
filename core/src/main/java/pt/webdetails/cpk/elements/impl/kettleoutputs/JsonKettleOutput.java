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


import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class JsonKettleOutput extends KettleOutput {

  @Override
  public JsonKettleOutput setConfiguration( Configuration configuration ) {
    configuration.setMimeType( MimeTypes.JSON );
    super.setConfiguration( configuration );

    return this;
  }

  @Override
  public void processResult( KettleResult result ) {
    logger.debug( "Process Json" );

    // TODO: Check: This is assuming that all rows have the same metadata! This could eventually lead to an error.
    RowMetaInterface rowMeta = result.getRows().size() > 0 ? result.getRows().get( 0 ).getRowMeta() : null;
    Collection<Object[]> rows = new ArrayList<Object[]>();
    if ( rowMeta != null ) {
      int rowSize = rowMeta.size();
      for ( RowMetaAndData row : result.getRows() ) {
        // array needs copy to truncate null elements
        rows.add( Arrays.copyOfRange( row.getData(), 0, rowSize ) );
      }
    }

    RowsJson rowsJson = new RowsJson( rows, rowMeta );

    try {
      CpkUtils.setResponseHeaders( this.getResponse(), this.getConfiguration().getMimeType() );
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue( this.getOut(), rowsJson );
    } catch ( IOException ex ) {
      this.logger.error( "IO Error processing Json kettle output.", ex );
    }
  }

}
