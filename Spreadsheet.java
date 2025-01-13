import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spreadsheet {
    private Cell[][] cells;
    private int width;
    private int height;

    public Spreadsheet() {
    }

    public void Generate(int x, int y){

    }

    public Cell get(int x, int y) {
        return cells[x][y];
    }

    public void set(int x, int y, Cell c) {
        cells[x][y] = c;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int xCell(String cellRef) {
       return 1;
    }

    public int yCell(String cellRef) {
       return 1;
    }

    public String eval(int x, int y) {
        Cell cell = get(x, y);
        String content = cell.getContent();
        // Check for number or text
        if (cell.isNumber(content)) return content;
        if (cell.isText(content)) return content;
        // Check for valid formula
        if (cell.isForm(content)) {
            if (!cell.isValidFormula(content)) {
                return "ERR_WRONG_FORM"; // Invalid formula
            }
            // Check for cycle
            boolean[][] visited = new boolean[width][height];
            if (cell.hasCycle(x, y, visited)) {
                return "ERR_CYCLE"; // Cycle detected
            }
            try {
                return cell.computeForm(content).toString(); // Compute valid formula
            } catch (Exception e) {
                return "ERR"; // General error in formula computation
            }
        }

        return "ERR"; 
    }

    // Evaluate all cells and return a 2D array of their values
    public String[][] evalAll() {
        String[][] result = new String[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = eval(x, y);
            }
        }
        return result;
    }

    // Compute the computational depth of each cell
    public int[][] depth() {
        int[][] depths = new int[width][height];
        boolean[][] visited = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                try {
                    depths[x][y] = computeDepth(x, y, visited);
                } catch (StackOverflowError e) {
                    depths[x][y] = -1; // Cycle detected
                }
            }
        }
        return depths;
    }

    // Helper method to compute depth recursively
    private int computeDepth(int x, int y, boolean[][] visited) {
        if (visited[x][y]) throw new StackOverflowError(); // Cycle detected
        visited[x][y] = true;
        Cell cell = get(x, y);
        String content = cell.getContent();
        if (cell.isNumber(content) || cell.isText(content)) {
            visited[x][y] = false;
            return 0;
        }
        if (cell.isForm(content)) {
            String[] dependencies = extractDependencies(content);
            int maxDepth = 0;
            for (String dep : dependencies) {
                int depX = xCell(dep);
                int depY = yCell(dep);

                if (depX >= 0 && depX < width && depY >= 0 && depY < height) {
                    maxDepth = Math.max(maxDepth, computeDepth(depX, depY, visited));
                } else {
                    throw new IllegalArgumentException("Invalid cell reference in formula: " + dep);
                }
            }

            visited[x][y] = false;
            return 1 + maxDepth; // Depth of the current cell is 1 + max depth of dependenc
        }
        visited[x][y] = false;
        return 0;
    }

    private String[] extractDependencies(String form) {
        List<String> dependencies = new ArrayList<>();
        Pattern pattern = Pattern.compile("[A-Z]+\\d+");
        Matcher matcher = pattern.matcher(form);
        while (matcher.find()) {
            dependencies.add(matcher.group());
        }
        return dependencies.toArray(new String[0]);
    }
}