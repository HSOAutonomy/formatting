import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
		new Main(new ArrayList<>(Arrays.asList(args)));
	}

	private String jarLocation =
			new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();

	private String binaryLocation = jarLocation + "/binaries/windows/clang-format.exe";

	private Main(List<String> args) throws IOException, URISyntaxException
	{
		if (args.isEmpty()) {
			System.out.println("No arguments provided.");
			System.exit(1);
		}

		boolean shouldInit = args.remove("--init");
		if (shouldInit) {
			initDirectories(args);
		} else {
			formatFiles(args);
		}
	}

	private void formatFiles(List<String> filePaths) throws IOException, URISyntaxException
	{
		List<File> files = new ArrayList<>();
		for (String filePath : filePaths) {
			FileUtil.walk(filePath, files::add);
		}
		files.parallelStream().forEach(this ::format);
	}

	private void format(File file)
	{
		try {
			String[] command = new String[] {binaryLocation, "-i"};
			command = Stream.concat(Stream.of(command), Stream.of(file.getAbsolutePath())).toArray(String[] ::new);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.inheritIO();
			builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initDirectories(List<String> filePaths) throws IOException
	{
		for (String filePath : filePaths) {
			File file = new File(filePath);
			if (file.isDirectory()) {
				FileUtil.copyFile(new File(jarLocation + "/templates/.clang-format"),
						new File(file.getAbsolutePath() + "/.clang-format"));
				String ideaDir = file.getAbsolutePath() + "/.idea";
				new File(ideaDir).mkdir();
				String template =
						String.join("\n", Files.readAllLines(Paths.get(jarLocation + "/templates/watcherTasks.xml")));
				FileUtil.createFile(
						ideaDir + "/watcherTasks.xml", template.replace("clang-format-binary", binaryLocation));
			}
		}
	}
}
