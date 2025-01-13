public class Cell {
    private String content;

    public Cell(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isNumber(String text) {
        return true;
    }

    public boolean isText(String text) {
        return true;
    }

    public boolean isForm(String text) {
        return true;
    }

    public boolean isValidFormula(String text) {
        return true;
    }

    public boolean hasCycle(int x, int y, boolean[][] visited){
        return true;
    }

    public Double computeForm(String form) {
        return 1.0;
    }



}