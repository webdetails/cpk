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


package pt.webdetails.cpk.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public interface IElement {

  @JsonProperty( "type" )
  public String getType();

  @JsonProperty( "pluginId" )
  public String getPluginId();

  @JsonProperty( "id" )
  public String getId();

  @JsonProperty( "name" )
  public String getName();

  @JsonProperty( "location" )
  public String getLocation();

  @JsonProperty( "adminOnly" )
  public boolean isAdminOnly();

  @JsonProperty( "isRenderable" )
  public boolean isRenderable();

  @JsonIgnore
  public void processRequest( Map<String, Map<String, Object>> bloatedMap );
}
