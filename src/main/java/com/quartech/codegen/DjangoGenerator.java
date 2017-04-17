package com.quartech.codegen;

import io.swagger.codegen.*;
import io.swagger.models.properties.Property;
import java.util.*;
import java.io.File;
import static java.util.UUID.randomUUID;

/**
 *
 * @author George Walker
 */

public class DjangoGenerator extends DefaultCodegen implements CodegenConfig {
    
    protected String implFolder = "";
    protected String sourceFolder = "";
    
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

        //apiTemplateFiles.put("controller.mustache", ".py");       

        // from https://docs.python.org/release/2.5.4/ref/keywords.html
        setReservedWordsLowerCase(
                Arrays.asList(
                        "and", "del", "from", "not", "while", "as", "elif", "global", "or", "with",
                        "assert", "else", "if", "pass", "yield", "break", "except", "import",
                        "print", "class", "exec", "in", "raise", "continue", "finally", "is",           
                        "return", "def", "for", "lambda", "try"));   
        
        //supportingFiles.clear();

        
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
                String maxLength = "";
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
            else if (Boolean.TRUE.equals(property.isContainer))
            {
                modelDatatype = "models.ManyToManyField(" + property.complexType + ")";
            }
            else if (property.complexType != null)
            {
                modelDatatype = "models.ForeignKey("+ property.complexType +", on_delete=models.CASCADE)";
            }
            else // default to string
            {
                modelDatatype = "models.CharField()";
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
        // add supporting files here
        
        
        //cliOptions.clear();

        // CLI options

        addOption(CodegenConstants.SOURCE_FOLDER,
                CodegenConstants.SOURCE_FOLDER_DESC,
                this.sourceFolder);
        
                supportingFiles.add(new SupportingFile("model.mustache", "", "model.py"));
        supportingFiles.add(new SupportingFile("serializers.mustache", "", "serializers.py"));

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
            Iterator<CodegenProperty> iter = cm.vars.iterator();
            while(iter.hasNext()){
                // check to see if model name is same as the property name
                // which will result in compilation error
                // if found, prepend with _ to workaround the limitation                
                if(iter.next().name.equalsIgnoreCase("id"))
                {
                    iter.remove();
                }
            }
        }
        // process enum in models
        return postProcessModelsEnum(objs);
    }
    
    /*
    @Override
    public CodegenProperty fromProperty(String name, Property p)
    {
        CodegenProperty result = null;
        // django automatically adds the id fields, so they are not included in code.
        if (name != null && ! name.toLowerCase().equals("id"))
        {            
            result = super.fromProperty (name, p);
        }
        return result;
    }
    */

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
      return outputFolder; // + File.separator + sourceFolder + File.separator +  "main" + File.separator + "java" + File.separator + apiPackage().replace('.', '/');
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
        return camelize(name, false) + "Controller";
    }

    @Override
    public String toApiFilename(String name) {
        return underscore(toApiName(name));
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
     * those terms here.  This logic is only called if a variable matches the reseved words
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
