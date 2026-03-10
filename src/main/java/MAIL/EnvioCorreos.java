package MAIL;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;

/**
 * Clase principal para el envío automatizado de correos electrónicos utilizando JavaMail API y Gmail SMTP.
 * @author Andrés Iglesias Camacho
 * @version 1.5
 * @since 06/03/2026
 */
public class EnvioCorreos {
    //  Variables globales de sesión (credenciales)
    private static String correoRemitente = "";
    private static String claveRemitente = "";

    //  Rutas de los archivos de datos
    private static final String RUTA_CLIENTES = "data/Clientes.txt";
    private static final String RUTA_MENSAJE = "data/Mensaje.txt";
    private static final String ASUNTO = "Información de IESDH";

    public static void main(String[] args) {
        mostrarBienvenida();

        // 1. Solicitar credenciales al usuario
        solicitarCredenciales();

        // 2. Leer lista de destinatarios
        List<String> destinatarios = leerDestinatarios(RUTA_CLIENTES);
        if (destinatarios.isEmpty()) {
            System.out.println("[ERROR] No se encontraron destinatarios en " + RUTA_CLIENTES);
            System.exit(1);
        }
        System.out.println("\n[INFO] Destinatarios cargados: " + destinatarios.size());

        // 3. Leer el mensaje
        String mensaje = leerMensaje(RUTA_MENSAJE);
        if (mensaje.isEmpty()) {
            System.out.println("[ERROR] No se pudo leer el mensaje desde " + RUTA_MENSAJE);
            System.exit(1);
        }
        System.out.println("[INFO] Mensaje cargado correctamente.");

        // 4 + 5. Configurar SMTP y enviar correos
        System.out.println("\n[INFO] Iniciando envío de correos...\n");
        enviarCorreo(correoRemitente, claveRemitente, destinatarios, mensaje);

        System.out.println("\n[INFO] Proceso finalizado.");
        System.out.println("======================================================");
    }

    /**
     * Metodo para mostrar una bienvenida al usuario con información del programa.
     */
    private static void mostrarBienvenida() {
        System.out.println("======================================================");
        System.out.println("        IESDH - Envío Automatizado de Correos");
        System.out.println("            JavaMail API + Gmail SMTP");
        System.out.println("======================================================\n");
    }

    /**
     * Metodo para solicitar al usuario su correo Gmail y contraseña de forma segura.
     */
    public static void solicitarCredenciales() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduce tu dirección de correo Gmail: ");
        correoRemitente = sc.nextLine().trim();

        // Intentar leer la contraseña de forma segura (sin eco)
        Console consola = System.console();
        if (consola != null) {
            char[] pwd = consola.readPassword("Introduce tu contraseña (o Contraseña de Aplicación): ");
            claveRemitente = new String(pwd);
            // Limpiar array por seguridad
            Arrays.fill(pwd, ' ');
        } else {
            // Fallback si no hay consola real (p.ej. en algunos IDEs)
            System.out.print("Introduce tu contraseña (o Contraseña de Aplicación): ");
            claveRemitente = sc.nextLine().trim();
        }

