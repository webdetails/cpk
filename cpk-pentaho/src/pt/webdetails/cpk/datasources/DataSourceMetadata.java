package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSourceMetadata {

  @JsonProperty("datype")
  protected String dataType;

  @JsonProperty("group")
  protected String groupId;

  @JsonProperty("groupdesc")
  protected String groupDescription;

  @JsonProperty("name")
  protected String name;

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
    if (!(obj instanceof DataSourceMetadata)) {
      return false;
    }
    DataSourceMetadata other = (DataSourceMetadata) obj;
    if (dataType == null) {
      if (other.dataType != null) {
        return false;
      }
    } else if (!dataType.equals(other.dataType)) {
      return false;
    }
    if (groupId == null) {
      if (other.groupId != null) {
        return false;
      }
    } else if (!groupId.equals(other.groupId)) {
      return false;
    }
    if (groupDescription == null) {
      if (other.groupDescription != null) {
        return false;
      }
    } else if (!groupDescription.equals(other.groupDescription)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * @return the datype
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * @return the group
   */
  public String getGroup() {
    return groupId;
  }

  /**
   * @return the groupdesc
   */
  public String getGroupDescription() {
    return groupDescription;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
    result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
    result = prime * result + ((groupDescription == null) ? 0 : groupDescription.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * @param datype the datype to set
   */
  public DataSourceMetadata setDataType(String datype) {
    this.dataType = datype;
    return this;
  }

  /**
   * @param group the group to set
   */
  public DataSourceMetadata setGroup(String group) {
    this.groupId = group;
    return this;
  }

  /**
   * @param groupDescription the groupDescription to set
   */
  public DataSourceMetadata setGroupDescription(String groupDescription) {
    this.groupDescription = groupDescription;
    return this;
  }

  /**
   * @param name the name to set
   */
  public DataSourceMetadata setName(String name) {
    this.name = name;
    return this;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DataSourceMetadata [");
    if (dataType != null) {
      builder.append("dataType=");
      builder.append(dataType);
      builder.append(", ");
    }
    if (groupId != null) {
      builder.append("groupId=");
      builder.append(groupId);
      builder.append(", ");
    }
    if (groupDescription != null) {
      builder.append("groupDescription=");
      builder.append(groupDescription);
      builder.append(", ");
    }
    if (name != null) {
      builder.append("name=");
      builder.append(name);
    }
    builder.append("]");
    return builder.toString();
  }

}
