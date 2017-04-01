import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * We create a wrapper around clang-format for two reasons:
 *
 * - to be able to run it recursively on directories of source files
 * - to avoid duplicating the config in each repository
 */
public class Main
{
	public static void main(String[] args) throws IOException
	{
		new Main(args);
		System.out.println(Arrays.toString(args));
	}

	private static String[] STYLE = new String[] {"IndentWidth: 4",
												  "TabWidth: 4",
												  "UseTab: Always",
												  "ColumnLimit: 120",
												  "BreakBeforeBraces: Linux",
												  "BreakAfterJavaFieldAnnotations: true",
												  "AllowShortFunctionsOnASingleLine: false"};

	private Main(String[] args) throws IOException
	{
		if (args.length == 0) {
			System.out.println("No input file(s).");
			System.exit(1);
		}

		String style = String.format("-style={%s}", String.join(", ", STYLE));
		String[] command = new String[] {"clang-format", "-i", style};
		command = Stream.concat(Stream.of(command), Stream.of(args)).toArray(String[] ::new);

		System.out.println(Arrays.toString(command));

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.inheritIO();
		builder.start();
	}
}
