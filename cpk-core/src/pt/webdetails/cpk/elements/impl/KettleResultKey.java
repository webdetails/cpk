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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class KettleResultKey implements Serializable {
  private static final long serialVersionUID = 928475298475L;

  private final String pluginId;
  private final String elementId;
  private final String outputStepName;
  private final Map<String, String> parameters;

  public String getPluginId() {
    return this.pluginId;
  }
  public String getElementId() {
    return this.elementId;
  }
  public String getOutputStepName() {
    return this.outputStepName;
  }
  public Map<String, String> getParameters() { return Collections.unmodifiableMap( this.parameters ); }


  // region Constructors

  public KettleResultKey( String pluginId, String elementId, String outputStepName, Map<String, String> parameters ) {
    this.pluginId = pluginId;
    this.elementId = elementId;
    this.outputStepName = outputStepName;
    this.parameters = new HashMap<String, String>( parameters );
  }

  // endregion

  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( other == null || getClass() != other.getClass() ) {
      return false;
    }

    final KettleResultKey that = (KettleResultKey) other;

    return stringEquals( this.pluginId, that.pluginId )
      && stringEquals( this.elementId, that.elementId )
      && stringEquals( this.outputStepName, that.outputStepName )
      && this.parameters.equals( that.parameters );
  }

  private static boolean stringEquals( String textL, String testR ) {
    return textL == null ? testR == null : textL.equals( testR );
  }

  @Override
  public int hashCode() {
    int result = stringHashCode( this.pluginId );
    result = 31 * result + stringHashCode( this.elementId );
    result = 31 * result + stringHashCode( this.outputStepName );
    result = 31 * result + this.parameters.hashCode();
    return result;
  }

  private static int stringHashCode( String text ) {
    return text != null ? text.hashCode() : 0;
  }

}
