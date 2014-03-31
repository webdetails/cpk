package pt.webdetails.cpk.elements.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class KettleResultKey implements Serializable {
  private final HashMap<String, String> parameters;
  private final String outputStepName;
  private final String elementName;

  public String getElementName() {
    return this.elementName;
  }

  public String getOutputStepName() {
    return this.outputStepName;
  }

  public Map<String, String> getParameters() {
    return this.parameters;
  }

  // Constructors

  public KettleResultKey( String elementName, String outputStepName, Map<String, String> parameters ) {
    this.elementName = elementName;
    this.outputStepName = outputStepName;
    this.parameters = new HashMap<String, String>( parameters );
  }

  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( other == null || getClass() != other.getClass() ) {
      return false;
    }

    final KettleResultKey that = (KettleResultKey) other;

    return
         stringEquals( this.elementName, that.elementName )
      && stringEquals( this.outputStepName, that.outputStepName );

  }

  private static boolean stringEquals( String textL, String testR ) {
    return textL != null ? textL.equals( testR ) : testR != null;
  }

  @Override
  public int hashCode() {
    int result = stringHashCode( this.elementName );
    result = 31 * result + stringHashCode( this.outputStepName );
    result = 31 * result + this.parameters.hashCode();
    return result;
  }

  private static int stringHashCode( String text ) {
    return text != null ? text.hashCode() : 0;
  }

  // Serialization


}