        System.out.println("\n[INFO] Credenciales recibidas para: " + correoRemitente);
    }

    /**
     * Metodo para leer la lista de destinatarios desde un archivo de texto.
     * @param archivo Ruta del archivo que contiene los destinatarios (uno por línea).
     * @return Lista de direcciones de correo electrónico válidas para enviar el mensaje.
     */
    public static List<String> leerDestinatarios(String archivo) {
        List<String> listaDestinatarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                // Ignorar líneas vacías o comentarios (#)
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    listaDestinatarios.add(linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] Archivo no encontrado: " + archivo);
        } catch (IOException e) {
            System.err.println("[ERROR] Error al leer el archivo: " + archivo);
            System.err.println("Detalle: " + e.getMessage());
        }

        return listaDestinatarios;
    }

    /**
     * Metodo para leer el contenido del mensaje desde un archivo de texto.
     * @param archivo Ruta del archivo que contiene el mensaje a enviar.
     * @return El contenido completo del mensaje como una cadena de texto,
     * o una cadena vacía si hubo un error.
     */
    public static String leerMensaje(String archivo) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] Archivo de mensaje no encontrado: " + archivo);
        } catch (IOException e) {
            System.err.println("[ERROR] Error al leer el mensaje: " + e.getMessage());
        }

        return sb.toString();
    }

    /**
     * Metodo para configurar las propiedades necesarias para conectarse al servidor SMTP de Gmail.
     * @return Un objeto Properties con la configuración adecuada para SMTP con Gmail,
     * incluyendo host, puerto, autenticación y seguridad.
     */
    public static Properties configurarServidorSMTP() {
        Properties propiedadesSMTP = new Properties();

        // Host SMTP de Gmail
        propiedadesSMTP.put("mail.smtp.host", "smtp.gmail.com");
        // Puerto 587 con STARTTLS (recomendado) o 465 con SSL
        propiedadesSMTP.put("mail.smtp.port", "587");
        // Habilitar autenticación
        propiedadesSMTP.put("mail.smtp.auth", "true");
        // Activar cifrado STARTTLS
        propiedadesSMTP.put("mail.smtp.starttls.enable", "true");
        // Protocolo TLS
        propiedadesSMTP.put("mail.smtp.ssl.protocols", "TLSv1.2");
        // Tiempo de espera de conexión (ms)
        propiedadesSMTP.put("mail.smtp.connectiontimeout", "5000");
        propiedadesSMTP.put("mail.smtp.timeout", "5000");

        return propiedadesSMTP;
    }

    /**
     * Metodo para enviar correos electrónicos a una lista de destinatarios utilizando las credenciales del remitente.
     * @param remitente La dirección de correo electrónico del remitente (Gmail).
     * @param clave La contraseña del remitente o una Contraseña de Aplicación si se usa verificación en 2 pasos.
     * @param destinatarios Una lista de direcciones de correo electrónico a las que se enviará el mensaje.
     * @param mensaje El contenido del mensaje que se desea enviar a los destinatarios. Se enviará como texto plano.
     */
    public static void enviarCorreo(String remitente, String clave, List<String> destinatarios, String mensaje) {
        Properties props = configurarServidorSMTP();

        // Autenticador con las credenciales del remitente
        Authenticator autenticador = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        };

        // Crear sesión SMTP
        Session sesion = Session.getInstance(props, autenticador);
        // Desactivar debug (cambiar a true para ver tráfico SMTP)
        sesion.setDebug(false);

        int enviados = 0;
        int errores  = 0;

        for (String destinatario : destinatarios) {
            try {
                MimeMessage correo = new MimeMessage(sesion);

                // Remitente
                correo.setFrom(new InternetAddress(remitente, "IESDH"));

                // Destinatario principal (TO)
                correo.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(destinatario));

                // Copia oculta al remitente (BCC) para verificar el envío
                correo.addRecipient(Message.RecipientType.BCC,
                        new InternetAddress(remitente));

                // Asunto
                correo.setSubject(ASUNTO, "UTF-8");

                // Fecha de envío
                correo.setSentDate(new Date());

                // Cuerpo del mensaje (texto plano)
                correo.setText(mensaje, "UTF-8");

                // Enviar
                Transport.send(correo);

                enviados++;
                System.out.println("[OK]    Correo enviado a: " + destinatario);

            } catch (AddressException e) {
                errores++;
                System.err.println("[ERROR] Dirección inválida: " + destinatario
                        + " → " + e.getMessage());
            } catch (AuthenticationFailedException e) {
                errores++;
                System.err.println("[ERROR] Autenticación fallida. Comprueba tu correo y contraseña.");
                System.err.println("        Si usas verificación en 2 pasos, utiliza una Contraseña de Aplicación.");
                // Si la autenticación falla, no tiene sentido seguir intentando
                break;
            } catch (MessagingException e) {
                errores++;
                System.err.println("[ERROR] No se pudo enviar a: " + destinatario
                        + " → " + e.getMessage());
            } catch (Exception e) {
                errores++;
                System.err.println("[ERROR] Error inesperado con: " + destinatario
                        + " → " + e.getMessage());
            }
        }

        // Resumen final
        System.out.println("\n------------------------------------------------------");
        System.out.println("  RESUMEN DEL ENVÍO");
        System.out.println("------------------------------------------------------");
        System.out.println("  Total destinatarios : " + destinatarios.size());
        System.out.println("  Correos enviados    : " + enviados);
        System.out.println("  Errores             : " + errores);
        System.out.println("  BCC al remitente    : " + remitente);
        System.out.println("------------------------------------------------------");
    }
}
