package tools;

import java.io.File;
import tools.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javax.swing.JOptionPane;


/**
 *
 * @author SeBi
 */
public class KLoader {
    private ClassPool cp;
    private URLClassLoader loader;
    
    public KLoader() {
        try {
        String appPath = String.format("%s\\Knuddels-Stapp\\", System.getProperty("user.home"));
        File appDir = new File(appPath);

        if (!appDir.exists()) {
            JOptionPane.showMessageDialog(null, "Knuddels Standalone Directory not found!", "Error", 0);
            System.exit(0);
        }
         
            Collection<File> files = this.listFiles(appPath, "jar");
            File[] jarFiles =  files.toArray(new File[files.size()]);
            URL[] urlList = new URL[jarFiles.length];
            for (int i = 0; i < jarFiles.length; ++i) {
                    try {
                    urlList[i] = jarFiles[i].toURL();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
            
            this.loader = new URLClassLoader(urlList, Thread.currentThread().getContextClassLoader());  
        
            this.cp = ClassPool.getDefault();
            this.cp.insertClassPath(new LoaderClassPath(this.loader));
        
        //    this.getHuffman();
            this.prepareGroupChat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareGroupChat() {
        try {
            CtClass groupChat = this.cp.get(Parameter.getDefault().get("GROUPCHAT"));
            
            groupChat.addField(CtField.make(
                    "private String hello;"
            , groupChat));

            groupChat.addField(CtField.make(
                    "private String password;"
            , groupChat));

            groupChat.addMethod(CtMethod.make(
                    "public void init2() {"
            +		Parameter.getDefault().get("INIT_TREE") + "(" + Parameter.getDefault().get("TREE") + ");"
            +	"}"
            , groupChat));

            groupChat.getDeclaredMethod("getVersionFull").setBody("return \"\"; ");
            
            groupChat.getDeclaredMethod("getDocumentBase").setName("getAppletBase");
            groupChat.addMethod(CtMethod.make(
                "public java.net.URL getDocumentBase() {"
            +        "return new java.net.URL(tools.Parameter.getDefault().get(\"dbase\")); "
            +   "}"
            , groupChat));
            
            groupChat.getDeclaredMethod(
                    Parameter.getDefault().get("SEND"), new CtClass[] { cp.get("java.lang.String") }
            ).setBody(
                    "{"
            +		"if ($1.startsWith(\"t\0\")) {"
            +			"hello = $1;"
            +			"return true;"
                            
            +		"} else if ($1.startsWith(\"n\0\")) {"
            +			"password = $1.split(\"\0\")[3];"
            +			"return true;"
            +		"}"

            +		"return " + Parameter.getDefault().get("SEND") + "($1, false);"
            +           "}"
            );

            groupChat.addMethod(CtMethod.make(
                    "public byte[] compress(byte[] param1) {"
            +		"String token = new String($1, \"UTF-8\");"
            +		"if (token.startsWith(\"t\0\")) {"
            +			Parameter.getDefault().get("HELLO") + "();"
            +			"token = hello;"
            +		"} else if (token.startsWith(\"n\0\")) {"
            +			"String[] tokens = token.split(\"\0\");"

            +			"if (!" + Parameter.getDefault().get("KEY") + ".equals(tokens[5])) {"
            +				Parameter.getDefault().get("RESET") + "();" // reset
            +				Parameter.getDefault().get("KEY") + " = tokens[5];" // key
            +			"}"
        
            +			Parameter.getDefault().get("LOGIN") + "(\"\", tokens[3], \"\", false);"
            
                    +			"tokens[3] = password;"
            +			"StringBuilder buffer = new StringBuilder(tokens[0]);"

            +			"for (int i = 1; i < tokens.length - 1; i++) {"
            +				"buffer.append(\"\0\");"
            +				"buffer.append(tokens[i]);"
            +			"}"

            +			"token = buffer.toString();"
            +		"}"

            +		"return " + Parameter.getDefault().get("COMPRESS") + "." + Parameter.getDefault().get("COMPRESS_FUNC") + "(token, 0);"
            +	"}"
            , groupChat));

            groupChat.addMethod(CtMethod.make(
                    "public byte[] decompress(byte[] param1) {"
            +		"return  " + Parameter.getDefault().get("DECOMPRESS") + ". " + Parameter.getDefault().get("DECOMPRESS_FUNC") + "($1).getBytes(\"UTF-8\");"
            +	"}"
            , groupChat));
            
            this.cp.toClass(groupChat, this.loader);
        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getHuffman() {
        try {
            CtClass compress = this.cp.get("cb");
            compress.setName("Compress");
            compress.writeFile();
            
            CtClass decompress = this.cp.get("base.DeCompressor");
            decompress.setName("Decompress");
            decompress.writeFile();
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
     private Collection<File> listFiles(String dirPath, String ext) {
        Collection<File> result = new ArrayList<>();
        
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(listFiles(file.getAbsolutePath(),ext));
            } else if (file.isFile()) {
                if (file.getName().endsWith(ext)) {
                    result.add(file);
                }
            }
        }
        return result;
    }
    
    public Class findClass(String name) {
        try {
            return this.loader.loadClass(name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
