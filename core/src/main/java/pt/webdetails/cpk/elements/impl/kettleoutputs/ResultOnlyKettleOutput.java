/*!
* Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company.  All rights reserved.
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
