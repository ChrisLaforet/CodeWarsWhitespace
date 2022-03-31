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
		return execute(code, input, null);
	}
	
	public static String execute(String code, InputStream input, OutputStream output) {
		if (code == null || code.isEmpty()) {
			throw new IllegalStateException("Code is null");
		}

		// preflush buffer
		if (output != null) {
			try {
				output.flush();
			} catch (IOException ex) {
				throw new RuntimeException("Error writing to output stream");
			}
		}
		
		final Code instructions = new Code(code);
		Reader reader = null;
		if (input != null) {
			reader = new InputStreamReader(input, Charset.defaultCharset());
		}
		return execute(instructions, reader, output);
	}
	
	private static String execute(Code code, Reader reader, OutputStream outputStream) {
		final Stack<Integer> stack = new Stack<>();
		final Map<Integer, Integer> heap = new HashMap<>();
		final StringBuilder output = new StringBuilder();

		boolean cleanTermination = false;
		while (!code.isCompleted()) {
			char imp = code.nextOpCode();
			if (imp == SPACE) {
				processSpace(code, stack, heap, reader, output);
			} else if (imp == LF) {
				boolean endProgram = processLF(code, stack, heap, reader, output);
				if (endProgram) {
					cleanTermination = true;
					break;
				}
			} else {
				processTab(code, stack, heap, reader, output, outputStream);
			}			
		}

		if (!cleanTermination) {
			throw new IllegalStateException("Unclean termination");
		}
		return output.toString();		
	}
	
	private static int extractNumber(ICode code) {
		char sign = code.nextOpCode();
		if (sign == LF) {
			throw new IllegalStateException("Numbers must start with a sign at minimum");
		}
		boolean isNegative = sign == TAB;
		
		final StringBuilder binary = new StringBuilder();
		while (true) {
			char bit = code.nextOpCode();
			if (bit == LF) {
				break;
			}
			binary.append(bit == TAB ? '1' : '0');
		}
		// A number expression [sign][terminal] will be treated as zero
		int number = binary.length() == 0 ? 0 : Integer.parseInt(binary.toString(), 2);
		return number * (isNegative ? -1 : 1);
	}
	
	private static String extractLabel(ICode code) {
		final StringBuilder label = new StringBuilder();
		while (true) {
			char ch = code.nextOpCode();
			if (ch == LF) {
				break;
			}
			label.append(ch);
		}
		return label.toString();
	}
	
	private static void processSpace(Code code, Stack<Integer> stack,
			Map<Integer, Integer> heap, Reader reader, StringBuilder output) {
		char command = code.nextOpCode();
		if (command == SPACE) {
			stack.push(extractNumber(code));
		} else if (command == TAB) {
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				int index = extractNumber(code);	// Duplicate the nth value from the top of the stack and push onto the stack
				int stackIndex = (stack.size() - 1) - index;
				stack.push(stack.get(stackIndex));
			} else if (subCommand == LF) {
				int itemCount = extractNumber(code);	// Discard the top n values below the top of the stack from the stack
				int top = stack.pop();
				if (itemCount < 0 || itemCount >= stack.size()) {
					stack.clear();
				} else {
					for (int count = 0; count < itemCount; count++) {
						stack.pop();
					}
				}
				stack.push(top);
			} else {
				throw new IllegalStateException("SPACE TAB TAB is invalid IMP sequence");
			}
		} else if (command == LF) {
			char subCommand = code.nextOpCode();
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
	
	private static boolean processLF(Code code, Stack<Integer> stack,
			Map<Integer, Integer> heap, Reader reader, StringBuilder output) {
		char command = code.nextOpCode();
		if (command == SPACE) {
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				extractLabel(code);
			} else if (subCommand == TAB) {
				code.callSub(extractLabel(code));
			} else {
				code.jump(extractLabel(code));
			}
		} else if (command == TAB) {
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				// jump if 0 (JZ)
				String label = extractLabel(code);
				if (stack.pop() == 0) {
					code.jump(label);
				}
			} else if (subCommand == LF) {
				code.returnFromSub();
			} else {
				// jump if negative (JLZ)
				String label = extractLabel(code);
				if (stack.pop() < 0) {
					code.jump(label);
				}
			}
		} else if (command == LF) {
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				throw new IllegalStateException("LF LF SPACE is invalid IMP sequence");
			} else if (subCommand == LF) {
				return true;
			} else {
				throw new IllegalStateException("LF LF TAB is invalid IMP sequence");
			}
		}
		
		return false;
	}
	
	private static void processTab(Code code, Stack<Integer> stack,
			Map<Integer, Integer> heap, Reader reader, 
			StringBuilder output, OutputStream outputStream) {
		char command = code.nextOpCode();
		if (command == SPACE) {
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				processTabSpaceSpace(code, stack);
			} else if (subCommand == TAB) {
				processTabSpaceTab(code, stack);
			} else {
				
			}
		} else if (command == TAB) {
			char subCommand = code.nextOpCode();
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
			char subCommand = code.nextOpCode();
			if (subCommand == SPACE) {
				processTabLFSpace(code, stack, output, outputStream);
			} else if (subCommand == LF) {
				throw new IllegalStateException("TAB LF LF is invalid IMP sequence");
			} else {
				processTabLFTab(code, stack, heap, reader);
			}
		}
	}
	
	private static void emitString(String string, StringBuilder output, OutputStream outputStream) {
		output.append(string);
	    if (outputStream != null) {
	    	// I cannot believe the foolishness of depending on output for tests when
	    	// an exceptional circumstance exists!  Oh well...
	    	try {
			    for (byte b : string.getBytes()) {
			    	outputStream.write(b);
			    }
			    outputStream.flush();
	    	} catch (IOException ex) {
	    		throw new RuntimeException("Error writing to output stream");
	    	}
	    }
	}
	
	private static void processTabLFSpace(Code code, Stack<Integer> stack,
			StringBuilder output, OutputStream outputStream) {
		char opcode = code.nextOpCode();
		if (opcode == SPACE) {
			emitString(Character.toString(stack.pop()), output, outputStream);
		} else if (opcode == TAB) {
			emitString(Integer.toString(stack.pop()), output, outputStream);
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

	private static void processTabLFTab(Code code, Stack<Integer> stack,
										Map<Integer, Integer> heap, Reader reader) {
		char opcode = code.nextOpCode();
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
	
	private static void processTabSpaceSpace(Code code, Stack<Integer> stack) {
		char opcode = code.nextOpCode();
		if (opcode == SPACE) {
			stack.push(stack.pop() + stack.pop());
		} else if (opcode == TAB) {
			int subtrahend = stack.pop();
			int minuend = stack.pop();
			stack.push(minuend - subtrahend);
		} else {
			int multiplier = stack.pop();
			int multiplicand = stack.pop();
			stack.push(multiplicand * multiplier);
		}
	}
	
	private static void processTabSpaceTab(Code code, Stack<Integer> stack) {
		char opcode = code.nextOpCode();
		if (opcode == SPACE) {
			int divisor = stack.pop();
			int dividend = stack.pop();
			if (divisor == 0) {		// floating point divide by 0.0 doesn't fail!
				throw new ArithmeticException("Divide by zero");
			}
			int result = (int)Math.floorDiv(dividend, divisor);
			stack.push(result);
		} else if (opcode == TAB) {
			int divisor = stack.pop();
			int dividend = stack.pop();
			if (divisor == 0) {		// floating point divide by 0.0 doesn't fail!
				throw new ArithmeticException("Divide by zero");
			}
			int result = (int)Math.floorMod(dividend, divisor);
			stack.push(result);
		} else {
			throw new IllegalStateException("TAB SPACE TAB LF is invalid IMP sequence");
		}
	}
	
	
	interface ICode {
		public char nextOpCode();
		public boolean isCompleted();
	}
	
	private static class CodeScanner implements ICode {
		private List<Character> code = new ArrayList<>();
		private int ip = 0;
		
		public CodeScanner(String opcodes) {
			for (char opcode : opcodes.toCharArray()) {
				code.add(opcode);
			}
		}
		
		@Override
		public char nextOpCode() {
			if (isCompleted()) {
				throw new IllegalStateException("Request for opcode beyond code boundaries");
			}
			return code.get(ip++);
		}

		@Override
		public boolean isCompleted() {
			return ip >= code.size();
		}
		
		public int getInstructionPointer() {
			return ip;
		}

		public Map<String, Integer> validateAndExtractLabels() {
			final Map<String, Integer> labels = new HashMap<>();
			while (!isCompleted()) {
				char imp = nextOpCode();
				char second = nextOpCode();
				if (imp == SPACE) {
					if (second == SPACE) {
						extractNumber(this);
					} else if (second == TAB) {
						char third = nextOpCode();
						if (third == TAB) {
							throw new IllegalStateException("SPACE TAB TAB is invalid IMP sequence");
						}
						extractNumber(this);
					} else {
						nextOpCode();
					}
				} else if (imp == TAB) {
					if (second == SPACE) {
						char third = nextOpCode();
						if (third == SPACE) {
							nextOpCode();
						} else if (third == TAB) {
							char fourth = nextOpCode();
							if (fourth == LF) {
								throw new IllegalStateException("TAB SPACE TAB LF is invalid IMP sequence");
							}
						}
					} else if (second == TAB) {
						char third = nextOpCode();
						if (third == LF) {
							throw new IllegalStateException("TAB TAB LF is invalid IMP sequence");
						}
					} else {		// LF
						char third = nextOpCode();
						if (third == LF) {
							throw new IllegalStateException("TAB LF LF is invalid IMP sequence");
						}
						char fourth = nextOpCode();
						if (fourth == LF) {
							throw new IllegalStateException("TAB LF SPACE/TAB LF is invalid IMP sequence");
						}
					}
				} else {	// LF
					if (second == SPACE) {
						char third = nextOpCode();
						String label = extractLabel(this);
						if (third == SPACE) {
							if (labels.containsKey(label)) {
								throw new IllegalStateException("Duplicated label found for " + label);
							}
							labels.put(label, getInstructionPointer());
						} 
					} else if (second == TAB) {
						char third = nextOpCode();
						if (third != LF) {
							extractLabel(this);
						}
					} else {		// LF
						char third = nextOpCode();
						if (third != LF) {
							throw new IllegalStateException("LF LF SPACE/TAB is invalid IMP sequence");
						}
					}
				}
			}
			return labels;
		}
	}


	private static class Code implements ICode {
		private List<Character> code = new ArrayList<>();
		private Map<String, Integer> labels;
		private int ip = 0;
		
		private Stack<Integer> subStack = new Stack<>();

		public Code(String rawCode) {
			final String opcodes = reduceCode(rawCode);
			for (char opcode : opcodes.toCharArray()) {
				code.add(opcode);
			}
			
			CodeScanner scanner = new CodeScanner(opcodes);
			labels = scanner.validateAndExtractLabels();
		}
		
		@Override
		public char nextOpCode() {
			if (isCompleted()) {
				throw new IllegalStateException("Request for opcode beyond code boundaries");
			}
			return code.get(ip++);
		}
		
		@Override
		public boolean isCompleted() {
			return ip >= code.size();
		}
		
		public void callSub(String label) {
			if (!labels.containsKey(label)) {
				throw new IllegalStateException("Calling non-existent subroutine at " + label);
			}
			subStack.push(ip);
			ip = labels.get(label);
		}
		
		public void returnFromSub() {
			ip = subStack.pop();
		}
		
		public void jump(String label) {
			if (!labels.containsKey(label)) {
				throw new IllegalStateException("Calling non-existent jump at " + label);
			}
			ip = labels.get(label);
		}
	}

}