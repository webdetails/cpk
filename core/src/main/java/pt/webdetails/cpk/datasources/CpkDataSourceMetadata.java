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

public class CpkDataSourceMetadata extends DataSourceMetadata {

  private String endpointName;

  public CpkDataSourceMetadata() {
    /*
     *  which data type should be declared?
     *  is it plugin implementation dependable?
     */
    setDataType( "cpk" );
  }

  public CpkDataSourceMetadata( String pluginId, String endpointName ) {
    this();
    this.setPluginId( pluginId )
        .setEndpointName( endpointName );
  }

  @Override
  public CpkDataSourceMetadata setPluginId( String pluginId ) {
    super.setPluginId( pluginId );
    this.setGroup( String.format( "%s_CPKENDPOINTS", pluginId.toUpperCase() ) )
        .setGroupDescription( String.format( "%s Endpoints", pluginId.toUpperCase() ) );
    return this;
  }

  @JsonProperty( "endpoint" )
  public String getEndpointName() {
    return this.endpointName;
  }

  /**
   * Sets the endpoint name and renames the name to "[EndpointName] Endpoint".
   * @param endpointName the endpointName to set.
   *
   */
  public CpkDataSourceMetadata setEndpointName( String endpointName ) {
    this.endpointName = endpointName;
    this.setName( String.format( "%s Endpoint", endpointName ) );
    return this;
  }
}
