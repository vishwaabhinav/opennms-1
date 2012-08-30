package org.opennms.features.topology.plugins.topo.simple.internal;

import org.opennms.features.topology.api.TopologyProvider;

public interface EditableTopologyProvider extends TopologyProvider {

	public abstract void removeVertex(Object vertexId);

	public abstract void save(String filename);

	public abstract void load(String filename);

	public abstract void resetContainer();

	public abstract Object addVertex(int x, int y);

	public abstract Object connectVertices(Object sourceVertextId,
			Object targetVertextId);

}