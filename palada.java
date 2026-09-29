import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class palada {
    private static final String URL_BASE = "jdbc:sqlite:citas.db";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        inicializarBase();
        String opcion;

        do {
            opcion = JOptionPane.showInputDialog(null,
                    "AGENDA DE CITAS\n\n"
                            + "1. Crear una cita\n"
                            + "2. Buscar citas\n"
                            + "3. Ver todas las citas\n"
                            + "0. Salir\n\n"
                            + "Escribe una opción:",
                    "Agenda de citas", JOptionPane.QUESTION_MESSAGE);

            if (opcion == null || opcion.equals("0")) {
                break;
            }

            switch (opcion.trim()) {
                case "1" -> crearCita();
                case "2" -> buscarCitas();
                case "3" -> listarCitas();
                default -> mostrarMensaje("Opción no válida.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } while (true);

        mostrarMensaje("Hasta luego.", "Agenda de citas", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void crearCita() {
        String nombre = pedirTexto("Escribe el nombre de la persona:");
        if (nombre == null) {
            return;
        }

        LocalDateTime fecha = pedirFecha();
        if (fecha == null) {
            return;
        }

        String descripcion = pedirTexto("Escribe el motivo o descripción de la cita:");
        if (descripcion == null) {
            return;
        }

        Cita cita = new Cita(nombre, fecha, descripcion);
        String sql = "INSERT INTO citas(nombre, fecha, descripcion) VALUES(?, ?, ?)";
        try (Connection conexion = conectar();
                PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, cita.getNombre());
            stmt.setString(2, cita.getFecha().format(FORMATO));
            stmt.setString(3, cita.getDescripcion());
            stmt.executeUpdate();
            mostrarMensaje("La cita fue guardada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            mostrarMensaje("No se pudo guardar la cita:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void buscarCitas() {
        String termino = pedirTexto("Busca por nombre, fecha o descripción:");
        if (termino == null) {
            return;
        }

        List<Cita> resultados = new ArrayList<>();
        for (Cita cita : cargarCitas()) {
            if (cita.toString().toLowerCase().contains(termino.toLowerCase())) {
                resultados.add(cita);
            }
        }

        if (resultados.isEmpty()) {
            mostrarMensaje("No se encontraron citas.", "Resultados", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        mostrarMensaje("CITAS ENCONTRADAS\n\n" + convertirAtexto(resultados),
                "Resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void listarCitas() {
        List<Cita> citas = cargarCitas();
        if (citas.isEmpty()) {
            mostrarMensaje("Todavía no hay citas guardadas.", "Agenda de citas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        mostrarMensaje("TODAS LAS CITAS\n\n" + convertirAtexto(citas),
                "Agenda de citas", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String pedirTexto(String mensaje) {
        while (true) {
            String texto = JOptionPane.showInputDialog(null, mensaje, "Agenda de citas", JOptionPane.QUESTION_MESSAGE);
            if (texto == null) {
                return null;
            }
            texto = texto.trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            mostrarMensaje("El campo no puede estar vacío.", "Dato no válido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static LocalDateTime pedirFecha() {
        while (true) {
            String texto = JOptionPane.showInputDialog(null,
                    "Escribe la fecha y hora.\nEjemplo: 25/12/2026 14:30",
                    "Agenda de citas", JOptionPane.QUESTION_MESSAGE);
            if (texto == null) {
                return null;
            }
            try {
                return LocalDateTime.parse(texto.trim(), FORMATO);
            } catch (DateTimeParseException e) {
                mostrarMensaje("Formato incorrecto.\nUsa, por ejemplo: 25/12/2026 14:30",
                        "Fecha no válida", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static List<Cita> cargarCitas() {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT nombre, fecha, descripcion FROM citas ORDER BY fecha";
        try (Connection conexion = conectar();
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                citas.add(new Cita(
                        rs.getString("nombre"),
                        LocalDateTime.parse(rs.getString("fecha"), FORMATO),
                        rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            mostrarMensaje("No se pudieron leer las citas:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return citas;
    }

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL_BASE);
    }

    private static void inicializarBase() {
        String sql = "CREATE TABLE IF NOT EXISTS citas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "descripcion TEXT NOT NULL)";
        try (Connection conexion = conectar();
                Statement stmt = conexion.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            mostrarMensaje("No se pudo inicializar la base de datos:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String convertirAtexto(List<Cita> citas) {
        StringBuilder texto = new StringBuilder();
        for (Cita cita : citas) {
            texto.append(cita).append("\n\n");
        }
        return texto.toString();
    }

    private static void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, tipo);
    }

    private static class Cita {
        private final String nombre;
        private final LocalDateTime fecha;
        private final String descripcion;

        Cita(String nombre, LocalDateTime fecha, String descripcion) {
            this.nombre = nombre;
            this.fecha = fecha;
            this.descripcion = descripcion;
        }

        String getNombre() {
            return nombre;
        }

        LocalDateTime getFecha() {
            return fecha;
        }

        String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return "Nombre: " + nombre + "\nFecha: " + fecha.format(FORMATO)
                    + "\nDescripción: " + descripcion;
        }
    }
}
