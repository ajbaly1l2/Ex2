package assignments.ex2;
import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    // Add your code here

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x,y);
        if(c!=null) {
            ans = eval(x, y);
        }
        return ans;
    }
    @Override
    public Cell get(int x, int y) {
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    @Override
    public Cell get(String cords) {
        CellEntry entry = new CellEntry(cords);
        if (entry.isValid()) {
            return get(entry.getX(), entry.getY());
        }
        return null;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
       if (isIn(x, y)) {
           table[x][y] = new SCell(s);
       }
    }

    @Override
    public void eval() {
        int [][] dd = depth();
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                table[x][y].setOrder(computeOrder(x, y)); // Set the order of each cell
                eval(x, y);
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && xx < width() && yy >= 0 && yy < height();
    }

    @Override
    public int[][] depth() {
        int w = width();
        int h = height();
        int[][] ans = new int[w][h];
        // Initialize each cell in ans to -1
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1;
            }
        }

        int depth = 0, count = 0, max = w * h;
        boolean flagC = true;

        while (count < max && flagC) {
            flagC = false;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if(table[x][y].getOrder() == -2) {
                        ans[x][y] = -2;
                    } else if (ans[x][y] == -1 && canBeComputedNow(x, y, ans)) {
                        ans[x][y] = depth;
                        count++;
                        flagC = true;
                    }
                }
            }
            depth++;
        }

        return ans;
    }

    /**
     * Helper method to check if a cell can be computed.
     * A cell can be computed if:
     * - It is not a formula.
     * - Its dependencies (if it is a formula) have been evaluated.
     */
    private boolean canBeComputedNow(int x, int y, int[][] ans) {
        Cell cell = get(x, y);
        if (cell == null || cell.getType() != Ex2Utils.FORM) {
            return true; // Non-formula cells can always be computed.
        }

        // Extract dependencies from the formula and check if they have been computed.
        String formula = cell.getData().substring(1); // Remove '='
        String[] dependencies = extractDependencies(formula);

        for (String dep : dependencies) {
            CellEntry entry = new CellEntry(dep);
            if (entry.isValid()) {
                int depX = entry.getX();
                int depY = entry.getY();
                if (!isIn(depX, depY)) {
                    return false; // Missing dependency
                } else if (ans[depX][depY] == -1) {
                    return false; // Dependency not computed yet
                }
            }
        }
        return true; // All dependencies have been computed.
    }

    private int computeOrder(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null || cell.getType() != Ex2Utils.FORM) {
            return 0; // Non-formula cells have order 0.
        }

        String formula = cell.getData().substring(1); // Skip '='
        String[] dependencies = extractDependencies(formula);
        int maxOrder = 0;

        for (String dep : dependencies) {
            CellEntry entry = new CellEntry(dep);
            if (entry.isValid()) {
                int depX = entry.getX();
                int depY = entry.getY();
                if (isIn(depX, depY)) {
                    Cell dependentCell = get(depX, depY);
                    if (dependentCell == null) {
                        return -1; // Invalid reference.
                    }
                    maxOrder = Math.max(maxOrder, dependentCell.getOrder());
                } else {
                    return -1; // Out-of-bounds reference.
                }
            }
        }

        return 1 + maxOrder;
    }


    /**
     * Helper method to extract dependencies from a formula.
     * This assumes dependencies are in the form of cell references (e.g., A1, B2).
     */
    private String[] extractDependencies(String formula) {
        return formula.split("[^A-Za-z0-9]"); // Split by non-alphanumeric characters.
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            reader.readLine(); // Skip the header
            for(int i=0;i<width();i=i+1) {
                for(int j=0;j<height();j=j+1) {
                    table[i][j].setData(Ex2Utils.EMPTY_CELL);
                }
            }
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    String data = parts[2].split(",")[0]; // Exclude remarks
                    set(x, y, data);
                }
            }
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("I2CS ArielU: SpreadSheet (Ex2) assignment - this line should be ignored in the load method");
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    String data = table[x][y].getData();
                    if (!data.isEmpty()) {
                        writer.printf("%d,%d,%s%n", x, y, data);
                    }
                }
            }
        }
    }


    public boolean checkCycle(int x, int y, Set<String> visited) {
        String cellKey = x + "," + y;
        if (visited.contains(cellKey)) {
            return true; // Cycle detected
        }
        Cell cell = get(x, y);
        if (cell == null || cell.getType() == Ex2Utils.NUMBER) {
            return false;
        }
        if (cell.getType() == Ex2Utils.TEXT) {
            return false;
        }
        visited.add(cellKey); // Mark the current cell as visited
        String formula = cell.getData().substring(1).toUpperCase(); // Remove '='
        String[] dependencies = extractDependencies(formula);

        for (String dep : dependencies) {
            CellEntry entry = new CellEntry(dep);

            if (!entry.isValid()) {
                continue; // Skip invalid dependencies
            }

            int depX = entry.getX();
            int depY = entry.getY();

            if (!isIn(depX, depY)) {
                continue;
            }

            if (checkCycle(depX, depY, visited)) {
                return true; // Cycle detected in the dependency
            }
        }
        visited.remove(cellKey); // Backtrack: remove cell from visited set
        return false; // No cycle detected
    }



    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) return Ex2Utils.EMPTY_CELL;
        if (checkCycle(x, y, new HashSet<>())) {
            cell.setType(Ex2Utils.ERR_CYCLE_FORM);
            return Ex2Utils.ERR_CYCLE;
        }
        if (cell.getType() == Ex2Utils.FORM || cell.getType() == Ex2Utils.ERR_FORM_FORMAT || cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
            try {
                String formula = cell.getData().substring(1).toUpperCase();
                String[] dependencies = extractDependencies(formula);
                for (String dep : dependencies) {
                    CellEntry entry = new CellEntry(dep);
                    if(!entry.isValid()){
                        continue;
                    }
                    if (formula.contains(entry.toString())) {
                        if(!isIn(entry.getX(), entry.getY())){
                            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                            cell.setOrder(-2);
                            return Ex2Utils.ERR_FORM;
                        }
                        if(cell.getType() != Ex2Utils.ERR_CYCLE_FORM) {
                            if(get(entry.getX(), entry.getY()).getType() == Ex2Utils.ERR_CYCLE_FORM){
                                cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                                return Ex2Utils.ERR_CYCLE;
                            } else if(get(entry.getX(), entry.getY()).getType() == Ex2Utils.ERR_FORM_FORMAT){
                                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                                return Ex2Utils.ERR_FORM;
                            }
                            formula = formula.replace(entry.toString(), eval(entry.getX(), entry.getY()));
                        } else {
                            return Ex2Utils.ERR_CYCLE;
                        }
                    }
                }
                cell.setType(Ex2Utils.FORM);
                return String.valueOf(evaluate(formula));
            } catch (Exception e) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
            }
        }
        if(cell.getType() == Ex2Utils.ERR_FORM_FORMAT){
            return Ex2Utils.ERR_FORM;
        } else if(cell.getType() == Ex2Utils.ERR_CYCLE_FORM){
            return Ex2Utils.ERR_CYCLE;
        } else {
            return cell.getData();
        }
    }

    // Method to check if a string represents a number
    public boolean isNumber(String a) {
        try {
            Double.parseDouble(a);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to evaluate a mathematical expression
    public double evaluate(String form) {
        if (!isValidForm(form)) {
            throw new IllegalArgumentException("Invalid expression format");
        }
        return evaluateExpression(form);
    }

    // Method to find the index of the main operator in the expression
    public int indOfMainOp(String form) {
        int balance = 0; // Track parentheses level
        int index = -1; // Index of the main operator

        for (int i = 0; i < form.length(); i++) {
            char c = form.charAt(i);
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
            } else if (balance == 0 && "+-*/".indexOf(c) >= 0) {
                index = i; // Main operator found
            }
        }
        return index;
    }

    // Method to check if the expression is valid
    public boolean isValidForm(String s) {
        if (s == null || s.isEmpty()) return false;

        int balance = 0; // Track parentheses balance
        boolean lastWasOperator = true;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) return false; // More closing parentheses than opening
            } else if ("+-*/".indexOf(c) >= 0) {
                if (lastWasOperator) return false; // Two consecutive operators
                lastWasOperator = true;
            } else if (Character.isDigit(c) || c == '.' || Character.isWhitespace(c)) {
                lastWasOperator = false;
            } else {
                return false; // Invalid character
            }
        }

        return balance == 0 && !lastWasOperator; // Balanced parentheses and no trailing operator
    }

    // Helper to evaluate expressions using recursion
    private double evaluateExpression(String form) {
        form = form.replaceAll("\\s+", ""); // Remove whitespace
        int index = indOfMainOp(form);

        // If no main operator, parse the number or handle parentheses
        if (index == -1) {
            if (form.startsWith("(") && form.endsWith(")")) {
                return evaluateExpression(form.substring(1, form.length() - 1));
            }
            return Double.parseDouble(form);
        }

        // Split expression into left and right parts
        char op = form.charAt(index);
        String left = form.substring(0, index);
        String right = form.substring(index + 1);

        // Evaluate based on the operator
        switch (op) {
            case '+': return evaluateExpression(left) + evaluateExpression(right);
            case '-': return evaluateExpression(left) - evaluateExpression(right);
            case '*': return evaluateExpression(left) * evaluateExpression(right);
            case '/': return evaluateExpression(left) / evaluateExpression(right);
            default: throw new IllegalArgumentException("Unexpected operator: " + op);
        }
    }

}
