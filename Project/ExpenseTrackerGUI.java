import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class ExpenseTrackerGUI extends JFrame {

    private ExpenseManager manager;

    private JTextField amountField;
    private JTextField dateField;
    private JTextField descriptionField;
    private JComboBox<Category> categoryBox;

    private JButton addButton;
    private JButton deleteButton;
    private JButton viewButton;
    private JButton summaryButton;

    private JTextArea outputArea;

    private static final Category[] DEFAULT_CATEGORIES = {
        new Category("Food",           new Color(255, 200,  200)),
        new Category("Rent",           new Color(180,  200, 240)),
        new Category("Transportation", new Color(180,  230, 180)),
        new Category("Entertainment",  new Color(255, 230, 180)),
        new Category("Shopping",       new Color(220, 190,  240)),
        new Category("Other",          new Color(210, 210, 210))
    };

    public ExpenseTrackerGUI(ExpenseManager manager) {
        this.manager = manager;
        initUI();
    }

    public void initUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Personal Expense Tracker", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(40, 60, 100));
        title.setBorder(new EmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = buildInputPanel();
        inputPanel.setPreferredSize(new Dimension(280, 0));
        add(inputPanel, BorderLayout.WEST);

        JPanel outputPanel = buildOutputPanel();
        add(outputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 5),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230)),
                "Add / Edit Expense",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(60, 80, 140)
            )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(8, 8, 4, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(makeLabel("Amount ($)"), gbc);
        gbc.gridy = 1;
        amountField = makeTextField("e.g. 12.50");
        panel.add(amountField, gbc);

        gbc.gridy = 2;
        panel.add(makeLabel("Date (YYYY-MM-DD)"), gbc);
        gbc.gridy = 3;
        dateField = makeTextField(LocalDate.now().toString());
        panel.add(dateField, gbc);

        gbc.gridy = 4;
        panel.add(makeLabel("Category"), gbc);
        gbc.gridy = 5;
        categoryBox = new JComboBox<>(DEFAULT_CATEGORIES);
        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryBox.setRenderer(new CategoryRenderer());
        panel.add(categoryBox, gbc);

        gbc.gridy = 6;
        panel.add(makeLabel("Description"), gbc);
        gbc.gridy = 7;
        descriptionField = makeTextField("e.g. Lunch at cafe");
        panel.add(descriptionField, gbc);

        gbc.gridy = 8;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setBackground(new Color(250, 252, 255));
        outputArea.setForeground(new Color(30, 40, 60));
        outputArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setBackground(new Color(235, 240, 250));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 230)));

        addButton     = makeButton("Add",     new Color(60, 140, 80));
        deleteButton  = makeButton("Delete",  new Color(200, 60, 60));
        viewButton    = makeButton("View All", new Color(60, 100, 180));
        summaryButton = makeButton("Summary", new Color(140, 80, 180));

        addButton    .addActionListener(e -> addExpenseAction());
        deleteButton .addActionListener(e -> deleteExpenseAction());
        viewButton   .addActionListener(e -> viewExpensesAction());
        summaryButton.addActionListener(e -> showSummary());

        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(viewButton);
        panel.add(summaryButton);
        return panel;
    }

    public void addExpenseAction() {
        try {
            double      amount      = Double.parseDouble(amountField.getText().trim());
            LocalDate   date        = LocalDate.parse(dateField.getText().trim());
            String      description = descriptionField.getText().trim();
            Category    category    = (Category) categoryBox.getSelectedItem();

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a description.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Expense expense = new Expense(amount, date, description, category);
            manager.addExpense(expense);
            outputArea.setText("Expense added:\n" + expense);
            clearInputs();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter date in YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteExpenseAction() {
        List<Expense> all = manager.getAllExpenses();
        if (all.isEmpty()) {
            outputArea.setText("No expenses to delete.");
            return;
        }

        StringBuilder sb = new StringBuilder("Current expenses:\n\n");
        for (int i = 0; i < all.size(); i++) {
            sb.append(i).append(". ").append(all.get(i)).append("\n");
        }
        sb.append("\nEnter the number to delete (starting from 0):");
        outputArea.setText(sb.toString());

        String input = JOptionPane.showInputDialog(this, "Enter number to delete (starting from 0):");
        if (input == null) return;
        try {
            int index = Integer.parseInt(input.trim());
            Expense removed = all.get(index);
            manager.deleteExpense(index);
            outputArea.setText("Deleted:\n" + removed);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void viewExpensesAction() {
        List<Expense> all = manager.getAllExpenses();
        if (all.isEmpty()) {
            outputArea.setText("No expenses recorded.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-12s %-12s %-20s %s%n",
                  "No.", "Date", "Amount", "Category", "Description"));
        sb.append("─".repeat(65)).append("\n");

        for (int i = 0; i < all.size(); i++) {
            Expense e = all.get(i);
            sb.append(String.format("%-5d %-12s $%-11.2f %-20s %s%n",
                i,
                e.getDate(),
                e.getAmount(),
                e.getCategory().getName(),
                e.getDescription()
            ));
        }

        sb.append("─".repeat(65)).append("\n");
        sb.append(String.format("Total Spent: $%.2f%n", manager.getTotalSpent()));
        outputArea.setText(sb.toString());
    }

    public void showSummary() {
        LocalDate now   = LocalDate.now();
        int month = now.getMonthValue();
        int year  = now.getYear();

        Map<Category, Double> summary = manager.getMonthlySummary(month, year);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Monthly Summary: %d/%d%n", month, year));
        sb.append("─".repeat(40)).append("\n");

        if (summary.isEmpty()) {
            sb.append("No expenses this month.\n");
        } else {
            double total = 0;
            for (Map.Entry<Category, Double> entry : summary.entrySet()) {
                sb.append(String.format("%-20s $%.2f%n",
                    entry.getKey().getName(), entry.getValue()));
                total += entry.getValue();
            }
            sb.append("─".repeat(40)).append("\n");
            sb.append(String.format("%-20s $%.2f%n", "Total", total));
        }

        outputArea.setText(sb.toString());
    }

    public void clearInputs() {
        amountField     .setText("");
        dateField       .setText(LocalDate.now().toString());
        descriptionField.setText("");
        categoryBox     .setSelectedIndex(0);
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(60, 80, 120));
        return label;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 195, 220)),
            new EmptyBorder(4, 6, 4, 6)
        ));
        return field;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private static class CategoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Category) {
                Category cat = (Category) value;
                setText(cat.getName());
                if (!isSelected) {
                    setBackground(cat.getColor());
                }
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseManager manager = new ExpenseManager();
            new ExpenseTrackerGUI(manager);
        });
    }
}
