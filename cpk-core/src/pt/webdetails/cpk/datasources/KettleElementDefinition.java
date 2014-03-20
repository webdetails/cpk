package pt.webdetails.cpk.datasources;


public class KettleElementDefinition extends DataSourceDefinition {

  protected Parameter outputStepName;
  protected Parameter outputType;

  public KettleElementDefinition() {
    this.outputStepName = new Parameter( "STRING", "ATTRIB" );
    this.outputType = new Parameter( "STRING", "ATTRIB" );

    this.dataAccessParameters.put( "outputStepName", this.outputStepName );
    this.dataAccessParameters.put( "outputType", this.outputType );
  }

}
