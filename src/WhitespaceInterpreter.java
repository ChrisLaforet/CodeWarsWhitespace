import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WhitespaceInterpreter {

	private static final char LF = 'n';
	private static final char TAB = 't';
	private static final char SPACE = 's';

	// transforms space characters to ['s','t','n'] chars;
	public static String unbleach(String code) {
		return code != null ? code.replace(' ', SPACE).replace('\t', TAB).replace('\n', LF) : null;
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
		return execute(instructions, input);
	}

	private static Stack<Character> prepareInstructionStream(String code) {
		final Stack<Character> instructions = new Stack<>();
		for (char instruction : reduceCode(code).toCharArray()) {
			instructions.add(0,instruction);
		}
		return instructions;
	}
	
	private static String execute(Stack<Character> instructions, InputStream input) {
		final Stack<Integer> stack = new Stack<>();
		final Map<Integer, Integer> heap = new HashMap<>();
		final StringBuilder output = new StringBuilder();

		while (!instructions.isEmpty()) {
			char imp = instructions.pop();
			if (imp == SPACE) {
				processSpace(instructions, stack, heap, input, output);
			} else if (imp == LF) {
				boolean endProgram = processLF(instructions, stack, heap, input, output);
				if (endProgram) {
					break;
				}
			} else {
				processTab(instructions, stack, heap, input, output);
			}			
		}

		return output.toString();		
	}
	
	private static int extractNumber(Stack<Character> instructions) {
		boolean isNegative = instructions.pop() == TAB;
		
		final StringBuilder binary = new StringBuilder();
		while (true) {
			char bit = instructions.pop();
			if (bit == LF) {
				break;
			}
			binary.append(bit == TAB ? '1' : '0');
		}
		int number = Integer.parseInt(binary.toString(), 2);
		return number * (isNegative ? -1 : 1);
	}
	
	private static void processSpace(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		char command = instructions.pop();
		if (command == SPACE) {
			stack.push(extractNumber(instructions));
		} else if (command == TAB) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				
			} else if (subCommand == LF) {
				
			} else {
				throw new IllegalStateException("SPACE TAB TAB is invalid IMP sequence");
			}
		} else if (command == LF) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				
			} else if (subCommand == LF) {
				
			} else {
				
			}
		}
	}
	
	private static boolean processLF(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		char command = instructions.pop();
		if (command == SPACE) {
			stack.push(extractNumber(instructions));
		} else if (command == TAB) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				
			} else if (subCommand == LF) {
				
			} else {
				
			}
		} else if (command == LF) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				throw new IllegalStateException("LF LF SPACE is invalid IMP sequence");
			} else if (subCommand == LF) {
				return true;
			} else {
				
			}
		}
		
		return false;
	}
	
	private static void processTab(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, InputStream input, StringBuilder output) {
		char command = instructions.pop();
		if (command == SPACE) {
			stack.push(extractNumber(instructions));
		} else if (command == TAB) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				
			} else if (subCommand == LF) {
				throw new IllegalStateException("TAB SPACE LF is invalid IMP sequence");
			} else {
			}
		} else if (command == LF) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				processTabLFSpace(instructions, stack, output);
			} else if (subCommand == LF) {
				throw new IllegalStateException("TAB LF LF is invalid IMP sequence");

			} else {
				
			}
		}
	}
	
	private static void processTabLFSpace(Stack<Character> instructions, Stack<Integer> stack,
			StringBuilder output) {
		char opcode = instructions.pop();
		if (opcode == SPACE) {
			output.append(Character.toString(stack.pop()));
		} else if (opcode == TAB) {
			output.append(Integer.toString(stack.pop()));
		} else {
			throw new IllegalStateException("TAB LF Space LF is invalid IMP sequence");

		}
	}

}