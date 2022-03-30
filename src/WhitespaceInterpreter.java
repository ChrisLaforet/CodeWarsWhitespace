import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WhitespaceInterpreter {

	// transforms space characters to ['s','t','n'] chars;
	public static String unbleach(String code) {
		return code != null ? code.replace(' ', 's').replace('\t', 't').replace('\n', 'n') : null;
	}

	public static String reduceCode(String code) {
	  if (code == null) {
		  return code;
	  }
	  final StringBuilder sb = new StringBuilder();
	  for (char ch : code.toCharArray()) {
		  if (ch == ' ' || ch == '\t' || ch == '\n') {
			  sb.append(ch);
		  }
	  }
	  return unbleach(sb.toString());
  }

	// solution
	public static String execute(String code, InputStream input) {
		if (code == null) {
			return null;
		}
		
		final Stack<Character> instructions = prepareInstructionStream(code);
		return execute(code, input);
	}

	private static Stack<Character> prepareInstructionStream(String code) {
		final Stack<Character> instructions = new Stack<>();
		for (char instruction : reduceCode(code).toCharArray()) {
			instructions.push(instruction);
		}
		return instructions;
	}
	
	private static String execute(Stack<Character> instructions, InputStream input) {
		final Stack<Integer> stack = new Stack<>();
		final Map<Integer, Integer> heap = new HashMap<>();
		final StringBuilder output = new StringBuilder();

		while (!instructions.isEmpty()) {
			char imp = instructions.pop();
			if (imp == 's') {
				processSpace(instructions, stack, heap, input, output);
			} else if (imp == 'n') {
				processLF(instructions, stack, heap, input, output);
			} else {
				processTab(instructions, stack, heap, input, output);
			}			
		}

		return output.toString();		
	}
	
	private static String processSpace(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		
	}
	
	private static String processLF(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		
	}
	
	private static String processTab(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		
	}

}