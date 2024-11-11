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


package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSource {

  private DataSourceDefinition definition;
  private DataSourceMetadata metadata;

  public DataSource() {
  }

  /**
   * @return the definition
   */
  @JsonProperty( "definition" )
  public DataSourceDefinition getDefinition() {
    return definition;
  }

  /**
   * @return the metadata
   */
  @JsonProperty( "metadata" )
  public DataSourceMetadata getMetadata() {
    return metadata;
  }

  /**
   * @param definition the definition to set
   */
  public DataSource setDefinition( DataSourceDefinition definition ) {
    this.definition = definition;
    return this;
  }

  /**
   * @param metadata the metadata to set
   */
  public DataSource setMetadata( DataSourceMetadata metadata ) {
    this.metadata = metadata;
    return this;
  }
}
