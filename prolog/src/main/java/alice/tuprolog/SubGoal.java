package alice.tuprolog;

/**
 * Identifier of single subGoal during the demo.
 *
 * @author Alex Benini
 */
public record SubGoal(SubGoal parent, SubGoalTree root, int index) {


    public String toString() {
        return root.get(index).toString();
    }

}