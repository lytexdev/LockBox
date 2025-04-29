import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

public class LockBox extends JFrame {
    private static final String APP_TITLE = "LockBox Password Manager";
    private static final String APP_AUTHOR = "Made by lytexdev (Immanuel Mruk)";
    private static final String FILE_EXTENSION = "lbx";
    
    // Lila/Pink Theme mit Dark Mode
    private static final Color PRIMARY_COLOR = new Color(186, 104, 200);     // Lila
    private static final Color ACCENT_COLOR = new Color(233, 30, 99);        // Pink
    private static final Color BACKGROUND_COLOR = new Color(33, 33, 33);     // Dunkelgrau
    private static final Color CARD_COLOR = new Color(66, 66, 66);           // Mittleres Grau
    private static final Color TEXT_COLOR = new Color(255, 255, 255);        // Weiß
    private static final Color TEXT_SECONDARY_COLOR = new Color(180, 180, 180); // Hellgrau
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    private PasswordManager manager;
    private JPanel mainPanel;
    private JPanel welcomePanel;
    private JPanel contentPanel;
    private JPasswordField masterPasswordField;
    private DefaultListModel<Account> accountListModel;
    private JList<Account> accountList;
    private JScrollPane accountScrollPane;
    private JLabel statusLabel;
    private JTextField websiteField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton generateButton;
    private JSpinner lengthSpinner;
    private JCheckBox showPasswordCheckBox;
    private String currentFileName = null;
    private Preferences prefs;
    private JTextField searchField;
    private JPanel detailsPanel;
    private JLabel lockIcon;
    private Timer autoLockTimer;
    private int autoLockMinutes = 5;
    
