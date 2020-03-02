package cj.netos.router.services;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.network.ListenMode;
import cj.netos.router.IRouterService;
import cj.netos.router.entities.RouterInfo;
import cj.netos.router.NetworkNode;
import cj.netos.router.entities.NetworkNodeInfo;
import cj.netos.router.openports.ListenPosition;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.openport.ISecuritySession;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CjService(name = "routerService")
public class DefaultRouterService implements IRouterService {
    @CjServiceRef(refByName = "mongodb.netos.home")
    ICube home;

    @Override
    public NetworkNode addNetworkNode(String routerName, String nodeName, String connURL, String peer, String person, String password, String listenNetwork, ListenPosition listenPosition, ListenMode listenmode) {
        NetworkNodeInfo info = new NetworkNodeInfo();
        info.setNodeName(nodeName);
        info.setRouterName(routerName);
        info.setConnURL(connURL);
        info.setPeer(peer);
        info.setListenNetwork(listenNetwork);
        info.setPerson(person);
        info.setPassword(password);
        info.setListenPosition(listenPosition);
        info.setListenmode(listenmode);
        home.saveDoc("router.nodes", new TupleDocument<>(info));
        return new NetworkNode(info, home);
    }

    @Override
    public void removeNetworkNode(String routerName, String nodeName) {
        home.deleteDocOne("router.nodes", String.format("{'tuple.routerName':'%s','tuple.nodeName':'%s'}", routerName, nodeName));
    }

    @Override
    public NetworkNode getNetworkNode(String routerName, String nodeName) {
        String cjql = String.format("select {'tuple':'*'}.limit(1) from tuple router.nodes %s where {'tuple.routerName':'%s','tuple.nodeName':'%s'}", NetworkNodeInfo.class.getName(), routerName, nodeName);
        IQuery<NetworkNodeInfo> q = home.createQuery(cjql);
        IDocument<NetworkNodeInfo> doc = q.getSingleResult();
        if (doc == null) {
            return null;
        }
        NetworkNodeInfo info = doc.tuple();
        return new NetworkNode(info, home);
    }

    @Override
    public void updaterRouterState(ISecuritySession securitySession, String routerName, boolean isRunning) {
        Document filter = Document.parse(String.format("{'tuple.name':'%s'}", routerName));
        Document update = Document.parse(String.format("{'$set':{'tuple.isRunning':'%s'}}", isRunning));
        home.updateDocOne("routers", filter, update);
    }

    @Override
    public void updateNetworkNodeState(String routerName, String nodeName, boolean isRunning) {
        String where = String.format("{'tuple.routerName':'%s','tuple.nodeName':'%s'}", routerName, nodeName);
        Document filter = Document.parse(where);
        long count=home.tupleCount("router.nodes.states", where);
        if (count < 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("routerName", routerName);
            map.put("nodeName", nodeName);
            map.put("isRunning", isRunning+"");
            home.saveDoc("router.nodes.states", new TupleDocument<>(map));
            return;
        }
        Document update = Document.parse(String.format("{'$set':{'tuple.isRunning':'%s'}}", isRunning));
        home.updateDocOne("router.nodes.states", filter, update);
    }

    @Override
    public RouterInfo getRouterInfo(ISecuritySession securitySession, String routerName) {
        String cjql = String.format("select {'tuple':'*'}.limit(1) from tuple routers %s where {'tuple.name':'%s'}", RouterInfo.class.getName(), routerName);
        IQuery<RouterInfo> q = home.createQuery(cjql);
        IDocument<RouterInfo> doc = q.getSingleResult();
        return doc.tuple();
    }

    @Override
    public List<NetworkNodeInfo> listNetworkNodeInfos(String routerName) {
        String cjql = String.format("select {'tuple':'*'} from tuple router.nodes %s where {'tuple.routerName':'%s'}", NetworkNodeInfo.class.getName(), routerName);
        IQuery<NetworkNodeInfo> q = home.createQuery(cjql);
        List<IDocument<NetworkNodeInfo>> docs = q.getResultList();
        List<NetworkNodeInfo> list = new ArrayList<>();
        for (IDocument<NetworkNodeInfo> doc : docs) {
            list.add(doc.tuple());
        }
        return list;
    }

    @Override
    public List<NetworkNode> listNetworkNodes(String routerName) {
        String cjql = String.format("select {'tuple':'*'} from tuple router.nodes %s where {'tuple.routerName':'%s'}", NetworkNodeInfo.class.getName(), routerName);
        IQuery<NetworkNodeInfo> q = home.createQuery(cjql);
        List<IDocument<NetworkNodeInfo>> docs = q.getResultList();
        List<NetworkNode> list = new ArrayList<>();
        for (IDocument<NetworkNodeInfo> doc : docs) {
            list.add(new NetworkNode(doc.tuple(), home));
        }
        return list;
    }

}
