/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*                
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed unde r the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements.impl;


import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.row.RowMetaInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: implement serializable to allow disk caching
public final class KettleResult implements Serializable {

  boolean executedSuccessfully;
  int exitStatus;
  long nrErrors;

  List<ResultFile> files;

  // TODO: these rows could be organized in a Map with rowMeta as keys
  private List<Row> rows;

  public static final class Row {
    private final Object[] row;
    private final RowMetaInterface rowMeta;

    public Row( RowMetaInterface rowMeta, Object[] row ) {
      this.row = row;
      this.rowMeta = rowMeta;
    }

    public Object[] getRow() { return this.row; }
    public RowMetaInterface getRowMeta() { return this.rowMeta; }
  }

  // TODO: Is this needed?
  private KettleElementHelper.KettleType kettleType;

  // TODO: this could be fecthed from this.nrErrors == 0?
  public boolean wasExecutedSuccessfully() { return this.executedSuccessfully; }
  public KettleResult setWasExecutedSuccessfully( boolean executedSuccessfully ) {
    this.executedSuccessfully = executedSuccessfully;
    return this;
  }

  public int getExitStatus() { return this.exitStatus; }
  public KettleResult setExitStatus( int exitStatus ) {
    this.exitStatus = exitStatus;
    return this;
  }

  public long getNumberOfErrors() { return this.nrErrors; }
  public KettleResult setNumberOfErrors( long nrErrors ) {
    this.nrErrors = nrErrors;
    return this;
  }

  // TODO: use org.apache.commons.vfs.FileObject instead? is the remaining info necessary?
  public List<ResultFile> getFiles() { return this.files; }
  public KettleResult setFiles( List<ResultFile> files ) {
    this.files = files;
    return this;
  }

  // Getters / Setters

  public List<Row> getRows() { return this.rows; }

  public KettleElementHelper.KettleType getKettleType() { return this.kettleType; }
  public KettleResult setKettleType( KettleElementHelper.KettleType kettleType ) {
    this.kettleType = kettleType;
    return this;
  }



  // Constructors

  // TODO: do copy constructor from org.pentaho.di.core.Result?

  public KettleResult() {
    this.rows = new ArrayList<Row>();
  }
}