    public LockBox() {
        setTitle(APP_TITLE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Load application preferences
        prefs = Preferences.userNodeForPackage(LockBox.class);
        
        // Set application icon
        setIconImage(createLockIcon(32).getImage());
        
        // Apply modern look and feel
        applyDarkTheme();
        
        // Initialize auto-lock timer first
        initAutoLockTimer();
        
        // Initialize components
        initializeComponents();
        
        // Show the welcome panel first
        showWelcomePanel();
        
        setVisible(true);
    }
    
    private void initAutoLockTimer() {
        autoLockTimer = new Timer(autoLockMinutes * 60 * 1000, e -> {
            if (manager != null) {
                logout();
                JOptionPane.showMessageDialog(this, 
                    "LockBox has been automatically locked due to inactivity.", 
                    "Auto-Lock", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        autoLockTimer.setRepeats(false);
    }
    
    private void resetAutoLockTimer() {
        if (autoLockTimer.isRunning()) {
            autoLockTimer.restart();
        } else {
            autoLockTimer.start();
        }
    }
    
    private void applyDarkTheme() {
        try {
            // Set system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize UI colors for dark theme
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("TextField.background", CARD_COLOR);
            UIManager.put("TextField.foreground", TEXT_COLOR);
            UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
            UIManager.put("PasswordField.background", CARD_COLOR);
            UIManager.put("PasswordField.foreground", TEXT_COLOR);
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", TEXT_COLOR);
            UIManager.put("Label.foreground", TEXT_COLOR);
            UIManager.put("CheckBox.foreground", TEXT_COLOR);
            UIManager.put("List.background", CARD_COLOR);
            UIManager.put("List.foreground", TEXT_COLOR);
            UIManager.put("List.selectionBackground", ACCENT_COLOR);
            UIManager.put("List.selectionForeground", TEXT_COLOR);
            UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
            UIManager.put("Menu.foreground", TEXT_COLOR);
            UIManager.put("Menu.background", BACKGROUND_COLOR);
            UIManager.put("MenuBar.background", CARD_COLOR);
            UIManager.put("MenuItem.foreground", TEXT_COLOR);
            UIManager.put("MenuItem.background", CARD_COLOR);
            UIManager.put("OptionPane.background", BACKGROUND_COLOR);
            UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
            UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
            UIManager.put("ComboBox.background", CARD_COLOR);
            UIManager.put("ComboBox.foreground", TEXT_COLOR);
            UIManager.put("Spinner.background", CARD_COLOR);
            UIManager.put("Spinner.foreground", TEXT_COLOR);
            UIManager.put("ToolBar.background", CARD_COLOR);
            
            // Update all components
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        // Main panel with card layout to switch between welcome and content
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);
        
        // Create welcome panel
        createWelcomePanel();
        
        // Create content panel
        createContentPanel();
        
        // Add panels to main panel
        mainPanel.add(welcomePanel, "welcome");
        mainPanel.add(contentPanel, "content");
        
        // Create menu bar
        createMenuBar();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(CARD_COLOR);
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(NORMAL_FONT);
        fileMenu.setForeground(TEXT_COLOR);
        
        JMenuItem newItem = new JMenuItem("New Database");
        newItem.setFont(NORMAL_FONT);
        newItem.setBackground(CARD_COLOR);
        newItem.setForeground(TEXT_COLOR);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> createNewDatabase());
        
        JMenuItem openItem = new JMenuItem("Open Database");
        openItem.setFont(NORMAL_FONT);
        openItem.setBackground(CARD_COLOR);
        openItem.setForeground(TEXT_COLOR);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openExistingDatabase());
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setFont(NORMAL_FONT);
        saveItem.setBackground(CARD_COLOR);
        saveItem.setForeground(TEXT_COLOR);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveDatabase());
        
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setFont(NORMAL_FONT);
        saveAsItem.setBackground(CARD_COLOR);
        saveAsItem.setForeground(TEXT_COLOR);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> saveDatabaseAs());
        
        JMenuItem lockItem = new JMenuItem("Lock Database");
        lockItem.setFont(NORMAL_FONT);
        lockItem.setBackground(CARD_COLOR);
        lockItem.setForeground(TEXT_COLOR);
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        lockItem.addActionListener(e -> logout());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(NORMAL_FONT);
        exitItem.setBackground(CARD_COLOR);
        exitItem.setForeground(TEXT_COLOR);
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(lockItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setFont(NORMAL_FONT);
        editMenu.setForeground(TEXT_COLOR);
        
        JMenuItem addAccountItem = new JMenuItem("Add New Entry");
        addAccountItem.setFont(NORMAL_FONT);
        addAccountItem.setBackground(CARD_COLOR);
        addAccountItem.setForeground(TEXT_COLOR);
        addAccountItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        addAccountItem.addActionListener(e -> addAccount());
        
        JMenuItem deleteAccountItem = new JMenuItem("Delete Entry");
        deleteAccountItem.setFont(NORMAL_FONT);
        deleteAccountItem.setBackground(CARD_COLOR);
        deleteAccountItem.setForeground(TEXT_COLOR);
        deleteAccountItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        deleteAccountItem.addActionListener(e -> deleteAccount());
        
        JMenuItem copyUsernameItem = new JMenuItem("Copy Username");
        copyUsernameItem.setFont(NORMAL_FONT);
        copyUsernameItem.setBackground(CARD_COLOR);
        copyUsernameItem.setForeground(TEXT_COLOR);
        copyUsernameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        copyUsernameItem.addActionListener(e -> copyUsername());
        
        JMenuItem copyPasswordItem = new JMenuItem("Copy Password");
        copyPasswordItem.setFont(NORMAL_FONT);
        copyPasswordItem.setBackground(CARD_COLOR);
        copyPasswordItem.setForeground(TEXT_COLOR);
        copyPasswordItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyPasswordItem.addActionListener(e -> copyPassword());
        
        JMenuItem generatePasswordItem = new JMenuItem("Generate Password");
        generatePasswordItem.setFont(NORMAL_FONT);
        generatePasswordItem.setBackground(CARD_COLOR);
        generatePasswordItem.setForeground(TEXT_COLOR);
        generatePasswordItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        generatePasswordItem.addActionListener(e -> generatePassword());
        
        editMenu.add(addAccountItem);
        editMenu.add(deleteAccountItem);
        editMenu.addSeparator();
        editMenu.add(copyUsernameItem);
        editMenu.add(copyPasswordItem);
        editMenu.add(generatePasswordItem);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setFont(NORMAL_FONT);
        toolsMenu.setForeground(TEXT_COLOR);
        
        JMenuItem changePasswordItem = new JMenuItem("Change Master Password");
        changePasswordItem.setFont(NORMAL_FONT);
        changePasswordItem.setBackground(CARD_COLOR);
        changePasswordItem.setForeground(TEXT_COLOR);
        changePasswordItem.addActionListener(e -> changeMasterPassword());
        
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setFont(NORMAL_FONT);
        settingsItem.setBackground(CARD_COLOR);
        settingsItem.setForeground(TEXT_COLOR);
        settingsItem.addActionListener(e -> showSettings());
        
        toolsMenu.add(changePasswordItem);
        toolsMenu.add(settingsItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(NORMAL_FONT);
        helpMenu.setForeground(TEXT_COLOR);
        
        JMenuItem aboutItem = new JMenuItem("About LockBox");
        aboutItem.setFont(NORMAL_FONT);
        aboutItem.setBackground(CARD_COLOR);
        aboutItem.setForeground(TEXT_COLOR);
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        // Add author credit on the right
        JMenu authorMenu = new JMenu(APP_AUTHOR);
        authorMenu.setFont(SMALL_FONT);
        authorMenu.setForeground(TEXT_SECONDARY_COLOR);
        authorMenu.setEnabled(false);
        
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(authorMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createWelcomePanel() {
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setBackground(BACKGROUND_COLOR);
        
        // Create a panel for the content with some padding
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        innerPanel.setBackground(BACKGROUND_COLOR);
        
        // Logo and title
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel logoLabel = new JLabel(createLockIcon(96));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("LockBox");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Secure Password Manager");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel authorLabel = new JLabel(APP_AUTHOR);
        authorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        authorLabel.setForeground(TEXT_SECONDARY_COLOR);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        logoPanel.add(logoLabel);
        logoPanel.add(Box.createVerticalStrut(20));
        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(subtitleLabel);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(authorLabel);
        
        // Recent files panel
        JPanel recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setBackground(BACKGROUND_COLOR);
        recentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        recentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        
        JLabel recentLabel = new JLabel("Recent Databases");
        recentLabel.setFont(HEADING_FONT);
        recentLabel.setForeground(TEXT_COLOR);
        recentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        recentPanel.add(recentLabel);
        recentPanel.add(Box.createVerticalStrut(10));
        
        // Fix for recent files
        File appDir = new File(System.getProperty("user.home"), ".lockbox");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        
        // Create a dummy file in the .lockbox directory
        File dummyFile = new File(appDir, "recent.lbx");
        if (!dummyFile.exists()) {
            try {
                dummyFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Load and display recent files
        String[] recentFiles = getRecentFiles();
        
        // Force add the dummy file to recent files if none exist
        if (recentFiles.length == 0) {
            addRecentFile(dummyFile.getAbsolutePath());
            recentFiles = getRecentFiles();
        }
        
        if (recentFiles.length > 0) {
            for (String file : recentFiles) {
                if (file != null && !file.isEmpty()) {
                    File f = new File(file);
                    if (f.exists()) {
                        JPanel filePanel = createRecentFilePanel(file);
                        recentPanel.add(filePanel);
                        recentPanel.add(Box.createVerticalStrut(8));
                    }
                }
            }
        }
        
        if (recentPanel.getComponentCount() <= 2) { // Only label and strut
            JLabel noRecentLabel = new JLabel("No recent databases");
            noRecentLabel.setFont(NORMAL_FONT);
            noRecentLabel.setForeground(TEXT_SECONDARY_COLOR);
            noRecentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            recentPanel.add(noRecentLabel);
        }
        
        // Actions panel
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBackground(BACKGROUND_COLOR);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel actionsLabel = new JLabel("Get Started");
        actionsLabel.setFont(HEADING_FONT);
        actionsLabel.setForeground(TEXT_COLOR);
        actionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        buttonsPanel.setMaximumSize(new Dimension(400, 50));
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton createButton = createStyledButton("Create New Database", ACCENT_COLOR);
        createButton.setFont(NORMAL_FONT);
        createButton.addActionListener(e -> createNewDatabase());
        
        JButton openButton = createStyledButton("Open Existing Database", PRIMARY_COLOR);
        openButton.setFont(NORMAL_FONT);
        openButton.addActionListener(e -> openExistingDatabase());
        
        buttonsPanel.add(createButton);
        buttonsPanel.add(openButton);
        
        actionsPanel.add(actionsLabel);
        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(buttonsPanel);
        
        // Add all panels to the inner panel
        innerPanel.add(logoPanel);
        innerPanel.add(Box.createVerticalStrut(40));
        innerPanel.add(recentPanel);
        innerPanel.add(Box.createVerticalStrut(20));
        innerPanel.add(actionsPanel);
        
        // Wrap inner panel in a scroll pane
        JScrollPane scrollPane = new JScrollPane(innerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);
        
        welcomePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createRecentFilePanel(String filePath) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        panel.setMaximumSize(new Dimension(2000, 60));
        
        File file = new File(filePath);
        JLabel fileLabel = new JLabel(file.getName());
        fileLabel.setFont(NORMAL_FONT);
        fileLabel.setForeground(TEXT_COLOR);
        
        JLabel pathLabel = new JLabel(file.getParent());
        pathLabel.setFont(SMALL_FONT);
        pathLabel.setForeground(TEXT_SECONDARY_COLOR);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_COLOR);
        textPanel.add(fileLabel);
        textPanel.add(pathLabel);
        
        JButton openButton = new JButton("Open");
        openButton.setFont(SMALL_FONT);
        openButton.setForeground(TEXT_COLOR);
        openButton.setBackground(PRIMARY_COLOR);
        openButton.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
        openButton.setFocusPainted(false);
        openButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openButton.addActionListener(e -> openDatabase(filePath));
        
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(openButton, BorderLayout.EAST);
        
        // Make panel clickable
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDatabase(filePath);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(80, 80, 80));
                textPanel.setBackground(new Color(80, 80, 80));
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(CARD_COLOR);
                textPanel.setBackground(CARD_COLOR);
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(NORMAL_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darken(color, 0.1f));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private Color darken(Color color, float fraction) {
        int red = Math.max(0, Math.round(color.getRed() * (1 - fraction)));
        int green = Math.max(0, Math.round(color.getGreen() * (1 - fraction)));
        int blue = Math.max(0, Math.round(color.getBlue() * (1 - fraction)));
        return new Color(red, green, blue);
    }
    
    private void createContentPanel() {
        contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(CARD_COLOR);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JButton newButton = createToolbarButton("New", "add");
        newButton.addActionListener(e -> addAccount());
        
        JButton saveButton = createToolbarButton("Save", "save");
        saveButton.addActionListener(e -> saveDatabase());
        
        JButton deleteButton = createToolbarButton("Delete", "delete");
        deleteButton.addActionListener(e -> deleteAccount());
        
        JButton copyUsernameButton = createToolbarButton("Username", "user");
        copyUsernameButton.addActionListener(e -> copyUsername());
        
        JButton copyPasswordButton = createToolbarButton("Password", "key");
        copyPasswordButton.addActionListener(e -> copyPassword());
        
        JButton lockButton = createToolbarButton("Lock", "lock");
        lockButton.addActionListener(e -> logout());
        
        // Search field
        searchField = new JTextField(15);
        searchField.setFont(NORMAL_FONT);
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setForeground(TEXT_COLOR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        searchField.putClientProperty("JTextField.placeholderText", "Search entries...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterAccounts();
            }
        });
        
        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(20, 20));
        toolBar.add(copyUsernameButton);
        toolBar.add(copyPasswordButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(searchField);
        toolBar.addSeparator(new Dimension(20, 20));
        toolBar.add(lockButton);
        
        contentPanel.add(toolBar, BorderLayout.NORTH);
        
        // Split pane for list and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(300);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BACKGROUND_COLOR);
        
        // Account list panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        
        accountListModel = new DefaultListModel<>();
        accountList = new JList<>(accountListModel);
        accountList.setFont(NORMAL_FONT);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setCellRenderer(new AccountListCellRenderer());
        accountList.setBackground(CARD_COLOR);
        accountList.setForeground(TEXT_COLOR);
        accountList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedAccount();
                resetAutoLockTimer();
            }
        });
        
        accountScrollPane = new JScrollPane(accountList);
        accountScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        accountScrollPane.setBackground(CARD_COLOR);
        
        leftPanel.add(accountScrollPane, BorderLayout.CENTER);
        
        // Details panel
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(BACKGROUND_COLOR);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        
        // Empty state for details panel
        createEmptyDetailsState();
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(detailsPanel);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(CARD_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(SMALL_FONT);
        statusLabel.setForeground(TEXT_COLOR);
        
        JLabel authorLabel = new JLabel(APP_AUTHOR);
        authorLabel.setFont(SMALL_FONT);
        authorLabel.setForeground(TEXT_SECONDARY_COLOR);
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        lockIcon = new JLabel(createLockIcon(16));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(authorLabel, BorderLayout.CENTER);
        statusPanel.add(lockIcon, BorderLayout.EAST);
        
        contentPanel.add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void createEmptyDetailsState() {
        detailsPanel.removeAll();
        
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel emptyLabel = new JLabel("Select an entry or create a new one");
        emptyLabel.setFont(NORMAL_FONT);
        emptyLabel.setForeground(TEXT_SECONDARY_COLOR);
        
        JButton addNewButton = createStyledButton("Add New Entry", PRIMARY_COLOR);
        addNewButton.addActionListener(e -> addAccount());
        addNewButton.setMaximumSize(new Dimension(150, 40));
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(emptyLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(addNewButton);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Make all components centered horizontally
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        emptyPanel.add(centerPanel);
        
        detailsPanel.add(emptyPanel);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
    
    private void createAccountDetailsPanel() {
        detailsPanel.removeAll();
        
        // Form panel with better spacing
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Account Details");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Website field
        JLabel websiteLabel = new JLabel("Website / Service");
        websiteLabel.setFont(NORMAL_FONT);
        websiteLabel.setForeground(TEXT_COLOR);
        websiteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(websiteLabel);
        formPanel.add(Box.createVerticalStrut(5));
        
        websiteField = new JTextField();
        websiteField.setFont(NORMAL_FONT);
        websiteField.setBackground(CARD_COLOR);
        websiteField.setForeground(TEXT_COLOR);
        websiteField.setCaretColor(ACCENT_COLOR);
        websiteField.setMaximumSize(new Dimension(2000, 35));
        websiteField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(websiteField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username / Email");
        usernameLabel.setFont(NORMAL_FONT);
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        
        usernameField = new JTextField();
        usernameField.setFont(NORMAL_FONT);
        usernameField.setBackground(CARD_COLOR);
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setCaretColor(ACCENT_COLOR);
        usernameField.setMaximumSize(new Dimension(2000, 35));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Password field with controls
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(NORMAL_FONT);
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.setBackground(BACKGROUND_COLOR);
        passwordPanel.setMaximumSize(new Dimension(2000, 35));
        passwordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = new JPasswordField();
        passwordField.setFont(NORMAL_FONT);
        passwordField.setBackground(CARD_COLOR);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(ACCENT_COLOR);
        
        showPasswordCheckBox = new JCheckBox("Show");
        showPasswordCheckBox.setFont(SMALL_FONT);
        showPasswordCheckBox.setBackground(BACKGROUND_COLOR);
        showPasswordCheckBox.setForeground(TEXT_COLOR);
        showPasswordCheckBox.addActionListener(e -> togglePasswordVisibility());
        
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createHorizontalStrut(10));
        passwordPanel.add(showPasswordCheckBox);
        
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Password generation
        JPanel generatePanel = new JPanel();
        generatePanel.setLayout(new BoxLayout(generatePanel, BoxLayout.X_AXIS));
        generatePanel.setBackground(BACKGROUND_COLOR);
        generatePanel.setMaximumSize(new Dimension(2000, 35));
        generatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        generateButton = new JButton("Generate Password");
        generateButton.setFont(SMALL_FONT);
        generateButton.setBackground(ACCENT_COLOR);
        generateButton.setForeground(TEXT_COLOR);
        generateButton.addActionListener(e -> generatePassword());
        
        JLabel lengthLabel = new JLabel("Length:");
        lengthLabel.setFont(SMALL_FONT);
        lengthLabel.setForeground(TEXT_COLOR);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(16, 8, 64, 1);
        lengthSpinner = new JSpinner(spinnerModel);
        lengthSpinner.setMaximumSize(new Dimension(60, 30));
        lengthSpinner.setBackground(CARD_COLOR);
        lengthSpinner.setForeground(TEXT_COLOR);
        
        generatePanel.add(generateButton);
        generatePanel.add(Box.createHorizontalStrut(10));
        generatePanel.add(lengthLabel);
        generatePanel.add(Box.createHorizontalStrut(5));
        generatePanel.add(lengthSpinner);
        generatePanel.add(Box.createHorizontalGlue());
        
        formPanel.add(generatePanel);
        formPanel.add(Box.createVerticalStrut(30));
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton saveButton = createStyledButton("Save", PRIMARY_COLOR);
        saveButton.addActionListener(e -> updateAccount());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalGlue());
        
        formPanel.add(buttonPanel);
        
        // Add form panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);
        
        detailsPanel.add(scrollPane);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
    
    private JButton createToolbarButton(String text, String iconName) {
        JButton button = new JButton(text);
        button.setForeground(TEXT_COLOR);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(SMALL_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private ImageIcon createLockIcon(int size) {
        // Upgraded lock icon with lila/pink colors
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw lock body with gradient
        GradientPaint gradient = new GradientPaint(
            size/4, size/3, PRIMARY_COLOR,
            size*3/4, size*5/6, ACCENT_COLOR);
        g2d.setPaint(gradient);
        g2d.fillRoundRect(size/4, size/3, size/2, size/2, size/8, size/8);
        
        // Draw lock shackle
        g2d.setStroke(new BasicStroke(size/8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(ACCENT_COLOR);
        g2d.drawArc(size/4, size/8, size/2, size/3, 0, 180);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private void showWelcomePanel() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "welcome");
        // Stop auto-lock timer if it's running
        if (autoLockTimer != null && autoLockTimer.isRunning()) {
            autoLockTimer.stop();
        }
    }
    
    private void showContentPanel() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "content");
        refreshAccountList();
        resetAutoLockTimer();
    }
    
    private void createNewDatabase() {
        // If already logged in, ask to save current database
        if (manager != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the current database before creating a new one?",
                    "Save Current Database", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (result == JOptionPane.YES_OPTION) {
                saveDatabase();
            }
        }
        
        // Create file chooser for new database
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Create New Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("LockBox Database (*." + FILE_EXTENSION + ")", FILE_EXTENSION));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // Add extension if not present
            if (!filePath.toLowerCase().endsWith("." + FILE_EXTENSION)) {
                filePath += "." + FILE_EXTENSION;
            }
            
            // Ask for master password
            JPasswordField masterPassField = new JPasswordField(20);
            JPasswordField confirmPassField = new JPasswordField(20);
            
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.setBackground(BACKGROUND_COLOR);
            
            JLabel titleLabel = new JLabel("Create a strong master password:");
            titleLabel.setForeground(TEXT_COLOR);
            panel.add(titleLabel);
            panel.add(masterPassField);
            
            JLabel confirmLabel = new JLabel("Confirm password:");
            confirmLabel.setForeground(TEXT_COLOR);
            panel.add(confirmLabel);
            panel.add(confirmPassField);
            
            // Update the UI components to match theme
            UIManager.put("OptionPane.background", BACKGROUND_COLOR);
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
            
            int result = JOptionPane.showConfirmDialog(this, panel, 
                    "New Database - Set Master Password", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String masterPassword = new String(masterPassField.getPassword());
                String confirmPassword = new String(confirmPassField.getPassword());
                
                if (masterPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                            "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!masterPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, 
                            "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    // Create new manager with the password
                    manager = new PasswordManager(masterPassword);
                    currentFileName = filePath;
                    
                    // Save the empty database
                    manager.saveToFile(currentFileName);
                    
                    // Add to recent files
                    addRecentFile(currentFileName);
                    
                    showContentPanel();
                    setStatus("Created new database: " + new File(currentFileName).getName());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                            "Error creating database: " + e.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void openExistingDatabase() {
        // If already logged in, ask to save current database
        if (manager != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the current database before opening another one?",
                    "Save Current Database", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (result == JOptionPane.YES_OPTION) {
                saveDatabase();
            }
        }
        
        // Create file chooser for opening database
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("LockBox Database (*." + FILE_EXTENSION + ")", FILE_EXTENSION));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            openDatabase(selectedFile.getAbsolutePath());
        }
    }
    
    private void openDatabase(String filePath) {
        // Ask for master password
        JPasswordField masterPassField = new JPasswordField(20);
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Enter master password for:");
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel);
        
        JLabel filenameLabel = new JLabel(new File(filePath).getName());
        filenameLabel.setForeground(TEXT_COLOR);
        panel.add(filenameLabel);
        
        panel.add(masterPassField);
        
        // Update the UI components to match theme
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Open Database", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String masterPassword = new String(masterPassField.getPassword());
            
            if (masterPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Create new manager with the password
                manager = new PasswordManager(masterPassword);
                
                // Try to load the database
                manager.loadFromFile(filePath);
                
                currentFileName = filePath;
                
                // Add to recent files
                addRecentFile(currentFileName);
                
                showContentPanel();
                setStatus("Opened database: " + new File(currentFileName).getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "Error opening database: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveDatabase() {
        if (manager == null) {
            return;
        }
        
        if (currentFileName == null) {
            saveDatabaseAs();
            return;
        }
        
        try {
            manager.saveToFile(currentFileName);
            setStatus("Saved database: " + new File(currentFileName).getName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error saving database: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveDatabaseAs() {
        if (manager == null) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Database As");
        fileChooser.setFileFilter(new FileNameExtensionFilter("LockBox Database (*." + FILE_EXTENSION + ")", FILE_EXTENSION));
        
        if (currentFileName != null) {
            fileChooser.setSelectedFile(new File(currentFileName));
        }
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // Add extension if not present
            if (!filePath.toLowerCase().endsWith("." + FILE_EXTENSION)) {
                filePath += "." + FILE_EXTENSION;
            }
            
            // Check if file exists
            File file = new File(filePath);
            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                manager.saveToFile(filePath);
                currentFileName = filePath;
                
                // Add to recent files
                addRecentFile(currentFileName);
                
                setStatus("Saved database as: " + new File(currentFileName).getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "Error saving database: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void logout() {
        // If changes are unsaved, ask to save
        if (manager != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the database before locking?",
                    "Save Database", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (result == JOptionPane.YES_OPTION) {
                saveDatabase();
            }
        }
        
        accountListModel.clear();
        if (detailsPanel != null) {
            createEmptyDetailsState();
        }
        manager = null;
        
        if (autoLockTimer.isRunning()) {
            autoLockTimer.stop();
        }
        
        showWelcomePanel();
    }
    
    private void exitApplication() {
        // If changes are unsaved, ask to save
        if (manager != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save the database before exiting?",
                    "Save Database", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (result == JOptionPane.YES_OPTION) {
                saveDatabase();
            }
        }
        
        System.exit(0);
    }
    
    private void changeMasterPassword() {
        if (manager == null) {
            return;
        }
        
        JPasswordField currentPassField = new JPasswordField(20);
        JPasswordField newPassField = new JPasswordField(20);
        JPasswordField confirmPassField = new JPasswordField(20);
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel currentLabel = new JLabel("Current master password:");
        currentLabel.setForeground(TEXT_COLOR);
        
        JLabel newLabel = new JLabel("New master password:");
        newLabel.setForeground(TEXT_COLOR);
        
        JLabel confirmLabel = new JLabel("Confirm new password:");
        confirmLabel.setForeground(TEXT_COLOR);
        
        panel.add(currentLabel);
        panel.add(currentPassField);
        panel.add(newLabel);
        panel.add(newPassField);
        panel.add(confirmLabel);
        panel.add(confirmPassField);
        
        // Update the UI components to match theme
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Change Master Password", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String currentPassword = new String(currentPassField.getPassword());
            String newPassword = new String(newPassField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                        "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verify current password by attempting to create a CryptoUtils instance
                CryptoUtils testCrypto = new CryptoUtils(currentPassword);
                
                // Create a new manager with the new password
                List<Account> accounts = manager.getAccounts();
                manager = new PasswordManager(newPassword);
                
                // Add all accounts to the new manager
                for (Account account : accounts) {
                    manager.addAccount(account);
                }
                
                JOptionPane.showMessageDialog(this, 
                        "Master password changed successfully.\nMake sure to save the database with the new password.", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                
                setStatus("Master password changed");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "Current password is incorrect or an error occurred.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showSettings() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel autoLockLabel = new JLabel("Auto-lock after (minutes):");
        autoLockLabel.setForeground(TEXT_COLOR);
        
        SpinnerNumberModel autoLockModel = new SpinnerNumberModel(autoLockMinutes, 1, 60, 1);
        JSpinner autoLockSpinner = new JSpinner(autoLockModel);
        autoLockSpinner.setBackground(CARD_COLOR);
        autoLockSpinner.setForeground(TEXT_COLOR);
        
        panel.add(autoLockLabel);
        panel.add(autoLockSpinner);
        
        // Update the UI components to match theme
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Settings", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            autoLockMinutes = (Integer) autoLockSpinner.getValue();
            autoLockTimer.setInitialDelay(autoLockMinutes * 60 * 1000);
            
            if (manager != null) {
                resetAutoLockTimer();
            }
            
            setStatus("Settings updated");
        }
    }
    
    private void showAbout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel logoLabel = new JLabel(createLockIcon(64));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("LockBox Password Manager");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("Version 1.0");
        versionLabel.setFont(NORMAL_FONT);
        versionLabel.setForeground(TEXT_COLOR);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descriptionLabel = new JLabel("<html><center>A secure password manager<br>for storing and organizing your credentials.</center></html>");
        descriptionLabel.setFont(NORMAL_FONT);
        descriptionLabel.setForeground(TEXT_COLOR);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel authorLabel = new JLabel(APP_AUTHOR);
        authorLabel.setFont(NORMAL_FONT);
        authorLabel.setForeground(ACCENT_COLOR);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(versionLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(descriptionLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(authorLabel);
        
        // Update the UI components to match theme
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        
        JOptionPane.showMessageDialog(this, panel, "About LockBox", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void refreshAccountList() {
        if (manager == null) {
            return;
        }
        
        String searchText = searchField.getText().toLowerCase();
        accountListModel.clear();
        
        for (Account account : manager.getAccounts()) {
            if (searchText.isEmpty() || 
                account.getWebsite().toLowerCase().contains(searchText) ||
                account.getUsername().toLowerCase().contains(searchText)) {
                accountListModel.addElement(account);
            }
        }
        
        if (accountListModel.isEmpty()) {
            createEmptyDetailsState();
        } else {
            accountList.setSelectedIndex(0);
        }
    }
    
    private void filterAccounts() {
        refreshAccountList();
    }
    
    private void displaySelectedAccount() {
        Account selectedAccount = accountList.getSelectedValue();
        
        if (selectedAccount != null) {
            createAccountDetailsPanel();
            websiteField.setText(selectedAccount.getWebsite());
            usernameField.setText(selectedAccount.getUsername());
            passwordField.setText(selectedAccount.getPassword());
            showPasswordCheckBox.setSelected(false);
            togglePasswordVisibility();
        } else {
            createEmptyDetailsState();
        }
    }
    
    private void addAccount() {
        createAccountDetailsPanel();
        accountList.clearSelection();
        websiteField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheckBox.setSelected(false);
        togglePasswordVisibility();
        websiteField.requestFocusInWindow();
    }
    
    private void deleteAccount() {
        int selectedIndex = accountList.getSelectedIndex();
        if (selectedIndex != -1) {
            Account selectedAccount = accountListModel.getElementAt(selectedIndex);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the account for " + selectedAccount.getWebsite() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                manager.getAccounts().remove(selectedIndex);
                refreshAccountList();
                setStatus("Account deleted");
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select an account to delete", 
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateAccount() {
        String website = websiteField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int selectedIndex = accountList.getSelectedIndex();
        Account account = new Account(website, username, password);
        
        if (selectedIndex != -1) {
            // Update existing account
            manager.getAccounts().set(selectedIndex, account);
            setStatus("Account updated");
        } else {
            // Add new account
            manager.addAccount(account);
            setStatus("New account added");
        }
        
        refreshAccountList();
        
        // Find and select the new/updated account
        for (int i = 0; i < accountListModel.getSize(); i++) {
            if (accountListModel.getElementAt(i).getWebsite().equals(website)) {
                accountList.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void generatePassword() {
        int length = (Integer) lengthSpinner.getValue();
        String password = manager.generatePassword(length);
        passwordField.setText(password);
        
        // Copy to clipboard
        StringSelection selection = new StringSelection(password);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        setStatus("Password generated and copied to clipboard");
    }
    
    private void copyUsername() {
        Account selectedAccount = accountList.getSelectedValue();
        if (selectedAccount != null) {
            StringSelection selection = new StringSelection(selectedAccount.getUsername());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            setStatus("Username copied to clipboard");
        }
    }
    
    private void copyPassword() {
        Account selectedAccount = accountList.getSelectedValue();
        if (selectedAccount != null) {
            StringSelection selection = new StringSelection(selectedAccount.getPassword());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            setStatus("Password copied to clipboard");
        }
    }
    
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }
    
    private void setStatus(String message) {
        statusLabel.setText(message);
    }
    
    private String[] getRecentFiles() {
        String recentFilesStr = prefs.get("recentFiles", "");
        if (recentFilesStr.isEmpty()) {
            return new String[0];
        }
        return recentFilesStr.split("\\|");
    }
    
    private void addRecentFile(String filePath) {
        // Ensure the file exists before adding to recent files
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        
        // Load existing recent files
        String[] recentFiles = getRecentFiles();
        StringBuilder newRecentFiles = new StringBuilder(filePath);
        
        // Add up to 4 previous recent files, skipping the current one if it's already in the list
        int count = 0;
        for (String existingFile : recentFiles) {
            if (existingFile != null && !existingFile.isEmpty() && !existingFile.equals(filePath) && count < 4) {
                File f = new File(existingFile);
                if (f.exists()) {
                    newRecentFiles.append("|").append(existingFile);
                    count++;
                }
            }
        }
        
        // Save the updated recent files list
        prefs.put("recentFiles", newRecentFiles.toString());
    }
    
    // Custom cell renderer for the account list
    class AccountListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Account) {
                Account account = (Account) value;
                label.setText("<html><b>" + account.getWebsite() + "</b><br>" +
                             "<font size='2' color='#b4b4b4'>" + account.getUsername() + "</font></html>");
                label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                
                if (!isSelected) {
                    label.setBackground(CARD_COLOR);
                    label.setForeground(TEXT_COLOR);
                }
            }
            
            return label;
        }
    }
    
    public static void main(String[] args) {
        try {
            // Use system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Enable dark mode on macOS (if supported)
            System.setProperty("apple.awt.application.appearance", "system");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LockBox());
    }
}