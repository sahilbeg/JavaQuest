package jq.m12.feature;

/** A data-quality check found a problem, so the month was rolled back. check() names the check that failed. */
public class DataQualityException extends StatementJobException {

    private final String check;

    public DataQualityException(String check, String detail) {
        super("Data quality check failed: " + check + " (" + detail + ")", null);
        this.check = check;
    }

    public String check() {
        return check;
    }
}
