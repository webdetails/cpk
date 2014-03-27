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


import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.row.RowMetaInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class KettleResult {
  private Result result;
  private List<Row> rows;

  public static class Row {

    public Object[] row;
    public RowMetaInterface rowMeta;

    public Row( RowMetaInterface rowMeta, Object[] row ) {
      this.rowMeta = rowMeta;
      this.row = row;
    }
  }

  // Getters / Setters

  // TODO: rename this method or class name. It should be easy to differentiate this class from org.pentaho.di.core.Result
  public Result getResult() { return this.result; }

  public KettleResult setResult( Result result ) {
    this.result = result;
    return this;
  }

  public List<Row> getRows() { return this.rows; }

  public List<ResultFile> getFiles() {
    if ( this.result == null ) {
      return Collections.emptyList();
    }

    return this.result.getResultFilesList();
  }

  // Constructors

  public KettleResult() {
    this.rows = new ArrayList<Row>();
  }
}
