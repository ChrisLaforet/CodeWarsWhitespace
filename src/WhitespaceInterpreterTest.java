import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
	
	@Test
	public void given10_whenStoredAtAddress3AndPushedBackOnStack_thenYields10() {
		assertEquals("10", WhitespaceInterpreter.execute("   \t\t\n   \t \t \n\t\t    \t\t\n\t\t\t\t\n \t\n\n\n", null));
	}

	@Test
	public void givenInputStream_whenReadingCharacters_thenOutputsCharactersInOrder() {
		final String characters = "B8";
		final InputStream inputStream = new ByteArrayInputStream(characters.getBytes());
		assertEquals(characters,
				WhitespaceInterpreter.execute("   \t\n\t\n\t    \t \n\t\n\t    \t\n\t\t\t\t\n     \t \n\t\t\t\t\n  \n\n\n",
						inputStream));
	}

	@Test
	public void givenInputStreamWithDecimal_whenReadingNumber_thenOutputsNumber() {
		final String characters = "123\n";
		final InputStream inputStream = new ByteArrayInputStream(characters.getBytes());
		assertEquals(characters.trim(),
				WhitespaceInterpreter.execute("   \t\n\t\n\t\t   \t\n\t\t\t\t\n \t\n\n\n",
						inputStream));
	}

	@Test
	public void givenInputStreamWithOctal_whenReadingNumber_thenOutputsNumber() {
		final String characters = "076512\n";
		final InputStream inputStream = new ByteArrayInputStream(characters.getBytes());
		assertEquals("32074",
				WhitespaceInterpreter.execute("   \t\n\t\n\t\t   \t\n\t\t\t\t\n \t\n\n\n",
						inputStream));
	}

	@Test
	public void givenInputStreamWithHexadecimal_whenReadingNumber_thenOutputsNumber() {
		final String characters = "0xD5F07\n";
		final InputStream inputStream = new ByteArrayInputStream(characters.getBytes());
		assertEquals("876295",
				WhitespaceInterpreter.execute("   \t\n\t\n\t\t   \t\n\t\t\t\t\n \t\n\n\n",
						inputStream));
	}

	@Test
	public void givenInputStreamWithBinary_whenReadingNumber_thenOutputsNumber() {
		final String characters = "0b11000000000100100011\n";
		final InputStream inputStream = new ByteArrayInputStream(characters.getBytes());
		assertEquals("786723",
				WhitespaceInterpreter.execute("   \t\n\t\n\t\t   \t\n\t\t\t\t\n \t\n\n\n",
						inputStream));
	}
}
