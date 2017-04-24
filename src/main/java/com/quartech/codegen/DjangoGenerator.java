package com.quartech.codegen;

import io.swagger.codegen.*;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.*;
import java.io.File;
import static java.util.UUID.randomUUID;
import org.atteo.evo.inflector.English;


/**
 *
 * @author George Walker
 */

public class DjangoGenerator extends DefaultCodegen implements CodegenConfig {
    
    protected String implFolder = "";
    protected String sourceFolder = "";
    protected String basePath = "";
    
    public static final String CONTROLLER_PACKAGE = "controllerPackage";
    public static final String DEFAULT_CONTROLLER = "defaultController";
    
    // if the model is on this list then it can be auto tested.
    List<String> autoTestList = new java.util.Vector<String>();
    
    public DjangoGenerator()
    {
        super();
        super.embeddedTemplateDir = templateDir = "django";
        
        languageSpecificPrimitives.clear();
        languageSpecificPrimitives.add("int");
        languageSpecificPrimitives.add("float");
        languageSpecificPrimitives.add("list");
        languageSpecificPrimitives.add("bool");
        languageSpecificPrimitives.add("str");
        languageSpecificPrimitives.add("datetime");
        languageSpecificPrimitives.add("date");

        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("float", "float");
        typeMapping.put("number", "float");
        typeMapping.put("long", "int");
        typeMapping.put("double", "float");
        typeMapping.put("array", "list");
        typeMapping.put("map", "dict");
        typeMapping.put("boolean", "bool");
        typeMapping.put("string", "str");
        typeMapping.put("date", "date");
        typeMapping.put("DateTime", "datetime");
        typeMapping.put("object", "object");
        typeMapping.put("file", "file");

        // set the output folder here
        outputFolder = "generated-code/django";

        modelTemplateFiles.clear();        

        // from https://docs.python.org/release/2.5.4/ref/keywords.html
        setReservedWordsLowerCase(
                Arrays.asList(
                        "and", "del", "from", "not", "while", "as", "elif", "global", "or", "with",
                        "assert", "else", "if", "pass", "yield", "break", "except", "import",
                        "print", "class", "exec", "in", "raise", "continue", "finally", "is",           
                        "return", "def", "for", "lambda", "try"));   

        
    }
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {        
        
        String modelDatatype = "";
        if (property != null)
        {
            // Replace List<T> with T[] for controller method parameters
            if (Boolean.TRUE.equals(property.isDateTime) || Boolean.TRUE.equals(property.isDate))
            {                
                modelDatatype = "models.DateField()";
            }
            else if (Boolean.TRUE.equals(property.isString))
            {
                String maxLength = "max_length=255";
                if (property.maxLength != null)
                {
                    maxLength = "max_length=" + property.maxLength.toString();
                }
                modelDatatype = "models.CharField(" + maxLength + ")";
            }
            else if (Boolean.TRUE.equals(property.isInteger))
            {                
                modelDatatype = "models.IntegerField()";
            }
            else if (Boolean.TRUE.equals(property.isBoolean))
            {                
                modelDatatype = "models.BooleanField()";
            }
            else if (Boolean.TRUE.equals(property.isByteArray) || Boolean.TRUE.equals(property.isBinary))
            {
                String maxLength = "";
                if (property.maxLength != null)
                {
                    maxLength = "max_length=" + property.maxLength.toString();
                }
                modelDatatype = "models.BinaryField(" + maxLength + ")";
            }
            else if (Boolean.TRUE.equals(property.isContainer))
            {
                modelDatatype = "models.ManyToManyField('" + property.complexType + "',related_name='"+ model.name + property.name+"')";
            }            
            else if (property.complexType != null)
            {
                modelDatatype = "models.ForeignKey('"+ property.complexType +"', on_delete=models.CASCADE,related_name='"+ model.name + property.name + "')";
            }
            else // default to string
            {
                modelDatatype = "models.CharField(max_length=255)";
            }                                           
            //LOGGER.info("modelDatatype is " + modelDatatype);
            property.vendorExtensions.put("modelDatatype", modelDatatype);    
        } 
        super.postProcessModelProperty(model, property);
    }
    
