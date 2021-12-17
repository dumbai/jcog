package alice.tuprolog;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

class FamilyClausesListTest {


	@Test
    void test1() {
		FamilyClausesList clauseList = new FamilyClausesList();
		ClauseInfo first = new ClauseInfo(new Struct(new Struct("First"), new Struct("First")), "First Element");
		ClauseInfo second = new ClauseInfo(new Struct(new Struct("Second"), new Struct("Second")), "Second Element");
		ClauseInfo third = new ClauseInfo(new Struct(new Struct("Third"), new Struct("Third")), "Third Element");
		ClauseInfo fourth = new ClauseInfo(new Struct(new Struct("Fourth"), new Struct("Fourth")), "Fourth Element");

		clauseList.add(first);
		clauseList.add(second);
		clauseList.add(third);
		clauseList.add(fourth);


		Iterator<ClauseInfo> allClauses = clauseList.iterator();

		allClauses.next();


		allClauses.next();

		allClauses.next();


//        System.out.println("Ok!!!");
	}


}