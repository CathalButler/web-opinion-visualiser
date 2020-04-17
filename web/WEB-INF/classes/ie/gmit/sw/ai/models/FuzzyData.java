package ie.gmit.sw.ai.models;

/**
 * Class which is used for creating Fuzzy data objects
 *
 * @author Cathal Butler
 */

public class FuzzyData {
    // === M e m b e r V a r i a b l e s =============================
    private int title;
    private int h1;
    private int para;


    public FuzzyData(int title, int h1, int para) {
        this.title = title;
        this.h1 = h1;
        this.para = para;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getH1() {
        return h1;
    }

    public void setH1(int h1) {
        this.h1 = h1;
    }

    public int getPara() {
        return para;
    }

    public void setPara(int para) {
        this.para = para;
    }
}
