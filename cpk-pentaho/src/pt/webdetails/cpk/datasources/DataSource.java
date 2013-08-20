package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSource {

  @JsonProperty("definition")
  private DataSourceDefinition definition;

  @JsonProperty("metadata")
  private DataSourceMetadata metadata;

  public DataSource() {
  }

  /**
   * @return the definition
   */
  public DataSourceDefinition getDefinition() {
    return definition;
  }

  /**
   * @return the metadata
   */
  public DataSourceMetadata getMetadata() {
    return metadata;
  }

  /**
   * @param definition the definition to set
   */
  public DataSource setDefinition(DataSourceDefinition definition) {
    this.definition = definition;
    return this;
  }

  /**
   * @param metadata the metadata to set
   */
  public DataSource setMetadata(DataSourceMetadata metadata) {
    this.metadata = metadata;
    return this;
  }

}
