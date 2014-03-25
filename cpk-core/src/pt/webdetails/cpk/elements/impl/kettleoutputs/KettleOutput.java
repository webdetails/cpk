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
import org.pentaho.di.core.Result;
import org.pentaho.di.core.row.RowMetaInterface;
import pt.webdetails.cpk.elements.impl.KettleElementHelper.KettleType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class KettleOutput implements IKettleOutput {

  protected Log logger = LogFactory.getLog( this.getClass() );
  protected final String ENCODING = "UTF-8";
  private ArrayList<Object[]> rows;
  private RowMetaInterface rowMeta;

  private Result result = null;
  private String outputStepName = "OUTPUT";
  private KettleType kettleType;

  private OutputStream out;
  private HttpServletResponse response;

  private boolean download;

  protected OutputStream getOut() { return this.out; }

  protected KettleOutput( HttpServletResponse response, boolean download ) {
    this.response = response;
    this.download = download;

    this.rows = new ArrayList<Object[]>();
    this.rowMeta = null;

    try {
      this.out = response.getOutputStream();
    } catch ( IOException ex ) {
      this.logger.error( "Something went wrong on the KettleOutput class initialization.", ex );
    }
  }

  @Override
  public void storeRow( Object[] row, RowMetaInterface _rowMeta ) {

    if ( rowMeta == null ) {
      rowMeta = _rowMeta;
    }
    Object[] rightRow = new Object[ rowMeta.size() ];
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      rightRow[ i ] = row[ i ];
    }
    rows.add( rightRow );

  }

  public ArrayList<Object[]> getRows() {
    return rows;
  }

  @Override
  public void setResult( Result r ) {
    this.result = r;
  }

  @Override
  public Result getResult() {
    return this.result;
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
  public abstract boolean needsRowListener();

  @Override
  public abstract void processResult();

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

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }
}
