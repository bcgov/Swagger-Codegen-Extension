package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.JavaJAXRSCXFCDIServerCodegen;
import java.util.*;
import java.io.File;
import static java.util.UUID.randomUUID;

/**
 *
 * @author george
 */
public class FuseGenerator extends JavaJAXRSCXFCDIServerCodegen implements CodegenConfig {
    
    protected String implFolder = "src/main/java";
    
    public FuseGenerator()
    {
        super();
        super.embeddedTemplateDir = templateDir = "fuse" + File.separator + "cxf-cdi";
        
        //apiTemplateFiles.put("apiServiceFactory.mustache", ".java");
        
        // Three API templates to support CDI injection
        
        // Currently there is one application per API.  These could be consolidated into one application.  
        apiTemplateFiles.put("apiApplication.mustache",".java");          
        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
                  
        // Use standard types
        typeMapping.put("DateTime", "java.util.Date");
        
        apiPackage = "io.swagger.client.api";

        /**
         * Model Package.  Optional, if needed, this can be used in templates
         */
        modelPackage = "io.swagger.client.model";        
    }
    
    @Override
    public void processOpts() {
        super.processOpts();
        supportingFiles.clear(); // Don't need extra files provided by AbstractJAX-RS & Java Codegen
        writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));        
        
        // custom settings file for s2i build.
        supportingFiles.add(new SupportingFile("settings.xml.mustache", "configuration", "settings.xml"));
        
        String apiRelativePath = sourceFolder + File.separator + "main" + File.separator + "java" + File.separator + apiPackage.replace(".", "/");
        supportingFiles.add(new SupportingFile("ApplicationStarter.mustache", apiRelativePath, "ApplicationStarter.java"));

        // Add the stock resources.  These are all required in order for the services to be found.        
        String resourcesRelativePath = sourceFolder + File.separator + "main" + File.separator + "resources" ;
        supportingFiles.add(new SupportingFile("resources" + File.separator + "log4j.properties", resourcesRelativePath, "log4j.properties"));
        supportingFiles.add(new SupportingFile("resources" + File.separator + "META-INF" + File.separator + "beans.xml", resourcesRelativePath  + File.separator + "META-INF", "beans.xml"));        
    }
    
    
    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator +  "main" + File.separator + "java" + File.separator + modelPackage().replace('.', '/');
    }    

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
      return outputFolder + File.separator + sourceFolder + File.separator +  "main" + File.separator + "java" + File.separator + apiPackage().replace('.', '/');
    }    
    
    @Override
    public String apiFilename(String templateName, String tag) {
        String result = apiFileFolder() + '/' + toApiFilename(tag) + apiTemplateFiles().get(templateName);

        if ( templateName.endsWith("Impl.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/impl" + result.substring(ix, result.length() - 5) + "ServiceImpl.java";
            result = result.replace(apiFileFolder(), implFileFolder(implFolder));
        } else if ( templateName.endsWith("Factory.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/factories" + result.substring(ix, result.length() - 5) + "ServiceFactory.java";
            result = result.replace(apiFileFolder(), implFileFolder(implFolder));
        } else if ( templateName.endsWith("Application.mustache") ) {
            // special case for the Application class
            int ix = result.lastIndexOf('.');
            result = result.substring(0, ix) + "Application.java";
        } else if ( templateName.endsWith("Service.mustache") ) {
            int ix = result.lastIndexOf('.');
            result = result.substring(0, ix) + "Service.java";
        }
        return result;
    }
    
    private String implFileFolder(String output) {
        return outputFolder + "/" + output + "/" + apiPackage().replace('.', '/');
    }
    /*
    private String implFileFolder(String output) {
        return outputFolder + File.separator + sourceFolder + File.separator + apiPackage().replace('.', '/');
    }
    */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }
    
    public String getName() {
        return "fuse";
    }
     
    public String getHelp() {
        return "Generates a fuse file set";
    }
}
