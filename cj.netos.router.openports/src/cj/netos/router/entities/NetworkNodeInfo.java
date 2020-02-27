package cj.netos.router.entities;

import cj.netos.network.ListenMode;
import cj.netos.router.openports.ListenPosition;

public class NetworkNodeInfo {
    String nodeName;
    String routerName;
    String connURL;
    String peer;
    String person;
    String password;
    String listenNetwork;
    ListenPosition listenPosition;
    ListenMode listenmode;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getListenNetwork() {
        return listenNetwork;
    }

    public void setListenNetwork(String listenNetwork) {
        this.listenNetwork = listenNetwork;
    }


    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getConnURL() {
        return connURL;
    }

    public void setConnURL(String connURL) {
        this.connURL = connURL;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ListenPosition getListenPosition() {
        return listenPosition;
    }

    public void setListenPosition(ListenPosition listenPosition) {
        this.listenPosition = listenPosition;
    }

    public ListenMode getListenmode() {
        return listenmode;
    }

    public void setListenmode(ListenMode listenmode) {
        this.listenmode = listenmode;
    }

}
