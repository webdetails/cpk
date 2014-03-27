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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.pentaho.di.core.Result;
import pt.webdetails.cpk.elements.impl.KettleResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResultOnlyKettleOutput extends KettleOutput {

  public ResultOnlyKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );
  }

  @Override
  public boolean needsRowListener() {
    return false;
  }

  @Override
  public void processResult() {

    ObjectMapper mapper = new ObjectMapper();

    class ResultStruct {
      boolean result;
      int exitStatus, nrRows, nrErrors;

      public ResultStruct( Result result ) {
        this.result = result.getResult();
        this.exitStatus = result.getExitStatus();
        this.nrRows = ( result.getRows() == null ) ? 0 : result.getRows().size();
        this.nrErrors = (int) result.getNrErrors();
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
      public int getNrErrors() {
        return nrErrors;
      }
    }

    ResultStruct resultStruct = new ResultStruct( this.getResult() );

    try {
      mapper.writeValue( this.getOut(), resultStruct );
    } catch ( IOException ex ) {
      this.logger.fatal( null, ex );
    }
  }

  @Override
  public void processResult( KettleResult result ) {
    super.processResult( result );
    this.processResult();
  }
}
