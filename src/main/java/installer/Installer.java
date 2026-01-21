package installer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class Installer {

    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color FIELD_BG_COLOR = new Color(45, 45, 45);
    private static final Color ACCENT_COLOR = new Color(220, 20, 60);
    private static final Color SUCCESS_COLOR = new Color(0, 180, 120);
    private static final Color TEXT_COLOR = new Color(200, 200, 200);
    private static final Color BRIGHT_TEXT_COLOR = Color.WHITE;

    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font LOGO_FONT = new Font("Segoe UI", Font.BOLD, 28);

    private static final File CREDENTIALS_FILE = new File(System.getProperty("user.home"), ".privatehook_credentials");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow(null);
            loginWindow.setVisible(true);
        });
    }

    private static class GradientLabel extends JLabel {
        private final Timer animationTimer;
        private float gradientOffset = 0;

        public GradientLabel(String text) {
            super(text);
            animationTimer = new Timer(30, e -> {
                gradientOffset -= 1.5f;
                repaint();
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            animationTimer.start();
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            animationTimer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            Color[] colors = new Color[]{
                new Color(255, 0, 0), new Color(255, 127, 0), new Color(255, 255, 0),
                new Color(0, 255, 0), new Color(0, 0, 255), new Color(75, 0, 130),
                new Color(148, 0, 211), new Color(255, 0, 0)
            };

            float[] fractions = new float[]{0.0f, 0.15f, 0.3f, 0.45f, 0.6f, 0.75f, 0.9f, 1.0f};

            LinearGradientPaint paint = new LinearGradientPaint(
                gradientOffset, 0,
                gradientOffset + getWidth() * 2, 0,
                fractions, colors, CycleMethod.REPEAT
            );

            g2d.setPaint(paint);
            g2d.fillRect(0, getHeight() - 2, getWidth(), 2);
            g2d.dispose();
        }
    }

    private static JLabel createLogoLabel(String text) {
        JLabel logoLabel = new GradientLabel(text);
        logoLabel.setFont(LOGO_FONT);
        logoLabel.setForeground(BRIGHT_TEXT_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        logoLabel.setMinimumSize(new Dimension(1, 40));
        logoLabel.setPreferredSize(new Dimension(1, 40));
        return logoLabel;
    }

    private static JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(UI_FONT);
        field.setBackground(FIELD_BG_COLOR);
        field.setForeground(BRIGHT_TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        Border outer = BorderFactory.createLineBorder(BG_COLOR, 2);
        Border inner = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
        field.setBorder(BorderFactory.createCompoundBorder(outer, inner));
        field.setMaximumSize(new Dimension(250, 35));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(UI_FONT);
        field.setBackground(FIELD_BG_COLOR);
        field.setForeground(BRIGHT_TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setBorder(createStyledTextField().getBorder());
        field.setMaximumSize(new Dimension(250, 35));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private static JPanel createHorizontalInputPanel(String labelText, JComponent inputField) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BG_COLOR);
        panel.setMaximumSize(new Dimension(250, 60));
        JLabel label = new JLabel(labelText);
        label.setFont(UI_FONT);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputField, BorderLayout.CENTER);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text.toUpperCase());
        button.setFont(UI_FONT_BOLD);
        button.setBackground(FIELD_BG_COLOR);
        button.setForeground(BRIGHT_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 35));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(ACCENT_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(FIELD_BG_COLOR);
            }
        });
        return button;
    }

    private static JButton createTextButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT);
        button.setForeground(TEXT_COLOR);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });
        return button;
    }

    private static void showPopupMessage(JFrame parentFrame, String message, Color borderColor) {
        JDialog popup = new JDialog(parentFrame, false);
        popup.setUndecorated(true);
        popup.setBackground(new Color(0, 0, 0, 0));
        popup.setAlwaysOnTop(true);
        JPanel messagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(borderColor);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        messagePanel.setOpaque(false);
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(UI_FONT_BOLD);
        messageLabel.setForeground(BRIGHT_TEXT_COLOR);
        messageLabel.setBorder(new EmptyBorder(10, 15, 10, 15));
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        popup.add(messagePanel);
        popup.pack();
        int parentX = parentFrame.getX();
        int parentY = parentFrame.getY();
        int parentWidth = parentFrame.getWidth();
        int parentHeight = parentFrame.getHeight();
        int popupWidth = popup.getWidth();
        int popupHeight = popup.getHeight();
        int newX = parentX + (parentWidth - popupWidth) / 2;
        int newY = parentY + (parentHeight - popupHeight) / 2;
        popup.setLocation(newX, newY);
        Timer fadeOutTimer = new Timer(50, null);
        fadeOutTimer.addActionListener(e -> {
            float opacity = popup.getOpacity() - 0.05f;
            if (opacity <= 0.0f) {
                fadeOutTimer.stop();
                popup.dispose();
            } else {
                popup.setOpacity(opacity);
            }
        });
        Timer delayTimer = new Timer(2000, e -> fadeOutTimer.start());
        delayTimer.setRepeats(false);
        popup.setOpacity(1.0f);
        popup.setVisible(true);
        delayTimer.start();
    }

    private static void saveCredentials(String username, String password) {
        String user64 = Base64.getEncoder().encodeToString(username.getBytes());
        String pass64 = Base64.getEncoder().encodeToString(password.getBytes());
        String token64 = Base64.getEncoder().encodeToString("placeholder_token".getBytes());
        String content = user64 + ":" + pass64 + ":" + token64;
        try (PrintWriter out = new PrintWriter(new FileWriter(CREDENTIALS_FILE))) {
            out.println(content);
        } catch (IOException e) {
            System.err.println("Could not save credentials: " + e.getMessage());
        }
    }

    private static void loadCredentials(JTextField userField, JPasswordField passField, JCheckBox rememberBox) {
        if (!CREDENTIALS_FILE.exists()) return;
        try {
            String content = Files.readString(CREDENTIALS_FILE.toPath());
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                userField.setText(new String(Base64.getDecoder().decode(parts[0])));
                passField.setText(new String(Base64.getDecoder().decode(parts[1])));
                rememberBox.setSelected(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load or parse credentials: " + e.getMessage());
            deleteCredentials();
        }
    }

    private static void deleteCredentials() {
        try {
            if (CREDENTIALS_FILE.exists()) {
                Files.delete(CREDENTIALS_FILE.toPath());
            }
        } catch (IOException e) {
            System.err.println("Could not delete credentials file: " + e.getMessage());
        }
    }

    private static String getMinecraftModsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String separator = File.separator;
        if (os.contains("win")) {
            return userHome + separator + "AppData" + separator + "Roaming" + separator + ".minecraft" + separator + "mods";
        } else if (os.contains("mac")) {
            return userHome + separator + "Library" + separator + "Application Support" + separator + "minecraft" + separator + "mods";
        } else {
            return userHome + separator + ".minecraft" + separator + "mods";
        }
    }

    private static abstract class AppFrame extends JFrame {
        protected JPanel contentPanel;
        private Point mouseDownCompCoords;
        public AppFrame(String title, Point position) {
            super(title);
            setUndecorated(true);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 320);
            setResizable(false);
            JPanel rootPanel = new JPanel(new BorderLayout());
            rootPanel.setBackground(BG_COLOR);
            rootPanel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1));
            setContentPane(rootPanel);
            addDragListener(rootPanel);
            JPanel titleBar = createTitleBar();
            addDragListener(titleBar);
            rootPanel.add(titleBar, BorderLayout.NORTH);
            contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(BG_COLOR);
            contentPanel.setBorder(new EmptyBorder(0, 30, 10, 30));
            rootPanel.add(contentPanel, BorderLayout.CENTER);
            if (position != null) {
                setLocation(position);
            } else {
                setLocationRelativeTo(null);
            }
        }
        private JPanel createTitleBar() {
            JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            titleBar.setOpaque(false);
            JButton closeButton = new JButton("Close");
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setForeground(TEXT_COLOR);
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setPreferredSize(new Dimension(65, 30));
            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeButton.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { closeButton.setForeground(ACCENT_COLOR); }
                @Override public void mouseExited(MouseEvent e) { closeButton.setForeground(TEXT_COLOR); }
            });
            closeButton.addActionListener(e -> System.exit(0));
            titleBar.add(closeButton);
            return titleBar;
        }
        private void addDragListener(Component component) {
            component.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { mouseDownCompCoords = e.getPoint(); }
                public void mouseReleased(MouseEvent e) { mouseDownCompCoords = null; }
            });
            component.addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point currCoords = e.getLocationOnScreen();
                    setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
                }
            });
        }
    }

    private static class LoginWindow extends AppFrame {
        public LoginWindow(Point position) {
            super("PrivateHook Login", position);

            JTextField usernameField = createStyledTextField();
            JPasswordField passwordField = createStyledPasswordField();

            JCheckBox rememberMeBox = new JCheckBox("Remember Me");
            rememberMeBox.setBackground(BG_COLOR);
            rememberMeBox.setForeground(TEXT_COLOR);
            rememberMeBox.setFocusPainted(false);
            rememberMeBox.setAlignmentX(Component.CENTER_ALIGNMENT);

            contentPanel.add(createLogoLabel("PrivateHook"));
            contentPanel.add(Box.createVerticalStrut(25));
            contentPanel.add(createHorizontalInputPanel("Username", usernameField));
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(createHorizontalInputPanel("Password", passwordField));
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(rememberMeBox);
            contentPanel.add(Box.createVerticalStrut(15));

            JButton loginButton = createStyledButton("Login");
            contentPanel.add(loginButton);
            contentPanel.add(Box.createVerticalStrut(10));

            JButton registerButton = createTextButton("Don't have an account? Register");
            contentPanel.add(registerButton);

            registerButton.addActionListener(e -> {
                this.dispose();
                new RegisterWindow(this.getLocation()).setVisible(true);
            });

            loginButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    showPopupMessage(this, "Username and password cannot be empty.", ACCENT_COLOR);
                    return;
                }

                if (rememberMeBox.isSelected()) {
                    saveCredentials(username, password);
                } else {
                    deleteCredentials();
                }

                boolean loginSuccess = true;
                if (loginSuccess) {
                    showPopupMessage(this, "Login successful!", SUCCESS_COLOR);
                    new Timer(1500, event -> {
                        this.dispose();
                        new InstallerWindow(this.getLocation(), username).setVisible(true);
                    }) {{ setRepeats(false); start(); }};
                } else {
                    showPopupMessage(this, "Login failed: Invalid credentials.", ACCENT_COLOR);
                }
            });

            this.pack();
            this.setSize(400, 360);
            this.setLocationRelativeTo(null);

            loadCredentials(usernameField, passwordField, rememberMeBox);
            usernameField.requestFocusInWindow();
        }
    }

    private static class RegisterWindow extends AppFrame {
        public RegisterWindow(Point position) {
            super("PrivateHook Register", position);
            JTextField usernameField = createStyledTextField();
            JPasswordField passwordField = createStyledPasswordField();
            JTextField keyField = createStyledTextField();
            contentPanel.add(createLogoLabel("Create Account"));
            contentPanel.add(Box.createVerticalStrut(25));
            contentPanel.add(createHorizontalInputPanel("Username", usernameField));
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(createHorizontalInputPanel("Password", passwordField));
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(createHorizontalInputPanel("License Key", keyField));
            contentPanel.add(Box.createVerticalStrut(25));
            JButton registerButton = createStyledButton("Register");
            contentPanel.add(registerButton);
            contentPanel.add(Box.createVerticalStrut(10));
            JButton backButton = createTextButton("Already have an account? Login");
            contentPanel.add(backButton);
            registerButton.addActionListener(e -> {
                showPopupMessage(this, "Registration successful! Please log in.", SUCCESS_COLOR);
                new Timer(1500, event -> {
                    this.dispose();
                    new LoginWindow(this.getLocation()).setVisible(true);
                }) {{ setRepeats(false); start(); }};
            });
            backButton.addActionListener(e -> {
                this.dispose();
                new LoginWindow(this.getLocation()).setVisible(true);
            });
            this.pack();
            this.setSize(400, 380);
            this.setLocation(position);
            usernameField.requestFocusInWindow();
        }
    }

    private static class InstallerWindow extends AppFrame {
        public InstallerWindow(Point position, String username) {
            super("PrivateHook Installer", position);
            JTextField pathField = createStyledTextField();
            pathField.setText(getMinecraftModsPath());
            pathField.setEditable(false);
            JButton browseButton = createStyledButton("Browse");
            JButton installButton = createStyledButton("Install PrivateHook");
            JLabel statusLabel = new JLabel("Status: Idle", SwingConstants.CENTER);

            contentPanel.add(createLogoLabel("Installer"));
            contentPanel.add(Box.createVerticalStrut(15));

            JLabel usernameLabel = new JLabel("Welcome, " + username);
            usernameLabel.setFont(UI_FONT_BOLD);
            usernameLabel.setForeground(BRIGHT_TEXT_COLOR);
            usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(usernameLabel);
            contentPanel.add(Box.createVerticalStrut(5));

            JLabel subscriptionLabel = new JLabel("Subscription: Lifetime");
            subscriptionLabel.setFont(UI_FONT);
            subscriptionLabel.setForeground(TEXT_COLOR);
            subscriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(subscriptionLabel);
            contentPanel.add(Box.createVerticalStrut(5));

            JLabel rankLabel = new JLabel("Rank: Beta Tester");
            rankLabel.setFont(UI_FONT);
            rankLabel.setForeground(TEXT_COLOR);
            rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(rankLabel);
            contentPanel.add(Box.createVerticalStrut(5));

            int uid = ThreadLocalRandom.current().nextInt(100000, 999999);
            JLabel uidLabel = new JLabel("UID: " + uid);
            uidLabel.setFont(UI_FONT);
            uidLabel.setForeground(TEXT_COLOR);
            uidLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(uidLabel);

            contentPanel.add(Box.createVerticalStrut(25));
            contentPanel.add(createHorizontalInputPanel("Install Path", pathField));
            contentPanel.add(Box.createVerticalStrut(10));

            JPanel browsePanel = new JPanel();
            browsePanel.setBackground(BG_COLOR);
            browsePanel.add(browseButton);
            browseButton.setPreferredSize(new Dimension(120, 35));
            contentPanel.add(browsePanel);

            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(installButton);
            contentPanel.add(Box.createVerticalStrut(10));

            statusLabel.setFont(UI_FONT);
            statusLabel.setForeground(TEXT_COLOR);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(statusLabel);

            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Select your mods folder");
                try {
                    fileChooser.setCurrentDirectory(new File(pathField.getText()));
                } catch (Exception ignored) {}
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });
            installButton.addActionListener(e -> {
                String installPath = pathField.getText();
                final String DOWNLOAD_URL = "https://your-host.com/path/to/private-hook.jar";
                installButton.setEnabled(false);
                browseButton.setEnabled(false);
                statusLabel.setText("Status: Connecting...");
                SwingWorker<String, Integer> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        URL url = new URL(DOWNLOAD_URL);
                        URLConnection connection = url.openConnection();
                        int fileSize = connection.getContentLength();
                        String fileName = DOWNLOAD_URL.substring(DOWNLOAD_URL.lastIndexOf('/') + 1);
                        File saveFile = new File(installPath, fileName);
                        try (java.io.InputStream in = connection.getInputStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(saveFile)) {
                            SwingUtilities.invokeLater(() -> statusLabel.setText("Status: Installing " + fileName));
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            long totalBytesRead = 0;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                if (fileSize > 0) {
                                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                                    publish(progress);
                                }
                            }
                        }
                        return "Install Complete!";
                    }
                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        int latestProgress = chunks.get(chunks.size() - 1);
                        statusLabel.setText("Status: Installing... " + latestProgress + "%");
                    }
                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            statusLabel.setText("Status: " + result);
                            showPopupMessage(InstallerWindow.this, result, SUCCESS_COLOR);
                        } catch (InterruptedException | ExecutionException e) {
                            String errorMsg = "Error: " + e.getCause().getMessage();
                            statusLabel.setText("Status: " + errorMsg);
                            showPopupMessage(InstallerWindow.this, errorMsg, ACCENT_COLOR);
                        } finally {
                            installButton.setEnabled(true);
                            browseButton.setEnabled(true);
                        }
                    }
                };
                worker.execute();
            });
            this.pack();
            this.setSize(400, 440);
            this.setLocation(position);
        }
    }
}
