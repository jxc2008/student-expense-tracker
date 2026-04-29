import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManager {

    private List<Expense> expenses;

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
    }

    public void addExpense(Expense expense) {
        validate(expense);
        expenses.add(expense);
    }

    public void updateExpense(int index, Expense expense) {
        validate(expense);
        expenses.set(index, expense);
    }

    public void deleteExpense(int index) {
        expenses.remove(index);
    }

    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public Expense getExpense(int index) {
        return expenses.get(index);
    }

    public List<Expense> filterByCategory(Category category) {
        List<Expense> result = new ArrayList<>();
        if (category == null) {
            return result;
        }
        String target = category.getName();
        for (Expense e : expenses) {
            Category c = e.getCategory();
            if (c != null && c.getName().equals(target)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Expense> filterByDate(LocalDate date) {
        List<Expense> result = new ArrayList<>();
        if (date == null) {
            return result;
        }
        for (Expense e : expenses) {
            if (date.equals(e.getDate())) {
                result.add(e);
            }
        }
        return result;
    }

    public Map<Category, Double> getMonthlySummary(int month, int year) {
        Map<String, Category> categoryByName = new LinkedHashMap<>();
        Map<String, Double> totalsByName = new LinkedHashMap<>();

        for (Expense e : expenses) {
            LocalDate d = e.getDate();
            if (d == null || d.getMonthValue() != month || d.getYear() != year) {
                continue;
            }
            Category c = e.getCategory();
            if (c == null) {
                continue;
            }
            String name = c.getName();
            categoryByName.putIfAbsent(name, c);
            totalsByName.merge(name, e.getAmount(), Double::sum);
        }

        Map<Category, Double> summary = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : totalsByName.entrySet()) {
            summary.put(categoryByName.get(entry.getKey()), entry.getValue());
        }
        return summary;
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Expense e : expenses) {
            total += e.getAmount();
        }
        return total;
    }

    private static void validate(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        if (expense.getAmount() < 0) {
            throw new IllegalArgumentException("Expense amount cannot be negative");
        }
    }
}
