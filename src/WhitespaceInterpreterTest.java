import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class WhitespaceInterpreterTest {

	@Test
	public void testIgnoreExtraneousCharacters() {
		assertEquals("ssstntnstnnn", WhitespaceInterpreter.reduceCode("A89Bcx   \t\n\t\n901 \t\n\np-.98@\nZss%^u"));
	}

	@Test
	public void testPush() {
		String[][] tests = { { "   \t\n\t\n \t\n\n\n", "1" }, { "   \t \n\t\n \t\n\n\n", "2" },
				{ "   \t\t\n\t\n \t\n\n\n", "3" }, { "    \n\t\n \t\n\n\n", "0" } };
		for (String[] test : tests) {
			assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
		}
	}

	@Test
	public void testOutNumbers() {
		String[][] tests = { { "  \t\t\n\t\n \t\n\n\n", "-1" }, { "  \t\t \n\t\n \t\n\n\n", "-2" },
				{ "  \t\t\t\n\t\n \t\n\n\n", "-3" }, };
		for (String[] test : tests) {
			assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
		}
	}

	@Test(expected = Exception.class)
	public void testFlowEdge() {
		WhitespaceInterpreter.execute("", null);
	}

	@Test
	public void testOutputLettersAtoC() {
		String[][] tests = { { "   \t     \t\n\t\n  \n\n\n", "A" }, { "   \t    \t \n\t\n  \n\n\n", "B" },
				{ "   \t    \t\t\n\t\n  \n\n\n", "C" }, };
		for (String[] test : tests) {
			assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
		}
	}

	@Test
	public void testOutLettersWithComments() {
		String[][] tests = { { "blahhhh   \targgggghhh     \t\n\t\n  \n\n\n", "A" },
				{ " I heart \t  cats  \t \n\t\n  \n\n\n", "B" },
				{ "   \t  welcome  \t\t\n\t\n to the\nnew\nworld\n", "C" }, };
		for (String[] test : tests) {
			assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
		}
	}

	@Test
	public void testStack() {
		String[][] tests = { { "   \t\t\n   \t\t\n\t\n \t\t\n \t\n\n\n", "33" },
				{ "   \t\t\n \n \t\n \t\t\n \t\n\n\n", "33" },
				{ "   \t\n   \t \n   \t\t\n \t  \t \n\t\n \t\n\n\n", "1" },
				{ "   \t\n   \t \n   \t\t\n \t  \t\n\t\n \t\n\n\n", "2" },
				{ "   \t\n   \t \n   \t\t\n \t   \n\t\n \t\n\n\n", "3" },
				{ "   \t\t\n   \t \n \n\t\t\n \t\t\n \t\n\n\n", "32" },
				{ "   \t\t\n   \t \n \n\t \n\n\t\n \t\n\n\n", "2" },
				{ "   \t\t\n   \t \n   \t\n   \t  \n   \t\t \n   \t \t\n   \t\t\t\n \n\t \t\n \t\t\n\t\n \t\t\n \t\t\n \t\t\n \t\n\n\n",
						"5123" }, };
		for (String[] test : tests) {
			assertEquals(test[1], WhitespaceInterpreter.execute(test[0], null));
		}
	}
	
	@Test
	public void given10_whenAddedTo3_thenYields13() {
		assertEquals("13", WhitespaceInterpreter.execute("   \t \t \n   \t\t\n\t   \t\n \t\n\n\n", null));
	}

	@Test
	public void given10_when3IsSubtracted_thenYields7() {
		assertEquals("7", WhitespaceInterpreter. execute("   \t\t\n   \t \t \n\t  \t\t\n \t\n\n\n", null));
	}
	
	@Test
	public void given10_whenMultipledBy3_thenYields30() {
		assertEquals("30", WhitespaceInterpreter. execute("   \t\t\n   \t \t \n\t  \n\t\n \t\n\n\n", null));
	}
	
	@Test
	public void given10_whenDividedBy2_thenYields5() {
		assertEquals("5", WhitespaceInterpreter. execute("   \t \n   \t \t \n\t \t \t\n \t\n\n\n", null));
	}
	
	@Test
	public void given10_whenModuloDividedBy3_thenYields1() {
		assertEquals("1", WhitespaceInterpreter. execute("   \t\t\n   \t \t \n\t \t\t\t\n \t\n\n\n", null));
	}
}