    @Override
    public void processOpts() {
        super.processOpts();
        supportingFiles.clear(); 
        
        addOption(CodegenConstants.SOURCE_FOLDER,
                CodegenConstants.SOURCE_FOLDER_DESC,
                this.sourceFolder);
        
        cliOptions.add(new CliOption(CONTROLLER_PACKAGE, "controller package").
                defaultValue("views"));
        cliOptions.add(new CliOption(DEFAULT_CONTROLLER, "default controller").
                defaultValue("default_controller"));
        
        //apiTemplateFiles.put("controller.mustache", ".py");       
        
        modelTemplateFiles.put("models.mustache", ".py");
                
        supportingFiles.add(new SupportingFile("views_generated.mustache", "", "views.py"));
        supportingFiles.add(new SupportingFile("views_custom.mustache", "", "views_custom.py"));
        
        // serializers is required to use the django rest library
        supportingFiles.add(new SupportingFile("serializers.mustache", "", "serializers.py"));    
        
        // urls contains the routes for the application
        supportingFiles.add(new SupportingFile("urls.mustache", "", "urls.py"));  
        
        // admin.py is needed for database creation
        supportingFiles.add(new SupportingFile("admin.mustache", "", "admin.py"));  
        
        // test data for automated tests
        supportingFiles.add(new SupportingFile("fakedata.mustache", "", "fakedata.py"));          
        
        // automated tests
        supportingFiles.add(new SupportingFile("test_api_simple.mustache", "", "test_api_simple.py"));  
        supportingFiles.add(new SupportingFile("test_api_complex.mustache", "", "test_api_complex.py"));
        
        supportingFiles.add(new SupportingFile("__init__model.mustache",
                            "models",
                            "__init__.py")
            );
        /*
        supportingFiles.add(new SupportingFile("__init__model.mustache",
                            "views",
                            "__init__.py")
            );
        */
        
    }
    
    
    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + "models"; //sourceFolder + File.separator +  "main" + File.separator + "java" + File.separator + modelPackage().replace('.', '/');
    }   
    
    
    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");                         
            // add a lower case plural form of the model suitable for making a service out of.
            String pluralClassname = English.plural(cm.classname);
            cm.vendorExtensions.put ("pluralClassname", pluralClassname.toLowerCase());
            String modelImports = "";
            List<String> modelImportList = new java.util.Vector<String>();
            
            Iterator<CodegenProperty> iter = cm.vars.iterator();
            Boolean canAutoTest = true;
            while(iter.hasNext()){
                // check to see if model name is same as the property name
                // which will result in compilation error
                // if found, prepend with _ to workaround the limitation  
                CodegenProperty codegenProperty = iter.next();
                if(codegenProperty.name.equalsIgnoreCase("id"))
                {
                    iter.remove();
                }
                if (!Boolean.TRUE.equals(codegenProperty.isBinary) 
                    && !Boolean.TRUE.equals(codegenProperty.isBoolean)
                    && !Boolean.TRUE.equals(codegenProperty.isInteger)
                    && !Boolean.TRUE.equals(codegenProperty.isString)
                    && !Boolean.TRUE.equals(codegenProperty.isDateTime)
                    && !Boolean.TRUE.equals(codegenProperty.isDate)
                    && codegenProperty.complexType != null && codegenProperty.complexType.length() > 0)
                {
                    if (! modelImportList.contains(codegenProperty.complexType))
                    {
                        // add the import.
                        modelImports += "from ." + codegenProperty.complexType + " import " + codegenProperty.complexType + "\r\n";                        
                        modelImportList.add(codegenProperty.complexType);
                        // in the future devise a means of creating test data for complex types.
                        canAutoTest = false;
                    }                                        
                }                
            }
            if (Boolean.TRUE.equals(canAutoTest))
            {
                autoTestList.add(cm.name);                
            }
            cm.vendorExtensions.put ("modelImports", modelImports);
        }
        // process enum in models
        return postProcessModelsEnum(objs);
    }

    String getUnitTest(CodegenOperation operation)
    {
        String result = "        self.fail(\"Not implemented\")\r\n";
        
        String vendorExtensionOperation = (String)operation.vendorExtensions.get("x-codegen-operation");
        String model = (String)operation.vendorExtensions.get("model");
        switch (vendorExtensionOperation)
        {
            case "list":
                result = 
                     "        # Test Create and List operations.\r\n"
                   + "        testUrl = \"" + basePath + "/" + operation.path +"\"\r\n"
                   + "        # Create:\r\n"
                   + "        serializer_class = " + (String) operation.vendorExtensions.get("serializer") + "\r\n"
                   + "        payload = fakedata." + model + "TestDataCreate()\r\n"
                   + "        jsonString = json.dumps(payload)\r\n"     
                   + "        response = self.client.post(testUrl, content_type='application/json', data=jsonString)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_201_CREATED == response.status_code\r\n"
                   + "        # parse the response.\r\n"  
                   + "        jsonString = response.content.decode(\"utf-8\")\r\n"     
                   + "        data = json.loads(jsonString)\r\n" 
                   + "        createdId = data['id']\r\n"                   
                   + "        # List:\r\n"                        
                   + "        response = self.client.get(testUrl)\r\n"                   
                   + "        # Check that the response is 200 OK.\r\n"
                   + "        assert status.HTTP_200_OK == response.status_code\r\n"
                   + "        # Cleanup:\r\n"
                   + "        deleteUrl = testUrl + \"/\" + str(createdId) + \"/delete\"\r\n"
                   + "        response = self.client.post(deleteUrl)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_204_NO_CONTENT == response.status_code\r\n";                           
                break;                                
            case "retrieve":
                result = 
                     "        # Test Retrieve and Update operations.\r\n"
                   + "        testUrl = \"" + basePath + "/" + operation.path +"\"\r\n"
                   + "        createUrl = testUrl.replace (\"/(?P<id>[0-9]+)\",\"\")\r\n"
                   + "        # Create an object:\r\n"
                   + "        payload = fakedata." + model + "TestDataCreate()\r\n"
                   + "        jsonString = json.dumps(payload)\r\n"     
                   + "        response = self.client.post(createUrl, content_type='application/json', data=jsonString)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_201_CREATED == response.status_code\r\n"
                   + "        # parse the response.\r\n"     
                   + "        jsonString = response.content.decode(\"utf-8\")\r\n"     
                   + "        data = json.loads(jsonString)\r\n" 
                   + "        createdId = data['id']\r\n"                   
                   + "        # Update the object:\r\n"
                   + "        updateUrl = testUrl.replace (\"(?P<id>[0-9]+)\",str(createdId))\r\n"                        
                   + "        payload = fakedata." + model + "TestDataUpdate()\r\n"
                   + "        jsonString = json.dumps(payload)\r\n"     
                   + "        response = self.client.put(updateUrl, content_type='application/json', data=jsonString)\r\n"                   
                   + "        # Check that the response is 200 OK.\r\n"
                   + "        assert status.HTTP_200_OK == response.status_code\r\n"
                   + "        # Cleanup:\r\n"
                   + "        deleteUrl = createUrl + \"/\" + str(createdId) + \"/delete\"\r\n"
                   + "        response = self.client.post(deleteUrl)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_204_NO_CONTENT == response.status_code\r\n";
                
                break;
            case "update":
                result = "";           
                break;
            case "create":
                result = "";
                break;
            case "destroy":                
                result = 
                     "        # Test Retrieve and Update operations.\r\n"
                   + "        testUrl = \"" + basePath + "/" + operation.path +"\"\r\n"
                   + "        createUrl = testUrl.replace (\"/(?P<id>[0-9]+)/delete\",\"\")\r\n"                                           
                   + "        # Create an object:\r\n"
                   + "        payload = fakedata." + model + "TestDataCreate()\r\n"
                   + "        jsonString = json.dumps(payload)\r\n"     
                   + "        response = self.client.post(createUrl, content_type='application/json', data=jsonString)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_201_CREATED == response.status_code\r\n"
                   + "        # parse the response.\r\n"     
                   + "        jsonString = response.content.decode(\"utf-8\")\r\n"     
                   + "        data = json.loads(jsonString)\r\n" 
                   + "        createdId = data['id']\r\n"                   
                   + "        deleteUrl = testUrl.replace (\"(?P<id>[0-9]+)\",str(createdId))\r\n"
                   + "        response = self.client.post(deleteUrl)\r\n"                   
                   + "        # Check that the response is OK.\r\n"
                   + "        assert status.HTTP_204_NO_CONTENT == response.status_code\r\n";                
                break;
            case "bulk":
                result =
                     "        # Test Bulk Load.\r\n"                        
                   + "        payload = fakedata." + model + "TestDataCreate()\r\n"
                   + "        jsonString = \"[]\"\r\n"                        
                   + "        response = self.client.post('" + basePath + "/" + operation.path +"',content_type='application/json', data=jsonString)\r\n"                   
                   + "        # Check that the response is 200 OK.\r\n"
                   + "        assert status.HTTP_201_CREATED == response.status_code\r\n";
                break;
        }
        return result;    
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            // http method verb conversion (e.g. PUT => put)
            operation.httpMethod = operation.httpMethod.toLowerCase();
            
            // strip the slash prefix if it exists. 
            if ( operation.path.length() > 1 && operation.path.charAt(0) == '/')
            {
                operation.path = operation.path.substring(1);
            }
            
            // change the {var} to (.*)         
            operation.path = operation.path.replaceAll("\\{.*}", "(?P<id>[0-9]+)");
            
            // see if the vendor extension operation is present.
            if (operation.vendorExtensions.containsKey("x-codegen-operation"))
            {
                String vendorExtensionOperation = (String)operation.vendorExtensions.get("x-codegen-operation");
                String model = "";
                if (operation.tags.size() > 0)
                {
                  operation.vendorExtensions.put("serializer", operation.tags.get(0) + "Serializer"); 
                  model = operation.tags.get(0);
                  operation.vendorExtensions.put("model", model); 
                }
                
                if (this.autoTestList.contains(model))
                {
                    operation.vendorExtensions.put ("autoTest", "YES");
                }
                
                switch (vendorExtensionOperation)
                {
                    case "list":
                        // combination of list and create.                        
                        operation.vendorExtensions.put("operationParameters", "mixins.ListModelMixin, mixins.CreateModelMixin, generics.GenericAPIView");
                        operation.vendorExtensions.put("operationSource", 
                                 "  def get(self, request, *args, **kwargs):\r\n"
                               + "    \"\"\"\r\n"
                               + "    Lists available " + model + " objects\r\n"
                               + "    \"\"\"\r\n"
                               + "    return self.list(request, *args, **kwargs)\r\n"
                               + "  def post(self, request, *args, **kwargs):\r\n" 
                               + "    \"\"\"\r\n"
                               + "    Creates a new " + model + " object\r\n"
                               + "    \"\"\"\r\n"                                       
                               + "    return self.create(request, *args, **kwargs)"
                        );
                        operation.vendorExtensions.put("operationReturn", "list");
                        break;
                    case "retrieve":
                        // combination of retrieve and update                        
                        operation.vendorExtensions.put("operationParameters", "mixins.RetrieveModelMixin, mixins.UpdateModelMixin, generics.GenericAPIView");
                        operation.vendorExtensions.put("operationSource", 
                                 "  def get(self, request, *args, **kwargs):\r\n" 
                               + "    \"\"\"\r\n"
                               + "    Retrieves the specified " + model + " object\r\n"
                               + "    \"\"\"\r\n"                                         
                               + "    return self.retrieve(request, *args, **kwargs)\r\n"                                         
                               + "  def put(self, request, *args, **kwargs):\r\n" 
                               + "    \"\"\"\r\n"
                               + "    Updates the specified " + model + " object\r\n"
                               + "    \"\"\"\r\n"                                                                      
                               + "    return self.update(request, *args, **kwargs)"
                        );                                                
                        break;
                    case "update":
                        operation.vendorExtensions.put("operationIgnore", "YES");                         
                        break;
                    case "create":
                        operation.vendorExtensions.put("operationIgnore", "YES");                        
                        break;
                    case "ignore":
                        operation.vendorExtensions.put("operationIgnore", "YES");                        
                        break;
                    case "destroy":
                        operation.vendorExtensions.put("operationSource", 
                                 "  def post(self, request, *args, **kwargs):\r\n" 
                               + "    \"\"\"\r\n"
                               + "    Destroys the specified " + model + " object\r\n"
                               + "    \"\"\"\r\n"                                         
                               + "    return self.destroy(request, *args, **kwargs)\r\n"                                         
                        );                                                
                        operation.vendorExtensions.put("operationParameters", "mixins.DestroyModelMixin, generics.GenericAPIView");
                        operation.vendorExtensions.put("operationReturn", "destroy");                        
                        break;
                    case "bulk":
                        operation.vendorExtensions.put("operationParameters", "BulkCreateModelMixin, generics.GenericAPIView");
                        operation.vendorExtensions.put("operationReturn", "create");
                        operation.vendorExtensions.put("operationSource",
                                 "  def post(self, request, *args, **kwargs):\r\n" 
                               + "    \"\"\"\r\n"
                               + "    Creates a number of new " + model + " objects\r\n"
                               + "    \"\"\"\r\n"                                       
                               + "    return self.create(request, *args, **kwargs)");                                
                        break;                        
                }
                operation.vendorExtensions.put("testcode", getUnitTest (operation)); 
            }
        }
        return objs;
    }
    
    @Override
    public void preprocessSwagger(Swagger swagger) {
        this.basePath = swagger.getBasePath();
        if ( this.basePath.length() > 1 && this.basePath.charAt(0) == '/')
        {
            swagger.setBasePath(this.basePath.substring(1));
        }
        
        if ( "/".equals(swagger.getBasePath()) ) {
            swagger.setBasePath("");
        }

        String host = swagger.getHost();
        String port = "8080"; // Default value for a JEE Server
        if ( host != null ) {
            String[] parts = host.split(":");
            if ( parts.length > 1 ) {
                port = parts[1];
            }
        }
        
    }
    
    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
      return outputFolder + File.separator + "views" + File.separator; 
    }    
    
    @Override
    public String apiFilename(String templateName, String tag) {
        String result = apiFileFolder() + '/' + toApiFilename(tag) + apiTemplateFiles().get(templateName);
        return result;
    }   
    
    @Override
    public String toApiName(String name) {
        if (name == null || name.length() == 0) {
            return "DefaultController";
        }
        return camelize(name, false); // + "View";
    }

    @Override
    public String toApiFilename(String name) {
        return camelize(name, false); // + "View";
        //return underscore(toApiName(name));
    }    
    
    private String implFileFolder(String output) {
        return outputFolder + "/" + output + "/" + apiPackage().replace('.', '/');
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }
    
    public String getName() {
        return "django";
    }
     
    public String getHelp() {
        return "Generates a django file set";
    }    
    
    protected void addOption(String key, String description, String defaultValue) {
        CliOption option = new CliOption(key, description);
        if (defaultValue != null) option.defaultValue(defaultValue);
        cliOptions.add(option);
    }

    protected void addSwitch(String key, String description, Boolean defaultValue) {
        CliOption option = CliOption.newBoolean(key, description);
        if (defaultValue != null) option.defaultValue(defaultValue.toString());
        cliOptions.add(option);
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
    public String escapeQuotationMark(String input) {
        // remove ' to avoid code injection
        return input.replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // remove multiline comment
        return input.replace("'''", "'_'_'");
    }
}