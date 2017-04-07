import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
	private static final String[] SOURCE_FILE_EXTENSIONS = new String[] {".java", ".c", ".h", ".cpp", ".hpp"};

	public static void main(String[] args) throws IOException, URISyntaxException
	{
		new Main(new ArrayList<>(Arrays.asList(args)));
	}

	private String jarLocation;
	private String clangFormatPath;
	private String yapfPath;

	private boolean verbose = false;
	private boolean success = true;

	private Main(List<String> args) throws IOException, URISyntaxException
	{
		if (args.isEmpty()) {
			System.out.println("No arguments provided.");
			System.exit(1);
		}

		lookupPaths();

		verbose = args.remove("--verbose");

		boolean shouldInit = args.remove("--init");
		if (shouldInit) {
			initDirectories(args);
		} else {
			formatFiles(args);
		}

		System.exit(success ? 0 : 1);
	}

	private void lookupPaths() throws URISyntaxException
	{
		jarLocation =
				new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
		clangFormatPath = getBinaryPath("clang-format");
		yapfPath = getBinaryPath("yapf");
	}

	private String getBinaryPath(String name)
	{
		String binaryPath = jarLocation + "/binaries";
		if (OSUtil.isWindows()) {
			binaryPath += "/windows32/" + name + ".exe";
		} else if (OSUtil.isUnix()) {
			binaryPath += "/linux64/" + name;
		} else if (OSUtil.isMac()) {
			binaryPath += "/mac64/" + name;
		}
		return binaryPath;
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
		if (!isSourceFile(file)) {
			return;
		}

		try {
			String[] command = new String[] {clangFormatPath, "-i"};
			command = Stream.concat(Stream.of(command), Stream.of(file.getAbsolutePath())).toArray(String[] ::new);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.inheritIO();
			builder.start();

			if (verbose) {
				System.out.println(file.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
	}

	private static boolean isSourceFile(File file)
	{
		return Arrays.stream(SOURCE_FILE_EXTENSIONS).anyMatch(s -> FileUtil.hasEnding(file, s));
	}

	private void initDirectories(List<String> filePaths) throws IOException
	{
		for (String filePath : filePaths) {
			File file = new File(filePath);
			if (file.isDirectory()) {
				FileUtil.copyFile(jarLocation + "/templates/.clang-format", file.getAbsolutePath() + "/.clang-format");
				initIdeaProject(file);
				initEclipseProject(file);
			}
		}
	}

	private void initIdeaProject(File file) throws IOException
	{
		String ideaDir = file.getAbsolutePath() + "/.idea";
		new File(ideaDir).mkdir();
		String template = FileUtil.readFile(jarLocation + "/templates/watcherTasks.xml");
		FileUtil.createFile(ideaDir + "/watcherTasks.xml",
				template.replace("clang-format-binary", clangFormatPath).replace("yapf-binary", yapfPath));
	}

	private void initEclipseProject(File file) throws IOException
	{
		File projectFile = new File(new File(file.getAbsolutePath()) + "/.project");
		if (!projectFile.exists()) {
			return;
		}

		String externalToolBuildersDir = file.getAbsolutePath() + "/.externalToolBuilders";
		new File(externalToolBuildersDir).mkdir();

		FileUtil.copyFile(
				jarLocation + "/templates/clang-format.launch", externalToolBuildersDir + "/clang-format.launch");

		String template = FileUtil.readFile(jarLocation + "/templates/buildCommand.xml");
		String project = FileUtil.readFile(projectFile.getAbsolutePath());
		if (project.contains("clang-format.launch")) {
			return;
		}

		// this is a terrible hack...
		FileUtil.createFile(
				projectFile.getAbsolutePath(), project.replace("</buildSpec>", template + "\n\t</buildSpec>"));
	}
}
