import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

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

	public static String execute(String code, InputStream input) {
		if (code == null || code.isEmpty()) {
			throw new IllegalStateException("Code is null");
		}
		
		final Stack<Character> instructions = prepareInstructionStream(code);
		Reader reader = null;
		if (input != null) {
			reader = new InputStreamReader(input, Charset.defaultCharset());
		}
		return execute(instructions, reader);
	}
	
	public static String execute(String code, InputStream input, OutputStream output) {
	    final String response = execute(code, input);
	    if (output != null) {
	    	try {
			    for (byte b : response.getBytes()) {
			    	output.write(b);
			    }
			    output.flush();
	    	} catch (IOException ex) {
	    		throw new RuntimeException("Error writing to output stream");
	    	}
	    }
	    return response;
	}

	private static Stack<Character> prepareInstructionStream(String code) {
		final Stack<Character> instructions = new Stack<>();
		for (char instruction : reduceCode(code).toCharArray()) {
			instructions.add(0,instruction);
		}
		return instructions;
	}
	
	private static String execute(Stack<Character> instructions, Reader reader) {
		final Stack<Integer> stack = new Stack<>();
		final Map<Integer, Integer> heap = new HashMap<>();
		final StringBuilder output = new StringBuilder();

		boolean cleanTermination = false;
		while (!instructions.isEmpty()) {
			char imp = instructions.pop();
			if (imp == SPACE) {
				processSpace(instructions, stack, heap, reader, output);
			} else if (imp == LF) {
				boolean endProgram = processLF(instructions, stack, heap, reader, output);
				if (endProgram) {
					cleanTermination = true;
					break;
				}
			} else {
				processTab(instructions, stack, heap, reader, output);
			}			
		}

		if (!cleanTermination) {
			throw new IllegalStateException("Unclean termination");
		}
		return output.toString();		
	}
	
	private static int extractNumber(Stack<Character> instructions) {
		char sign = instructions.pop();
		if (sign == LF) {
			throw new IllegalStateException("Numbers must start with a sign at minimum");
		}
		boolean isNegative = sign == TAB;
		
		final StringBuilder binary = new StringBuilder();
		while (true) {
			char bit = instructions.pop();
			if (bit == LF) {
				break;
			}
			binary.append(bit == TAB ? '1' : '0');
		}
		// A number expression [sign][terminal] will be treated as zero
		int number = binary.length() == 0 ? 0 : Integer.parseInt(binary.toString(), 2);
		return number * (isNegative ? -1 : 1);
	}
	
	private static void processSpace(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, Reader reader, StringBuilder output) {
		char command = instructions.pop();
		if (command == SPACE) {
			stack.push(extractNumber(instructions));
		} else if (command == TAB) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				int index = extractNumber(instructions);	// Duplicate the nth value from the top of the stack and push onto the stack
				int stackIndex = (stack.size() - 1) - index;
				stack.push(stack.get(stackIndex));
			} else if (subCommand == LF) {
				int itemCount = extractNumber(instructions);	// Discard the top n values below the top of the stack from the stack
				int top = stack.pop();
				if (itemCount >= stack.size()) {
					throw new IllegalStateException("Discard elements from stack underflow");
				}
				for (int count = 0; count < itemCount; count++) {
					stack.pop();
				}
				stack.push(top);
			} else {
				throw new IllegalStateException("SPACE TAB TAB is invalid IMP sequence");
			}
		} else if (command == LF) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				stack.push(stack.peek());		// duplicate top of stack
			} else if (subCommand == LF) {
				stack.pop();	// discard top of stack
			} else {
				int first = stack.pop();		// swap top 2 items in stack
				int second = stack.pop();
				stack.push(first);
				stack.push(second);
			}
		}
	}
	
	private static boolean processLF(Stack<Character> instructions, Stack<Integer> stack,
			Map<Integer, Integer> heap, Reader reader, StringBuilder output) {
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
			Map<Integer, Integer> heap, Reader reader, StringBuilder output) {
		char command = instructions.pop();
		if (command == SPACE) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				processTabSpaceSpace(instructions, stack, output);
			} else if (subCommand == TAB) {
				processTabSpaceTab(instructions, stack, output);
			} else {
				
			}
		} else if (command == TAB) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				int value = stack.pop();
				int address = stack.pop();
				heap.put(address, value);
			} else if (subCommand == LF) {
				throw new IllegalStateException("TAB SPACE LF is invalid IMP sequence");
			} else {
				int address = stack.pop();
				stack.push(heap.get(address));
			}
		} else if (command == LF) {
			char subCommand = instructions.pop();
			if (subCommand == SPACE) {
				processTabLFSpace(instructions, stack, output);
			} else if (subCommand == LF) {
				throw new IllegalStateException("TAB LF LF is invalid IMP sequence");
			} else {
				processTabLFTab(instructions, stack, heap, reader);
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
			throw new IllegalStateException("TAB LF SPACE LF is invalid IMP sequence");
		}
	}

	private static int readNumber(Reader reader) {
		final StringBuilder sb = new StringBuilder();
		try {
			while (true) {
				int ch = reader.read();
				if (ch == -1 || ch == '\n') {
					break;
				}
				sb.append((char)ch);
			}
		} catch (IOException ex) {
			throw new IllegalStateException("IOException reading number from input");
		}

		String number = sb.toString();
		int radix = 10;
		if (number.startsWith("0b")) {
			number = number.substring(2);
			radix = 2;
		} else if (number.startsWith("0x")) {
			number = number.substring(2);
			radix = 16;
		} else if (number.length() > 1 && number.startsWith("0")) {
			number = number.substring(1);
			radix = 8;
		}
		return Integer.parseInt(number, radix);
	}

	private static void processTabLFTab(Stack<Character> instructions, Stack<Integer> stack,
										Map<Integer, Integer> heap, Reader reader) {
		char opcode = instructions.pop();
		if (opcode == SPACE) {
			int address = stack.pop();
			try {
				int ch = reader.read();
				heap.put(address, ch);
			} catch (IOException ex) {
				throw new IllegalStateException("IOException reading character from input");
			}
		} else if (opcode == TAB) {
			int address = stack.pop();
			int value = readNumber(reader);
			heap.put(address, value);
		} else {
			throw new IllegalStateException("TAB LF TAB LF is invalid IMP sequence");
		}
	}
	
	private static void processTabSpaceSpace(Stack<Character> instructions, Stack<Integer> stack,
			StringBuilder output) {
		char opcode = instructions.pop();
		if (opcode == SPACE) {
			stack.push(stack.pop() + stack.pop());
		} else if (opcode == TAB) {
			int minuend = stack.pop();
			int subtrahend = stack.pop();
			stack.push(minuend - subtrahend);
		} else {
			int multiplicand = stack.pop();
			int multiplier = stack.pop();
			stack.push(multiplicand * multiplier);
		}
	}
	
	
	private static void processTabSpaceTab(Stack<Character> instructions, Stack<Integer> stack,
			StringBuilder output) {
		char opcode = instructions.pop();
		if (opcode == SPACE) {
			int dividend = stack.pop();
			int divisor = stack.pop();
			stack.push(dividend / divisor);
		} else if (opcode == TAB) {
			int dividend = stack.pop();
			int divisor = stack.pop();
			stack.push(dividend % divisor);
		} else {
			throw new IllegalStateException("TAB SPACE TAB LF is invalid IMP sequence");
		}
	}

	private static class Label {
		int label;
		int address;

		public Label(int label, int address) {
			this.label = label;
			this.address = address;
		}
	}

	private static class Code {
		List<Character> code = new ArrayList<>();
		Map<Integer, Integer> labels = new HashMap<>();

		public Code(String opcodes) {
			for (char opcode : opcodes.toCharArray()) {
				code.add(opcode);
			}
		}
	}

}