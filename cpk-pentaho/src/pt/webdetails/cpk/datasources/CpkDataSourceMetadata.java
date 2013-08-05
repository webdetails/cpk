package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CpkDataSourceMetadata extends DataSourceMetadata {

  @JsonProperty("pluginId")
  private String pluginId;

  @JsonProperty("endpoint")
  private String endpointName;

  /**
   * @param pluginId the pluginId to set
   */
  protected void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  /**
   * @return the pluginId
   */
  public String getPluginId() {
    return pluginId;
  }

  public CpkDataSourceMetadata(String pluginId, String endpointName) {
    setName(String.format("%s Endpoint", endpointName));
    setPluginId(pluginId);
    setEndpointName(endpointName);
    /*
     *  which data type should be declared?
     *  is it plugin implementation dependable?
     */
    setDataType("cpk");
    setGroup(String.format("%s_CPKENDPOINTS", pluginId.toUpperCase()));
    setGroupDescription(String.format("%s Endpoints", pluginId.toUpperCase()));
  }

  /**
   * @param endpointName the endpointName to set
   */
  protected void setEndpointName(String endpointName) {
    this.endpointName = endpointName;
  }

}
