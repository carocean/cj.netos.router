package cj.netos.router.program;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.router.entities.RouterInfo;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;
import org.bson.Document;

@CjService(name = "$.cj.studio.gateway.app", isExoteric = true)
public class AppSiteProgram extends GatewayAppSiteProgram {

    @Override
    protected void onstart(Destination dest, String home, ProgramAdapterType arg2) throws CircuitException {
        IRouterConfig config = new DefaultRouterConfig();
        config.load(home);
        site.addService("$.router.config", config);
        ICube cube = (ICube) site.getService("mongodb.netos.home");
        registerRouter(cube, config);
    }

    private void registerRouter(ICube cube, IRouterConfig config) {
        if (existsRouter(cube, config)) {
            updaterRouter(cube, config);
        } else {
            addRouter(cube, config);
        }
        emptyNetworkNodeStates(cube, config);
    }

    private void emptyNetworkNodeStates(ICube cube, IRouterConfig config) {
        //router.nodes.states
        cube.deleteDocs("router.nodes.states", String.format("{'tuple.routerName':'%s'}", config.getProperty("routerName")));
    }

    private void addRouter(ICube cube, IRouterConfig config) {
        RouterInfo routerInfo = new RouterInfo();
        routerInfo.setName(config.getProperty("routerName"));
        routerInfo.setRunning(false);
        cube.saveDoc("routers", new TupleDocument<>(routerInfo));
    }

    private void updaterRouter(ICube cube, IRouterConfig config) {
        Document filter = Document.parse(String.format("{'tuple.name':'%s'}", config.getProperty("routerName")));
        Document update = Document.parse(String.format("{'$set':{'tuple.name':'%s','tuple.isRunning':'false'}}", config.getProperty("routerName")));
        cube.updateDocOne("routers", filter, update);
    }

    private boolean existsRouter(ICube cube, IRouterConfig config) {
        return cube.tupleCount("routers", String.format("{'tuple.name':'%s'}", config.getProperty("routerName"))) > 0;
    }

}
