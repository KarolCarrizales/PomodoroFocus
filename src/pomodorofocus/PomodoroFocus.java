package pomodorofocus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PomodoroFocus extends JFrame {

    private static final int TIEMPO_TRABAJO = 25 * 60;
    private static final int TIEMPO_DESCANSO_CORTO = 5 * 60;
    private static final int TIEMPO_DESCANSO_LARGO = 15 * 60;

    private int tiempoRestante = TIEMPO_TRABAJO;
    private boolean esDescanso = false;
    private Timer timer;
    private int ciclosCompletados = 0;

    private JLabel lblCronometro;
    private JLabel lblEstado;
    private JLabel lblTareaActiva;
    private DefaultListModel<String> modeloTareas;
    private JList<String> listaTareas;
    private JTextField txtNuevaTarea;

    private final Color COLOR_FONDO = new Color(30, 30, 30);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);
    private final Color COLOR_ACENTO = new Color(76, 175, 80);

    public PomodoroFocus() {
        super("Pomodoro Focus");
        GestorBD.inicializarBaseDeDatos();
        configurarVentana();
        inicializarComponentes();
        cargarTareasGuardadas();
    }

    private void configurarVentana() {
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
    }

    private void inicializarComponentes() {
        JPanel panelTimer = new JPanel();
        panelTimer.setLayout(new BoxLayout(panelTimer, BoxLayout.Y_AXIS));
        panelTimer.setBackground(COLOR_FONDO);
        panelTimer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        lblEstado = new JLabel("MODO ENFOQUE");
        lblEstado.setForeground(COLOR_ACENTO);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 16));
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCronometro = new JLabel(formatearTiempo(tiempoRestante));
        lblCronometro.setForeground(COLOR_TEXTO);
        lblCronometro.setFont(new Font("Monospaced", Font.BOLD, 60));
        lblCronometro.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTareaActiva = new JLabel("Sin tarea activa");
        lblTareaActiva.setForeground(Color.GRAY);
        lblTareaActiva.setFont(new Font("Arial", Font.ITALIC, 14));
        lblTareaActiva.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTimer.add(lblEstado);
        panelTimer.add(Box.createVerticalStrut(10));
        panelTimer.add(lblCronometro);
        panelTimer.add(Box.createVerticalStrut(10));
        panelTimer.add(lblTareaActiva);

        JPanel panelControles = new JPanel();
        panelControles.setBackground(COLOR_FONDO);
        
        JButton btnIniciar = crearBoton("Iniciar / Pausar");
        JButton btnReset = crearBoton("Reiniciar");

        btnIniciar.addActionListener(e -> toggleTimer());
        btnReset.addActionListener(e -> reiniciarTimer());

        panelControles.add(btnIniciar);
        panelControles.add(btnReset);
        panelTimer.add(panelControles);

        add(panelTimer, BorderLayout.NORTH);

        JPanel panelTareas = new JPanel(new BorderLayout());
        panelTareas.setBackground(COLOR_FONDO);
        panelTareas.setBorder(BorderFactory.createTitledBorder(null, "Mis Tareas", 0, 0, new Font("Arial", Font.BOLD, 12), COLOR_TEXTO));

        modeloTareas = new DefaultListModel<>();
        listaTareas = new JList<>(modeloTareas);
        listaTareas.setBackground(new Color(50, 50, 50));
        listaTareas.setForeground(COLOR_TEXTO);
        listaTareas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        listaTareas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaTareas.getSelectedValue() != null) {
                lblTareaActiva.setText("Trabajando en: " + listaTareas.getSelectedValue());
                lblTareaActiva.setForeground(Color.CYAN);
            }
        });

        JScrollPane scrollTareas = new JScrollPane(listaTareas);
        scrollTareas.setBorder(null);
        panelTareas.add(scrollTareas, BorderLayout.CENTER);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.setBackground(COLOR_FONDO);

        txtNuevaTarea = new JTextField();
        txtNuevaTarea.setBackground(new Color(50, 50, 50));
        txtNuevaTarea.setForeground(COLOR_TEXTO);
        txtNuevaTarea.setCaretColor(COLOR_TEXTO);

        JButton btnAgregar = crearBoton("+");
        JButton btnEliminar = crearBoton("X");

        btnAgregar.addActionListener(e -> agregarTarea());
        btnEliminar.addActionListener(e -> eliminarTarea());
        txtNuevaTarea.addActionListener(e -> agregarTarea());

        panelInput.add(txtNuevaTarea, BorderLayout.CENTER);
        panelInput.add(btnAgregar, BorderLayout.EAST);
        panelInput.add(btnEliminar, BorderLayout.WEST);

        panelTareas.add(panelInput, BorderLayout.SOUTH);

        add(panelTareas, BorderLayout.CENTER);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarCronometro();
            }
        });
    }

    private void cargarTareasGuardadas() {
        for (String tarea : GestorBD.obtenerTareas()) {
            modeloTareas.addElement(tarea);
        }
    }

    private void toggleTimer() {
        if (timer.isRunning()) {
            timer.stop();
        } else {
            timer.start();
        }
    }

    private void actualizarCronometro() {
        tiempoRestante--;
        lblCronometro.setText(formatearTiempo(tiempoRestante));

        if (tiempoRestante <= 0) {
            timer.stop();
            Toolkit.getDefaultToolkit().beep();
            cambiarModo();
        }
    }

    private void cambiarModo() {
        if (!esDescanso) {
            ciclosCompletados++;
            esDescanso = true;
            
            if (ciclosCompletados % 4 == 0) {
                tiempoRestante = TIEMPO_DESCANSO_LARGO;
                lblEstado.setText("¡DESCANSO LARGO!");
                JOptionPane.showMessageDialog(this, "Has completado 4 ciclos. Toma un descanso largo.");
            } else {
                tiempoRestante = TIEMPO_DESCANSO_CORTO;
                lblEstado.setText("TIEMPO DE DESCANSO");
                JOptionPane.showMessageDialog(this, "Tiempo terminado. Toma 5 minutos.");
            }
            lblEstado.setForeground(Color.ORANGE);
            
        } else {
            esDescanso = false;
            tiempoRestante = TIEMPO_TRABAJO;
            lblEstado.setText("MODO ENFOQUE");
            lblEstado.setForeground(COLOR_ACENTO);
            JOptionPane.showMessageDialog(this, "A trabajar de nuevo.");
        }
        lblCronometro.setText(formatearTiempo(tiempoRestante));
    }

    private void reiniciarTimer() {
        timer.stop();
        esDescanso = false;
        tiempoRestante = TIEMPO_TRABAJO;
        lblCronometro.setText(formatearTiempo(tiempoRestante));
        lblEstado.setText("LISTO PARA EMPEZAR");
        lblEstado.setForeground(COLOR_ACENTO);
    }

    private void agregarTarea() {
        String tarea = txtNuevaTarea.getText().trim();
        if (!tarea.isEmpty()) {
            modeloTareas.addElement(tarea);
            GestorBD.insertarTarea(tarea);
            txtNuevaTarea.setText("");
        }
    }

    private void eliminarTarea() {
        int index = listaTareas.getSelectedIndex();
        if (index != -1) {
            String tarea = modeloTareas.get(index);
            modeloTareas.remove(index);
            GestorBD.eliminarTarea(tarea);
            
            if (modeloTareas.isEmpty()) {
                lblTareaActiva.setText("Sin tarea activa");
                lblTareaActiva.setForeground(Color.GRAY);
            }
        }
    }

    private String formatearTiempo(int segundosTotales) {
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PomodoroFocus().setVisible(true);
        });
    }
}