package alice.tuprolog;

import alice.tuprolog.event.OutputEvent;
import alice.tuprolog.event.OutputListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;


@Disabled
class ISOIOPrologLibTestCase {

	private static Prolog engine;
	private String theory;
	private Solution info;
	private static String writePath;
	private static String readPath;
	private static String binPath;

	@BeforeAll
	static void initTest()
	{
		try
		{	
			engine = new Prolog("alice.tuprolog.lib.BasicLibrary",
					
					"alice.tuprolog.lib.ISOIOLibrary");

			File file = new File(".");
			writePath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "writeFile.txt";
			readPath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "readFile.txt";
			binPath = file.getCanonicalPath() 
					+ File.separator + "test"
					+ File.separator + "unit"
					+ File.separator + "binFile.bin";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	void test_open() throws MalformedGoalException, InvalidTheoryException
	{
		
		info = engine.solve("open('" + writePath +"','write',X,[alias('editor'), type(text)]).");
		assertTrue(info.isSuccess());

		
		info = engine.solve("open('" + writePath.replace(".txt", ".myext") +"','write',X,[alias('editor'), type(text)]).");
		assertFalse(info.isSuccess());

		
		info = engine.solve("open('" + writePath + "','read',[]).");
		assertFalse(info.isSuccess());

		
		info = engine.solve("open('" + writePath + "','read',X,X).");
		assertFalse(info.isSuccess());

		
		info = engine.solve("open('" + writePath + "','read',X,[ciao(caramelle)]).");
		assertFalse(info.isSuccess());

		
		String theoryText = "test:- open('" + writePath + "','write',X),close(X,force(true)).\n";
		engine.setTheory(new Theory(theoryText));
		info = engine.solve("test.");
		assertFalse(info.isSuccess());
	}

	@Test
	void test_2() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite = "B";
		String theory = "test2:-" +
				"open('" + writePath + "','write',X,[alias(ciao),type(text),eof_action(reset),reposition(true)])," +
				"write_term('ciao','" + dataToWrite + "',[quoted(true)])," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test2.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite, getStringDataWritten(writePath));
	}

	@Test
	void test_3() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite1 = "term.";
		String dataToWrite2 = "ciao.";
		String theory = "test3:- " +
				"open('" + readPath + "','write',X,[alias(ciao, computer, casa, auto),type(text),eof_action(reset),reposition(true)])," +
				"open('" + writePath + "','write',Y,[alias(telefono, rosa),type(text),eof_action(reset),reposition(true)])," +
				"write_term('telefono','" + dataToWrite1 + "',[quoted(true)])," +
				"write_term('auto','" + dataToWrite2 + "',[quoted(true)])," +
				"close(X)," +
				"close(Y).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test3.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite1, getStringDataWritten(writePath));
		assertEquals("", dataToWrite2, getStringDataWritten(readPath));
	}

	@Test
	void test_4() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		String dataToWrite = "term.";
		String theory = "test4:-" +
				"open('" + writePath + "','write',Y,[alias(telefono, casa),type(text),eof_action(reset),reposition(true)])," +
				"write_term('telefono','" + dataToWrite + "',[quoted(true)])," +
				"flush_output('casa')," +
				"close(Y).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test4.");
		assertTrue(info.isSuccess());
		assertEquals("", dataToWrite, getStringDataWritten(writePath));
	}

	@Test
	void test_5() throws InvalidTheoryException, MalformedGoalException {
		final String dataToRead = "ciao";
		
		
		OutputListener listener = e -> assertEquals("", dataToRead, () -> e.msg);

		engine.addOutputListener(listener);

		theory = "test5:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"read_term(X,I,[])," +
				"write('user_output', I)," +
				"close('reading').";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test5.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	void test_6() throws InvalidTheoryException, MalformedGoalException {
		String[] dataToRead = { "c", "\n", "iao" };
		
		
		OutputListener listener = new OutputListener() {

			int count;

			@Override
			public void onOutput(OutputEvent e) 
			{
                assertEquals("", dataToRead[count], () -> e.msg);
				count++;
			}
		};

		engine.addOutputListener(listener);

		theory = "test6:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"get_char('reading',M)," +
				"read_term(X,J,[])," +
				"write(M)," +
				"nl('user_output')," +
				"write(J)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test6.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	void test_7() throws InvalidTheoryException, MalformedGoalException {
		final String dataToRead = "c";
		
		
		OutputListener listener = e -> assertEquals("", dataToRead, () -> e.msg);

		engine.addOutputListener(listener);

		theory = "test7:- put_char('user_output',c).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test7.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}
	
	@Test
	void test_8() throws InvalidTheoryException, MalformedGoalException {
		final int dataToRead = 51;
		
		
		OutputListener listener = e -> assertEquals("", dataToRead+"", () -> e.msg);

		engine.addOutputListener(listener);

		theory = "test8:-" +
				"open('" + binPath + "','read',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," +
				"peek_byte('nome', PB)," +
				"write(PB)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test8.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}

	@Test
	void test_9() throws InvalidTheoryException, MalformedGoalException, IOException
	{
		int dataToWrite = 51;

		theory = "test9:-" +
				"open('" + binPath + "','write',X,[alias(readCode, nome),type(binary),eof_action(reset),reposition(true)])," +
				"put_byte('nome'," + dataToWrite + ")," +
				"flush_output('nome')," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test9.");
		assertTrue(info.isSuccess());
		assertEquals(dataToWrite, getByteDataWritten(binPath));
	}

	@Test
	void test_10() throws InvalidTheoryException, MalformedGoalException {
		int[] dataToRead = { 99, 105, 105 };

		
		
		OutputListener listener = new OutputListener() {

			int count;

			@Override
			public void onOutput(OutputEvent e) 
			{
                assertEquals(String.valueOf(dataToRead[count]), e.msg, "");
				count++;
			}
		};

		engine.addOutputListener(listener);

		theory = "test10:-" +
				"open('" + readPath + "','read',X,[alias(reading, nome),type(text),eof_action(reset),reposition(true)])," +
				"get_code('reading',M)," +
				"peek_code('nome',N)," +
				"peek_code(X,O)," +
				"write(M)," +
				"write(N)," +
				"write(O)," +
				"close(X).";
		engine.setTheory(new Theory(theory));
		info = engine.solve("test10.");
		assertTrue(info.isSuccess());

		engine.removeOutputListener(listener);
	}



	private static String getStringDataWritten(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String dataRead = reader.readLine();
		reader.close();
		return dataRead;
	}

	private static int getByteDataWritten(String filePath) throws IOException {
		FileInputStream fins = new FileInputStream(filePath);
		int dataRead = fins.read();
		fins.close();
		return dataRead;
	}
}