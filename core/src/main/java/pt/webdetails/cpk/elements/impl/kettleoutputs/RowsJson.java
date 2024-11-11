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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// This class aids in the construction of a CDA-like JSON output (made to be build with jackson library)
public class RowsJson {
  private ArrayList<Object[]> rows;
  private RowMetaInterface rowsMeta;

  public RowsJson( Collection<Object[]> rows, RowMetaInterface meta ) {
    init( rows, meta );
  }

  @JsonIgnore
  private void init( Collection<Object[]> rows, RowMetaInterface meta ) {
    this.rows = new ArrayList<Object[]>( rows );
    this.rowsMeta = meta != null ? meta : new RowMeta();
  }

  @JsonProperty( "queryInfo" )
  private QueryInfo getQueryInfo() {
    QueryInfo queryInfo = new QueryInfo( this.rows.size() );
    return queryInfo;
  }

  @JsonProperty( "resultset" )
  private ArrayList<Object[]> getResultset() {
    return this.rows;
  }

  @JsonProperty( "metadata" )
  private ArrayList<Metadata> getMetadata() {
    ArrayList<Metadata> metadataList = new ArrayList<Metadata>();


    List<ValueMetaInterface> meta = this.rowsMeta.getValueMetaList();
    int nrColumns = meta.size();


    Metadata metadata = null;
    String type = null;
    String name = null;
    for ( int i = 0; i < nrColumns; i++ ) {
      type = meta.get( i ).getTypeDesc();
      name = meta.get( i ).getName();

      metadata = new Metadata( i, type, name );
      metadataList.add( metadata );
    }

    return metadataList;
  }


  private class Metadata {
    private int colIndex;
    private String colType;
    private String colName;

    public Metadata( int index, String type, String name ) {
      this.colIndex = index;
      this.colType = type;
      this.colName = name;
    }

    @JsonIgnore
    private void setColIndex( int index ) {
      this.colIndex = index;
    }

    @JsonIgnore
    private void setColType( String type ) {
      this.colType = type;
    }

    @JsonIgnore
    private void setColName( String name ) {
      this.colName = name;
    }

    @JsonProperty( "colIndex" )
    private int getColIndex() {
      return this.colIndex;
    }

    @JsonProperty( "colType" )
    private String getColType() {
      return this.colType;
    }

    @JsonProperty( "colName" )
    private String getColName() {
      return this.colName;
    }
  }

  private class QueryInfo {

    private int rowsCount;

    public QueryInfo( int rowsCount ) {
      setRowsCount( rowsCount );
    }

    @JsonIgnore
    private void setRowsCount( int count ) {
      this.rowsCount = count;
    }

    @JsonProperty( "totalRows" )
    private int getRowsCount() {
      return rowsCount;
    }
  }

}
