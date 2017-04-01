import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We create a wrapper around clang-format for two reasons:
 *
 * - to be able to run it recursively on directories of source files
 * - to avoid duplicating the config in each repository
 */
public class Main
{
	public static void main(String[] args) throws IOException, URISyntaxException
	{
		new Main(args);
	}

	private static String[] STYLE = new String[] {"IndentWidth: 4",
												  "TabWidth: 4",
												  "UseTab: Always",
												  "ColumnLimit: 120",
												  "BreakBeforeBraces: Linux",
												  "BreakAfterJavaFieldAnnotations: true",
												  "AllowShortFunctionsOnASingleLine: false"};

	private Main(String[] args) throws IOException, URISyntaxException
	{
		if (args.length == 0) {
			System.out.println("No arguments provided.");
			System.exit(1);
		}

		for (String file : args) {
			walkFiles(file, this ::format);
		}
	}

	private void format(File file) throws URISyntaxException, IOException
	{
		String executable = getJarLocation() + "/binaries/windows/clang-format.exe";
		String style = String.format("-style={%s}", String.join(", ", STYLE));
		String[] command = new String[] {executable, "-i", style};
		command = Stream.concat(Stream.of(command), Stream.of(file.getAbsolutePath())).toArray(String[] ::new);

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.inheritIO();
		builder.start();
	}

	private static String getJarLocation() throws URISyntaxException
	{
		return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
	}

	private void walkFiles(String path, FileVisitor fileVisitor) throws IOException, URISyntaxException
	{
		File file = new File(path);

		if (file.getParentFile().getName().equals(".git")) {
			return;
		}

		if (!file.isDirectory()) {
			String name = file.getName();
			if (name.endsWith(".java") || name.endsWith(".c") || name.endsWith(".h") || name.endsWith(".cpp") ||
				name.endsWith(".hpp")) {
				fileVisitor.visit(file);
				return;
			}
		}

		File[] list = file.listFiles();
		if (list == null)
			return;

		for (File f : list) {
			walkFiles(f.getAbsolutePath(), fileVisitor);
		}
	}

	@FunctionalInterface
	interface FileVisitor {
		void visit(File file) throws IOException, URISyntaxException;
	}
}
