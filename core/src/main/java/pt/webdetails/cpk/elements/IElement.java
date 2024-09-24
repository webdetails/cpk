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
