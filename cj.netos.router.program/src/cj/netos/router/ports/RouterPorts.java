package cj.netos.router.ports;

import cj.netos.network.ListenMode;
import cj.netos.router.IRouterService;
import cj.netos.router.entities.RouterInfo;
import cj.netos.router.NetworkNode;
import cj.netos.router.entities.NetworkNodeInfo;
import cj.netos.router.openports.IRouterPorts;
import cj.netos.router.openports.ListenPosition;
import cj.netos.router.program.IRouterConfig;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CjService(name = "/router.service")
public class RouterPorts implements IRouterPorts {
    @CjServiceRef
    IRouterService routerService;
    Map<String, NetworkNode> isRunningNetworkNodeMap;//key=name://person/peer
    @CjServiceSite
    IServiceSite site;

    public RouterPorts() {
        isRunningNetworkNodeMap = new ConcurrentHashMap<>();
    }

    void checkRights(ISecuritySession securitySession) throws CircuitException {
        boolean hasRights = false;
        for (int i = 0; i < securitySession.roleCount(); i++) {
            String role = securitySession.role(i);
            if (role.startsWith("app:administrators") || role.startsWith("tenant:administrators") || role.startsWith("platform:tenantAdministrators") || role.startsWith("platform:administrators")) {
                hasRights = true;
            }
        }
        if (!hasRights) {
            throw new CircuitException("801", "无权访问");
        }
    }

    @Override
    public void addNetworkNode(ISecuritySession securitySession, String nodeName, String connURL, String person, String peer, String password, String listenNetwork, ListenPosition listenPosition, ListenMode listenmode) throws CircuitException {
        checkRights(securitySession);
        if (routerService.getNetworkNode(routerName(securitySession), nodeName) != null) {
            throw new CircuitException("500", "已存在路由项：" + routerName(securitySession) + "." + nodeName);
        }
        routerService.addNetworkNode(routerName(securitySession), nodeName, connURL, peer, person, password, listenNetwork, listenPosition, listenmode);
    }

    @Override
    public void removeNetworkNode(ISecuritySession securitySession, String nodeName) throws CircuitException {
        checkRights(securitySession);
        NetworkNode networkNode = isRunningNetworkNodeMap.get(nodeName);
        if (networkNode != null) {
            networkNode.close();
        }
        isRunningNetworkNodeMap.remove(nodeName);
        routerService.removeNetworkNode(routerName(securitySession), nodeName);
    }

    @Override
    public void connectNetworkNode(ISecuritySession securitySession, String nodeName) throws CircuitException {
        checkRights(securitySession);
        if (isNetworkNodeRunning(securitySession, nodeName)) {
            return;
        }
        NetworkNode networkNode = isRunningNetworkNodeMap.get(nodeName);
        if (networkNode == null) {
            networkNode = routerService.getNetworkNode(routerName(securitySession), nodeName);
            if (networkNode != null) {
                isRunningNetworkNodeMap.put(nodeName, networkNode);
            }
        }
        if (networkNode == null) {
            throw new CircuitException("404", "路由器中不存在节点:" + nodeName);
        }
        networkNode.connect(new Onmessage(isRunningNetworkNodeMap, nodeName));
        _updateNetworkNodeState(routerName(securitySession), nodeName, true);
        if(!getRouterInfo(securitySession).isRunning()) {
            updaterRouterState(securitySession, true);
        }
    }

    private void _updateNetworkNodeState(String routerName, String nodeName, boolean b) {
        routerService.updateNetworkNodeState(routerName, nodeName, b);
    }

    @Override
    public void disconnectNetworkNode(ISecuritySession securitySession, String nodeName) throws CircuitException {
        checkRights(securitySession);
        if (!isNetworkNodeRunning(securitySession, nodeName)) {
            return;
        }
        NetworkNode networkNode = isRunningNetworkNodeMap.get(nodeName);
        if (networkNode == null) {
            networkNode = routerService.getNetworkNode(routerName(securitySession), nodeName);
            if (networkNode != null) {
                isRunningNetworkNodeMap.put(nodeName, networkNode);
            }
        }
        if (networkNode == null) {
            throw new CircuitException("404", "路由器中不存在节点:" + nodeName);
        }
        networkNode.close();
        isRunningNetworkNodeMap.remove(nodeName);
        _updateNetworkNodeState(routerName(securitySession), nodeName, false);
        if (isRunningNetworkNodeMap.isEmpty()) {
            updaterRouterState(securitySession, false);
        }
    }

