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
