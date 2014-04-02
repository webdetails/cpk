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
import pt.webdetails.cpk.elements.impl.KettleElementHelper.KettleType;
import pt.webdetails.cpk.elements.impl.KettleResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public abstract class KettleOutput implements IKettleOutput {

  protected Log logger = LogFactory.getLog( this.getClass() );
  protected final String ENCODING = "UTF-8";

  private String outputStepName = "OUTPUT";
  private KettleType kettleType;

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

  @Override
  public KettleType getKettleType() {
    return kettleType;
  }

  @Override
  public void setKettleType( KettleType kettleType ) {
    this.kettleType = kettleType;
  }

  protected boolean getDownload() { return this.download; }

  protected HttpServletResponse getResponse() { return this.response; }


  @Override
  public void processResult( KettleResult result ) {
    if ( result != null ) {
      this.setKettleType( result.getKettleType() );

      // TODO change for to set
      //for ( KettleResult.Row row : result.getRows() ) {
      //  this.storeRow( row.row, row.rowMeta );
      //}
    }
  }

  // is this needed?
  @Override
  public String getOutputStepName() {
    return outputStepName;
  }

  // is this needed?
  @Override
  public void setOutputStepName( String outputStepName ) {
    this.outputStepName = outputStepName;
  }

}
