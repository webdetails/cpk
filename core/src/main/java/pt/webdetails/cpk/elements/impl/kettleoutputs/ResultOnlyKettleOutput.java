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
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;

import java.io.IOException;

public class ResultOnlyKettleOutput extends KettleOutput {

  public static final class ResultStruct {
    boolean result;
    int exitStatus;
    int nrRows;
    long nrErrors;

    public ResultStruct( KettleResult result ) {
      this.result = result.wasExecutedSuccessfully();
      this.exitStatus = result.getExitStatus();
      this.nrRows = ( result.getRows() == null ) ? 0 : result.getRows().size();
      this.nrErrors = result.getNumberOfErrors();
    }

    @JsonProperty( "result" )
    public boolean isResult() {
      return result;
    }

    @JsonProperty( "exitStatus" )
    public int getExitStatus() {
      return exitStatus;
    }

    @JsonProperty( "nrRows" )
    public int getNrRows() {
      return nrRows;
    }

    @JsonProperty( "nrErrors" )
    public long getNrErrors() {
      return nrErrors;
    }
  }

  @Override
  public ResultOnlyKettleOutput setConfiguration( Configuration configuration ) {
    configuration.setMimeType( MimeTypes.JSON );
    super.setConfiguration( configuration );

    return this;
  }

  @Override
  public void processResult( KettleResult result ) {
    this.logger.debug( "Process Result Only" );

    ResultStruct resultStruct = new ResultStruct( result );

    try {
      CpkUtils.setResponseHeaders( this.getResponse(), this.getConfiguration().getMimeType() );
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue( this.getOut(), resultStruct );
    } catch ( IOException ex ) {
      this.logger.fatal( null, ex );
    }
  }
}
