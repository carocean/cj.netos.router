package cj.netos.router.ports;

import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IOnmessage;
import cj.netos.router.NetworkNode;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.util.Map;

public class Onmessage implements IOnmessage {
    Map<String, NetworkNode> routerPeerMap;
    String nodeName;
    public Onmessage(Map<String, NetworkNode> routerPeerMap,String nodeName) {
        this.routerPeerMap = routerPeerMap;
        this.nodeName=nodeName;
    }

    @Override
    public void onmessage(ILogicNetwork logicNetwork, NetworkFrame frame) {
//        CJSystem.logging().info(getClass(),frame);
        for (Map.Entry<String, NetworkNode> entry : routerPeerMap.entrySet()) {
            if (nodeName.equals(entry.getKey())) {
                continue;
            }
            try {
                entry.getValue().send(frame);
            } catch (CircuitException e) {
                CJSystem.logging().error(getClass(), e);
            }
        }
    }
}
