package cj.netos.router.program;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DefaultRouterConfig implements IRouterConfig {
    private Properties properties;

    @Override
    public void load(String home) {
        String dir = String.format("%s%sconf%s", home, File.separator, File.separator);
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        properties = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(String.format("%srouter.properties", dir));
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
