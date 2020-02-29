package cj.netos.router;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.*;
import cj.netos.router.entities.NetworkNodeInfo;
import cj.netos.router.openports.ListenPosition;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IClosable;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class NetworkNode implements IOnopen, IOnclose, IOnreconnection, IOnnotify, IClosable {
    NetworkNodeInfo info;
    IPeer peer;
    boolean isConnected;
    ICube cube;
    ILogicNetwork logicNetwork;
    IOnmessage onmessage;

    public NetworkNode(NetworkNodeInfo info, ICube cube) {
        this.info = info;
        this.cube = cube;
    }

    public void connect(IOnmessage onmessage) {
        this.onmessage = onmessage;
        peer = Peer.connect(info.getConnURL(), this, this,this, this);
        peer.authByPassword(info.getPeer(), info.getPerson(), info.getPassword());
        logicNetwork = peer.listen(info.getListenNetwork(), info.getListenPosition() == ListenPosition.frontend ? true : false, info.getListenmode());
        logicNetwork.onmessage(onmessage);
        peer.viewServer();
    }

    @Override
    public void close() {
        if (peer != null) {
            peer.close();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void onreconnected(String protocol, String host, int port, Map<String, String> props) {
        try {
            logicNetwork = peer.listen(info.getListenNetwork(), info.getListenPosition() == ListenPosition.frontend ? true : false, info.getListenmode());
            logicNetwork.onmessage(onmessage);
            peer.viewServer();
        } catch (Exception e) {
            CJSystem.logging().error(getClass(), e);
        }
    }

    @Override
    public void onclose() {
        isConnected = false;
        Document filter = Document.parse(String.format("{'tuple.name':'%s','tuple.person':'%s','tuple.peer':'%s'}", info.getRouterName(), info.getPerson(), info.getPeer()));
        Document update = Document.parse(String.format("{'$set':{'tuple.isRunning':'false'}}"));
        cube.updateDocOne("router.nodes", filter, update);
        info = null;
        isConnected = false;
        cube = null;
        logicNetwork = null;
        peer = null;
    }

    @Override
    public void onevent(NetworkFrame frame) {
        Map<String, Object> map = new Gson().fromJson(frame.toJson(), HashMap.class);
        cube.saveDoc("router.nodes.onevents", new TupleDocument<>(map));
        if (frame.command().equals("viewServer")) {
            _doViewServer(map);
        }
    }

    @Override
    public void onerror(NetworkFrame frame) {
        Map<String, Object> map = new Gson().fromJson(frame.toJson(), HashMap.class);
        cube.saveDoc("router.nodes.onerrors", new TupleDocument<>(map));
    }

    @Override
    public void onopen() {
        Document filter = Document.parse(String.format("{'tuple.name':'%s','tuple.person':'%s','tuple.peer':'%s'}", info.getRouterName(), info.getPerson(), info.getPeer()));
        Document update = Document.parse(String.format("{'$set':{'tuple.isRunning':'true'}}"));
        cube.updateDocOne("router.nodes", filter, update);
        isConnected = true;
    }

    public void send(NetworkFrame frame) throws CircuitException {
        logicNetwork.send(frame);
    }

    private void _doViewServer(Map<String, Object> map) {
        String colname = "router.nodes.ports";
        String where = String.format("{'tuple.routerName':'%s','tuple.nodeName':'%s'}", info.getRouterName(), info.getNodeName());
        long count = cube.tupleCount(colname, where);
        if (count > 0) {
            cube.deleteDocOne(colname, where);
        }
        String json = (String) map.get("content");
        Map<String, Object> ports = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        ports.put("routerName", info.getRouterName());
        ports.put("nodeName", info.getNodeName());
        cube.saveDoc(colname, new TupleDocument<>(ports));
    }

    public String nodeName() {
        return info.getNodeName();
    }

    public NetworkNodeInfo getInfo() {
        return info;
    }
}
