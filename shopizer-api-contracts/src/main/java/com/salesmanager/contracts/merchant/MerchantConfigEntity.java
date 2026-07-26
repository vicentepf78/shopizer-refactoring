package com.salesmanager.contracts.merchant;


import com.salesmanager.contracts.common.Entity;

public class MerchantConfigEntity extends Entity {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String key;
  /** Configuration type name (e.g. SOCIAL); string avoids core-model enum in contracts. */
  private String type;
  private String value;
  private boolean active;
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public boolean isActive() {
    return active;
  }
  public void setActive(boolean active) {
    this.active = active;
  }

}
