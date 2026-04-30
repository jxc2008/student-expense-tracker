import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExpenseStorage {

    private static final String HEADER = "date,amount,description,categoryName,r,g,b";

    private ExpenseStorage() {}

    public static void save(List<Expense> expenses, Path file) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp)) {
            w.write(HEADER);
            w.newLine();
            for (Expense e : expenses) {
                Category c = e.getCategory();
                Color color = c.getColor();
                StringBuilder row = new StringBuilder();
                row.append(e.getDate().toString()).append(',');
                row.append(e.getAmount()).append(',');
                row.append(escape(e.getDescription())).append(',');
                row.append(escape(c.getName())).append(',');
                row.append(color.getRed()).append(',');
                row.append(color.getGreen()).append(',');
                row.append(color.getBlue());
                w.write(row.toString());
                w.newLine();
            }
        }
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailed) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static List<Expense> load(Path file) throws IOException {
        List<Expense> result = new ArrayList<>();
        Map<String, Category> registry = new LinkedHashMap<>();
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String header = r.readLine();
            if (header == null) {
                return result;
            }
            if (!header.equals(HEADER)) {
                throw new IOException("Unexpected CSV header: " + header);
            }
            String line;
            int lineNo = 1;
            StringBuilder pending = new StringBuilder();
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (pending.length() > 0) {
                    pending.append('\n').append(line);
                } else {
                    pending.append(line);
                }
                if (countUnescapedQuotes(pending) % 2 != 0) {
                    continue;
                }
                List<String> fields = parseRow(pending.toString(), lineNo);
                pending.setLength(0);
                if (fields.size() != 7) {
                    throw new IOException("Row " + lineNo + " has " + fields.size() + " fields, expected 7");
                }
                LocalDate date = LocalDate.parse(fields.get(0));
                double amount = Double.parseDouble(fields.get(1));
                String description = fields.get(2);
                String categoryName = fields.get(3);
                int rr = Integer.parseInt(fields.get(4));
                int gg = Integer.parseInt(fields.get(5));
                int bb = Integer.parseInt(fields.get(6));
                Category canonical = registry.computeIfAbsent(
                        categoryName, n -> new Category(n, new Color(rr, gg, bb)));
                result.add(new Expense(amount, date, description, canonical));
            }
            if (pending.length() > 0) {
                throw new IOException("Unterminated quoted field at end of file");
            }
        }
        return result;
    }

    public static List<Expense> loadOrEmpty(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return load(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        boolean needsQuotes = s.indexOf(',') >= 0 || s.indexOf('"') >= 0
                || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseRow(String row, int lineNo) throws IOException {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < row.length()) {
            char c = row.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < row.length() && row.charAt(i + 1) == '"') {
                        field.append('"');
                        i += 2;
                        continue;
                    }
                    inQuotes = false;
                    i++;
                } else {
                    field.append(c);
                    i++;
                }
            } else {
                if (c == ',') {
                    out.add(field.toString());
                    field.setLength(0);
                    i++;
                } else if (c == '"' && field.length() == 0) {
                    inQuotes = true;
                    i++;
                } else {
                    field.append(c);
                    i++;
                }
            }
        }
        if (inQuotes) {
            throw new IOException("Row " + lineNo + " has unterminated quoted field");
        }
        out.add(field.toString());
        return out;
    }

    private static int countUnescapedQuotes(CharSequence s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }
}
