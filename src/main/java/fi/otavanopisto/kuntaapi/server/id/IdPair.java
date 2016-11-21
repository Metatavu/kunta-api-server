package fi.otavanopisto.kuntaapi.server.id;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Meta Id that describes a id relationship
 * 
 * @author Antti Leppä
 *
 * @param <P> parent id type
 * @param <C> child id type
 */
public class IdPair <P extends BaseId, C extends BaseId> implements Serializable {
  
  private static final long serialVersionUID = -7466669510105731272L;
  
  private P parent;
  private C child;
  
  public IdPair(P parent, C child) {
    this.parent = parent;
    this.child = child;
  }
  
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder(783, 433)
      .append(child.getSource())
      .append(child.getId())
      .append(parent.getSource())
      .append(parent.getId());
    
    if (child instanceof OrganizationBaseId) {
      builder.append(((OrganizationBaseId) child).getOrganizationId().getSource()); 
      builder.append(((OrganizationBaseId) child).getOrganizationId().getId()); 
    }
    
    if (parent instanceof OrganizationBaseId) {
      builder.append(((OrganizationBaseId) parent).getOrganizationId().getSource()); 
      builder.append(((OrganizationBaseId) parent).getOrganizationId().getId()); 
    }
    
    return builder.toHashCode();
  }
  
  public boolean parentEquals(P id) {
    return parent.equals(id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IdPair) {
      @SuppressWarnings("rawtypes") IdPair another = (IdPair) obj;
      return another.child.equals(this.child) && another.parent.equals(this.parent);
    }

    return false;
  }

  public C getChild() {
    return child;
  }
  
  public P getParent() {
    return parent;
  }
  
}