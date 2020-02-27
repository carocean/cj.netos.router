package cj.netos.router.program;

public interface IRouterConfig {
    void load(String home);

    String getProperty(String key);
}
