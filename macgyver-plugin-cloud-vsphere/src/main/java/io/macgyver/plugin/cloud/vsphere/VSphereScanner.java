/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.cloud.vsphere;

import io.macgyver.neorx.rest.NeoRxClient;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.HostHardwareInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ServerConnection;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VSphereScanner {

	ObjectMapper mapper = new ObjectMapper();
	Logger logger = LoggerFactory.getLogger(VSphereScanner.class);

	ServiceInstance serviceInstance;
	NeoRxClient client;
	String vcenterUuid;

	protected VSphereScanner() {
		// for unit testing
	}

	public VSphereScanner(ServiceInstance si, NeoRxClient client) {

		Preconditions.checkNotNull(si);
		Preconditions.checkNotNull(client);
		serviceInstance = si;
		this.client = client;
	}

	protected JsonNode ensureController() {
		String cypher = "merge (c:ComputeController {macId: {macId}}) set c.type='vcenter' return c";
		return client.execCypher(cypher, "macId", getVCenterId()).toBlocking()
				.first();
	}

	public synchronized String getVCenterId() {
		if (vcenterUuid == null) {
			vcenterUuid = serviceInstance.getAboutInfo().getInstanceUuid();
		}
		return vcenterUuid;
	}

	private void setVal(ObjectNode n, String prop, String val) {
		n.put(prop, val);
	}

	public String createSetClause(String alias, JsonNode n) {
		StringBuffer sb = new StringBuffer();
		Iterator<String> t = n.fieldNames();
		int i = 0;
		while (t.hasNext()) {
			String key = t.next();

			sb.append(String.format("%s %s.%s={%s} ", (i > 0) ? "," : "",
					alias, key, key));
			i++;
		}
		return sb.toString();
	}

	public void updateComputeHost(ObjectNode n) {
		String setClause = createSetClause("c", n);

		String cypher = String
				.format("merge (c:ComputeHost {macId:{macId}}) on match set %s ,c.updateTs=timestamp() ON CREATE SET %s, c.updateTs=timestamp() return c",
						setClause, setClause);

		JsonNode computeHost = client.execCypher(cypher, n).toBlocking()
				.first();

		JsonNode vcenter = ensureController();

		String vcenterMacId = vcenter.get("macId").asText();
		cypher = "match (c:ComputeController {macId:{vcenterMacId}}), (h:ComputeHost {macId:{hostMacId} }) MERGE (c)-[r:MANAGES]->(h) ON CREATE SET r.updateTs=timestamp() ON MATCH SET r.updateTs=timestamp() return r";
		client.execCypher(cypher, "vcenterMacId", vcenterMacId, "hostMacId",
				computeHost.get("macId").asText());
	}

	public void updateComputeInstance(ObjectNode n) {

		String setClause = createSetClause("c", n);

		String cypher = "merge (c:ComputeInstance {macId:{macId}}) on match set "
				+ setClause
				+ ",c.updateTs=timestamp() ON CREATE SET "
				+ setClause + ", c.updateTs=timestamp() return c";

		client.execCypher(cypher, n).toBlocking().first();

	}

	String getMacUuid(VirtualMachine vm) {
		return vm.getConfig().getInstanceUuid();
	}

	public String computeMacId(ManagedObjectReference mor) {
		Preconditions
				.checkNotNull(mor, "ManagedObjectReference cannot be null");

		Preconditions.checkArgument(
				!ManagedObjectTypes.VIRTUAL_MACHINE.equals(mor.getType()),
				"cannot call computeMacId() with mor.type=VirtualMachine");

		return Hashing
				.sha1()
				.hashString(getVCenterId() + mor.getType() + mor.getVal(),
						Charsets.UTF_8).toString();
	}

	ObjectNode toComputeNodeData(VirtualMachine vm) {
		ObjectNode n = mapper.createObjectNode();
		try {
			VirtualMachineConfigInfo cfg = vm.getConfig();

			// http://www.virtuallyghetto.com/2011/11/vsphere-moref-managed-object-reference.html

			ServerConnection sc = vm.getServerConnection();

			ManagedObjectReference mor = vm.getMOR();
			String moType = mor.getType();
			String moVal = mor.getVal();
			GuestInfo g = vm.getGuest();
			setVal(n, "name", vm.getName());
			setVal(n, "macId", getMacUuid(vm));
			setVal(n, "vmw_instanceUuid", cfg.getInstanceUuid());
			setVal(n, "vmw_morVal", moVal);
			setVal(n, "vmw_morType", moType);
			setVal(n, "vmw_annotation", cfg.getAnnotation());
			setVal(n, "vmw_guestToolsVersion", g.getToolsVersion());
			setVal(n, "vmw_guestId", g.getGuestId());
			setVal(n, "vmw_guestFamily", g.getGuestFamily());
			setVal(n, "vmw_guestFullName", g.getGuestFullName());
			setVal(n, "vmw_guestIpAddress", g.getIpAddress());
			setVal(n, "vmw_guestId", g.getGuestId());
			setVal(n, "vmw_guestHostName", g.getHostName());
			setVal(n, "vmw_guestAlternateName", cfg.getAlternateGuestName());
			setVal(n, "vmw_locationId", cfg.getLocationId());
			setVal(n, "vmw_numCPU", ""+cfg.getHardware().getMemoryMB());
			setVal(n, "vmw_memoryMB", ""+cfg.getHardware().getNumCPU());

		} catch (Exception e) {
			logger.warn("", e);
		}
		return n;
	}

	public void scan(VirtualMachine vm) {
		logger.debug("scanning vm: {}", vm.getName());
		ObjectNode n = toComputeNodeData(vm);
		updateComputeInstance(n);
	}

	ObjectNode toObjectNode(HostSystem host) {
		ManagedObjectReference mor = host.getMOR();
		HostHardwareInfo hh = host.getHardware();
		ObjectNode n = mapper.createObjectNode().put("macId", getMacId(host))
				.put("name", host.getName()).put("vmw_morType", mor.getType())
				.put("vmw_morVal", mor.getVal()).put("vmw_hardwareModel",

				hh.getSystemInfo().getModel())
				.put("vmw_cpuCoreCount", hh.getCpuInfo().getNumCpuCores())
				.put("vmw_memorySize", hh.getMemorySize());

		return n;
	}

	public void updateHostVmRelationship(HostSystem h, VirtualMachine vm) {

		logger.debug("updating relationship between host={} and vm={}",
				h.getName(), vm.getName());
		String cypher = "match (h:ComputeHost {macId:{hostMacUuid} }), (c:ComputeInstance {macId: {computeMacUuid}}) MERGE (h)-[r:HOSTS]->(c) ON CREATE SET r.updateTs=timestamp(),r.createTs=timestamp() ON MATCH SET r.updateTs=timestamp() return r";
		client.execCypher(cypher, "hostMacUuid", getMacId(h), "computeMacUuid",
				getMacUuid(vm));

		// This might be a better place to remove any *other* rlationships from
		// VM->Host

	}

	public void scanHost(HostSystem host) {
		try {
			logger.info("scanning esxi host {} and vms hosted on it",
					host.getName());
			ObjectNode n = toObjectNode(host);

			updateComputeHost(n);

			long now = client.execCypher("return timestamp() as ts")
					.toBlocking().first().asLong();

			VirtualMachine[] vms = host.getVms();
			if (vms != null) {
				for (VirtualMachine vm : vms) {
					try {
						scan(vm);
					} catch (RuntimeException e) {
						logger.warn("", e);
					}

					// Use a separate try/catch for the relationships so that if
					// something went wrong above, we still get the
					// relationships right.
					try {
						updateHostVmRelationship(host, vm);
					} catch (RuntimeException e) {
						logger.warn("", e);
					}
				}
			}

			clearStaleRelationships(host, now);
		} catch (RemoteException e) {
			throw new VSphereExceptionWrapper(e);
		}

	}

	protected String getMacId(HostSystem h) {
		return computeMacId(h.getMOR());

	}

	protected void clearStaleRelationships(HostSystem host, long ts) {
		logger.info(
				"clearing stale ComputeHost->ComputeInstance relationships for host: {}",
				host.getName());
		String cypher = "match (h:ComputeHost {macId:{macId}})-[r:HOSTS]-(c:ComputeInstance) where r.updateTs<{ts} delete r";
		
		// this is for logging only
			
			for (JsonNode n : client
					.execCypher(cypher.replace("delete r", "return r"), "macId", getMacId(host), "ts", ts)
					.toBlocking().toIterable()) {
				logger.info("clearing stale relationship: {}", n);
			}
		// end of logging section


		client.execCypher(cypher, "macId", getMacId(host), "ts", ts);
	}

	public void scanAllHosts() {
		VSphereQueryTemplate t = new VSphereQueryTemplate(serviceInstance);
		for (HostSystem host : t.findAllHostSystems()) {

			try {

				scanHost(host);

			} catch (RuntimeException e) {
				logger.warn("scan host failed: {}", host.getName());
			}

		}
	}

}
