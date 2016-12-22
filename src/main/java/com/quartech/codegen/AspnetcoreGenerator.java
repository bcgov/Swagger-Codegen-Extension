package com.quartech.codegen;

import io.swagger.codegen.*;
import io.swagger.models.properties.*;
import io.swagger.codegen.languages.AbstractCSharpCodegen;
import java.util.*;
import java.io.File;
import static java.util.UUID.randomUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspnetcoreGenerator extends AbstractCSharpCodegen implements CodegenConfig {


    
    protected String apiVersion = "1.0.0";

    private final String packageGuid = "{" + randomUUID().toString().toUpperCase() + "}";
    
    private final String project1Guid = "{" + randomUUID().toString().toUpperCase() + "}";
    private final String project2Guid = "{" + randomUUID().toString().toUpperCase() + "}";
    private final String solutionItemsGuid = "{" + randomUUID().toString().toUpperCase() + "}";    
    private final String testPackageGuid = "{" + randomUUID().toString().toUpperCase() + "}";    
    private final String sonarProjectGuid = "{" + randomUUID().toString().toUpperCase() + "}";

  //  @SuppressWarnings("hiding")
    protected Logger LOGGER = LoggerFactory.getLogger(AspnetcoreGenerator.class);
  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     io.swagger.codegen.CodegenType
   */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -l flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "aspnetmvc";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates an ASP.NET Core MVC file set";
  }

  public AspnetcoreGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code" + File.separator + this.getName();

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    
    modelTemplateFiles.put("model.mustache", ".cs");

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    
    apiTemplateFiles.put("controller.mustache", ".cs");
    apiTemplateFiles.put("controllerService.mustache", ".cs");
    apiTemplateFiles.put("controllerServiceImpl.mustache", ".cs");
    
    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "aspnetCore";

    /**
     * Api Package.  Optional, if needed, this can be used in templates
     */
    apiPackage = "io.swagger.client.api";

    /**
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = "io.swagger.client.model";
    
    // test templates
    
    apiTestTemplateFiles.put("test" + File.separator + "integration.mustache", "Integration.cs");
    apiTestTemplateFiles.put("test" + File.separator + "unit.mustache", "Unit.cs");
    
    modelTestTemplateFiles.put("test" + File.separator + "model.mustache", ".cs");
        
    /**
     * Reserved words.  Override this with reserved words specific to your language
     */    
            
    reservedWords = new HashSet<String> (
      Arrays.asList(
        "var", "async", "await", "dynamic", "yield")
    );
    
    cliOptions.clear();

    // CLI options
    addOption(CodegenConstants.PACKAGE_NAME,
            "C# package name (convention: Title.Case).",
            this.packageName);

    addOption(CodegenConstants.PACKAGE_VERSION,
            "C# package version.",
            this.packageVersion);

    addOption(CodegenConstants.SOURCE_FOLDER,
            CodegenConstants.SOURCE_FOLDER_DESC,
            this.sourceFolder);

    // CLI Switches
    addSwitch(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG,
            CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG_DESC,
            this.sortParamsByRequiredFlag);

    addSwitch(CodegenConstants.USE_DATETIME_OFFSET,
            CodegenConstants.USE_DATETIME_OFFSET_DESC,
            this.useDateTimeOffsetFlag);

    addSwitch(CodegenConstants.USE_COLLECTION,
            CodegenConstants.USE_COLLECTION_DESC,
            this.useCollection);

    addSwitch(CodegenConstants.RETURN_ICOLLECTION,
            CodegenConstants.RETURN_ICOLLECTION_DESC,
            this.returnICollection);
    
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  @Override
    public void processOpts() {
        super.processOpts();

        additionalProperties.put("packageGuid", packageGuid);
        additionalProperties.put("project1Guid", project1Guid);
        additionalProperties.put("project2Guid", project2Guid);
        additionalProperties.put("testPackageGuid", testPackageGuid);
        additionalProperties.put("sonarProjectGuid", sonarProjectGuid);
        
        additionalProperties.put("solutionItemsGuid", solutionItemsGuid);

        apiPackage = packageName + ".Controllers";
        modelPackage = packageName + ".Models";

        // due to a bug in io.swagger.codegen this isn't getting set from the config.
        this.sourceFolder = "src" + File.separator + packageName;
        supportingFiles.add(new SupportingFile("NuGet.Config", "", "NuGet.Config"));
        supportingFiles.add(new SupportingFile("global.json", "", "global.json"));
        supportingFiles.add(new SupportingFile("build.sh.mustache", "", "build.sh"));
        supportingFiles.add(new SupportingFile("build.bat.mustache", "", "build.bat"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("Solution.mustache", "", this.packageName + ".sln"));
        supportingFiles.add(new SupportingFile("Dockerfile.mustache", this.sourceFolder, "Dockerfile"));
        supportingFiles.add(new SupportingFile("gitignore", this.sourceFolder, ".gitignore"));
        supportingFiles.add(new SupportingFile("appsettings.json", this.sourceFolder, "appsettings.json"));

        supportingFiles.add(new SupportingFile("project.json.mustache", this.sourceFolder, "project.json"));
        supportingFiles.add(new SupportingFile("project.sonar.json.mustache", "test",  "project.sonar.json"));        
        supportingFiles.add(new SupportingFile("Startup.mustache", this.sourceFolder, "Startup.cs"));
        supportingFiles.add(new SupportingFile("Program.mustache", this.sourceFolder, "Program.cs"));		
        supportingFiles.add(new SupportingFile("DbAppContext.mustache", this.sourceFolder, "DbAppContext.cs"));
        supportingFiles.add(new SupportingFile("ModelBuilderExtensions.mustache", this.sourceFolder, "ModelBuilderExtensions.cs"));
        
        supportingFiles.add(new SupportingFile("web.config", this.sourceFolder, "web.config"));
        
        // sonar files
        supportingFiles.add(new SupportingFile("Sonar.csproj.mustache", this.sourceFolder, "Sonar.csproj"));
        supportingFiles.add(new SupportingFile("Sonar.bat", this.sourceFolder, "Sonar.bat"));
        supportingFiles.add(new SupportingFile("Project.xproj.mustache", this.sourceFolder, this.packageName + ".xproj"));
        
        supportingFiles.add(new SupportingFile("Properties" + File.separator + "launchSettings.json", this.sourceFolder + File.separator + "Properties", "launchSettings.json"));

        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "README.md", this.sourceFolder + File.separator + "wwwroot", "README.md"));
        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "index.html", this.sourceFolder + File.separator + "wwwroot", "index.html"));
        supportingFiles.add(new SupportingFile("wwwroot" + File.separator + "web.config", this.sourceFolder + File.separator + "wwwroot", "web.config"));
        
        // add helpers for the model
        supportingFiles.add(new SupportingFile("DbCommentsUpdater.mustache", this.sourceFolder , "Models" + File.separator + "DbCommentsUpdater.cs"));
        supportingFiles.add(new SupportingFile("MetaDataExtension.mustache", this.sourceFolder , "Models" + File.separator + "MetaDataExtension.cs"));	
        
        // test files        
        supportingFiles.add(new SupportingFile("test" + File.separator + "project.xproj.mustache", "test" , "test.xproj"));        
        supportingFiles.add(new SupportingFile("test" + File.separator + "project.json.mustache", "test",  "project.json"));
        
        supportingFiles.add(new SupportingFile("test" + File.separator + "MockDbSet.mustache", "test",  "MockDbSet.json"));
                
    }

    
    
  @Override
    public String apiFilename(String templateName, String tag) {
        String result = apiFileFolder() + '/' + toApiFilename(tag) + apiTemplateFiles().get(templateName);

        if ( templateName.endsWith("Service.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/I" + result.substring(ix+1, result.length() - 3) + "Service.cs";    
            result = result.replace(apiFileFolder(), serviceFileFolder());
        } else if ( templateName.endsWith("ServiceImpl.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/" + result.substring(ix, result.length() - 3) + "Service.cs";            
            result = result.replace(apiFileFolder(), implFileFolder());
        } else {
            result = super.apiFilename(templateName, tag);
        }
        return result;
    }
    
   public String implFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + "Services.Impl";
  }
   
   public String serviceFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + "Services";
  }
  
  
    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);  
        LOGGER.warn ("SwaggerType is " + swaggerType);
        String type;
        if (super.typeMapping.containsKey(swaggerType.toLowerCase())) {
            type = super.typeMapping.get(swaggerType.toLowerCase());
            if (super.languageSpecificPrimitives.contains(type)) {
                {
                    // fix for required; remove any question marks
                    if (p.getRequired())
                    {
                        type = type.replace("?", "");                                            
                    }
                    LOGGER.warn ("Setting primitive type " + type);
                    return type;
                }
            }
        } 
        else if (super.languageSpecificPrimitives.contains(swaggerType)) {
                {
                    type = swaggerType;
                    // fix for required; remove any question marks
                    if (p.getRequired())
                    {
                        type = type.replace("?", "");                                            
                    }
                    LOGGER.warn ("Setting primitive type " + type);
                    return type;
                }
        }
        else {
            type = swaggerType;
        }
        if (p.getRequired())
        {
            type = type.replace("?", "");                                            
        }
        LOGGER.warn ("Calling toModelName for type: " + type);
        type = toModelName(type);
        LOGGER.warn ("Result is " + type);
        
        return toModelName(type);
    }
        
    
  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + "Models";
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + "Controllers";
  }
  
  public String apiTestFileFolder() {
    return outputFolder + File.separator + "test" + File.separator + "API";
  }
  
  public String modelTestFileFolder() {
    return outputFolder + File.separator + "test" + File.separator + "Models";
  }        

  @Override
    protected void processOperation(CodegenOperation operation) {
        super.processOperation(operation);

        // HACK: Unlikely in the wild, but we need to clean operation paths for MVC Routing
        if (operation.path != null) {
            String original = operation.path;
            operation.path = operation.path.replace("?", "/");
            if (!original.equals(operation.path)) {
    //            LOGGER.warn("Normalized " + original + " to " + operation.path + ". Please verify generated source.");
            }
        }

        // Converts, for example, PUT to HttpPut for controller attributes
        operation.httpMethod = "Http" + operation.httpMethod.substring(0, 1) + operation.httpMethod.substring(1).toLowerCase();
    }
  
}