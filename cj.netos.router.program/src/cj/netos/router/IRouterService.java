package cj.netos.router;

import cj.netos.network.ListenMode;
import cj.netos.router.entities.RouterInfo;
import cj.netos.router.entities.NetworkNodeInfo;
import cj.netos.router.openports.ListenPosition;
import cj.studio.openport.ISecuritySession;

import java.util.List;

public interface IRouterService {

    NetworkNode addNetworkNode(String routerName, String nodeName, String connURL, String peer, String person, String password, String listenNetwork, ListenPosition listenPosition, ListenMode listenmode);


    void removeNetworkNode(String routerName, String nodeName);

    NetworkNode getNetworkNode(String routerName, String nodeName);

    List<NetworkNodeInfo> listNetworkNodeInfos(String routerName);

    List<NetworkNode> listNetworkNodes(String routerName);

    RouterInfo getRouterInfo(ISecuritySession securitySession, String routerName);

    void updaterRouterState(ISecuritySession securitySession, String routerName, boolean isRunning);

    void updateNetworkNodeState(String routerName, String nodeName, boolean b);

}
