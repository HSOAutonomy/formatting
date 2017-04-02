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
	public static void main(String[] args) throws IOException, URISyntaxException
	{
		new Main(new ArrayList<>(Arrays.asList(args)));
	}

	private String jarLocation;
	private String executable;

	private boolean verbose = false;

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
	}

	private void lookupPaths() throws URISyntaxException
	{
		jarLocation =
				new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
		executable = jarLocation + "/binaries";
		if (OSUtil.isWindows()) {
			executable += "/windows32/clang-format.exe";
		} else if (OSUtil.isUnix()) {
			executable += "/linux64/clang-format";
		} else if (OSUtil.isMac()) {
			executable += "/mac64/clang-format";
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
			String[] command = new String[] {executable, "-i"};
			command = Stream.concat(Stream.of(command), Stream.of(file.getAbsolutePath())).toArray(String[] ::new);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.inheritIO();
			builder.start();

			if (verbose) {
				System.out.println(file.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		FileUtil.createFile(ideaDir + "/watcherTasks.xml", template.replace("clang-format-binary", executable));
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
