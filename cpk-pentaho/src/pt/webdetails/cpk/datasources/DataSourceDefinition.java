package pt.webdetails.cpk.datasources;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSourceDefinition {

  @JsonProperty("connection")
  protected Map<String, String> connectionParameters = new LinkedHashMap<String, String>();

  @JsonProperty("dataaccess")
  protected Map<String, String> dataAccessParameters = new LinkedHashMap<String, String>();

  public DataSourceDefinition() {
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DataSourceDefinition)) {
      return false;
    }
    DataSourceDefinition other = (DataSourceDefinition) obj;
    if (connectionParameters == null) {
      if (other.connectionParameters != null) {
        return false;
      }
    } else if (!connectionParameters.equals(other.connectionParameters)) {
      return false;
    }
    if (dataAccessParameters == null) {
      if (other.dataAccessParameters != null) {
        return false;
      }
    } else if (!dataAccessParameters.equals(other.dataAccessParameters)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((connectionParameters == null) ? 0 : connectionParameters.hashCode());
    result = prime * result + ((dataAccessParameters == null) ? 0 : dataAccessParameters.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final int maxLen = 10;
    StringBuilder builder = new StringBuilder();
    builder.append("DataSourceDefinition [");
    if (connectionParameters != null) {
      builder.append("connectionParameters=");
      builder.append(toString(connectionParameters.entrySet(), maxLen));
      builder.append(", ");
    }
    if (dataAccessParameters != null) {
      builder.append("dataAccessParameters=");
      builder.append(toString(dataAccessParameters.entrySet(), maxLen));
    }
    builder.append("]");
    return builder.toString();
  }

  private String toString(Collection<?> collection, int maxLen) {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    int i = 0;
    for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
      if (i > 0)
        builder.append(", ");
      builder.append(iterator.next());
    }
    builder.append("]");
    return builder.toString();
  }

}
