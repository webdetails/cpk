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
