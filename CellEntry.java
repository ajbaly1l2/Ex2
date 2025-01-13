package assignments.ex2;
// Add your documentation below:

public class CellEntry  implements Index2D {

    private String index;

    public CellEntry(String index) {
        this.index = index;
    }

    @Override
    public boolean isValid() {
        if (index == null || index.length() < 2) {
            return false;
        }

        char column = Character.toUpperCase(index.charAt(0));
        if (column < 'A' || column > 'Z') {
            return false;
        }

        try {
            int row = Integer.parseInt(index.substring(1));
            return row >= 0 && row <= 99;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getX() {
        if(isValid()){
            return Character.toUpperCase(index.charAt(0)) - 65;
        } else {
            return Ex2Utils.ERR;
        }
    }

    @Override
    public int getY() {
        if(isValid()){
            return Integer.parseInt(index.substring(1));
        } else {
            return Ex2Utils.ERR;
        }
    }

    @Override
    public String toString(){
        return index;
    }

}
