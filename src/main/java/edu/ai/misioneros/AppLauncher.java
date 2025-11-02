package edu.ai.misioneros;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AppLauncher {
    public static void main(String[] args) {
        // Cargar librerías nativas de JavaFX antes de iniciar la aplicación
        try {
            loadNativeLibraries();
        } catch (Exception e) {
            System.err.println("Error cargando librerías nativas: " + e.getMessage());
            e.printStackTrace();
            // Continuar de todos modos, puede que las DLLs ya estén cargadas
        }
        
        // Llamar al main de la aplicación principal
        MainApp.main(args);
    }
    
    private static void loadNativeLibraries() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        if (osName.contains("win")) {
            // Windows - cargar DLLs necesarias
            if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                // Windows 64-bit
                String[] dlls = {
                    "glass.dll",
                    "javafx_font.dll",
                    "javafx_iio.dll",
                    "prism_common.dll",
                    "prism_d3d.dll",
                    "prism_sw.dll"
                };
                
                for (String dll : dlls) {
                    extractAndLoadNative(dll);
                }
            } else {
                // Windows 32-bit
                String[] dlls = {
                    "glass.dll",
                    "javafx_font.dll",
                    "javafx_iio.dll",
                    "prism_common.dll",
                    "prism_d3d.dll",
                    "prism_sw.dll"
                };
                
                for (String dll : dlls) {
                    extractAndLoadNative(dll);
                }
            }
        } else if (osName.contains("linux")) {
            // Linux - cargar .so files
            String[] libs = {
                "libglass.so",
                "libjavafx_font.so",
                "libjavafx_iio.so",
                "libprism_common.so",
                "libprism_es2.so",
                "libprism_sw.so"
            };
            
            for (String lib : libs) {
                extractAndLoadNative(lib);
            }
        } else if (osName.contains("mac")) {
            // macOS - cargar .dylib files
            String[] libs = {
                "libglass.dylib",
                "libjavafx_font.dylib",
                "libjavafx_iio.dylib",
                "libprism_common.dylib",
                "libprism_es2.dylib",
                "libprism_sw.dylib"
            };
            
            for (String lib : libs) {
                extractAndLoadNative(lib);
            }
        }
    }
    
    private static void extractAndLoadNative(String nativeLib) throws Exception {
        // Intentar cargar desde el classpath (si está en el JAR)
        InputStream libStream = AppLauncher.class.getClassLoader()
            .getResourceAsStream("natives/" + nativeLib);
        
        if (libStream != null) {
            // Extraer a carpeta temporal
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "javafx-natives-" + System.getProperty("user.name"));
            tempDir.mkdirs();
            File libFile = new File(tempDir, nativeLib);
            
            // Solo extraer si no existe o si es más reciente
            if (!libFile.exists()) {
                Files.copy(libStream, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                libFile.deleteOnExit(); // Limpiar al salir
            }
            
            // Cargar la librería nativa
            try {
                System.load(libFile.getAbsolutePath());
            } catch (UnsatisfiedLinkError e) {
                // Puede que ya esté cargada, continuar
                System.err.println("Warning: No se pudo cargar " + nativeLib + " (puede que ya esté cargada)");
            }
            
            libStream.close();
        }
    }
}
