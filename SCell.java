package assignments.ex2;


public class SCell implements Cell {
    private String data;
    private int type;
    private int order;


    public SCell(String data) {
        setData(data);
        this.type = determineType(data);
    }

    private int determineType(String s) {
        if (s == null || s.isEmpty()) {
            return Ex2Utils.TEXT;
        } else if (s.startsWith("=")) {
            return Ex2Utils.FORM;
        } else {
            try {
                Double.parseDouble(s);
                return Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                return Ex2Utils.TEXT;
            }
        }
    }


    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;

    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return data != null ? data : Ex2Utils.EMPTY_CELL;
    }
}
