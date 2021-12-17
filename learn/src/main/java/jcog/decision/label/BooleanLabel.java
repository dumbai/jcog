package jcog.decision.label;

/**
 * Simplest possible label. Simply labels data as true or false.
 *
 * @author Ignas
 */
@Deprecated public final class BooleanLabel  {

    public static final BooleanLabel TRUE_LABEL = BooleanLabel.newLabel(true);

    public static final BooleanLabel FALSE_LABEL = BooleanLabel.newLabel(false);

    /**
     * Label.
     */
    private final boolean label;

    /**
     * Constructor.
     */
    private BooleanLabel(boolean label) {
        super();
        this.label = label;
    }

    /**
     * Static factory method.
     */
    static BooleanLabel newLabel(Boolean label) {
        return new BooleanLabel(label);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (label ? 1231 : 1237);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BooleanLabel other = (BooleanLabel) obj;
        return label == other.label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(label);
        
    }

}
