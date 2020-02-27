package cj.netos.router.openports;

import cj.netos.network.ListenMode;
import cj.netos.router.entities.RouterInfo;
import cj.netos.router.entities.NetworkNodeInfo;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.List;

@CjOpenports(usage = "路由器负载到远程network的地址管理")
public interface IRouterPorts extends IOpenportService {
    @CjOpenport(usage = "添加路由项")
    void addNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "为要连接的节点服务器起个名，在本路由器内唯一", name = "nodeName") String nodeName,
            @CjOpenportParameter(usage = "连接地址，例：wss://ip:port?propertykey=v1", name = "connURL") String connURL,
            @CjOpenportParameter(usage = "公号", name = "person") String person,
            @CjOpenportParameter(usage = "端点名", name = "peer") String peer,
            @CjOpenportParameter(usage = "密码", name = "password") String password,
            @CjOpenportParameter(usage = "要侦听的网络", name = "listenNetwork") String listenNetwork,
            @CjOpenportParameter(usage = "侦听位置，frontend|backend", name = "listenPosition", defaultValue = "frontend") ListenPosition listenPosition,
            @CjOpenportParameter(usage = "侦听模式,upstream|downstream|both", name = "listenmode", defaultValue = "both") ListenMode listenmode
    ) throws CircuitException;

    @CjOpenport(usage = "移除路由项")
    void removeNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "为要连接的节点服务器起个名，在本路由器内唯一", name = "nodeName") String nodeName
    ) throws CircuitException;

    @CjOpenport(usage = "连接路由项")
    void connectNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "为要连接的节点服务器起个名，在本路由器内唯一", name = "nodeName") String nodeName
    ) throws CircuitException;

    @CjOpenport(usage = "断开路由项")
    void disconnectNetworkNode(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "为要连接的节点服务器起个名，在本路由器内唯一", name = "nodeName") String nodeName
    ) throws CircuitException;

    @CjOpenport(usage = "获取路由器信息")
    RouterInfo getRouterInfo(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "是否在运行状态")
    boolean isNetworkNodeRunning(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "为要连接的节点服务器起个名，在本路由器内唯一", name = "nodeName") String nodeName
    ) throws CircuitException;

    @CjOpenport(usage = "安全启动路由器，即：不会影响已连接的节点")
    void safeStart(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "运行路由器")
    void start(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "停止路由器")
    void stop(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "获取本地路由器名")
    String routerName(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "重启路由")
    void restart(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "获取正在运行的路由项列表")
    String[] listRunningNetworkNode(ISecuritySession securitySession) throws CircuitException;

    @CjOpenport(usage = "获取路由表")
    List<NetworkNodeInfo> listNetworkNodeInfos(ISecuritySession securitySession) throws CircuitException;

}