    @Override
    public RouterInfo getRouterInfo(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        return routerService.getRouterInfo(securitySession, routerName(securitySession));
    }

    private void updaterRouterState(ISecuritySession securitySession, boolean isRunning) throws CircuitException {
        checkRights(securitySession);
        routerService.updaterRouterState(securitySession, routerName(securitySession), isRunning);
    }

    @Override
    public boolean isNetworkNodeRunning(ISecuritySession securitySession, String nodeName) throws CircuitException {
        checkRights(securitySession);
        NetworkNode networkNode = isRunningNetworkNodeMap.get(nodeName);
        if (networkNode == null) {
            networkNode = routerService.getNetworkNode(routerName(securitySession), nodeName);
            if (networkNode != null) {
                isRunningNetworkNodeMap.put(nodeName, networkNode);
            }
        }
        if (networkNode == null) {
            throw new CircuitException("404", "路由器中不存在节点:" + nodeName);
        }
        return networkNode.isConnected();
    }

    @Override
    public void safeStart(ISecuritySession securitySession) throws CircuitException {
        //如果没有启动的才启动
        for (Map.Entry<String, NetworkNode> entry : isRunningNetworkNodeMap.entrySet()) {
            if (!entry.getValue().isConnected()) {
                entry.getValue().connect(new Onmessage(this.isRunningNetworkNodeMap, entry.getKey()));
                _updateNetworkNodeState(routerName(securitySession), entry.getKey(), true);
            }
        }
        List<NetworkNode> peers = routerService.listNetworkNodes(routerName(securitySession));
        for (NetworkNode peer : peers) {
            if (isRunningNetworkNodeMap.containsKey(peer.nodeName())) {
                continue;
            }
            peer.connect(new Onmessage(this.isRunningNetworkNodeMap, peer.nodeName()));
            isRunningNetworkNodeMap.put(peer.nodeName(), peer);
            _updateNetworkNodeState(routerName(securitySession), peer.nodeName(), true);
        }
        if (isRunningNetworkNodeMap.isEmpty()) {
            updaterRouterState(securitySession, false);
        } else {
            updaterRouterState(securitySession, true);
        }
    }

    @Override
    public void start(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        if (getRouterInfo(securitySession).isRunning()) {
            return;
        }
        List<NetworkNode> peers = routerService.listNetworkNodes(routerName(securitySession));
        for (NetworkNode peer : peers) {
            peer.connect(new Onmessage(isRunningNetworkNodeMap, peer.nodeName()));
            isRunningNetworkNodeMap.put(peer.nodeName(), peer);
            _updateNetworkNodeState(routerName(securitySession), peer.nodeName(), true);
        }
        if (isRunningNetworkNodeMap.isEmpty()) {
            updaterRouterState(securitySession, false);
        } else {
            updaterRouterState(securitySession, true);
        }
    }

    @Override
    public void stop(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        if (!getRouterInfo(securitySession).isRunning()) {
            return;
        }
        for (Map.Entry<String, NetworkNode> peerEntry : isRunningNetworkNodeMap.entrySet()) {
            peerEntry.getValue().close();
            _updateNetworkNodeState(routerName(securitySession), peerEntry.getValue().nodeName(), false);
        }
        isRunningNetworkNodeMap.clear();
        updaterRouterState(securitySession, false);
    }

    @Override
    public String routerName(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        IRouterConfig config = (IRouterConfig) site.getService("$.router.config");
        return config.getProperty("routerName");
    }

    @Override
    public void restart(ISecuritySession securitySession) throws CircuitException {
        stop(securitySession);
        start(securitySession);
    }

    @Override
    public String[] listRunningNetworkNode(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        return isRunningNetworkNodeMap.keySet().toArray(new String[0]);
    }

    @Override
    public List<NetworkNodeInfo> listNetworkNodeInfos(ISecuritySession securitySession) throws CircuitException {
        checkRights(securitySession);
        return this.routerService.listNetworkNodeInfos(routerName(securitySession));
    }
}
