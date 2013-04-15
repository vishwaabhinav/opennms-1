package org.opennms.netmgt.model.topology;

public final class NodeElementIdentifier extends ElementIdentifier {

	private Integer m_nodeid; 

	public NodeElementIdentifier(Integer nodeid) {
		super(ElementIdentifierType.ONMSNODE);
		m_nodeid = Integer.valueOf(nodeid);
	}

	public Integer getNodeid() {
		return m_nodeid;
	}

	public void setNodeid(Integer nodeid) {
		m_nodeid = nodeid;
	}

	@Override
	public boolean equals(ElementIdentifier elementIdentifier) {
		if (elementIdentifier instanceof NodeElementIdentifier) 
			return (m_nodeid == ((NodeElementIdentifier)elementIdentifier).getNodeid());
		return false;
	}
	
}